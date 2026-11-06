// Cartwise Channel3 Product Search Service
// Queries Channel3 API (POST https://api.trychannel3.com/v1/search)
// Includes in-memory aggressive caching to preserve free-tier quota (1,000 searches/mo)
// Merges local Demo Market catalog when DEMO_MODE=true

import { config } from '../config';

interface Channel3SearchResponse {
  products: any[];
  total?: number;
}

interface CacheEntry {
  data: any[];
  timestamp: number;
}

export class Channel3Service {
  // In-memory cache with 1-hour TTL
  private cache: Map<string, CacheEntry> = new Map();
  private readonly CACHE_TTL_MS = 60 * 60 * 1000; // 1 hour

  isConfigured(): boolean {
    return Boolean(config.channel3.apiKey && config.channel3.apiKey.length > 5);
  }

  /**
   * Fetch local Demo Market catalog
   */
  async fetchDemoMarketProducts(): Promise<any[]> {
    try {
      const res = await fetch(`${config.demoStoreUrl}/api/products`, {
        signal: AbortSignal.timeout(5000)
      });
      if (res.ok) {
        const data = (await res.json()) as any;
        return data.products || [];
      }
    } catch (e: any) {
      console.warn(`[Channel3] Could not connect to Demo Market at ${config.demoStoreUrl}: ${e.message}`);
    }
    return [];
  }

  /**
   * Search products via Channel3 API + merge Demo Market catalog
   * Completely immune to VPN/geolocation misrouting
   */
  async search(query: string, maxBudget?: number, country: string = 'NG', queryEn?: string): Promise<any[]> {
    const cleanQuery = query.trim();
    const cleanQueryEn = (queryEn || '').trim();
    const cacheKey = `${cleanQuery.toLowerCase()}_${cleanQueryEn.toLowerCase()}_${maxBudget || 'none'}_${country}`;

    // Check cache
    const cached = this.cache.get(cacheKey);
    if (cached && Date.now() - cached.timestamp < this.CACHE_TTL_MS) {
      console.log(`[Channel3] Returning cached results for "${cleanQuery}"`);
      return cached.data;
    }

    let products: any[] = [];

    // 1. Query Channel3 API if configured (use English query terms for US/global merchants)
    if (this.isConfigured()) {
      try {
        const apiQuery = cleanQueryEn || cleanQuery;
        console.log(`[Channel3] Querying API for: "${apiQuery}" (original: "${cleanQuery}")`);
        const payload: Record<string, any> = {
          query: apiQuery,
          limit: 10
        };
        if (maxBudget) {
          payload.filters = {
            price: { max_price: maxBudget }
          };
        }

        const res = await fetch(config.channel3.apiUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'x-api-key': config.channel3.apiKey,
            'Accept-Language': 'en-US,en;q=0.9'
          },
          body: JSON.stringify(payload),
          signal: AbortSignal.timeout(8000)
        });

        if (res.ok) {
          const json = (await res.json()) as Channel3SearchResponse;
          products = json.products || [];
        } else {
          console.warn(`[Channel3] API returned ${res.status}: ${await res.text()}`);
        }
      } catch (err: any) {
        console.error(`[Channel3] API error: ${err.message}`);
      }
    }

    // 2. If DEMO_MODE=true or no Channel3 results, merge Demo Market products
    if (config.demoMode || products.length === 0) {
      const demoProducts = await this.fetchDemoMarketProducts();
      const queryLower = cleanQuery.toLowerCase();
      const isChineseQuery = /[\u4e00-\u9fa5\u3400-\u4dbf]/.test(cleanQuery);
      const enKeywords = (cleanQueryEn || cleanQuery).toLowerCase().split(/\s+/).filter(w => w.length > 2);

      // Filter and score demo products matching keywords in both English & Chinese
      const matchedDemo = demoProducts.filter(p => {
        const textEn = `${p.title || ''} ${p.description || ''} ${p.category || ''} ${p.brand?.name || p.brand || ''}`.toLowerCase();
        const textZh = `${p.titleZh || ''} ${p.descriptionZh || ''} ${(p.keywordsZh || []).join(' ')}`.toLowerCase();

        let matchesKeyword = false;
        if (isChineseQuery) {
          // Check Chinese keywords & title
          matchesKeyword = textZh.includes(queryLower) ||
            (p.keywordsZh || []).some((kw: string) => queryLower.includes(kw.toLowerCase()) || kw.toLowerCase().includes(queryLower));
          // If not matched directly, check translated English keywords
          if (!matchesKeyword && enKeywords.length > 0) {
            matchesKeyword = enKeywords.some(k => textEn.includes(k));
          }
        } else {
          // Standard English keyword matching
          matchesKeyword = enKeywords.length === 0 || enKeywords.some(k => textEn.includes(k));
        }

        const offerPrice = p.offers?.[0]?.price !== undefined ? p.offers[0].price : p.price;
        const withinBudget = maxBudget ? offerPrice <= maxBudget : true;
        return matchesKeyword && withinBudget;
      });

      // Prepend matching demo market items so they can be bought live by the agent
      products = [...matchedDemo, ...products];
    }

    // Save to cache
    this.cache.set(cacheKey, {
      data: products,
      timestamp: Date.now()
    });

    return products;
  }
}

export const channel3Service = new Channel3Service();
