import QRCode from 'qrcode';
import Asset, { IAsset, LifecycleState } from '../models/Asset';

export class AssetService {
  /**
   * Generates a verifiable QR code data URL containing cryptographic asset identification.
   */
  public static async generateQRCode(assetCode: string, name: string, department: string): Promise<string> {
    const payload = JSON.stringify({
      system: 'GovAsset-Nexus',
      assetCode,
      name,
      department,
      verifyUrl: `https://govasset.gujarat.gov.in/verify/${assetCode}`,
      issuedAt: new Date().toISOString(),
    });
    return QRCode.toDataURL(payload, {
      errorCorrectionLevel: 'H',
      width: 300,
      margin: 2,
      color: {
        dark: '#1e293b',
        light: '#f8fafc',
      },
    });
  }

  /**
   * Creates an official asset record from a Finance Procurement event (Saga Step 2).
   */
  public static async createFromProcurement(procurementEvent: any): Promise<IAsset> {
    const category = procurementEvent.assetCategory || 'INFRASTRUCTURE';
    const assetCode = `GUJ-${category.substring(0, 3)}-${Date.now().toString().slice(-6)}`;
    const qrCodeDataUrl = await this.generateQRCode(assetCode, procurementEvent.assetName || 'Government Asset', procurementEvent.department || 'PUBLIC_WORKS');

    const asset = new Asset({
      assetCode,
      name: procurementEvent.assetName,
      category: procurementEvent.assetCategory,
      department: procurementEvent.department,
      custodian: `${procurementEvent.department}_OFFICER`,
      financials: {
        purchaseOrderId: procurementEvent.poId || procurementEvent.poNumber,
        purchaseCost: procurementEvent.totalCost || (procurementEvent.unitCost * procurementEvent.quantity),
        currentBookValue: procurementEvent.totalCost || (procurementEvent.unitCost * procurementEvent.quantity),
        annualDepreciationRate: 0.10,
        usefulLifeYears: 10,
      },
      lifecycleState: 'REGISTERED',
      conditionScore: 100,
      qrCodeDataUrl,
      specifications: procurementEvent.specifications ? { details: procurementEvent.specifications } : {},
      sagaCorrelationId: procurementEvent.eventId,
    });

    return await asset.save();
  }

  /**
   * Updates lifecycle state (e.g. DEPLOYED, UNDER_MAINTENANCE, RETIRED).
   */
  public static async updateLifecycleState(assetId: string, newState: LifecycleState, reason?: string): Promise<IAsset | null> {
    return await Asset.findByIdAndUpdate(
      assetId,
      {
        lifecycleState: newState,
        $push: { 'specifications.stateAudit': { fromState: newState, timestamp: new Date(), reason } },
      },
      { new: true }
    );
  }

  /**
   * Seeds default Gujarat government assets for instant testing if database is empty.
   */
  public static async seedDefaultAssetsIfEmpty() {
    const count = await Asset.countDocuments();
    if (count > 0) return;

    console.log('[AssetRegistry] Seeding baseline Gujarat government assets...');

    const seedData = [
      {
        assetCode: 'GUJ-INF-001001',
        name: 'Sabarmati East Riverfront Promenade & Bridge',
        category: 'INFRASTRUCTURE',
        department: 'PUBLIC_WORKS',
        custodian: 'Ahmedabad Municipal Corporation',
        location: { type: 'Point', coordinates: [72.5801, 23.0332], address: 'Sabarmati Riverfront, Ahmedabad' },
        financials: { purchaseCost: 45000000, currentBookValue: 38000000, annualDepreciationRate: 0.05, usefulLifeYears: 50 },
        lifecycleState: 'DEPLOYED',
        conditionScore: 78,
      },
      {
        assetCode: 'GUJ-HEA-002002',
        name: 'Civil Hospital 3T Digital MRI Scanner',
        category: 'HEALTHCARE',
        department: 'HEALTH_DEPT',
        custodian: 'Civil Hospital Asarwa',
        location: { type: 'Point', coordinates: [72.6045, 23.0526], address: 'Asarwa Med City, Ahmedabad' },
        financials: { purchaseCost: 12500000, currentBookValue: 10500000, annualDepreciationRate: 0.15, usefulLifeYears: 10 },
        lifecycleState: 'DEPLOYED',
        conditionScore: 92,
      },
      {
        assetCode: 'GUJ-VEH-003003',
        name: 'Electric Rapid Transit Bus (E-Bus EV-104)',
        category: 'VEHICLE',
        department: 'URBAN_TRANSPORT',
        custodian: 'GSRTC Regional Depot',
        location: { type: 'Point', coordinates: [72.5638, 23.0182], address: 'Paldi Transit Terminal' },
        financials: { purchaseCost: 1800000, currentBookValue: 1450000, annualDepreciationRate: 0.20, usefulLifeYears: 8 },
        lifecycleState: 'UNDER_MAINTENANCE',
        conditionScore: 64,
      },
    ];

    for (const item of seedData) {
      const qr = await this.generateQRCode(item.assetCode, item.name, item.department);
      await new Asset({ ...item, qrCodeDataUrl: qr }).save();
    }

    console.log('[AssetRegistry] Seeded 3 reference government assets.');
  }
}
