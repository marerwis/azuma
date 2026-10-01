import { Router } from 'express';
import { authenticate } from '../../middleware/auth.middleware';
import * as ctrl from './addresses.controller';

const router = Router();

router.use(authenticate);

router.get('/', ctrl.getAddresses);
router.post('/', ctrl.createAddress);
router.put('/:id', ctrl.updateAddress);
router.delete('/:id', ctrl.deleteAddress);

export { router as addressesRouter };
