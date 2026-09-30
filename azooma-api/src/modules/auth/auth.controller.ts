import { Request, Response } from 'express';
import { verifyAndSyncUser } from './auth.service';
import { z } from 'zod';

// ---------------------------------------------------------------------------
// Validation schema for the request body
// ---------------------------------------------------------------------------
// Accepts both camelCase (Android Moshi) and snake_case (legacy clients)
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
// Body: { idToken: string, fcmToken?: string }
//
// Flow:
//  1. Validate request body
//  2. Call auth.service → verifies Firebase JWT + upserts user in Supabase
//  3. Return user profile + isNewUser flag
// ---------------------------------------------------------------------------
export async function verifyController(
  req: Request,
  res: Response
): Promise<void> {
  // 1. Validate body
  const parsed = verifyBodySchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({
      error: 'Invalid request body',
      details: parsed.error.flatten().fieldErrors,
    });
    return;
  }

  const { idToken, fcmToken } = parsed.data;

  try {
    // 2. Verify + sync
    const { user, isNewUser, supabaseToken } = await verifyAndSyncUser(idToken!, fcmToken);

    // 3. Respond
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

    // Firebase token errors
    if (
      message.includes('auth/id-token-expired') ||
      message.includes('auth/argument-error') ||
      message.includes('Decoding Firebase ID token failed')
    ) {
      res.status(401).json({ error: 'Invalid or expired Firebase token' });
      return;
    }

    console.error('[AuthController] verifyController error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}
