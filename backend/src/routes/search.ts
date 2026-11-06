// Cartwise Search & Deal Ranking Route
// Combines Gemini NLP query parsing, Channel3 product search, and landed cost deal ranking
// Fully isolated from VPN / IP geolocation misrouting

import { Router, Request, Response } from 'express';
import { geminiService } from '../services/gemini';
import { channel3Service } from '../services/channel3';

export const searchRouter = Router();

searchRouter.post('/', async (req: Request, res: Response) => {
  try {
    const { query, country = 'NG', maxBudget, language: reqLang, model: reqModel } = req.body;

    if (!query || typeof query !== 'string') {
      return res.status(400).json({ error: 'Search query string is required' });
    }

    console.log(`[Search] New shopper request: "${query}" (Target Country: ${country}, Model: ${reqModel || 'gemini-3.8-flash'})`);

    // 1. Natural Language Query Extraction via Gemini (strictly preserving query language)
    const parsed = await geminiService.extractQuery(query, country, reqLang, reqModel);
    const effectiveBudget = maxBudget !== undefined ? Number(maxBudget) : parsed.maxBudget;

    // 2. Query Channel3 & Demo Market using English translated terms for global stores, and original for localized catalog
    const rawProducts = await channel3Service.search(
      parsed.item,
      effectiveBudget,
      parsed.targetCountry,
      parsed.itemEn
    );

    // 3. Rank Deals & Landed Cost Calculation via Gemini (with language enforcement)
    const topDeals = await geminiService.rankDeals(
      query,
      rawProducts,
      parsed.targetCountry,
      parsed.language,
      reqModel
    );

    res.json({
      query,
      language: parsed.language,
      modelUsed: reqModel || 'gemini-3.8-flash',
      parsed,
      deals: topDeals,
      totalFound: rawProducts.length
    });
  } catch (error: any) {
    console.error('[Search] Error in /api/search:', error);
    res.status(500).json({ error: error.message || 'Product search failed' });
  }
});
