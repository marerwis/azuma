import { Request, Response, NextFunction } from 'express';
import { supabase } from '../config/supabase';
import { prisma } from '../config/db';

// ---------------------------------------------------------------------------
// Extends Express Request to carry our resolved user
// ---------------------------------------------------------------------------
declare global {
  namespace Express {
    interface Request {
      user?: {
        id: string;
        firebaseUid: string;
        email: string | null;
        role: string;
        full_name: string;
      };
    }
  }
}

// ---------------------------------------------------------------------------
// authenticate middleware
// 1. Extracts Bearer token from Authorization header
// 2. Verifies it with Supabase Auth
// 3. Looks up the user in Supabase public_users
// 4. Attaches req.user for downstream handlers
// ---------------------------------------------------------------------------
export const authenticate = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith('Bearer ')) {
      res.status(401).json({ error: 'Unauthorized: No token provided' });
      return;
    }

    const token = authHeader.split(' ')[1];
    
    // Validate the token directly against the Supabase Auth server
    const { data: { user: authUser }, error } = await supabase.auth.getUser(token);
    
    if (error || !authUser) {
      console.error('[Auth Middleware] Supabase getUser error:', error?.message);
      res.status(401).json({ error: 'Unauthorized: Invalid or expired token' });
      return;
    }

    const supabaseUid = authUser.id;
    // provider_id can sometimes be stored in user_metadata, but we'll default to the UUID
    const providerId = authUser.user_metadata?.provider_id || authUser.user_metadata?.sub || supabaseUid;

    const user = await prisma.public_users.findFirst({
      where: {
        OR: [
          { id: supabaseUid },
          { firebase_uid: providerId }
        ]
      },
      select: { id: true, role: true, email: true, full_name: true, firebase_uid: true },
    });

    if (!user) {
      res.status(401).json({
        error: 'Unauthorized: User not found. Please call /auth/verify first.',
      });
      return;
    }

    req.user = {
      id: user.id,
      firebaseUid: user.firebase_uid || supabaseUid,
      email: user.email,
      role: user.role,
      full_name: user.full_name,
    };

    next();
  } catch (error: any) {
    console.error('[Auth Middleware] Error:', error?.message ?? error);
    res.status(401).json({ error: 'Unauthorized: Internal auth error' });
  }
};

// ---------------------------------------------------------------------------
// requireRole guard factory
// Usage: router.delete('/:id', authenticate, requireRole('admin'), handler)
// ---------------------------------------------------------------------------
export const requireRole = (...roles: string[]) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }
    if (!roles.includes(req.user.role)) {
      res.status(403).json({
        error: `Forbidden: requires one of [${roles.join(', ')}]`,
      });
      return;
    }
    next();
  };
};
