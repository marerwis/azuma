import { Router } from 'express';
import { authenticate, requireRole } from '../../middleware/auth.middleware';
import { list, getOne, create, update, remove } from './categories.controller';

const router = Router();

// Public — anyone can browse categories
router.get('/', list);
router.get('/:id', getOne);

// Admin only — create / edit / delete
router.post('/', authenticate, requireRole('admin'), create);
router.patch('/:id', authenticate, requireRole('admin'), update);
router.delete('/:id', authenticate, requireRole('admin'), remove);

export { router as categoriesRouter };
