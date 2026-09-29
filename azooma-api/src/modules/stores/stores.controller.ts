import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './stores.service';

// ── Validation Schemas ─────────────────────────────────────────────────────
const createStoreSchema = z.object({
  vendor_id: z.string().uuid(),
  name: z.string().min(1),
  description: z.string().optional(),
  image_url: z.string().url().optional(),
  cover_url: z.string().url().optional(),
  address: z.string().min(1),
  latitude: z.number(),
  longitude: z.number(),
  app_category_id: z.string().uuid().optional(),
  delivery_radius_km: z.number().optional(),
  commission_rate: z.number().optional(),
});

const updateStoreSchema = createStoreSchema
  .omit({ vendor_id: true })
  .partial()
  .extend({
    is_active: z.boolean().optional(),
    is_open: z.boolean().optional(),
  });

const createMenuCatSchema = z.object({
  name: z.string().min(1),
  sort_order: z.number().int().optional(),
});

const createProductSchema = z.object({
  name: z.string().min(1),
  description: z.string().optional(),
  base_price: z.number().positive(),
  image_url: z.string().url().optional(),
  is_veg: z.boolean().optional(),
  menu_category_id: z.string().uuid().optional(),
  stock_quantity: z.number().int().optional(),
});

const updateProductSchema = createProductSchema.partial().extend({
  is_active: z.boolean().optional(),
});

// ── Store Controllers ──────────────────────────────────────────────────────

/** GET /api/v1/stores */
export async function listStores(req: Request, res: Response): Promise<void> {
  try {
    const { categoryId, isOpen } = req.query;
    const data = await svc.getAllStores({
      categoryId: categoryId as string | undefined,
      isOpen: isOpen === 'true' ? true : isOpen === 'false' ? false : undefined,
    });
    res.json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] listStores error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** GET /api/v1/stores/:id */
export async function getStore(req: Request, res: Response): Promise<void> {
  try {
    const store = await svc.getStoreById(req.params.id as string);
    if (!store) {
      res.status(404).json({ error: 'Store not found' });
      return;
    }
    res.json({ success: true, data: store });
  } catch (e: any) {
    console.error('[Stores] getStore error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/stores  (admin only) */
export async function createStore(req: Request, res: Response): Promise<void> {
  const parsed = createStoreSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.createStore(parsed.data);
    res.status(201).json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] createStore error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/stores/:id  (admin only) */
export async function updateStore(req: Request, res: Response): Promise<void> {
  const parsed = updateStoreSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateStore(req.params.id as string, parsed.data);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'Store not found' }); return; }
    console.error('[Stores] updateStore error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** DELETE /api/v1/stores/:id  (admin only) */
export async function deleteStore(req: Request, res: Response): Promise<void> {
  try {
    await svc.deleteStore(req.params.id as string);
    res.json({ success: true, message: 'Store deleted' });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'Store not found' }); return; }
    console.error('[Stores] deleteStore error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

// ── Menu Category Controllers ──────────────────────────────────────────────

/** GET /api/v1/stores/:id/menu-categories */
export async function listMenuCategories(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.getMenuCategories(req.params.id as string);
    res.json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] listMenuCategories error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/stores/:id/menu-categories  (admin only) */
export async function createMenuCategory(req: Request, res: Response): Promise<void> {
  const parsed = createMenuCatSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.createMenuCategory(req.params.id as string, parsed.data.name, parsed.data.sort_order);
    res.status(201).json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] createMenuCategory error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** DELETE /api/v1/stores/:id/menu-categories/:catId  (admin only) */
export async function deleteMenuCategory(req: Request, res: Response): Promise<void> {
  try {
    await svc.deleteMenuCategory(req.params.catId as string);
    res.json({ success: true, message: 'Menu category deleted' });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'Menu category not found' }); return; }
    console.error('[Stores] deleteMenuCategory error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

// ── Product Controllers ────────────────────────────────────────────────────

/** GET /api/v1/stores/:id/products?menuCategoryId=xxx */
export async function listProducts(req: Request, res: Response): Promise<void> {
  try {
    const { menuCategoryId } = req.query;
    const data = await svc.getProducts(req.params.id as string, menuCategoryId as string | undefined);
    res.json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] listProducts error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/stores/:id/products  (admin/vendor only) */
export async function createProduct(req: Request, res: Response): Promise<void> {
  const parsed = createProductSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.createProduct({ store_id: req.params.id as string, ...parsed.data });
    res.status(201).json({ success: true, data });
  } catch (e: any) {
    console.error('[Stores] createProduct error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/stores/:id/products/:productId  (admin/vendor only) */
export async function updateProduct(req: Request, res: Response): Promise<void> {
  const parsed = updateProductSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateProduct(req.params.productId as string, parsed.data);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'Product not found' }); return; }
    console.error('[Stores] updateProduct error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** DELETE /api/v1/stores/:id/products/:productId  (admin/vendor only) */
export async function deleteProduct(req: Request, res: Response): Promise<void> {
  try {
    await svc.deleteProduct(req.params.productId as string);
    res.json({ success: true, message: 'Product deleted' });
  } catch (e: any) {
    if (e?.code === 'P2025') { res.status(404).json({ error: 'Product not found' }); return; }
    console.error('[Stores] deleteProduct error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}
