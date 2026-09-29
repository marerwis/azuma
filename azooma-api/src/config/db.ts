import { PrismaClient } from '@prisma/client';

// ---------------------------------------------------------------------------
// Prisma Client singleton.
// In development, reuses a single instance across hot reloads to avoid
// "Too many database connections" errors.
// ---------------------------------------------------------------------------

declare global {
  // eslint-disable-next-line no-var
  var __prisma: PrismaClient | undefined;
}

export const prisma: PrismaClient =
  global.__prisma ??
  new PrismaClient({
    log: process.env.NODE_ENV === 'development'
      ? ['query', 'info', 'warn', 'error']
      : ['error'],
  });

if (process.env.NODE_ENV !== 'production') {
  global.__prisma = prisma;
}
