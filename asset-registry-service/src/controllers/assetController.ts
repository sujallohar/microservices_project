import { Request, Response } from 'express';
import Asset from '../models/Asset';
import { AssetService } from '../services/assetService';

export const getAssets = async (req: Request, res: Response) => {
  try {
    const { category, department, state, search, page = 1, limit = 20 } = req.query;

    const filter: any = {};
    if (category) filter.category = category;
    if (department) filter.department = department;
    if (state) filter.lifecycleState = state;
    if (search) {
      filter.$or = [
        { name: { $regex: search, $options: 'i' } },
        { assetCode: { $regex: search, $options: 'i' } },
      ];
    }

    const skip = (Number(page) - 1) * Number(limit);
    const [assets, total] = await Promise.all([
      Asset.find(filter).skip(skip).limit(Number(limit)).sort({ createdAt: -1 }),
      Asset.countDocuments(filter),
    ]);

    res.json({
      success: true,
      message: 'Assets retrieved successfully',
      data: assets,
      pagination: {
        page: Number(page),
        limit: Number(limit),
        total,
        pages: Math.ceil(total / Number(limit)),
      },
    });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
};

export const getAssetById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const asset = await Asset.findOne({
      $or: [{ _id: id.match(/^[0-9a-fA-F]{24}$/) ? id : null }, { assetCode: id }],
    });

    if (!asset) {
      return res.status(404).json({ success: false, error: 'Asset not found' });
    }

    res.json({ success: true, data: asset });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
};

export const createAsset = async (req: Request, res: Response) => {
  try {
    const { name, category, department, purchaseCost, custodian, location, specifications } = req.body;

    const assetCode = `GUJ-${category.substring(0, 3)}-${Date.now().toString().slice(-6)}`;
    const qrCodeDataUrl = await AssetService.generateQRCode(assetCode, name, department);

    const asset = new Asset({
      assetCode,
      name,
      category,
      department,
      custodian: custodian || `${department}_OFFICER`,
      location: location || { type: 'Point', coordinates: [72.5714, 23.0225] },
      financials: {
        purchaseCost,
        currentBookValue: purchaseCost,
        annualDepreciationRate: 0.10,
        usefulLifeYears: 10,
      },
      lifecycleState: 'REGISTERED',
      conditionScore: 100,
      qrCodeDataUrl,
      specifications: specifications || {},
    });

    await asset.save();
    res.status(201).json({ success: true, message: 'Asset created successfully', data: asset });
  } catch (err: any) {
    res.status(400).json({ success: false, error: err.message });
  }
};

export const updateLifecycle = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const { state, reason } = req.body;

    const updated = await AssetService.updateLifecycleState(id, state, reason);
    if (!updated) {
      return res.status(404).json({ success: false, error: 'Asset not found' });
    }

    res.json({ success: true, message: `Asset lifecycle transitioned to ${state}`, data: updated });
  } catch (err: any) {
    res.status(400).json({ success: false, error: err.message });
  }
};

export const getStats = async (req: Request, res: Response) => {
  try {
    const [total, byCategory, byState, avgCondition] = await Promise.all([
      Asset.countDocuments(),
      Asset.aggregate([{ $group: { _id: '$category', count: { $sum: 1 } } }]),
      Asset.aggregate([{ $group: { _id: '$lifecycleState', count: { $sum: 1 } } }]),
      Asset.aggregate([{ $group: { _id: null, avgCondition: { $avg: '$conditionScore' } } }]),
    ]);

    res.json({
      success: true,
      data: {
        totalAssets: total,
        byCategory: Object.fromEntries(byCategory.map((i) => [i._id, i.count])),
        byState: Object.fromEntries(byState.map((i) => [i._id, i.count])),
        averageConditionScore: avgCondition[0] ? Math.round(avgCondition[0].avgCondition) : 100,
      },
    });
  } catch (err: any) {
    res.status(500).json({ success: false, error: err.message });
  }
};
