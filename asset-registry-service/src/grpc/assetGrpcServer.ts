import path from 'path';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import Asset from '../models/Asset';

const PROTO_PATH = path.resolve(__dirname, '../../../shared/proto/asset.proto');

export const startGrpcServer = (port = 9092) => {
  const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true,
  });

  const protoDescriptor = grpc.loadPackageDefinition(packageDefinition) as any;
  const assetPackage = protoDescriptor.govasset.asset;

  const server = new grpc.Server();

  server.addService(assetPackage.AssetValidator.service, {
    GetAssetSummary: async (call: any, callback: any) => {
      try {
        const { asset_id } = call.request;
        const asset = await Asset.findOne({
          $or: [{ _id: asset_id.match(/^[0-9a-fA-F]{24}$/) ? asset_id : null }, { assetCode: asset_id }],
        });

        if (!asset) {
          return callback(null, { found: false, asset_id, error_message: 'Asset not found' });
        }

        callback(null, {
          found: true,
          asset_id: asset._id.toString(),
          asset_code: asset.assetCode,
          name: asset.name,
          category: asset.category,
          lifecycle_state: asset.lifecycleState,
          purchase_cost: asset.financials.purchaseCost,
          current_book_value: asset.financials.currentBookValue,
          department: asset.department,
          custodian: asset.custodian,
        });
      } catch (err: any) {
        callback(err);
      }
    },

    ValidateAssetEligibility: async (call: any, callback: any) => {
      try {
        const { asset_id, target_operation } = call.request;
        const asset = await Asset.findOne({
          $or: [{ _id: asset_id.match(/^[0-9a-fA-F]{24}$/) ? asset_id : null }, { assetCode: asset_id }],
        });

        if (!asset) {
          return callback(null, { eligible: false, reason: 'Asset does not exist', current_state: 'UNKNOWN' });
        }

        if (asset.lifecycleState === 'RETIRED' || asset.lifecycleState === 'DISPOSED') {
          return callback(null, {
            eligible: false,
            reason: `Asset is already ${asset.lifecycleState} and cannot accept operations`,
            current_state: asset.lifecycleState,
          });
        }

        callback(null, {
          eligible: true,
          reason: 'Asset is eligible for operation',
          current_state: asset.lifecycleState,
        });
      } catch (err: any) {
        callback(err);
      }
    },
  });

  server.bindAsync(`0.0.0.0:${port}`, grpc.ServerCredentials.createInsecure(), (err, boundPort) => {
    if (err) {
      console.error('[AssetRegistry] Failed to bind gRPC server:', err.message);
      return;
    }
    console.log(`[AssetRegistry] gRPC server running at 0.0.0.0:${boundPort}`);
  });
};
