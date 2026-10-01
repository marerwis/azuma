import { prisma } from '../../config/db';

// ---------------------------------------------------------------------------
// Users Service — public.users table
// ---------------------------------------------------------------------------

export type UserRole = 'customer' | 'driver' | 'vendor' | 'admin';

const USER_SELECT = {
  id: true,
  email: true,
  full_name: true,
  phone: true,
  role: true,
  avatar_url: true,
  fcm_token: true,
  is_active: true,
  created_at: true,
  updated_at: true,
};

export async function listUsers(filters: { role?: UserRole; isActive?: boolean }) {
  return prisma.public_users.findMany({
    where: {
      ...(filters.role && { role: filters.role }),
      ...(filters.isActive !== undefined && { is_active: filters.isActive }),
    },
    orderBy: { created_at: 'desc' },
    select: {
      ...USER_SELECT,
      _count: { select: { stores: true, orders_orders_customer_idTousers: true } },
    },
  });
}

export async function getUserById(id: string) {
  return prisma.public_users.findUnique({
    where: { id },
    select: {
      ...USER_SELECT,
      stores: {
        select: { id: true, name: true, image_url: true, is_active: true },
      },
      wallets: { select: { id: true, balance: true } },
      _count: { select: { orders_orders_customer_idTousers: true } },
    },
  });
}

export async function updateProfile(
  id: string,
  data: {
    full_name?: string;
    phone?: string;
    email?: string;
    avatar_url?: string;
    fcm_token?: string;
  }
) {
  return prisma.public_users.update({
    where: { id },
    data: { ...data, updated_at: new Date() },
    select: USER_SELECT,
  });
}

export async function changeRole(id: string, role: UserRole) {
  return prisma.public_users.update({
    where: { id },
    data: { role, updated_at: new Date() },
    select: USER_SELECT,
  });
}

export async function setActiveStatus(id: string, is_active: boolean) {
  return prisma.public_users.update({
    where: { id },
    data: { is_active, updated_at: new Date() },
    select: USER_SELECT,
  });
}
