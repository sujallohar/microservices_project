import mongoose, { Schema, Document } from 'mongoose';

export type LifecycleState =
  | 'PLANNED'
  | 'PROCURED'
  | 'REGISTERED'
  | 'DEPLOYED'
  | 'UNDER_MAINTENANCE'
  | 'RETIRED'
  | 'DISPOSED'
  | 'FAILED_ROLLBACK';

export interface IAsset extends Document {
  assetCode: string;
  name: string;
  category: 'INFRASTRUCTURE' | 'VEHICLE' | 'HEALTHCARE' | 'IT_HARDWARE' | 'FACILITY';
  department: string;
  custodian: string;
  location: {
    type: 'Point';
    coordinates: [number, number]; // [longitude, latitude]
    address?: string;
  };
  financials: {
    purchaseOrderId?: string;
    purchaseCost: number;
    currentBookValue: number;
    annualDepreciationRate: number; // e.g. 0.10 for 10%
    usefulLifeYears: number;
  };
  lifecycleState: LifecycleState;
  conditionScore: number; // 0 to 100
  qrCodeDataUrl?: string;
  specifications?: Record<string, any>;
  sagaCorrelationId?: string;
  createdAt: Date;
  updatedAt: Date;
}

const AssetSchema: Schema = new Schema(
  {
    assetCode: { type: String, required: true, unique: true, index: true },
    name: { type: String, required: true, index: true },
    category: {
      type: String,
      required: true,
      enum: ['INFRASTRUCTURE', 'VEHICLE', 'HEALTHCARE', 'IT_HARDWARE', 'FACILITY'],
      index: true,
    },
    department: { type: String, required: true, index: true },
    custodian: { type: String, default: 'DEPARTMENT_CUSTODIAN' },
    location: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point',
      },
      coordinates: {
        type: [Number],
        default: [72.5714, 23.0225], // Default Ahmedabad, Gujarat coordinates
      },
      address: { type: String, default: 'Gujarat Capital Region' },
    },
    financials: {
      purchaseOrderId: { type: String },
      purchaseCost: { type: Number, required: true },
      currentBookValue: { type: Number, required: true },
      annualDepreciationRate: { type: Number, default: 0.10 },
      usefulLifeYears: { type: Number, default: 10 },
    },
    lifecycleState: {
      type: String,
      required: true,
      enum: ['PLANNED', 'PROCURED', 'REGISTERED', 'DEPLOYED', 'UNDER_MAINTENANCE', 'RETIRED', 'DISPOSED', 'FAILED_ROLLBACK'],
      default: 'REGISTERED',
      index: true,
    },
    conditionScore: { type: Number, min: 0, max: 100, default: 100 },
    qrCodeDataUrl: { type: String },
    specifications: { type: Schema.Types.Mixed, default: {} },
    sagaCorrelationId: { type: String, index: true },
  },
  { timestamps: true }
);

AssetSchema.index({ 'location': '2dsphere' });

export default mongoose.model<IAsset>('Asset', AssetSchema);
