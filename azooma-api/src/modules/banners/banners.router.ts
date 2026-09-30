import { Router } from 'express';
import { getActiveBanners } from './banners.service';

export const bannersRouter = Router();

// GET /api/v1/banners
bannersRouter.get('/', async (_req, res) => {
  try {
    const data = await getActiveBanners();
    res.json({ success: true, data, count: data.length });
  } catch (error) {
    console.error('GET /banners error:', error);
    res.status(500).json({ success: false, error: 'Failed to fetch banners' });
  }
});
