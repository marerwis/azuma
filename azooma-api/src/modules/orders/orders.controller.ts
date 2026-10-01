import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './orders.service';

// ── Validation Schemas ──────────────────────────────────────────────────────

const orderItemSchema = z.object({
  product_id: z.string().uuid(),
  variant_id: z.string().uuid().optional(),
  quantity: z.number().int().positive(),
  unit_price: z.number().positive(),
  addon_ids: z.array(z.string().uuid()).optional(),
});

const createOrderSchema = z.object({
  store_id: z.string().uuid(),
  order_type: z.enum(['DELIVERY', 'PICKUP']).default('DELIVERY'),
  coupon_id: z.string().uuid().optional(),
  delivery_address: z.string().min(5),
  delivery_latitude: z.number().min(-90).max(90),
  delivery_longitude: z.number().min(-180).max(180),
  /** Client-computed delivery fee — overridden to 0 for PICKUP by service layer */
  delivery_fee: z.number().min(0).optional(),
  /** Client-computed distance snapshot — overridden to null for PICKUP */
  total_distance_km: z.number().min(0).optional(),
  payment_method: z.enum(['cash', 'card', 'wallet']),
  special_instructions: z.string().optional(),
  items: z.array(orderItemSchema).min(1, 'Order must have at least one item'),
});

const ORDER_STATUSES = [
  'PENDING',
  'ACCEPTED_PREPARING',
  'READY_FOR_PICKUP',
  'ACCEPTED_BY_DRIVER',
  'PICKED_UP',
  'DELIVERED',
  'CANCELLED_BY_USER',
  'REJECTED_BY_STORE',
] as const;

const updateStatusSchema = z.object({
  status: z.enum(ORDER_STATUSES),
  prep_time_minutes: z.number().int().positive().optional(),
  cancellation_reason: z.string().optional(),
  notes: z.string().optional(),
});

const otpSchema = z.object({
  otp: z.string().length(4, 'OTP must be exactly 4 digits').regex(/^\d{4}$/, 'OTP must contain only digits'),
});

// ── Helper ──────────────────────────────────────────────────────────────────

function handleServiceError(res: Response, e: unknown, context: string): void {
  const err = e as Error;
  console.error(`[Orders] ${context} error:`, err);

  if (err?.message === 'Order not found') {
    res.status(404).json({ error: 'Order not found' }); return;
  }
  if (err?.message?.includes('Invalid transition') || err?.message?.includes('OTP can only')) {
    res.status(400).json({ error: err.message }); return;
  }
  if (err?.message?.includes('Invalid') && err?.message?.includes('OTP')) {
    res.status(401).json({ error: err.message }); return;
  }
  if (err?.message?.includes('Already assigned') || err?.message?.includes('not ready')) {
    res.status(409).json({ error: err.message }); return;
  }
  if (err?.message?.includes('only applicable')) {
    res.status(400).json({ error: err.message }); return;
  }

  res.status(500).json({ error: 'Internal server error' });
}

// ── Controllers ─────────────────────────────────────────────────────────────

/** POST /api/v1/orders
 *  Customer creates a new order.
 *  Returns OTPs in the response so the customer/store can display them immediately.
 */
export async function createOrder(req: Request, res: Response): Promise<void> {
  const parsed = createOrderSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.createOrder({
      customer_id: req.user!.id,
      ...parsed.data,
    });
    res.status(201).json({ success: true, data });
  } catch (e) {
    handleServiceError(res, e, 'createOrder');
  }
}

/** GET /api/v1/orders/me  — customer's own orders */
export async function getMyOrders(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.getMyOrders(req.user!.id);
    res.json({ success: true, count: data.length, data });
  } catch (e) {
    handleServiceError(res, e, 'getMyOrders');
  }
}

/** GET /api/v1/orders/:id  — owner | admin | vendor | driver */
export async function getOrderById(req: Request, res: Response): Promise<void> {
  try {
    const order = await svc.getOrderById(req.params.id as string);
    if (!order) { res.status(404).json({ error: 'Order not found' }); return; }

    const user = req.user!;
    const isOwner   = order.users_orders_customer_idTousers.id === user.id;
    const isDriver  = order.users_orders_driver_idTousers?.id === user.id;
    const isPrivileged = ['admin', 'vendor'].includes(user.role);

    if (!isOwner && !isDriver && !isPrivileged) {
      res.status(403).json({ error: 'Forbidden' }); return;
    }
    res.json({ success: true, data: order });
  } catch (e) {
    handleServiceError(res, e, 'getOrderById');
  }
}

/** GET /api/v1/orders  — admin: all orders; vendor: their store only */
export async function listOrders(req: Request, res: Response): Promise<void> {
  try {
    const { storeId, driverId, status } = req.query;
    const user = req.user!;

    let data;
    if (user.role === 'vendor') {
      const store = await import('../../config/db').then(({ prisma }) =>
        prisma.stores.findFirst({ where: { vendor_id: user.id }, select: { id: true } })
      );
      if (!store) { res.status(404).json({ error: 'No store found for this vendor' }); return; }
      data = await svc.listStoreOrders(store.id, status as string | undefined);
    } else {
      data = await svc.listAllOrders({
        storeId: storeId as string | undefined,
        driverId: driverId as string | undefined,
        status: status as string | undefined,
      });
    }

    res.json({ success: true, count: data.length, data });
  } catch (e) {
    handleServiceError(res, e, 'listOrders');
  }
}

/** PATCH /api/v1/orders/:id/status  — admin | vendor | driver */
export async function updateStatus(req: Request, res: Response): Promise<void> {
  const parsed = updateStatusSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateOrderStatus(
      req.params.id as string,
      parsed.data.status,
      {
        userId: req.user!.id,
        role: req.user!.role,
        prep_time_minutes: parsed.data.prep_time_minutes,
        cancellation_reason: parsed.data.cancellation_reason,
        notes: parsed.data.notes,
      }
    );
    res.json({ success: true, data });
  } catch (e) {
    handleServiceError(res, e, 'updateStatus');
  }
}

/** POST /api/v1/orders/:id/assign-driver  — driver */
export async function assignDriver(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.assignDriver(req.params.id as string, req.user!.id);
    res.json({ success: true, data });
  } catch (e) {
    handleServiceError(res, e, 'assignDriver');
  }
}

/** POST /api/v1/orders/:id/verify-pickup
 *
 *  DELIVERY flow: driver submits PIN to confirm collection from store → PICKED_UP
 *  PICKUP flow:   customer submits PIN to confirm self-collection → PICKED_UP
 *
 *  Body: { "otp": "1234" }
 */
export async function verifyPickup(req: Request, res: Response): Promise<void> {
  const parsed = otpSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.verifyPickupOtp(req.params.id as string, parsed.data.otp);
    res.json({ success: true, message: 'Pickup confirmed', data });
  } catch (e) {
    handleServiceError(res, e, 'verifyPickup');
  }
}

/** POST /api/v1/orders/:id/verify-delivery
 *
 *  Driver submits the customer's 4-digit PIN to confirm delivery handoff → DELIVERED
 *  Only applicable to DELIVERY orders.
 *
 *  Body: { "otp": "5678" }
 */
export async function verifyDelivery(req: Request, res: Response): Promise<void> {
  const parsed = otpSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.verifyDeliveryOtp(req.params.id as string, parsed.data.otp);
    res.json({ success: true, message: 'Delivery confirmed', data });
  } catch (e) {
    handleServiceError(res, e, 'verifyDelivery');
  }
}
