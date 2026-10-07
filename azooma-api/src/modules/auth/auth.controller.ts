import { Request, Response } from 'express';
import { verifyAndSyncUser } from './auth.service';
import { z } from 'zod';

// ---------------------------------------------------------------------------
// Validation schema for the request body
// ---------------------------------------------------------------------------
const verifyBodySchema = z
  .object({
    idToken:  z.string().optional(),
    id_token: z.string().optional(),
    fcmToken:  z.string().optional(),
    fcm_token: z.string().optional(),
    fullName:  z.string().optional(),
    full_name: z.string().optional(),
  })
  .transform((data) => ({
    idToken:  data.idToken  ?? data.id_token,
    fcmToken: data.fcmToken ?? data.fcm_token,
    fullName: data.fullName ?? data.full_name,
  }))
  .refine((data) => !!data.idToken, { message: 'idToken is required' });

// ---------------------------------------------------------------------------
// POST /api/v1/auth/verify
// Body: { idToken: string, fcmToken?: string, fullName?: string }
//
// Flow:
//  1. Validate request body
//  2. Verify Supabase JWT + upsert user in DB
//  3. Return user profile + isNewUser flag
// ---------------------------------------------------------------------------
export async function verifyController(
  req: Request,
  res: Response
): Promise<void> {
  const parsed = verifyBodySchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({
      error: 'Invalid request body',
      details: parsed.error.flatten().fieldErrors,
    });
    return;
  }

  const { idToken, fcmToken, fullName } = parsed.data;

  try {
    const { user, isNewUser, supabaseToken } = await verifyAndSyncUser(idToken!, fcmToken, fullName);

    res.status(200).json({
      success: true,
      isNewUser,
      supabaseToken,
      user: {
        id: user.id,
        email: user.email,
        full_name: user.full_name,
        role: user.role,
        avatar_url: user.avatar_url,
        is_active: user.is_active,
        created_at: user.created_at,
      },
    });
  } catch (error: any) {
    const message = error?.message ?? 'Unknown error';
    console.error('[AuthController] verifyController error:', message);

    if (
      message.includes('Invalid or expired') ||
      message.includes('JWT') ||
      message.includes('token')
    ) {
      res.status(401).json({ error: `Unauthorized: ${message}` });
      return;
    }

    res.status(500).json({ error: 'Internal server error' });
  }
}
