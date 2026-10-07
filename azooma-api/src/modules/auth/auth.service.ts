import { prisma } from '../../config/db';
import { supabase } from '../../config/supabase';

// ---------------------------------------------------------------------------
// Auth Service
// Handles the Supabase token verification + user upsert.
// ---------------------------------------------------------------------------

export interface VerifyResult {
  user: {
    id: string;
    firebase_uid: string;
    phone: string | null;
    email: string | null;
    full_name: string;
    role: string;
    avatar_url: string | null;
    fcm_token: string | null;
    is_active: boolean | null;
    created_at: Date | null;
  };
  isNewUser: boolean;
  supabaseToken: string;
}

export async function verifyAndSyncUser(
  idToken: string,
  fcmToken?: string,
  fullName?: string
): Promise<VerifyResult> {
  // 1. Verify the Supabase JWT directly — throws if invalid/expired
  const { data: { user: authUser }, error } = await supabase.auth.getUser(idToken);

  if (error || !authUser) {
    throw new Error(error?.message ?? 'Invalid or expired Supabase token');
  }

  const supabaseUid = authUser.id;
  const email = authUser.email ?? null;
  const phone = authUser.phone ?? null;
  const providerSub = authUser.user_metadata?.provider_id || authUser.user_metadata?.sub || null;

  // Determine name: use passed fullName, or from metadata, or from email prefix
  let name = fullName || authUser.user_metadata?.full_name || authUser.user_metadata?.name;
  if (!name && email) name = email.split('@')[0];
  if (!name) name = 'مستخدم جديد';

  const avatarUrl = authUser.user_metadata?.avatar_url ?? authUser.user_metadata?.picture ?? null;

  // 2. Upsert into public.users
  //    First try to find by Supabase UUID (id), then by firebase_uid (legacy migration)
  const existing = await prisma.public_users.findFirst({
    where: {
      OR: [
        { id: supabaseUid },
        ...(providerSub ? [{ firebase_uid: providerSub }] : []),
      ],
    },
  });

  let user: VerifyResult['user'];
  let isNewUser: boolean;

  if (existing) {
    // Update last-seen data
    const updated = await prisma.public_users.update({
      where: { id: existing.id },
      data: {
        fcm_token: fcmToken ?? existing.fcm_token,
        avatar_url: avatarUrl ?? existing.avatar_url,
        email: email ?? existing.email,
        phone: phone ?? existing.phone,
        full_name:
          existing.full_name === 'مستخدم جديد' && name !== 'مستخدم جديد'
            ? name
            : existing.full_name,
        updated_at: new Date(),
      },
      select: {
        id: true,
        firebase_uid: true,
        phone: true,
        email: true,
        full_name: true,
        role: true,
        avatar_url: true,
        fcm_token: true,
        is_active: true,
        created_at: true,
      },
    });
    user = { ...updated, firebase_uid: updated.firebase_uid ?? '' };
    isNewUser = false;
  } else {
    // Create new user record using the Supabase UUID as the primary id
    const created = await prisma.public_users.create({
      data: {
        id: supabaseUid,            // Use Supabase UUID as our PK
        firebase_uid: providerSub,  // Store Google sub for legacy compatibility
        phone,
        email,
        full_name: name,
        avatar_url: avatarUrl,
        fcm_token: fcmToken ?? null,
        role: 'customer',
        is_active: true,
      },
      select: {
        id: true,
        firebase_uid: true,
        phone: true,
        email: true,
        full_name: true,
        role: true,
        avatar_url: true,
        fcm_token: true,
        is_active: true,
        created_at: true,
      },
    });
    user = { ...created, firebase_uid: created.firebase_uid ?? '' };
    isNewUser = true;
  }

  // 3. Return the SAME token that was passed in — it is the valid Supabase JWT
  //    The client should keep using this token for all subsequent requests.
  return { user, isNewUser, supabaseToken: idToken };
}
