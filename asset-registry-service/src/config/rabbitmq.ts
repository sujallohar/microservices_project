import amqplib from 'amqplib';

let connection: any = null;
let channel: any = null;

export const EXCHANGE_NAME = process.env.RABBITMQ_EXCHANGE || 'govasset.topic.exchange';
export const QUEUE_ASSET_REGISTRY = 'queue.asset.registry';

export const connectRabbitMQ = async (): Promise<any> => {
  if (channel) return channel;

  const url = process.env.RABBITMQ_URL || 'amqp://guest:guest@localhost:5672';

  try {
    connection = await amqplib.connect(url);
    channel = await connection.createChannel();

    // Declare main topic exchange
    await channel.assertExchange(EXCHANGE_NAME, 'topic', { durable: true });

    // Assert queue for Asset Registry
    await channel.assertQueue(QUEUE_ASSET_REGISTRY, { durable: true });

    // Bind to procurement approvals
    await channel.bindQueue(QUEUE_ASSET_REGISTRY, EXCHANGE_NAME, 'finance.procurement.approved');

    console.log('[AssetRegistry] RabbitMQ connected and exchange/queue asserted.');
    return channel;
  } catch (err: any) {
    console.error('[AssetRegistry] RabbitMQ Connection Error:', err.message);
    throw err;
  }
};

export const publishEvent = async (routingKey: string, payload: any) => {
  if (!channel) {
    channel = await connectRabbitMQ();
  }
  const content = Buffer.from(JSON.stringify(payload));
  channel.publish(EXCHANGE_NAME, routingKey, content, {
    persistent: true,
    contentType: 'application/json',
    timestamp: Date.now(),
  });
  console.log(`[AssetRegistry] Published event to ${EXCHANGE_NAME} [${routingKey}]:`, payload.eventId || payload.assetCode);
};
