// Cartwise Product Extraction Route
// Powers instant direct link extraction using Kernel Cloud Browser + Gemini AI

import { Router, Request, Response } from 'express';
import { browserExtractor } from '../services/browserExtractor';

export const extractRouter = Router();

extractRouter.post('/', async (req: Request, res: Response) => {
  try {
    const { url, destinationCountry, destinationCity, shippingAddress } = req.body;

    if (!url || typeof url !== 'string' || url.trim().length === 0) {
      return res.status(400).json({ error: 'Valid URL is required' });
    }

    const country = destinationCountry || shippingAddress?.country || 'NG';
    const city = destinationCity || shippingAddress?.city || 'Lagos';

    console.log(`[API /extract] Received URL extraction request: ${url} for destination: ${city}, ${country}`);
    const result = await browserExtractor.extractProduct(url.trim(), {
      destinationCountry: country,
      destinationCity: city,
      shippingAddress
    });

    return res.json({
      success: true,
      deal: result.deal,
      rawTitle: result.rawTitle,
      originalPriceText: result.originalPriceText,
      extractedVia: result.extractedVia
    });
  } catch (error: any) {
    console.error('[API /extract] Error extracting product from URL:', error);
    return res.status(500).json({
      success: false,
      error: error.message || 'Failed to extract product from link'
    });
  }
});
