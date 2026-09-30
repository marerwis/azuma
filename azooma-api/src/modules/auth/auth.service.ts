import { prisma } from '../../config/db';
import { getAuth } from 'firebase-admin/auth';
import * as jwt from 'jsonwebtoken';

// ---------------------------------------------------------------------------
// Auth Service
// Handles the Firebase token verification + user upsert in Supabase.
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
  fcmToken?: string
): Promise<VerifyResult> {
  // 1. Verify the Firebase JWT — throws if invalid/expired
  const decoded = await getAuth().verifyIdToken(idToken);

  const firebaseUid = decoded.uid;
  const phone = decoded.phone_number ?? null;
  const email = decoded.email ?? null;
  
  let name = decoded.name;
  if (!name && email) {
    name = email.split('@')[0];
  }
  if (!name) {
    name = 'مستخدم جديد';
  }
  
  const avatarUrl = decoded.picture ?? null;

  // 2. Upsert into public.users (our Supabase table)
  //    We use firebase_uid as the stable identifier.
  const existing = await prisma.public_users.findFirst({
    where: { firebase_uid: firebaseUid },
  });

  let user: VerifyResult['user'];
  let isNewUser: boolean;

  if (existing) {
    // Update last-seen data (fcm_token, avatar, name, email, phone if missing)
    const updated = await prisma.public_users.update({
      where: { id: existing.id },
      data: {
        fcm_token: fcmToken ?? existing.fcm_token,
        avatar_url: avatarUrl ?? existing.avatar_url,
        email: email ?? existing.email,
        phone: phone ?? existing.phone,
        full_name: (existing.full_name === 'مستخدم جديد' && name !== 'مستخدم جديد') ? name : existing.full_name,
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
    user = { ...updated, firebase_uid: updated.firebase_uid! };
    isNewUser = false;
  } else {
    // Create new user record
    const created = await prisma.public_users.create({
      data: {
        firebase_uid: firebaseUid,
        phone: phone,
        email: email,
        full_name: name,
        avatar_url: avatarUrl,
        fcm_token: fcmToken ?? null,
        role: 'customer', // default role
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
    user = { ...created, firebase_uid: created.firebase_uid! };
    isNewUser = true;
  }

  // 3. Generate a Supabase JWT for the client to use with Realtime/RLS
  const jwtSecret = process.env.SUPABASE_JWT_SECRET;
  if (!jwtSecret) {
    throw new Error('SUPABASE_JWT_SECRET is missing from environment variables');
  }

  const supabaseToken = jwt.sign(
    {
      aud: 'authenticated',
      role: 'authenticated',
      sub: user.id, // Must match the UUID in public.users to satisfy auth.uid() in RLS
    },
    jwtSecret,
    { expiresIn: '30d' }
  );

  return { user, isNewUser, supabaseToken };
}
