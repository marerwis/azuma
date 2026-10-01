import { Router } from 'express';
import { authenticate, requireRole } from '../../middleware/auth.middleware';
import {
  createOrder,
  getMyOrders,
  getOrderById,
  listOrders,
  updateStatus,
  assignDriver,
  verifyPickup,
  verifyDelivery,
} from './orders.controller';

const router = Router();

// All orders routes require authentication
router.use(authenticate);

// ── Customer routes ──────────────────────────────────────────────────────────
router.post('/',    requireRole('customer', 'admin'), createOrder);
router.get('/me',   requireRole('customer', 'admin'), getMyOrders);

// ── Shared route — ownership check is done inside controller ─────────────────
router.get('/:id',  getOrderById);

// ── Admin + Vendor routes ────────────────────────────────────────────────────
router.get('/',     requireRole('admin', 'vendor'), listOrders);

// ── Status machine (admin | vendor | driver) ─────────────────────────────────
router.patch('/:id/status',        requireRole('admin', 'vendor', 'driver'), updateStatus);

// ── Driver dispatch ──────────────────────────────────────────────────────────
router.post('/:id/assign-driver',  requireRole('driver'), assignDriver);

// ── OTP verification ─────────────────────────────────────────────────────────
// verify-pickup:   driver (DELIVERY) or customer (PICKUP) confirms collection
// verify-delivery: driver confirms handoff to customer
router.post('/:id/verify-pickup',   requireRole('driver', 'customer', 'admin'), verifyPickup);
router.post('/:id/verify-delivery', requireRole('driver', 'admin'),             verifyDelivery);

export { router as ordersRouter };
