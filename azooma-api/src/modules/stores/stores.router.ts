import { Router } from 'express';
import { authenticate, requireRole } from '../../middleware/auth.middleware';
import {
  listStores, getStore, createStore, updateStore, deleteStore,
  listMenuCategories, createMenuCategory, deleteMenuCategory,
  listProducts, createProduct, updateProduct, deleteProduct,
} from './stores.controller';

const router = Router();

// ── Stores ─────────────────────────────────────────────────────────────────
router.get('/', listStores);                                         // Public
router.get('/:id', getStore);                                        // Public
router.post('/', authenticate, requireRole('admin'), createStore);
router.patch('/:id', authenticate, requireRole('admin', 'vendor'), updateStore);
router.delete('/:id', authenticate, requireRole('admin'), deleteStore);

// ── Menu Categories (nested) ───────────────────────────────────────────────
router.get('/:id/menu-categories', listMenuCategories);              // Public
router.post('/:id/menu-categories', authenticate, requireRole('admin', 'vendor'), createMenuCategory);
router.delete('/:id/menu-categories/:catId', authenticate, requireRole('admin', 'vendor'), deleteMenuCategory);

// ── Products (nested) ──────────────────────────────────────────────────────
router.get('/:id/products', listProducts);                           // Public
router.post('/:id/products', authenticate, requireRole('admin', 'vendor'), createProduct);
router.patch('/:id/products/:productId', authenticate, requireRole('admin', 'vendor'), updateProduct);
router.delete('/:id/products/:productId', authenticate, requireRole('admin', 'vendor'), deleteProduct);

export { router as storesRouter };
