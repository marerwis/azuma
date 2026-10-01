import { prisma } from '../../config/db';
import { addresses } from '@prisma/client';

export type AddressCreateInput = {
  title: string;
  full_address: string;
  building_details?: string | null;
  delivery_instructions?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  is_default?: boolean;
};

export async function listAddresses(userId: string): Promise<addresses[]> {
  return prisma.addresses.findMany({
    where: { user_id: userId },
    orderBy: { created_at: 'desc' }
  });
}

export async function addAddress(userId: string, data: AddressCreateInput): Promise<addresses> {
  if (data.is_default) {
    await prisma.addresses.updateMany({
      where: { user_id: userId, is_default: true },
      data: { is_default: false }
    });
  }

  return prisma.addresses.create({
    data: {
      ...data,
      user_id: userId
    }
  });
}

export async function updateAddress(userId: string, addressId: string, data: Partial<AddressCreateInput>): Promise<addresses | null> {
  const existing = await prisma.addresses.findFirst({
    where: { id: addressId, user_id: userId }
  });
  if (!existing) return null;

  if (data.is_default) {
    await prisma.addresses.updateMany({
      where: { user_id: userId, is_default: true, id: { not: addressId } },
      data: { is_default: false }
    });
  }

  return prisma.addresses.update({
    where: { id: addressId },
    data
  });
}

export async function deleteAddress(userId: string, addressId: string): Promise<boolean> {
  const result = await prisma.addresses.deleteMany({
    where: { id: addressId, user_id: userId }
  });
  return result.count > 0;
}
