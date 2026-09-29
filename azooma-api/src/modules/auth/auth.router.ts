import { Router } from 'express';
import { verifyController } from './auth.controller';

const router = Router();

// ---------------------------------------------------------------------------
// POST /api/v1/auth/verify
// Public endpoint — no auth middleware required here since this IS the
// endpoint that establishes the session.
// ---------------------------------------------------------------------------
router.post('/verify', verifyController);

export { router as authRouter };
