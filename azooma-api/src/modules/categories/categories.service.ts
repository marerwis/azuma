import { prisma } from '../../config/db';

// ---------------------------------------------------------------------------
// Categories Service — app_categories table (global, not store-scoped)
// ---------------------------------------------------------------------------

export async function getAllCategories() {
  return prisma.app_categories.findMany({
    where: { is_active: true },
    orderBy: { sort_order: 'asc' },
    select: {
      id: true,
      name: true,
      image_url: true,
      is_active: true,
      sort_order: true,
      created_at: true,
    },
  });
}

export async function getCategoryById(id: string) {
  return prisma.app_categories.findUnique({
    where: { id },
    select: {
      id: true,
      name: true,
      image_url: true,
      is_active: true,
      sort_order: true,
      created_at: true,
      stores: {
        where: { is_active: true },
        select: {
          id: true,
          name: true,
          image_url: true,
          address: true,
          is_open: true,
          delivery_radius_km: true,
        },
      },
    },
  });
}

export async function createCategory(data: {
  name: string;
  image_url?: string;
  sort_order?: number;
}) {
  return prisma.app_categories.create({ data });
}

export async function updateCategory(
  id: string,
  data: { name?: string; image_url?: string; sort_order?: number; is_active?: boolean }
) {
  return prisma.app_categories.update({ where: { id }, data });
}

export async function deleteCategory(id: string) {
  return prisma.app_categories.delete({ where: { id } });
}
