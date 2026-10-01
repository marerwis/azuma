import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './addresses.service';

const addressSchema = z.object({
  title: z.string().min(1),
  full_address: z.string().min(1),
  building_details: z.string().nullable().optional(),
  delivery_instructions: z.string().nullable().optional(),
  latitude: z.number().nullable().optional(),
  longitude: z.number().nullable().optional(),
  is_default: z.boolean().optional(),
});

const updateAddressSchema = addressSchema.partial();

export async function getAddresses(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.listAddresses(req.user!.id);
    res.json({ success: true, count: data.length, data });
  } catch (e: any) {
    console.error('[Addresses] getAddresses error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

export async function createAddress(req: Request, res: Response): Promise<void> {
  const parsed = addressSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.addAddress(req.user!.id, parsed.data);
    res.status(201).json({ success: true, data });
  } catch (e: any) {
    console.error('[Addresses] createAddress error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

export async function updateAddress(req: Request, res: Response): Promise<void> {
  const parsed = updateAddressSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateAddress(req.user!.id, req.params.id as string, parsed.data);
    if (!data) {
       res.status(404).json({ error: 'Address not found or unauthorized' });
       return;
    }
    res.json({ success: true, data });
  } catch (e: any) {
    console.error('[Addresses] updateAddress error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

export async function deleteAddress(req: Request, res: Response): Promise<void> {
  try {
    const success = await svc.deleteAddress(req.user!.id, req.params.id as string);
    if (!success) {
       res.status(404).json({ error: 'Address not found or unauthorized' });
       return;
    }
    res.json({ success: true, message: 'Address deleted successfully' });
  } catch (e: any) {
    console.error('[Addresses] deleteAddress error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}
