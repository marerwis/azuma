import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './categories.service';

// ── Validation Schemas ─────────────────────────────────────────────────────
const createSchema = z.object({
  name: z.string().min(1),
  image_url: z.string().url().optional(),
  sort_order: z.number().int().optional(),
});

const updateSchema = createSchema.partial().extend({
  is_active: z.boolean().optional(),
});

// ── Controllers ────────────────────────────────────────────────────────────

/** GET /api/v1/categories */
export async function list(req: Request, res: Response): Promise<void> {
  try {
    const data = await svc.getAllCategories();
    res.json({ success: true, data });
  } catch (e: any) {
    console.error('[Categories] list error:', e?.message ?? e);
    console.error('[Categories] error code:', e?.code);
    res.status(500).json({ error: 'Internal server error', detail: e?.message });
  }
}

/** GET /api/v1/categories/:id */
export async function getOne(req: Request, res: Response): Promise<void> {
  try {
    const category = await svc.getCategoryById(req.params.id as string);
    if (!category) {
      res.status(404).json({ error: 'Category not found' });
      return;
    }
    res.json({ success: true, data: category });
  } catch (e: any) {
    console.error('[Categories] getOne error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** POST /api/v1/categories  (admin only) */
export async function create(req: Request, res: Response): Promise<void> {
  const parsed = createSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.createCategory(parsed.data);
    res.status(201).json({ success: true, data });
  } catch (e: any) {
    console.error('[Categories] create error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** PATCH /api/v1/categories/:id  (admin only) */
export async function update(req: Request, res: Response): Promise<void> {
  const parsed = updateSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: 'Validation failed', details: parsed.error.flatten() });
    return;
  }
  try {
    const data = await svc.updateCategory(req.params.id as string, parsed.data);
    res.json({ success: true, data });
  } catch (e: any) {
    if (e?.code === 'P2025') {
      res.status(404).json({ error: 'Category not found' });
      return;
    }
    console.error('[Categories] update error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/** DELETE /api/v1/categories/:id  (admin only) */
export async function remove(req: Request, res: Response): Promise<void> {
  try {
    await svc.deleteCategory(req.params.id as string);
    res.json({ success: true, message: 'Category deleted' });
  } catch (e: any) {
    if (e?.code === 'P2025') {
      res.status(404).json({ error: 'Category not found' });
      return;
    }
    console.error('[Categories] delete error:', e);
    res.status(500).json({ error: 'Internal server error' });
  }
}
