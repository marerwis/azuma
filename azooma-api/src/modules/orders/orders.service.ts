import { prisma } from '../../config/db';
import { Prisma } from '@prisma/client';

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
  users_orders_customer_idTousers: { select: { id: true, full_name: true, email: true, phone: true } },
  users_orders_driver_idTousers: { select: { id: true, full_name: true, phone: true } },
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
      status: 'pending',
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
  driverId?: string
) {
  return prisma.orders.update({
    where: { id },
    data: {
      status: status as any,
      ...(driverId && { driver_id: driverId }),
      updated_at: new Date(),
    },
    select: ORDER_SELECT,
  });
}
