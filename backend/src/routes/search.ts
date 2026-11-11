// Cartwise Search & Deal Ranking Route
// Combines Gemini NLP query parsing, Channel3 product search, and landed cost deal ranking
// Fully isolated from VPN / IP geolocation misrouting

import { Router, Request, Response } from 'express';
import { geminiService } from '../services/gemini';
import { channel3Service } from '../services/channel3';

export const searchRouter = Router();

function resolveCountryCode(raw?: string): string {
  if (!raw) return 'NG';
  const val = raw.trim().toUpperCase();
  if (val.includes('NIGERIA') || val === 'NG' || val.includes('LAGOS') || val.includes('ABUJA')) return 'NG';
  if (val.includes('UNITED STATES') || val === 'US' || val === 'USA') return 'US';
  if (val.includes('UNITED KINGDOM') || val === 'UK' || val === 'GB' || val.includes('LONDON')) return 'GB';
  if (val.includes('CANADA') || val === 'CA') return 'CA';
  if (val.includes('GHANA') || val === 'GH') return 'GH';
  if (val.includes('GERMANY') || val === 'DE') return 'DE';
  if (val.includes('KENYA') || val === 'KE') return 'KE';
  if (val.includes('SOUTH AFRICA') || val === 'ZA') return 'ZA';
  if (val.includes('CHINA') || val === 'CN') return 'CN';
  if (val.length === 2) return val;
  return 'NG';
}

searchRouter.post('/', async (req: Request, res: Response) => {
  try {
    const {
      query,
      country,
      destinationCountry,
      city,
      shippingAddress,
      maxBudget,
      language: reqLang,
      model: reqModel
    } = req.body;

    if (!query || typeof query !== 'string') {
      return res.status(400).json({ error: 'Search query string is required' });
    }

    const targetCountryCode = resolveCountryCode(country || destinationCountry || shippingAddress?.country || 'NG');
    const targetCity = (city || shippingAddress?.city || (targetCountryCode === 'NG' ? 'Lagos' : '')).trim();

    console.log(`[Search] New shopper request: "${query}" (Target Country: ${targetCountryCode}, City: ${targetCity}, Model: ${reqModel || 'gemini-3.8-flash'})`);

    // 1. Natural Language Query Extraction via Gemini (strictly preserving query language)
    const parsed = await geminiService.extractQuery(query, targetCountryCode, reqLang, reqModel);
    const effectiveBudget = maxBudget !== undefined ? Number(maxBudget) : parsed.maxBudget;

    // 2. Query Channel3 & Demo Market using English translated terms for global stores, and original for localized catalog
    const rawProducts = await channel3Service.search(
      parsed.item,
      effectiveBudget,
      parsed.targetCountry || targetCountryCode,
      parsed.itemEn
    );

    // 3. Rank Deals & Landed Cost Calculation via Gemini (with language enforcement)
    const topDeals = await geminiService.rankDeals(
      query,
      rawProducts,
      parsed.targetCountry || targetCountryCode,
      parsed.language,
      reqModel
    );

    res.json({
      query,
      language: parsed.language,
      modelUsed: reqModel || 'gemini-3.8-flash',
      targetCountry: targetCountryCode,
      targetCity,
      parsed,
      deals: topDeals,
      totalFound: rawProducts.length
    });
  } catch (error: any) {
    console.error('[Search] Error in /api/search:', error);
    res.status(500).json({ error: error.message || 'Product search failed' });
  }
});
