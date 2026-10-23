import express from 'express';
import cors from 'cors';
import client from 'prom-client';
import { connectDB } from './config/db';
import { connectRabbitMQ } from './config/rabbitmq';
import { AssetService } from './services/assetService';
import { startSagaConsumer } from './services/sagaConsumer';
import { startGrpcServer } from './grpc/assetGrpcServer';
import assetRoutes from './routes/assetRoutes';

const app = express();
const PORT = process.env.PORT || 4002;
const GRPC_PORT = process.env.GRPC_PORT ? parseInt(process.env.GRPC_PORT, 10) : 9092;

// Prometheus Metrics Instrumentation
client.collectDefaultMetrics({ prefix: 'asset_registry_' });

app.use(cors());
app.use(express.json());

// Request logging middleware
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    console.log(`[AssetRegistry] ${req.method} ${req.url} - ${res.statusCode} (${Date.now() - start}ms)`);
  });
  next();
});

// Routes
app.use('/api/v1/assets', assetRoutes);

// Health & Metrics
app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'asset-registry-service', database: 'MongoDB' });
});

app.get('/metrics', async (req, res) => {
  res.set('Content-Type', client.register.contentType);
  res.end(await client.register.metrics());
});

// Bootstrapping
const startServer = async () => {
  try {
    // 1. Connect MongoDB
    await connectDB();
    await AssetService.seedDefaultAssetsIfEmpty();

    // 2. Connect RabbitMQ & start Saga consumer
    try {
      const channel = await connectRabbitMQ();
      await startSagaConsumer(channel);
    } catch (e: any) {
      console.warn('[AssetRegistry] RabbitMQ deferred connection:', e.message);
    }

    // 3. Start gRPC Service
    try {
      startGrpcServer(GRPC_PORT);
    } catch (e: any) {
      console.warn('[AssetRegistry] gRPC server initialization error:', e.message);
    }

    // 4. Start HTTP Express Server
    app.listen(PORT, () => {
      console.log(`[AssetRegistry] HTTP Server listening on port ${PORT}`);
    });
  } catch (err: any) {
    console.error('[AssetRegistry] Startup error:', err.message);
    process.exit(1);
  }
};

startServer();
