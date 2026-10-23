import { Router } from 'express';
import { getAssets, getAssetById, createAsset, updateLifecycle, getStats } from '../controllers/assetController';

const router = Router();

router.get('/', getAssets);
router.get('/stats', getStats);
router.get('/:id', getAssetById);
router.post('/', createAsset);
router.put('/:id/state', updateLifecycle);

export default router;
