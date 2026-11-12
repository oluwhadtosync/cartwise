// Cartwise TypeScript Type Definitions

export type OrderStatus =
  | 'CREATED'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'AUTHORIZED'
  | 'CHECKOUT_IN_PROGRESS'
  | 'CAPTURED'
  | 'VOIDED'
  | 'FAILED';

export interface ShippingAddress {
  fullName: string;
  email: string;
  address: string;
  city: string;
  state: string;
  country: string; // ISO code or full name
  postalCode?: string;
  phone?: string;
}

export interface BillingAddress {
  cardholderName: string;
  address: string;
  city: string;
  state: string;
  country: string;
  postalCode?: string;
  sameAsShipping: boolean;
}

export interface CartwiseOrder {
  id: string; // internal order ID e.g. cw_ord_xxx
  userId: string;
  itemTitle: string;
  itemUrl: string;
  imageUrl?: string;
  itemPrice: number;
  shippingCost: number;
  serviceFee: number;
  totalAmount: number;
  currency: string; // e.g. "USD"
  merchantName: string;
  selectedVariety?: string;
  client?: 'web' | 'android';
  merchantOrderId?: string;
  paypalOrderId?: string;
  paypalAuthId?: string;
  paypalCaptureId?: string;
  paypalApproveUrl?: string;
  status: OrderStatus;
  shippingAddress: ShippingAddress;
  billingAddress?: BillingAddress;
  liveViewUrl?: string;
  reviewScreenshot?: string;
  failureReason?: string;
  guestEmail?: string;
  guestPhone?: string;
  merchantPasswordRequired?: boolean;
  merchantPassword?: string;
  merchantRecoveryUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuditLogEntry {
  id: string;
  orderId: string;
  stepNumber: number;
  stepName: string;
  description: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  timestamp: string;
  metadata?: Record<string, any>;
}

export interface ShippingMethod {
  id: string;
  name: string;
  carrier?: string;
  cost: number;
  estimatedDelivery?: string;
  trackingAvailable: boolean;
  isFree: boolean;
}

export interface ProductVariety {
  id?: string;
  name: string;
  sku?: string;
  inStock: boolean;
  stockCount: number;
  priceOffset?: number;
  unitPrice?: number;
  color?: string;
  size?: string;
  imageUrl?: string;
}

export interface WebsiteSafety {
  score: number; // 0 - 100
  level: 'SAFE' | 'CAUTION' | 'RISKY';
  domain: string;
  sslVerified: boolean;
  summary: string;
}

export interface DealResult {
  id: string;
  title: string;
  titleZh?: string;
  description: string;
  descriptionZh?: string;
  keywordsZh?: string[];
  brand: string;
  category: string;
  price: number;
  shippingCost: number;
  incrementalShippingPerUnit?: number;
  isFreeShipping?: boolean;
  totalCost: number;
  currency: string;
  storeName: string;
  storeUrl: string;
  imageUrl: string;
  rating: number;
  reviewsCount: number;
  inStock: boolean;
  stockCount?: number;
  varieties?: ProductVariety[];
  selectedVarietyName?: string;
  shippingMethods?: ShippingMethod[];
  selectedShippingMethodId?: string;
  safety?: WebsiteSafety;
  shipsToCountry: boolean;
  reasonWhy: string; // AI explanation "why this deal"
  score: number;
}

export interface SearchQueryRequest {
  query: string;
  country?: string; // defaults to "NG" (Lagos, Nigeria)
  maxBudget?: number;
  safetyCheck?: boolean;
  language?: 'en' | 'zh';
  model?: string; // e.g. "gemini-3.8-flash", "gemini-3.8-pro", "gemini-2.5-flash", "gemini-2.5-pro"
}
