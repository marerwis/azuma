import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './orders.service';

// ── Schemas ────────────────────────────────────────────────────────────────
const orderItemSchema = z.object({
  product_id: z.string().uuid(),
  variant_id: z.string().uuid().optional(),
  quantity: z.number().int().positive(),
  unit_price: z.number().positive(),
  addon_ids: z.array(z.string().uuid()).optional(),
});

const createOrderSchema = z.object({
  store_id: z.string().uuid(),
  coupon_id: z.string().uuid().optional(),
  delivery_address: z.string().min(5),
  delivery_latitude: z.number().min(-90).max(90),
  delivery_longitude: z.number().min(-180).max(180),
  payment_method: z.enum(['cash', 'card', 'wallet']),
  special_instructions: z.string().optional(),
  items: z.array(orderItemSchema).min(1, 'Order must have at least one item'),
});

const ORDER_STATUSES = [
  'PENDING', 'ACCEPTED_PREPARING', 'READY_FOR_PICKUP', 'ACCEPTED_BY_DRIVER', 'PICKED_UP', 'DELIVERED', 'CANCELLED_BY_USER', 'REJECTED_BY_STORE',
] as const;

const updateStatusSchema = z.object({
  status: z.enum(ORDER_STATUSES),
  prep_time_minutes: z.number().int().positive().optional(),
  cancellation_reason: z.string().optional(),
  notes: z.string().optional(),
});

// ── Controllers ────────────────────────────────────────────────────────────

/** POST /api/v1/orders  (customer — creates an order) */
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
  } catch (e: any) {
    console.error('[Orders] createOrder error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/orders/me  (customer — their own orders) */
export async function getMyOrders(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.getMyOrders(req.user!.id);
    res.json({ success: true, count: data.length, data });
  } catch (e: any) {
    console.error('[Orders] getMyOrders error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/orders/:id  (owner | admin | vendor | driver) */
export async function getOrderById(req: Request, res: Response): Promise<void> {
  try {
    const order = await svc.getOrderById(req.params.id as string);
    if (!order) { res.status(404).json({ error: 'Order not found' }); return; }

    const user = req.user!;
    const isOwner = order.users_orders_customer_idTousers.id === user.id;
    const isDriver = order.users_orders_driver_idTousers?.id === user.id;
    const isPrivileged = ['admin', 'vendor'].includes(user.role);

    if (!isOwner && !isDriver && !isPrivileged) {
      res.status(403).json({ error: 'Forbidden' }); return;
    }
    res.json({ success: true, data: order });
  } catch (e: any) {
    console.error('[Orders] getOrderById error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/orders  (admin — all orders; vendor — filtered by their store) */
export async function listOrders(req: Request, res: Response): Promise<void> {
  try {
    const { storeId, driverId, status } = req.query;
    const user = req.user!;

    let data;
    if (user.role === 'vendor') {
      // Vendors can only see their own store's orders
      // Find their store first
      const store = await import('../../config/db').then(({ prisma }) =>
        prisma.stores.findFirst({ where: { vendor_id: user.id }, select: { id: true } })
      );
      if (!store) { res.status(404).json({ error: 'No store found for this vendor' }); return; }
      data = await svc.listStoreOrders(store.id, status as string | undefined);
    } else {
      // Admin — full filter
      data = await svc.listAllOrders({
        storeId: storeId as string | undefined,
        driverId: driverId as string | undefined,
        status: status as string | undefined,
      });
    }

    res.json({ success: true, count: data.length, data });
  } catch (e: any) {
    console.error('[Orders] listOrders error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/orders/:id/status  (admin | vendor | driver) */
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
  } catch (e: any) {
    if (e?.code === 'P2025' || e?.message === 'Order not found') { res.status(404).json({ error: 'Order not found' }); return; }
    if (e?.message?.includes('Invalid transition')) { res.status(400).json({ error: e.message }); return; }
    console.error('[Orders] updateStatus error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/orders/:id/assign-driver  (driver) */
export async function assignDriver(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.assignDriver(req.params.id as string, req.user!.id);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025' || e?.message === 'Order not found') { res.status(404).json({ error: 'Order not found' }); return; }
    if (e?.message?.includes('conflict') || e?.message?.includes('Already assigned') || e?.message?.includes('Not ready')) { 
      res.status(409).json({ error: e.message }); return; 
    }
    console.error('[Orders] assignDriver error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}
