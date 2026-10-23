import mongoose from 'mongoose';

export const connectDB = async () => {
  const mongoUri = process.env.MONGO_URI || 'mongodb://registry_admin:registry_password@localhost:27017/govasset_registry?authSource=admin';
  try {
    await mongoose.connect(mongoUri);
    console.log('[AssetRegistry] Connected successfully to MongoDB:', mongoUri.split('@')[1] || mongoUri);
  } catch (err: any) {
    console.error('[AssetRegistry] MongoDB Connection Error:', err.message);
    // Don't crash immediately in dev, retry after delay
    setTimeout(connectDB, 5000);
  }
};
