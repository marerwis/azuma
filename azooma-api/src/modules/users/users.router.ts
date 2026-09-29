import { Router } from 'express';
import { authenticate, requireRole } from '../../middleware/auth.middleware';
import {
  listUsers,
  getMe,
  getUserById,
  updateMe,
  changeRole,
  setStatus,
} from './users.controller';

const router = Router();

// All routes require authentication
router.use(authenticate);

// ── Own profile ────────────────────────────────────────────────────────────
router.get('/me', getMe);                         // Any authenticated user
router.patch('/me', updateMe);                    // Any authenticated user

// ── Admin-only routes ──────────────────────────────────────────────────────
router.get('/', requireRole('admin'), listUsers);
router.get('/:id', requireRole('admin'), getUserById);
router.patch('/:id/role', requireRole('admin'), changeRole);
router.patch('/:id/status', requireRole('admin'), setStatus);

export { router as usersRouter };
