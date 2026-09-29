import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('Running Firebase Migration on Supabase...');

  try {
    // 1. Drop the foreign key from public.users to auth.users if it exists
    await prisma.$executeRawUnsafe(`
      ALTER TABLE public.users 
      DROP CONSTRAINT IF EXISTS users_id_fkey;
    `);
    console.log('✅ Dropped foreign key to auth.users');

    // 2. Add firebase_uid column to public.users if it doesn't exist
    await prisma.$executeRawUnsafe(`
      ALTER TABLE public.users 
      ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(255) UNIQUE;
    `);
    console.log('✅ Added firebase_uid column to public.users');

    // 3. Make email required just in case (optional, we'll skip altering existing data)
    // 4. Change id default to generate a UUID so we don't have to provide it
    await prisma.$executeRawUnsafe(`
      ALTER TABLE public.users 
      ALTER COLUMN id SET DEFAULT gen_random_uuid();
    `);
    console.log('✅ Set default UUID generation for public.users.id');

    console.log('🚀 Migration complete! Please run `npm run prisma:pull` and `npm run prisma:generate` again to reflect these changes in Prisma Client.');
  } catch (error) {
    console.error('❌ Migration failed:', error);
  } finally {
    await prisma.$disconnect();
  }
}

main();
