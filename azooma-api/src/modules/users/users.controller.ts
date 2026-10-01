import { Request, Response } from 'express';
import { z } from 'zod';
import { getAuth } from 'firebase-admin/auth';
import * as svc from './users.service';

// ── Schemas ────────────────────────────────────────────────────────────────
const updateProfileSchema = z.object({
  full_name: z.string().min(1).optional(),
  phone: z.string().min(5).optional(),
  email: z.string().email().optional(),
  avatar_url: z.string().url().optional(),
  fcm_token: z.string().optional(),
});

const changeRoleSchema = z.object({
  role: z.enum(['customer', 'driver', 'vendor', 'admin']),
});

const setActiveSchema = z.object({
  is_active: z.boolean(),
});

// ── Controllers ────────────────────────────────────────────────────────────

/** GET /api/v1/users  (admin only)  ?role=customer|driver|vendor|admin&isActive=true */
export async function listUsers(req: Request, res: Response): Promise<void> {
  try {
    const { role, isActive } = req.query;
    const data = await svc.listUsers({
      role: role as svc.UserRole | undefined,
      isActive: isActive === 'true' ? true : isActive === 'false' ? false : undefined,
    });
    res.json({ success: true, count: data.length, data });
  } catch (e: any) {
    console.error('[Users] listUsers error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/users/me  (authenticated user) */
export async function getMe(req: Request, res: Response): Promise<void> {
  try {
    const user = await svc.getUserById(req.user!.id);
    if (!user) { res.status(404).json({ error: 'User not found' }); return; }
    res.json({ success: true, data: user });
  } catch (e: any) {
    console.error('[Users] getMe error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/users/:id  (admin only) */
export async function getUserById(req: Request, res: Response): Promise<void> {
  try {
    const user = await svc.getUserById(req.params.id as string);
    if (!user) { res.status(404).json({ error: 'User not found' }); return; }
    res.json({ success: true, data: user });
  } catch (e: any) {
    console.error('[Users] getUserById error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/users/me  (authenticated user — updates own profile) */
export async function updateMe(req: Request, res: Response): Promise<void> {
  const parsed = updateProfileSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateProfile(req.user!.id, parsed.data);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2002') { res.status(409).json({ error: 'Phone number already taken' }); return; }
    console.error('[Users] updateMe error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/users/:id/role  (admin only) */
export async function changeRole(req: Request, res: Response): Promise<void> {
  const parsed = changeRoleSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.changeRole(req.params.id as string, parsed.data.role);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'User not found' }); return; }
    console.error('[Users] changeRole error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/users/:id/status  (admin only — ban/unban) */
export async function setStatus(req: Request, res: Response): Promise<void> {
  const parsed = setActiveSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.setActiveStatus(req.params.id as string, parsed.data.is_active);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'User not found' }); return; }
    console.error('[Users] setStatus error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/users/me/change-password  (authenticated user — sends password reset email) */
export async function changePassword(req: Request, res: Response): Promise<void> {
  try {
    // Passwords live in Firebase Auth, not our DB — generate a reset link
    const firebaseUser = await getAuth().getUser(req.user!.firebaseUid);
    const email = firebaseUser.email;
    if (!email) {
      res.status(400).json({ error: 'No email on this account. Cannot send reset link.' });
      return;
    }
    const resetLink = await getAuth().generatePasswordResetLink(email);
    res.json({ success: true, message: 'Password reset link generated', resetLink });
  } catch (e: any) {
    console.error('[Users] changePassword error:', e);
    res.status(500).json({ error: 'Failed to generate password reset link' });
  }
}
