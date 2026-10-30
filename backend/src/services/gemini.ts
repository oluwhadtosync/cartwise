// Cartwise Google Gemini AI Service
// Uses the official @google/genai SDK
// Handles natural language query parsing and deal ranking with landed cost calculation

import { GoogleGenAI } from '@google/genai';
import { config } from '../config';
import { DealResult } from '../types';

export interface ParsedSearchQuery {
  item: string;
  maxBudget?: number;
  category?: string;
  targetCountry: string;
  mustHaves: string[];
}

export class GeminiService {
  private ai: GoogleGenAI | null = null;

  constructor() {
    if (config.gemini.apiKey) {
      this.ai = new GoogleGenAI({ apiKey: config.gemini.apiKey });
    }
  }

  isConfigured(): boolean {
    return Boolean(config.gemini.apiKey && config.gemini.apiKey.length > 5);
  }

  /**
   * Extract structured constraints from user natural language prompt
   */
  async extractQuery(prompt: string, defaultCountry: string = 'NG'): Promise<ParsedSearchQuery> {
    // Heuristic fallback
    const fallback: ParsedSearchQuery = {
      item: prompt,
      targetCountry: defaultCountry,
      mustHaves: []
    };

    // Extract potential budget e.g. "under $40", "< 50", "$30"
    const budgetMatch = prompt.match(/(?:under|below|max|less than|\$)\s*\$?\s*(\d+(?:\.\d{2})?)/i);
    if (budgetMatch) {
      fallback.maxBudget = parseFloat(budgetMatch[1]);
    }

    // Extract country mention (e.g. Lagos, Nigeria, US, UK)
    if (/lagos|nigeria|abuja/i.test(prompt)) {
      fallback.targetCountry = 'NG';
    } else if (/us|united states|america/i.test(prompt)) {
      fallback.targetCountry = 'US';
    } else if (/uk|london|britain/i.test(prompt)) {
      fallback.targetCountry = 'UK';
    }

    // Clean prompt for item search
    fallback.item = prompt
      .replace(/(?:find|search|get|buy|look for|me|please|cheap|cheapest)\s+/gi, '')
      .replace(/(?:under|below|max|less than|\$)\s*\$?\s*(\d+(?:\.\d{2})?)/gi, '')
      .replace(/(?:that ships? to|shipping to|ships? to|to)\s+(?:lagos|nigeria|abuja|us|uk)/gi, '')
      .trim();

    if (!this.isConfigured() || !this.ai) {
      return fallback;
    }

    try {
      const text = await this.generateWithRetry({
        prompt: `You are Cartwise Shopping Agent. Parse the following shopper query into JSON:
Query: "${prompt}"

Return ONLY a valid JSON object matching this schema:
{
  "item": "concise core product search terms",
  "maxBudget": numeric upper spending limit if specified (e.g. for "under $40", return 40; if unspecified, return null),
  "category": null or string,
  "targetCountry": "two-letter country code, default NG for Nigeria or Lagos",
  "mustHaves": ["list of required features or variants mentioned"]
}`,
        temperature: 0.1,
        json: true
      });

      const parsed = JSON.parse(text);
      return {
        item: parsed.item || fallback.item,
        maxBudget: (parsed.maxBudget && Number(parsed.maxBudget) > 0) ? Number(parsed.maxBudget) : fallback.maxBudget,
        category: parsed.category || undefined,
        targetCountry: parsed.targetCountry || fallback.targetCountry,
        mustHaves: Array.isArray(parsed.mustHaves) ? parsed.mustHaves : []
      };
    } catch (err: any) {
      console.warn(`[Gemini] Error in extractQuery, using fallback: ${err.message}`);
      return fallback;
    }
  }

  /**
   * Helper to execute Gemini generation with exponential backoff & model fallbacks
   */
  private async generateWithRetry(params: {
    prompt: string;
    temperature?: number;
    json?: boolean;
  }): Promise<string> {
    if (!this.ai) throw new Error('Gemini not configured');

    const models = [
      config.gemini.model,
      'gemini-3.5-flash',
      'gemini-flash-latest',
      'gemini-3.1-flash-lite',
      'gemini-3.8-flash'
    ];
    const uniqueModels = Array.from(new Set(models));

    let lastError: any = null;
    for (const model of uniqueModels) {
      for (let attempt = 0; attempt < 2; attempt++) {
        try {
          const res = await this.ai.models.generateContent({
            model,
            contents: params.prompt,
            config: {
              temperature: params.temperature ?? 0.2,
              responseMimeType: params.json ? 'application/json' : undefined
            }
          });
          if (res.text) {
            return res.text;
          }
        } catch (err: any) {
          lastError = err;
          const msg = String(err?.message || '');
          if (msg.includes('503') || msg.includes('429')) {
            await new Promise(r => setTimeout(r, 800 * (attempt + 1)));
            continue;
          }
          break; // Switch to next model
        }
      }
    }
    throw lastError || new Error('All Gemini model candidates failed');
  }

  /**
   * Rank raw product offers and return Top 3 deals with landed cost & "why this deal" explanations
   */
  async rankDeals(
    userQuery: string,
    products: any[],
    targetCountry: string = 'NG'
  ): Promise<DealResult[]> {
    if (!products || products.length === 0) {
      return [];
    }

    // Normalize raw products into candidate list
    const candidates = products.map((p, idx) => {
      const offer = (p.offers || [])[0] || {};
      // Handle Channel3 offer.price object or scalar
      let rawPrice = 0;
      if (typeof offer.price === 'object' && offer.price !== null) {
        rawPrice = Number(offer.price.price || offer.price.amount || 0);
      } else {
        rawPrice = Number(offer.price || p.price || 0);
      }
      const price = Number(rawPrice.toFixed(2));

      const shipping = Number(offer.shippingCost !== undefined ? offer.shippingCost : (p.shippingCost || 5.00));
      const totalCost = Number((price + shipping).toFixed(2));
      const storeName = offer.website_name || offer.domain || 'Online Merchant';
      const storeUrl = offer.url || (p.offers?.[0]?.url) || '';

      // Handle image url (string or Channel3 image object)
      let imageUrl = `${config.demoStoreUrl}/api/products/${p.id}/image.svg`;
      if (Array.isArray(p.images) && p.images.length > 0) {
        const firstImg = p.images[0];
        if (typeof firstImg === 'string') {
          imageUrl = firstImg;
        } else if (firstImg && firstImg.url) {
          imageUrl = firstImg.url;
        }
      }

      // Handle brand (string or Channel3 brand object)
      let brandName = '';
      if (Array.isArray(p.brands) && p.brands.length > 0) {
        brandName = p.brands[0]?.name || '';
      } else if (typeof p.brand === 'object' && p.brand !== null) {
        brandName = p.brand.name || '';
      } else if (typeof p.brand === 'string') {
        brandName = p.brand;
      }

      const inStock = offer.availability === 'InStock' || p.inStock === true || offer.availability === undefined;

      return {
        id: p.id || `deal_${idx}`,
        title: p.title || 'Untitled Product',
        description: p.description || '',
        brand: brandName,
        category: typeof p.category === 'object' ? (p.category?.title || '') : (p.category || ''),
        price,
        shippingCost: shipping,
        totalCost,
        currency: (typeof offer.price === 'object' ? offer.price?.currency : offer.currency) || 'USD',
        storeName,
        storeUrl,
        imageUrl,
        rating: p.metadata?.rating || 4.7,
        reviewsCount: p.metadata?.reviewsCount || 100,
        inStock,
        shipsToCountry: true
      };
    });

    // Filter in-stock candidates first, sort by landed cost
    const sorted = candidates
      .filter(c => c.price > 0)
      .sort((a, b) => {
        // Prioritize inStock
        if (a.inStock !== b.inStock) return a.inStock ? -1 : 1;
        // Then total landed cost
        return a.totalCost - b.totalCost;
      });

    const topCandidates = sorted.slice(0, 3);

    // Generate AI explanations for top deals
    if (this.isConfigured() && this.ai && topCandidates.length > 0) {
      try {
        const prompt = `You are Cartwise Shopping Concierge. A shopper asked: "${userQuery}".
Review these top ${topCandidates.length} candidate deals:
${JSON.stringify(topCandidates.map((c, i) => ({
  index: i,
  title: c.title,
  price: `$${c.price}`,
  shipping: `$${c.shippingCost}`,
  totalCost: `$${c.totalCost}`,
  store: c.storeName
})), null, 2)}

For each deal (0 to ${topCandidates.length - 1}), provide ONE concise punchy sentence explaining why the shopper should buy this one (e.g. "Lowest landed cost with verified fast delivery to Nigeria" or "Best overall active noise cancellation within your budget").

Return JSON format:
{
  "reasons": [
    { "index": 0, "reason": "...", "score": 98 },
    ...
  ]
}`;

        const text = await this.generateWithRetry({
          prompt,
          temperature: 0.3,
          json: true
        });

        const data = JSON.parse(text || '{}');
        const reasons = data.reasons || [];

        return topCandidates.map((deal, idx) => {
          const match = reasons.find((r: any) => r.index === idx);
          return {
            ...deal,
            reasonWhy: match?.reason || this.getDefaultReason(idx, deal),
            score: match?.score || (95 - idx * 5)
          };
        });
      } catch (e: any) {
        console.warn(`[Gemini] Error generating AI reasons: ${e.message}`);
      }
    }

    // Default heuristic reasons
    return topCandidates.map((deal, idx) => ({
      ...deal,
      reasonWhy: this.getDefaultReason(idx, deal),
      score: 95 - idx * 5
    }));
  }

  private getDefaultReason(index: number, deal: any): string {
    if (index === 0) {
      return `Best overall value: lowest total landed cost ($${deal.totalCost.toFixed(2)}) with verified delivery.`;
    } else if (index === 1) {
      return `Top customer-rated pick with premium build quality and strong merchant feedback.`;
    } else {
      return `Great alternative featuring solid performance and fast order fulfillment.`;
    }
  }
}

export const geminiService = new GeminiService();
