import { Channel } from 'amqplib';
import { QUEUE_ASSET_REGISTRY, publishEvent } from '../config/rabbitmq';
import { AssetService } from './assetService';

export const startSagaConsumer = async (channel: Channel) => {
  console.log('[AssetRegistry] Starting Saga Consumer for queue:', QUEUE_ASSET_REGISTRY);

  await channel.consume(
    QUEUE_ASSET_REGISTRY,
    async (msg) => {
      if (!msg) return;

      const rawContent = msg.content.toString();
      console.log('[AssetRegistry] Saga Step 2: Received Procurement Event:', rawContent);

      let eventData: any;
      try {
        eventData = JSON.parse(rawContent);
        if (typeof eventData === 'string') {
          eventData = JSON.parse(eventData);
        }
      } catch (e: any) {
        console.error('[AssetRegistry] Failed to parse message JSON:', e.message);
        channel.ack(msg);
        return;
      }

      try {
        // Business Rule / Failure Trigger Simulation:
        // If specifications contain "TRIGGER_SAGA_FAILURE", simulate a catalog registration failure
        if (eventData.specifications && eventData.specifications.includes('TRIGGER_SAGA_FAILURE')) {
          throw new Error('Asset catalog rejected: Duplicate barcode identifier detected.');
        }

        // Happy Path: Create Asset in MongoDB & generate QR code
        const asset = await AssetService.createFromProcurement(eventData);
        console.log(`[AssetRegistry] Successfully registered Asset [${asset.assetCode}] in MongoDB`);

        // Emit asset.registered event for Step 3 (Maintenance Service)
        const assetRegisteredEvent = {
          eventId: `evt-ast-${Date.now()}`,
          assetId: asset._id.toString(),
          assetCode: asset.assetCode,
          name: asset.name,
          category: asset.category,
          department: asset.department,
          purchaseOrderId: eventData.poId,
          poNumber: eventData.poNumber,
          cost: asset.financials.purchaseCost,
          timestamp: Date.now(),
        };

        await publishEvent('asset.registered', assetRegisteredEvent);

        // Acknowledge message
        channel.ack(msg);

      } catch (err: any) {
        console.error(`[AssetRegistry] Saga Step 2 Error: ${err.message}. Emitting Saga Compensation Event!`);

        // Emit Compensating event to rollback Finance Service!
        const compensationEvent = {
          eventId: `evt-comp-${Date.now()}`,
          poId: eventData.poId,
          poNumber: eventData.poNumber,
          failedService: 'asset-registry-service',
          reason: err.message,
          timestamp: Date.now(),
        };

        await publishEvent('asset.registration.failed', compensationEvent);
        await publishEvent('saga.compensate.procurement', compensationEvent);

        channel.ack(msg);
      }
    },
    { noAck: false }
  );
};
