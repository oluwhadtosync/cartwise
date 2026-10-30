// Cartwise Data Store
// Atomic, persistent JSON store with relational query helpers and audit trail

import fs from 'fs';
import path from 'path';
import { CartwiseOrder, AuditLogEntry } from '../types';

interface Schema {
  orders: Record<string, CartwiseOrder>;
  auditLogs: Record<string, AuditLogEntry[]>;
}

class DataStore {
  private filePath: string;
  private data: Schema = { orders: {}, auditLogs: {} };
  private writeQueue: Promise<void> = Promise.resolve();

  constructor(filePath?: string) {
    this.filePath = filePath || path.join(process.cwd(), 'data', 'cartwise-db.json');
    this.init();
  }

  private init() {
    try {
      const dir = path.dirname(this.filePath);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }
      if (fs.existsSync(this.filePath)) {
        const raw = fs.readFileSync(this.filePath, 'utf8');
        this.data = JSON.parse(raw);
        if (!this.data.orders) this.data.orders = {};
        if (!this.data.auditLogs) this.data.auditLogs = {};
      } else {
        this.persistSync();
      }
    } catch (e) {
      console.error('Error initializing DataStore, using in-memory default:', e);
      this.data = { orders: {}, auditLogs: {} };
    }
  }

  private persistSync() {
    const dir = path.dirname(this.filePath);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    const tmp = `${this.filePath}.tmp.${Date.now()}`;
    fs.writeFileSync(tmp, JSON.stringify(this.data, null, 2), 'utf8');
    fs.renameSync(tmp, this.filePath);
  }

  private async persist(): Promise<void> {
    this.writeQueue = this.writeQueue.then(async () => {
      try {
        const dir = path.dirname(this.filePath);
        if (!fs.existsSync(dir)) await fs.promises.mkdir(dir, { recursive: true });
        const tmp = `${this.filePath}.tmp.${Date.now()}`;
        await fs.promises.writeFile(tmp, JSON.stringify(this.data, null, 2), 'utf8');
        await fs.promises.rename(tmp, this.filePath);
      } catch (err) {
        console.error('Failed to persist DataStore:', err);
      }
    });
    return this.writeQueue;
  }

  // --- Orders ---

  async createOrder(order: CartwiseOrder): Promise<CartwiseOrder> {
    this.data.orders[order.id] = order;
    await this.persist();
    return order;
  }

  async getOrder(id: string): Promise<CartwiseOrder | null> {
    return this.data.orders[id] || null;
  }

  async getOrderByPaypalId(paypalOrderId: string): Promise<CartwiseOrder | null> {
    const list = Object.values(this.data.orders);
    return list.find(o => o.paypalOrderId === paypalOrderId) || null;
  }

  async updateOrder(id: string, updates: Partial<CartwiseOrder>): Promise<CartwiseOrder | null> {
    const order = this.data.orders[id];
    if (!order) return null;
    const updated: CartwiseOrder = {
      ...order,
      ...updates,
      updatedAt: new Date().toISOString()
    };
    this.data.orders[id] = updated;
    await this.persist();
    return updated;
  }

  async listOrders(userId?: string): Promise<CartwiseOrder[]> {
    const list = Object.values(this.data.orders);
    if (userId) {
      return list.filter(o => o.userId === userId).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
    }
    return list.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  }

  // --- Audit Logs ---

  async addAuditLog(entry: {
    orderId: string;
    stepNumber: number;
    stepName: string;
    description: string;
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
    metadata?: Record<string, any>;
  }): Promise<AuditLogEntry> {
    if (!this.data.auditLogs[entry.orderId]) {
      this.data.auditLogs[entry.orderId] = [];
    }

    const logEntry: AuditLogEntry = {
      id: `log_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      orderId: entry.orderId,
      stepNumber: entry.stepNumber,
      stepName: entry.stepName,
      description: entry.description,
      status: entry.status,
      timestamp: new Date().toISOString(),
      metadata: entry.metadata
    };

    this.data.auditLogs[entry.orderId].push(logEntry);
    await this.persist();
    return logEntry;
  }

  async getAuditLogs(orderId: string): Promise<AuditLogEntry[]> {
    return (this.data.auditLogs[orderId] || []).sort((a, b) => a.stepNumber - b.stepNumber);
  }
}

export const db = new DataStore();
