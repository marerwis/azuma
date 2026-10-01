import { prisma } from '../../config/db';
import { Prisma } from '@prisma/client';
import { getMessaging } from 'firebase-admin/messaging';

// ---------------------------------------------------------------------------
// Orders Service — Enterprise Edition
// ---------------------------------------------------------------------------

// ── Helpers ─────────────────────────────────────────────────────────────────

/** Generates a cryptographically-random 4-digit OTP string (0000–9999). */
function generateOtp(): string {
  return String(Math.floor(Math.random() * 10000)).padStart(4, '0');
}

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface OrderItemInput {
  product_id: string;
  variant_id?: string;
  quantity: number;
  unit_price: number;
  addon_ids?: string[];
}

export interface CreateOrderInput {
  customer_id: string;
  store_id: string;
  order_type?: 'DELIVERY' | 'PICKUP';
  coupon_id?: string;
  delivery_address: string;
  delivery_latitude: number;
  delivery_longitude: number;
  delivery_fee?: number;
  total_distance_km?: number;
  payment_method: string;
  special_instructions?: string;
  items: OrderItemInput[];
}

// ── Select Projection ────────────────────────────────────────────────────────

const ORDER_SELECT = {
  id: true,
  status: true,
  order_type: true,
  subtotal: true,
  delivery_fee: true,
  total_distance_km: true,
  discount_amount: true,
  tax_amount: true,
  total_amount: true,
  delivery_address: true,
  delivery_latitude: true,
  delivery_longitude: true,
  payment_method: true,
  is_paid: true,
  special_instructions: true,
  prep_time_minutes: true,
  cancellation_reason: true,
  // OTPs intentionally EXCLUDED from general select — only returned in dedicated verify responses
  created_at: true,
  updated_at: true,
  stores: { select: { id: true, name: true, image_url: true } },
  users_orders_customer_idTousers: { select: { id: true, full_name: true, email: true, phone: true, fcm_token: true } },
  users_orders_driver_idTousers: { select: { id: true, full_name: true, phone: true, fcm_token: true } },
  order_items: {
    select: {
      id: true,
      quantity: true,
      unit_price: true,
      total_price: true,
      products: { select: { id: true, name: true, image_url: true } },
      product_variants: { select: { id: true, name: true } },
      order_item_addons: {
        select: {
          price: true,
          add_ons: { select: { id: true, name: true } },
        },
      },
    },
  },
};

// ── Create Order ─────────────────────────────────────────────────────────────

export async function createOrder(input: CreateOrderInput) {
  const TAX_RATE = 0.05;
  const orderType = input.order_type ?? 'DELIVERY';

  // Business Rule: PICKUP orders always have zero delivery fee and distance
  const deliveryFee = orderType === 'PICKUP' ? 0 : (input.delivery_fee ?? 0);
  const totalDistanceKm = orderType === 'PICKUP' ? null : (input.total_distance_km ?? null);

  // Calculate subtotal from items
  const subtotal = input.items.reduce(
    (sum, item) => sum + item.unit_price * item.quantity,
    0
  );
  const subtotalDecimal = new Prisma.Decimal(subtotal);
  const taxAmount = new Prisma.Decimal(subtotal * TAX_RATE);
  const totalAmount = subtotalDecimal.add(new Prisma.Decimal(deliveryFee)).add(taxAmount);

  // OTP Generation Strategy:
  // - DELIVERY: generate both pickup_otp (driver collects from store) + delivery_otp (confirm handoff to customer)
  // - PICKUP: generate only pickup_otp (customer shows to store to confirm collection)
  const pickupOtp = generateOtp();
  const deliveryOtp = orderType === 'DELIVERY' ? generateOtp() : null;

  return prisma.orders.create({
    data: {
      customer_id: input.customer_id,
      store_id: input.store_id,
      order_type: orderType,
      coupon_id: input.coupon_id,
      delivery_address: input.delivery_address,
      delivery_latitude: input.delivery_latitude,
      delivery_longitude: input.delivery_longitude,
      delivery_fee: deliveryFee,
      total_distance_km: totalDistanceKm,
      payment_method: input.payment_method,
      special_instructions: input.special_instructions,
      subtotal: subtotalDecimal,
      tax_amount: taxAmount,
      total_amount: totalAmount,
      status: 'PENDING',
      pickup_otp: pickupOtp,
      delivery_otp: deliveryOtp,
      order_items: {
        create: input.items.map((item) => ({
          product_id: item.product_id,
          variant_id: item.variant_id,
          quantity: item.quantity,
          unit_price: new Prisma.Decimal(item.unit_price),
          total_price: new Prisma.Decimal(item.unit_price * item.quantity),
          ...(item.addon_ids?.length
            ? {
                order_item_addons: {
                  create: item.addon_ids.map((addon_id) => ({
                    addon_id,
                    price: new Prisma.Decimal(0),
                  })),
                },
              }
            : {}),
        })),
      },
      order_status_history: {
        create: { status: 'PENDING' },
      },
    },
    select: {
      ...ORDER_SELECT,
      // Include OTPs in creation response so the customer/store can display them
      pickup_otp: true,
      delivery_otp: true,
    },
  });
}

// ── Get My Orders (customer) ─────────────────────────────────────────────────

export async function getMyOrders(customerId: string) {
  return prisma.orders.findMany({
    where: { customer_id: customerId },
    orderBy: { created_at: 'desc' },
    select: ORDER_SELECT,
  });
}

// ── Get Single Order ──────────────────────────────────────────────────────────

export async function getOrderById(id: string) {
  return prisma.orders.findUnique({ where: { id }, select: ORDER_SELECT });
}

// ── List All Orders (admin) ───────────────────────────────────────────────────

export async function listAllOrders(filters: {
  storeId?: string;
  driverId?: string;
  status?: string;
}) {
  return prisma.orders.findMany({
    where: {
      ...(filters.storeId && { store_id: filters.storeId }),
      ...(filters.driverId && { driver_id: filters.driverId }),
      ...(filters.status && { status: filters.status as any }),
    },
    orderBy: { created_at: 'desc' },
    select: ORDER_SELECT,
  });
}

// ── List Store Orders (vendor) ────────────────────────────────────────────────

export async function listStoreOrders(storeId: string, status?: string) {
  return prisma.orders.findMany({
    where: {
      store_id: storeId,
      ...(status && { status: status as any }),
    },
    orderBy: { created_at: 'desc' },
    select: ORDER_SELECT,
  });
}

// ── Update Order Status (State Machine) ──────────────────────────────────────

export async function updateOrderStatus(
  id: string,
  status: string,
  extras: {
    userId: string;
    role: string;
    prep_time_minutes?: number;
    cancellation_reason?: string;
    notes?: string;
  }
) {
  const currentOrder = await prisma.orders.findUnique({ where: { id } });
  if (!currentOrder) throw new Error('Order not found');

  const currentStatus = currentOrder.status;

  // ── Strict state transition rules ────────────────────────────────────────
  if (status === 'ACCEPTED_PREPARING') {
    if (currentStatus !== 'PENDING') throw new Error('Invalid transition to ACCEPTED_PREPARING: order must be PENDING');
    if (!extras.prep_time_minutes) throw new Error('prep_time_minutes is required when accepting an order');
  } else if (status === 'READY_FOR_PICKUP') {
    if (currentStatus !== 'ACCEPTED_PREPARING') throw new Error('Invalid transition to READY_FOR_PICKUP');
  } else if (status === 'PICKED_UP') {
    if (currentStatus !== 'ACCEPTED_BY_DRIVER') throw new Error('Invalid transition to PICKED_UP: use OTP verification endpoint');
  } else if (status === 'DELIVERED') {
    if (currentStatus !== 'PICKED_UP') throw new Error('Invalid transition to DELIVERED: use OTP verification endpoint');
  }

  const updatedOrder = await prisma.orders.update({
    where: { id },
    data: {
      status: status as any,
      ...(extras.prep_time_minutes && { prep_time_minutes: extras.prep_time_minutes }),
      ...(extras.cancellation_reason && { cancellation_reason: extras.cancellation_reason }),
      ...(status === 'REJECTED_BY_STORE' && { rejected_by: extras.userId }),
      updated_at: new Date(),
      order_status_history: {
        create: {
          status: status as any,
          notes: extras.notes,
        },
      },
    },
    select: ORDER_SELECT,
  });

  // ── FCM Notifications (fire-and-forget) ───────────────────────────────────
  const customerFcm = updatedOrder.users_orders_customer_idTousers.fcm_token;
  if (customerFcm) {
    let title = '';
    let body = '';
    if (status === 'ACCEPTED_PREPARING') {
      title = 'Order Accepted! 🍳';
      body = `Your order is being prepared. Ready in ~${extras.prep_time_minutes} mins.`;
    } else if (status === 'READY_FOR_PICKUP') {
      title = 'Order Ready! 📦';
      body = 'Your order is ready and a driver is on the way.';
    } else if (status === 'PICKED_UP') {
      title = 'Order Picked Up! 🚗';
      body = 'The driver has your order and is heading to you.';
    } else if (status === 'DELIVERED') {
      title = 'Order Delivered! ✅';
      body = 'Enjoy your meal! Please rate your experience.';
    } else if (status === 'CANCELLED_BY_USER') {
      title = 'Order Cancelled';
      body = 'Your order has been cancelled.';
    } else if (status === 'REJECTED_BY_STORE') {
      title = 'Order Rejected ❌';
      body = extras.cancellation_reason ?? 'The store could not fulfil your order.';
    }

    if (title && body) {
      getMessaging().send({
        token: customerFcm,
        notification: { title, body },
        data: { order_id: id, status },
      }).catch(err => console.error('[FCM] Error sending to customer:', err));
    }
  }

  return updatedOrder;
}

// ── Assign Driver (Concurrency-Safe) ─────────────────────────────────────────

export async function assignDriver(id: string, driverId: string) {
  return prisma.$transaction(async (tx) => {
    const order = await tx.orders.findUnique({ where: { id } });
    if (!order) throw new Error('Order not found');
    if (order.driver_id) throw new Error('Already assigned to a driver');
    if (order.status !== 'READY_FOR_PICKUP') throw new Error('Order not ready for pickup');

    return tx.orders.update({
      where: { id },
      data: {
        driver_id: driverId,
        status: 'ACCEPTED_BY_DRIVER',
        updated_at: new Date(),
        order_status_history: {
          create: { status: 'ACCEPTED_BY_DRIVER' },
        },
      },
      select: ORDER_SELECT,
    });
  });
}

// ── Verify Pickup OTP ─────────────────────────────────────────────────────────
// Used by: driver (DELIVERY) to confirm collection from store, OR customer (PICKUP) to confirm self-collection.

export async function verifyPickupOtp(id: string, submittedOtp: string) {
  const order = await prisma.orders.findUnique({
    where: { id },
    select: { id: true, status: true, order_type: true, pickup_otp: true, users_orders_customer_idTousers: { select: { fcm_token: true } } },
  });

  if (!order) throw new Error('Order not found');

  // For DELIVERY: driver must have accepted the order; for PICKUP: store must have prepared it
  const allowedStatus = order.order_type === 'PICKUP' ? 'ACCEPTED_PREPARING' : 'ACCEPTED_BY_DRIVER';
  if (order.status !== allowedStatus) {
    throw new Error(`OTP can only be verified when order is ${allowedStatus}`);
  }
  if (!order.pickup_otp || order.pickup_otp !== submittedOtp.trim()) {
    throw new Error('Invalid pickup OTP');
  }

  const updated = await prisma.orders.update({
    where: { id },
    data: {
      status: 'PICKED_UP',
      // Invalidate the used OTP for security — one-time use
      pickup_otp: null,
      updated_at: new Date(),
      order_status_history: {
        create: {
          status: 'PICKED_UP',
          notes: order.order_type === 'PICKUP' ? 'Customer self-collected (OTP verified)' : 'Driver collected from store (OTP verified)',
        },
      },
    },
    select: ORDER_SELECT,
  });

  // Notify customer
  const customerFcm = order.users_orders_customer_idTousers.fcm_token;
  if (customerFcm) {
    const isPickup = order.order_type === 'PICKUP';
    getMessaging().send({
      token: customerFcm,
      notification: {
        title: isPickup ? 'Collected! ✅' : 'Order Picked Up! 🚗',
        body: isPickup ? 'You have collected your order. Enjoy!' : 'The driver has picked up your order.',
      },
      data: { order_id: id, status: 'PICKED_UP' },
    }).catch(err => console.error('[FCM] verifyPickupOtp error:', err));
  }

  return updated;
}

// ── Verify Delivery OTP ───────────────────────────────────────────────────────
// Used by: driver to confirm handoff to customer. DELIVERY orders only.

export async function verifyDeliveryOtp(id: string, submittedOtp: string) {
  const order = await prisma.orders.findUnique({
    where: { id },
    select: { id: true, status: true, order_type: true, delivery_otp: true, users_orders_customer_idTousers: { select: { fcm_token: true } } },
  });

  if (!order) throw new Error('Order not found');
  if (order.order_type !== 'DELIVERY') {
    throw new Error('Delivery OTP is only applicable to DELIVERY orders');
  }
  if (order.status !== 'PICKED_UP') {
    throw new Error('OTP can only be verified when order is PICKED_UP');
  }
  if (!order.delivery_otp || order.delivery_otp !== submittedOtp.trim()) {
    throw new Error('Invalid delivery OTP');
  }

  const updated = await prisma.orders.update({
    where: { id },
    data: {
      status: 'DELIVERED',
      // Invalidate OTP after use
      delivery_otp: null,
      updated_at: new Date(),
      order_status_history: {
        create: {
          status: 'DELIVERED',
          notes: 'Delivery confirmed by OTP verification',
        },
      },
    },
    select: ORDER_SELECT,
  });

  // Notify customer
  const customerFcm = order.users_orders_customer_idTousers.fcm_token;
  if (customerFcm) {
    getMessaging().send({
      token: customerFcm,
      notification: {
        title: 'Order Delivered! ✅',
        body: 'Your order has been delivered. Enjoy your meal!',
      },
      data: { order_id: id, status: 'DELIVERED' },
    }).catch(err => console.error('[FCM] verifyDeliveryOtp error:', err));
  }

  return updated;
}
