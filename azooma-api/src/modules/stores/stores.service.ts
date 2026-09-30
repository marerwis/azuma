import { prisma } from '../../config/db';
import { withCache } from '../../lib/redis';

// ---------------------------------------------------------------------------
// Stores Service
// ---------------------------------------------------------------------------

export async function getAllStores(filters: {
  categoryId?: string;
  isActive?: boolean;
  isOpen?: boolean;
}) {
  const cacheKey = `stores:all:${JSON.stringify(filters)}`;
  return withCache(cacheKey, 60, () => prisma.stores.findMany({
    where: {
      is_active: filters.isActive ?? true,
      ...(filters.isOpen !== undefined && { is_open: filters.isOpen }),
      ...(filters.categoryId && { app_category_id: filters.categoryId }),
    },
    orderBy: { created_at: 'desc' },
    select: {
      id: true,
      name: true,
      description: true,
      image_url: true,
      cover_url: true,
      address: true,
      latitude: true,
      longitude: true,
      delivery_radius_km: true,
      commission_rate: true,
      is_active: true,
      is_open: true,
      opening_time: true,
      closing_time: true,
      created_at: true,
      app_categories: { select: { id: true, name: true, image_url: true } },
      _count: { select: { menu_categories: true, products: true } },
    },
  }));
}

export async function getStoreById(id: string) {
  return withCache(`store:${id}`, 120, () => prisma.stores.findUnique({
    where: { id },
    select: {
      id: true,
      name: true,
      description: true,
      image_url: true,
      cover_url: true,
      address: true,
      latitude: true,
      longitude: true,
      delivery_radius_km: true,
      is_active: true,
      is_open: true,
      opening_time: true,
      closing_time: true,
      created_at: true,
      app_categories: { select: { id: true, name: true } },
      menu_categories: {
        where: { is_active: true },
        orderBy: { sort_order: 'asc' },
        select: {
          id: true,
          name: true,
          sort_order: true,
          products_products_menu_category_idTomenu_categories: {
            where: { is_active: true },
            select: {
              id: true,
              name: true,
              description: true,
              base_price: true,
              image_url: true,
              is_veg: true,
              stock_quantity: true,
            },
          },
        },
      },
    },
  }));
}

export async function createStore(data: {
  vendor_id: string;
  name: string;
  description?: string;
  image_url?: string;
  cover_url?: string;
  address: string;
  latitude: number;
  longitude: number;
  app_category_id?: string;
  delivery_radius_km?: number;
  commission_rate?: number;
}) {
  return prisma.stores.create({ data });
}

export async function updateStore(
  id: string,
  data: {
    name?: string;
    description?: string;
    image_url?: string;
    cover_url?: string;
    address?: string;
    latitude?: number;
    longitude?: number;
    app_category_id?: string;
    delivery_radius_km?: number;
    is_active?: boolean;
    is_open?: boolean;
    opening_time?: Date;
    closing_time?: Date;
  }
) {
  return prisma.stores.update({ where: { id }, data: { ...data, updated_at: new Date() } });
}

export async function deleteStore(id: string) {
  return prisma.stores.delete({ where: { id } });
}

// ---------------------------------------------------------------------------
// Store Menu Categories (nested under a store)
// ---------------------------------------------------------------------------
export async function getMenuCategories(storeId: string) {
  return withCache(`menu:${storeId}`, 300, () => prisma.menu_categories.findMany({
    where: { store_id: storeId, is_active: true },
    orderBy: { sort_order: 'asc' },
  }));
}

export async function createMenuCategory(storeId: string, name: string, sortOrder?: number) {
  return prisma.menu_categories.create({
    data: { store_id: storeId, name, is_active: true, sort_order: sortOrder ?? 0 },
  });
}

export async function deleteMenuCategory(id: string) {
  return prisma.menu_categories.delete({ where: { id } });
}

// ---------------------------------------------------------------------------
// Store Products (nested under a store)
// ---------------------------------------------------------------------------
export async function getProducts(storeId: string, menuCategoryId?: string) {
  const cacheKey = `products:${storeId}:${menuCategoryId || 'all'}`;
  return withCache(cacheKey, 300, () => prisma.products.findMany({
    where: {
      store_id: storeId,
      is_active: true,
      ...(menuCategoryId && { menu_category_id: menuCategoryId }),
    },
    orderBy: { created_at: 'desc' },
    select: {
      id: true,
      name: true,
      description: true,
      base_price: true,
      image_url: true,
      is_veg: true,
      is_active: true,
      stock_quantity: true,
      menu_category_id: true,
      created_at: true,
    },
  }));
}

export async function createProduct(data: {
  store_id: string;
  name: string;
  description?: string;
  base_price: number;
  image_url?: string;
  is_veg?: boolean;
  menu_category_id?: string;
  stock_quantity?: number;
}) {
  return prisma.products.create({ data: { ...data, base_price: data.base_price as any } });
}

export async function updateProduct(
  id: string,
  data: {
    name?: string;
    description?: string;
    base_price?: number;
    image_url?: string;
    is_veg?: boolean;
    is_active?: boolean;
    menu_category_id?: string;
    stock_quantity?: number;
  }
) {
  return prisma.products.update({
    where: { id },
    data: { ...data, base_price: data.base_price as any, updated_at: new Date() },
  });
}

export async function deleteProduct(id: string) {
  return prisma.products.delete({ where: { id } });
}
