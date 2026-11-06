// Cartwise Backend Configuration
import dns from 'dns';
import dotenv from 'dotenv';
import fs from 'fs';
import path from 'path';

// Force IPv4 resolution to prevent 70s IPv6 timeouts on PRoot/mobile networks
try {
  dns.setDefaultResultOrder('ipv4first');
} catch (e) {
  // ignore
}

// Load .env file if present
dotenv.config();

// Helper to read secret from environment variable or ~/.secrets/<NAME>
function getSecret(name: string, fallback: string = ''): string {
  if (process.env[name]) {
    return process.env[name] as string;
  }
  const secretPath = path.join(process.env.HOME || '/root', '.secrets', name);
  try {
    if (fs.existsSync(secretPath)) {
      return fs.readFileSync(secretPath, 'utf8').trim();
    }
  } catch (e) {
    // ignore
  }
  return fallback;
}

export const config = {
  port: parseInt(process.env.PORT || '3000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  backendUrl: process.env.BACKEND_URL || process.env.RENDER_EXTERNAL_URL || 'http://localhost:3000',
  demoStoreUrl: process.env.DEMO_STORE_URL || 'http://localhost:4000',

  // Feature Flags
  demoMode: process.env.DEMO_MODE !== 'false', // default true for hackathon
  dryRun: process.env.DRY_RUN === 'true', // stops before clicking final place order on real stores

  // PayPal Sandbox Settings
  paypal: {
    clientId: getSecret('PAYPAL_CLIENT_ID', 'sandbox_client_id_placeholder'),
    clientSecret: getSecret('PAYPAL_CLIENT_SECRET', 'sandbox_secret_placeholder'),
    apiUrl: 'https://api-m.sandbox.paypal.com',
    serviceFee: 3.00, // Cartwise platform service fee ($3.00 per agent buy)
    currency: 'USD'
  },

  // Google Gemini AI
  gemini: {
    apiKey: getSecret('GEMINI_API_KEY', ''),
    // Default to Gemini 3.8 Flash (Latest paid tier model)
    model: process.env.GEMINI_MODEL || 'gemini-3.8-flash'
  },

  // Channel3 Product Search API
  channel3: {
    apiKey: getSecret('CHANNEL3_API_KEY', ''),
    apiUrl: 'https://api.trychannel3.com/v1/search'
  },

  // Kernel Cloud Browser
  kernel: {
    apiKey: getSecret('KERNEL_API_KEY', '')
  },

  // Commercial purchasing card used by Cartwise for buying from non-PayPal merchants
  agentCard: {
    number: getSecret('AGENT_CARD_NUMBER', '4242424242424242'),
    expiry: getSecret('AGENT_CARD_EXPIRY', '12/28'),
    cvc: getSecret('AGENT_CARD_CVC', '123'),
    name: getSecret('AGENT_CARD_NAME', 'Cartwise Commercial Agent')
  }
};
