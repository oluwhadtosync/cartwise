// Cartwise PayPal Service (Sandbox Orders v2 with Intent AUTHORIZE)
// Implements full authorization hold, capture on merchant checkout success,
// and void on failure / stockout.

import { config } from '../config';

interface AccessTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
}

interface CreateOrderParams {
  orderId: string;
  itemTitle: string;
  itemPrice: number;
  shippingCost: number;
  serviceFee: number;
  currency?: string;
  returnUrl?: string;
  cancelUrl?: string;
}

export class PayPalService {
  private cachedToken: string | null = null;
  private tokenExpiresAt: number = 0;

  /**
   * Check if real PayPal credentials are provided
   */
  isConfigured(): boolean {
    return (
      config.paypal.clientId !== 'sandbox_client_id_placeholder' &&
      config.paypal.clientSecret !== 'sandbox_secret_placeholder' &&
      config.paypal.clientId.length > 5 &&
      config.paypal.clientSecret.length > 5
    );
  }

  /**
   * Obtain OAuth 2.0 Client Credentials Access Token from PayPal Sandbox
   */
  async getAccessToken(): Promise<string> {
    // Return cached token if valid (with 60s buffer)
    if (this.cachedToken && Date.now() < this.tokenExpiresAt - 60000) {
      return this.cachedToken;
    }

    if (!this.isConfigured()) {
      console.warn('[PayPal] Sandbox credentials not configured. Using mock token mode.');
      this.cachedToken = `mock_paypal_token_${Date.now()}`;
      this.tokenExpiresAt = Date.now() + 3600 * 1000;
      return this.cachedToken;
    }

    const auth = Buffer.from(
      `${config.paypal.clientId}:${config.paypal.clientSecret}`
    ).toString('base64');

    const res = await fetch(`${config.paypal.apiUrl}/v1/oauth2/token`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Accept-Language': 'en_US',
        'Authorization': `Basic ${auth}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: 'grant_type=client_credentials'
    });

    if (!res.ok) {
      const errText = await res.text();
      throw new Error(`PayPal OAuth failed (${res.status}): ${errText}`);
    }

    const data = (await res.json()) as AccessTokenResponse;
    this.cachedToken = data.access_token;
    this.tokenExpiresAt = Date.now() + data.expires_in * 1000;
    return this.cachedToken;
  }

  /**
   * Create PayPal Order with intent="AUTHORIZE"
   * The funds are verified and held, not captured yet.
   */
  async createAuthorizeOrder(params: CreateOrderParams): Promise<{
    paypalOrderId: string;
    approveUrl: string;
    status: string;
  }> {
    const currency = params.currency || config.paypal.currency;
    const totalAmount = Number((params.itemPrice + params.shippingCost + params.serviceFee).toFixed(2));

    if (!this.isConfigured()) {
      const mockId = `MOCK-PP-${Math.floor(100000 + Math.random() * 900000)}`;
      const mockReturnUrl = params.returnUrl || `cartwise://paypal/return?orderId=${params.orderId}`;
      return {
        paypalOrderId: mockId,
        approveUrl: `${config.backendUrl}/api/orders/mock-approve?orderId=${params.orderId}&paypalOrderId=${mockId}&returnUrl=${encodeURIComponent(mockReturnUrl)}`,
        status: 'CREATED'
      };
    }

    const token = await this.getAccessToken();

    const returnUrl = params.returnUrl || `cartwise://paypal/return?orderId=${params.orderId}`;
    const cancelUrl = params.cancelUrl || `cartwise://paypal/cancel?orderId=${params.orderId}`;

    const payload = {
      intent: 'AUTHORIZE',
      purchase_units: [
        {
          reference_id: params.orderId,
          description: `Cartwise AI Buy-For-Me: ${params.itemTitle}`,
          custom_id: params.orderId,
          amount: {
            currency_code: currency,
            value: totalAmount.toFixed(2),
            breakdown: {
              item_total: {
                currency_code: currency,
                value: params.itemPrice.toFixed(2)
              },
              shipping: {
                currency_code: currency,
                value: params.shippingCost.toFixed(2)
              },
              handling: {
                currency_code: currency,
                value: params.serviceFee.toFixed(2)
              }
            }
          },
          items: [
            {
              name: params.itemTitle.substring(0, 127),
              quantity: '1',
              unit_amount: {
                currency_code: currency,
                value: params.itemPrice.toFixed(2)
              },
              category: 'PHYSICAL_GOODS'
            }
          ]
        }
      ],
      payment_source: {
        paypal: {
          experience_context: {
            brand_name: 'Cartwise AI',
            landing_page: 'LOGIN',
            shipping_preference: 'NO_SHIPPING',
            user_action: 'CONTINUE',
            return_url: returnUrl,
            cancel_url: cancelUrl
          }
        }
      }
    };

    const res = await fetch(`${config.paypal.apiUrl}/v2/checkout/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        'Prefer': 'return=representation'
      },
      body: JSON.stringify(payload)
    });

    const data = (await res.json()) as any;
    if (!res.ok) {
      throw new Error(`PayPal createOrder failed (${res.status}): ${JSON.stringify(data)}`);
    }

    const approveLink = (data.links || []).find((l: any) => l.rel === 'payer-action' || l.rel === 'approve');
    const approveUrl = approveLink ? approveLink.href : '';

    return {
      paypalOrderId: data.id,
      approveUrl,
      status: data.status
    };
  }

  /**
   * Authorize Order after user approves in PayPal
   * POST /v2/checkout/orders/{id}/authorize
   * Returns authorization ID to be held until checkout completes
   */
  async authorizeOrder(paypalOrderId: string): Promise<{
    authorizationId: string;
    status: string;
    amount: { value: string; currency_code: string };
  }> {
    if (!this.isConfigured() || paypalOrderId.startsWith('MOCK-PP-')) {
      return {
        authorizationId: `MOCK-AUTH-${Date.now()}`,
        status: 'CREATED',
        amount: { value: '42.99', currency_code: 'USD' }
      };
    }

    const token = await this.getAccessToken();

    const res = await fetch(`${config.paypal.apiUrl}/v2/checkout/orders/${paypalOrderId}/authorize`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        'Prefer': 'return=representation'
      },
      body: JSON.stringify({})
    });

    const data = (await res.json()) as any;
    if (!res.ok) {
      throw new Error(`PayPal authorize failed (${res.status}): ${JSON.stringify(data)}`);
    }

    // Extract authorization details from purchase_units[0].payments.authorizations[0]
    const purchaseUnit = (data.purchase_units || [])[0];
    const authorizations = purchaseUnit?.payments?.authorizations || [];
    const auth = authorizations[0];

    if (!auth || !auth.id) {
      throw new Error(`No authorization ID returned by PayPal: ${JSON.stringify(data)}`);
    }

    return {
      authorizationId: auth.id,
      status: auth.status,
      amount: auth.amount
    };
  }

  /**
   * Capture an authorized payment after store confirms order
   * POST /v2/payments/authorizations/{authorization_id}/capture
   */
  async captureAuthorization(
    authorizationId: string,
    amount?: number,
    currency: string = 'USD'
  ): Promise<{
    captureId: string;
    status: string;
    capturedAmount: string;
  }> {
    if (!this.isConfigured() || authorizationId.startsWith('MOCK-AUTH-')) {
      return {
        captureId: `MOCK-CAP-${Date.now()}`,
        status: 'COMPLETED',
        capturedAmount: amount ? amount.toFixed(2) : '42.99'
      };
    }

    const token = await this.getAccessToken();

    const body: Record<string, any> = {
      is_final_capture: true,
      note_to_payer: 'Cartwise purchase fulfilled at merchant store'
    };

    if (amount) {
      body.amount = {
        value: amount.toFixed(2),
        currency_code: currency
      };
    }

    const res = await fetch(
      `${config.paypal.apiUrl}/v2/payments/authorizations/${authorizationId}/capture`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'Prefer': 'return=representation'
        },
        body: JSON.stringify(body)
      }
    );

    const data = (await res.json()) as any;
    if (!res.ok) {
      throw new Error(`PayPal capture failed (${res.status}): ${JSON.stringify(data)}`);
    }

    return {
      captureId: data.id,
      status: data.status,
      capturedAmount: data.amount?.value || ''
    };
  }

  /**
   * Void an authorization if merchant checkout fails or item is out of stock
   * POST /v2/payments/authorizations/{authorization_id}/void
   * The user is never charged.
   */
  async voidAuthorization(authorizationId: string): Promise<{
    status: string;
    voided: boolean;
  }> {
    if (!this.isConfigured() || authorizationId.startsWith('MOCK-AUTH-')) {
      return {
        status: 'VOIDED',
        voided: true
      };
    }

    const token = await this.getAccessToken();

    const res = await fetch(
      `${config.paypal.apiUrl}/v2/payments/authorizations/${authorizationId}/void`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (res.status === 204 || res.status === 200) {
      return {
        status: 'VOIDED',
        voided: true
      };
    }

    const data = await res.json().catch(() => ({}));
    throw new Error(`PayPal void failed (${res.status}): ${JSON.stringify(data)}`);
  }

  /**
   * Retrieve Order details from PayPal
   */
  async getOrder(paypalOrderId: string): Promise<any> {
    if (!this.isConfigured() || paypalOrderId.startsWith('MOCK-PP-')) {
      return {
        id: paypalOrderId,
        status: 'APPROVED',
        intent: 'AUTHORIZE'
      };
    }

    const token = await this.getAccessToken();
    const res = await fetch(`${config.paypal.apiUrl}/v2/checkout/orders/${paypalOrderId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!res.ok) {
      throw new Error(`PayPal getOrder failed (${res.status})`);
    }

    return await res.json();
  }
}

export const paypalService = new PayPalService();
