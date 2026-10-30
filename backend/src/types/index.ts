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
  country: string; // ISO code, e.g. "NG", "US"
  postalCode?: string;
  phone?: string;
}

export interface CartwiseOrder {
  id: string; // internal order ID e.g. cw_ord_xxx
  userId: string;
  itemTitle: string;
  itemUrl: string;
  itemPrice: number;
  shippingCost: number;
  serviceFee: number;
  totalAmount: number;
  currency: string; // e.g. "USD"
  merchantName: string;
  merchantOrderId?: string;
  paypalOrderId?: string;
  paypalAuthId?: string;
  paypalCaptureId?: string;
  paypalApproveUrl?: string;
  status: OrderStatus;
  shippingAddress: ShippingAddress;
  liveViewUrl?: string;
  reviewScreenshot?: string;
  failureReason?: string;
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

export interface DealResult {
  id: string;
  title: string;
  description: string;
  brand: string;
  category: string;
  price: number;
  shippingCost: number;
  totalCost: number;
  currency: string;
  storeName: string;
  storeUrl: string;
  imageUrl: string;
  rating: number;
  reviewsCount: number;
  inStock: boolean;
  shipsToCountry: boolean;
  reasonWhy: string; // AI explanation "why this deal"
  score: number;
}

export interface SearchQueryRequest {
  query: string;
  country?: string; // defaults to "NG" (Lagos, Nigeria)
  maxBudget?: number;
}
