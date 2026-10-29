// Cartwise Search & Deal Ranking Route
// Combines Gemini NLP query parsing, Channel3 product search, and landed cost deal ranking

import { Router, Request, Response } from 'express';
import { geminiService } from '../services/gemini';
import { channel3Service } from '../services/channel3';

export const searchRouter = Router();

searchRouter.post('/', async (req: Request, res: Response) => {
  try {
    const { query, country = 'NG', maxBudget } = req.body;

    if (!query || typeof query !== 'string') {
      return res.status(400).json({ error: 'Search query string is required' });
    }

    console.log(`[Search] New shopper request: "${query}" (Target Country: ${country})`);

    // 1. Natural Language Query Extraction via Gemini
    const parsed = await geminiService.extractQuery(query, country);
    const effectiveBudget = maxBudget !== undefined ? Number(maxBudget) : parsed.maxBudget;

    // 2. Query Channel3 (and Demo Market if in demo mode)
    const rawProducts = await channel3Service.search(
      parsed.item,
      effectiveBudget,
      parsed.targetCountry
    );

    // 3. Rank Deals & Landed Cost Calculation via Gemini
    const topDeals = await geminiService.rankDeals(
      query,
      rawProducts,
      parsed.targetCountry
    );

    res.json({
      query,
      parsed,
      deals: topDeals,
      totalFound: rawProducts.length
    });
  } catch (error: any) {
    console.error('[Search] Error in /api/search:', error);
    res.status(500).json({ error: error.message || 'Product search failed' });
  }
});
