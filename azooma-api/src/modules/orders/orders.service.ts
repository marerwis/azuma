import { prisma } from '../../config/db';
import { Prisma } from '@prisma/client';
import { getMessaging } from 'firebase-admin/messaging';

// ---------------------------------------------------------------------------
// Orders Service
// ---------------------------------------------------------------------------

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
  coupon_id?: string;
  delivery_address: string;
  delivery_latitude: number;
  delivery_longitude: number;
  payment_method: string;
  special_instructions?: string;
  items: OrderItemInput[];
}

const ORDER_SELECT = {
  id: true,
  status: true,
  subtotal: true,
  delivery_fee: true,
  discount_amount: true,
  tax_amount: true,
  total_amount: true,
  delivery_address: true,
  delivery_latitude: true,
  delivery_longitude: true,
  payment_method: true,
  is_paid: true,
  special_instructions: true,
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

// --------------- Create Order -----------------------------------------------
export async function createOrder(input: CreateOrderInput) {
  const DELIVERY_FEE = new Prisma.Decimal(5.0);
  const TAX_RATE = 0.05;

  // Calculate subtotal from items
  const subtotal = input.items.reduce(
    (sum, item) => sum + item.unit_price * item.quantity,
    0
  );
  const subtotalDecimal = new Prisma.Decimal(subtotal);
  const taxAmount = new Prisma.Decimal(subtotal * TAX_RATE);
  const totalAmount = subtotalDecimal.add(DELIVERY_FEE).add(taxAmount);

  return prisma.orders.create({
    data: {
      customer_id: input.customer_id,
      store_id: input.store_id,
      coupon_id: input.coupon_id,
      delivery_address: input.delivery_address,
      delivery_latitude: input.delivery_latitude,
      delivery_longitude: input.delivery_longitude,
      payment_method: input.payment_method,
      special_instructions: input.special_instructions,
      subtotal: subtotalDecimal,
      delivery_fee: DELIVERY_FEE,
      tax_amount: taxAmount,
      total_amount: totalAmount,
      status: 'PENDING',
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
                    price: new Prisma.Decimal(0), // addon price resolved via relation
                  })),
                },
              }
            : {}),
        })),
      },
      order_status_history: {
        create: {
          status: 'PENDING',
        },
      },
    },
    select: ORDER_SELECT,
  });
}

// --------------- Get My Orders (customer) -----------------------------------
export async function getMyOrders(customerId: string) {
  return prisma.orders.findMany({
    where: { customer_id: customerId },
    orderBy: { created_at: 'desc' },
    select: ORDER_SELECT,
  });
}

// --------------- Get Single Order -------------------------------------------
export async function getOrderById(id: string) {
  return prisma.orders.findUnique({ where: { id }, select: ORDER_SELECT });
}

// --------------- List All Orders (admin) ------------------------------------
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

// --------------- List Store Orders (vendor) ---------------------------------
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

// --------------- Update Order Status ----------------------------------------
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

  // State machine rules
  if (status === 'ACCEPTED_PREPARING') {
    if (currentStatus !== 'PENDING') throw new Error('Invalid transition to ACCEPTED_PREPARING');
    if (!extras.prep_time_minutes) throw new Error('prep_time_minutes is required');
  } else if (status === 'READY_FOR_PICKUP') {
    if (currentStatus !== 'ACCEPTED_PREPARING') throw new Error('Invalid transition to READY_FOR_PICKUP');
  } else if (status === 'PICKED_UP') {
    if (currentStatus !== 'ACCEPTED_BY_DRIVER') throw new Error('Invalid transition to PICKED_UP');
  } else if (status === 'DELIVERED') {
    if (currentStatus !== 'PICKED_UP') throw new Error('Invalid transition to DELIVERED');
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

  // Trigger Notifications asynchronously
  const customerFcm = updatedOrder.users_orders_customer_idTousers.fcm_token;
  if (customerFcm) {
    let title = '';
    let body = '';
    if (status === 'ACCEPTED_PREPARING') {
      title = 'Order Accepted!';
      body = `Your order is being prepared and will be ready in ~${extras.prep_time_minutes} mins.`;
    } else if (status === 'READY_FOR_PICKUP') {
      title = 'Order Ready!';
      body = 'Your order is ready and waiting for a driver.';
      // Note: Ideally, here we would also broadcast to nearby drivers
    } else if (status === 'PICKED_UP') {
      title = 'Order on the way!';
      body = 'The driver has picked up your order.';
    } else if (status === 'DELIVERED') {
      title = 'Order Delivered!';
      body = 'Enjoy your meal! Please rate your experience.';
    }

    if (title && body) {
      getMessaging().send({
        token: customerFcm,
        notification: { title, body },
      }).catch(err => console.error('[FCM] Error sending to customer:', err));
    }
  }

  return updatedOrder;
}

// --------------- Assign Driver (Concurrency lock) ---------------------------
export async function assignDriver(id: string, driverId: string) {
  return prisma.$transaction(async (tx) => {
    // Check current state directly
    const order = await tx.orders.findUnique({ where: { id } });
    if (!order) throw new Error('Order not found');
    if (order.driver_id) throw new Error('Already assigned to a driver');
    if (order.status !== 'READY_FOR_PICKUP') throw new Error('Order Not ready for pickup');

    return tx.orders.update({
      where: { id },
      data: {
        driver_id: driverId,
        status: 'ACCEPTED_BY_DRIVER',
        updated_at: new Date(),
        order_status_history: {
          create: {
            status: 'ACCEPTED_BY_DRIVER',
          },
        },
      },
      select: ORDER_SELECT,
    });
  });
}
