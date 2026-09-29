import { Request, Response, NextFunction } from 'express';
import { getAuth } from 'firebase-admin/auth';
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
// 2. Verifies it with Firebase Admin SDK
// 3. Looks up the user in Supabase via firebase_uid (our stable identifier)
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
    const decoded = await getAuth().verifyIdToken(token);

    // Lookup by firebase_uid — the stable identifier set on /auth/verify
    const user = await prisma.public_users.findFirst({
      where: { firebase_uid: decoded.uid },
      select: { id: true, role: true, email: true, full_name: true },
    });

    if (!user) {
      res.status(401).json({
        error: 'Unauthorized: User not found. Please call /auth/verify first.',
      });
      return;
    }

    req.user = {
      id: user.id,
      firebaseUid: decoded.uid,
      email: user.email,
      role: user.role,
      full_name: user.full_name,
    };

    next();
  } catch (error: any) {
    const msg = error?.message ?? '';
    if (msg.includes('auth/') || msg.includes('Decoding Firebase')) {
      res.status(401).json({ error: 'Unauthorized: Invalid or expired token' });
      return;
    }
    console.error('[Auth Middleware] Error:', error);
    res.status(500).json({ error: 'Internal server error' });
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
