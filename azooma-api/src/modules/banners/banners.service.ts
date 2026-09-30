import { prisma } from '../../config/db';
import { withCache } from '../../lib/redis';

export async function getActiveBanners() {
  return withCache('banners:active', 30, () =>
    prisma.banners.findMany({
      where: { is_active: true },
      orderBy: { sort_order: 'asc' },
      select: {
        id: true,
        image_url: true,
        action_url: true,
        store_id: true,
        sort_order: true,
      },
    })
  );
}
