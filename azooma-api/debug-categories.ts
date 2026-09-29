// Quick debug: test categories service directly
import 'dotenv/config';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  try {
    console.log('Testing app_categories query...');
    const result = await prisma.app_categories.findMany({
      where: { is_active: true },
      orderBy: { sort_order: 'asc' },
      select: {
        id: true,
        name: true,
        is_active: true,
        sort_order: true,
        created_at: true,
        _count: { select: { stores: true } },
      },
    });
    console.log('SUCCESS:', JSON.stringify(result, null, 2));
  } catch (e: any) {
    console.error('ERROR:', e.message);
    console.error('CODE:', e.code);
  } finally {
    await prisma.$disconnect();
  }
}

main();
