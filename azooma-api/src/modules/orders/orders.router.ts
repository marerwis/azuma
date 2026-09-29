import { Router } from 'express';
import { authenticate, requireRole } from '../../middleware/auth.middleware';
import {
  createOrder,
  getMyOrders,
  getOrderById,
  listOrders,
  updateStatus,
} from './orders.controller';

const router = Router();

// All orders routes require authentication
router.use(authenticate);

// ── Customer routes ─────────────────────────────────────────────────────────
router.post('/', requireRole('customer', 'admin'), createOrder);
router.get('/me', requireRole('customer', 'admin'), getMyOrders);

// ── Shared route — ownership check is done inside controller ────────────────
router.get('/:id', getOrderById);

// ── Admin + Vendor + Driver routes ─────────────────────────────────────────
router.get('/', requireRole('admin', 'vendor'), listOrders);
router.patch('/:id/status', requireRole('admin', 'vendor', 'driver'), updateStatus);

export { router as ordersRouter };
