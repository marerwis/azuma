import { prisma } from '../../config/db';
import { withCache, invalidateCache } from '../../lib/redis';

// ---------------------------------------------------------------------------
// Categories Service — app_categories table (global, not store-scoped)
// ---------------------------------------------------------------------------

export async function getAllCategories() {
  return withCache('categories:all', 30, () => prisma.app_categories.findMany({
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
  }));
}

export async function getCategoryById(id: string) {
  return withCache(`category:${id}`, 30, () => prisma.app_categories.findUnique({
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
  }));
}

export async function createCategory(data: {
  name: string;
  image_url?: string;
  sort_order?: number;
}) {
  const result = await prisma.app_categories.create({ data });
  await invalidateCache('categories:all');
  return result;
}

export async function updateCategory(
  id: string,
  data: { name?: string; image_url?: string; sort_order?: number; is_active?: boolean }
) {
  const result = await prisma.app_categories.update({ where: { id }, data });
  await invalidateCache('categories:all', `category:${id}`);
  return result;
}

export async function deleteCategory(id: string) {
  const result = await prisma.app_categories.delete({ where: { id } });
  await invalidateCache('categories:all', `category:${id}`);
  return result;
}
