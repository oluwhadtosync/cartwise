package com.cartwise.app.data

/**
 * Built-in Demo Market catalog for instant, zero-latency extraction
 * when an item is shared from Demo Market web store to the Cartwise APK.
 */
object DemoCatalog {
    val items: Map<String, Deal> = mapOf(
        "prod-earbuds-01" to Deal(
            id = "prod-earbuds-01",
            title = "SoundPulse Air Wireless Noise-Cancelling Earbuds",
            brand = "SoundPulse",
            price = 34.99,
            shippingCost = 5.00,
            totalCost = 39.99,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-earbuds-01",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-earbuds-01.png",
            rating = 4.8,
            reviewsCount = 312,
            inStock = true,
            stockCount = 25,
            reasonWhy = "Extracted from Demo Market share sheet. 100% Landed cost calculated. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Matte Black", sku = "SP-AIR-BLK", inStock = true, stockCount = 25),
                ProductVariety(id = "var-wht", name = "Pearl White", sku = "SP-AIR-WHT", inStock = true, stockCount = 12),
                ProductVariety(id = "var-nvy", name = "Deep Navy", sku = "SP-AIR-NVY", inStock = true, stockCount = 2),
                ProductVariety(id = "var-gld", name = "Rose Gold", sku = "SP-AIR-GLD", inStock = false, stockCount = 0)
            ),
            selectedVarietyName = "Matte Black",
            safety = WebsiteSafety(
                domain = "cartwise-demo-market.onrender.com",
                score = 98,
                level = "SAFE",
                sslVerified = true,
                summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted, sandbox payment isolation, 100% money-back escrow guarantee."
            )
        ),
        "prod-tracker-02" to Deal(
            id = "prod-tracker-02",
            title = "NovaFit Pro Smart Fitness Tracker Band",
            brand = "NovaFit",
            price = 29.99,
            shippingCost = 4.50,
            totalCost = 34.49,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-tracker-02",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-tracker-02.png",
            rating = 4.6,
            reviewsCount = 184,
            inStock = true,
            stockCount = 18,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Midnight Black", sku = "NF-PRO-BLK", inStock = true, stockCount = 18),
                ProductVariety(id = "var-grn", name = "Olive Green", sku = "NF-PRO-GRN", inStock = true, stockCount = 5),
                ProductVariety(id = "var-crl", name = "Sunset Coral", sku = "NF-PRO-CRL", inStock = false, stockCount = 0)
            ),
            selectedVarietyName = "Midnight Black",
            safety = WebsiteSafety(
                domain = "cartwise-demo-market.onrender.com",
                score = 98,
                level = "SAFE",
                sslVerified = true,
                summary = "Official Cartwise Sandbox Store. 256-bit TLS encrypted."
            )
        ),
        "prod-powerbank-03" to Deal(
            id = "prod-powerbank-03",
            title = "HyperCharge 20000mAh Ultra-Fast Power Bank 65W",
            brand = "HyperCharge",
            price = 24.99,
            shippingCost = 5.50,
            totalCost = 30.49,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-powerbank-03",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-powerbank-03.png",
            rating = 4.9,
            reviewsCount = 520,
            inStock = true,
            stockCount = 40,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-gry", name = "Gunmetal Gray", sku = "HC-20K-GRY", inStock = true, stockCount = 40),
                ProductVariety(id = "var-blk", name = "Stealth Black", sku = "HC-20K-BLK", inStock = true, stockCount = 15),
                ProductVariety(id = "var-wht", name = "Titanium White", sku = "HC-20K-WHT", inStock = false, stockCount = 0)
            ),
            selectedVarietyName = "Gunmetal Gray",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-keyboard-04" to Deal(
            id = "prod-keyboard-04",
            title = "SwiftType Compact Mechanical Gaming Keyboard (Red Switch)",
            brand = "SwiftType",
            price = 45.00,
            shippingCost = 6.00,
            totalCost = 51.00,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-keyboard-04",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-keyboard-04.png",
            rating = 4.7,
            reviewsCount = 142,
            inStock = false,
            stockCount = 0,
            reasonWhy = "Extracted from Demo Market share sheet. Currently Out of Stock.",
            varieties = listOf(
                ProductVariety(id = "var-red", name = "Linear Red Switch", sku = "ST-65-RED", inStock = false, stockCount = 0),
                ProductVariety(id = "var-blu", name = "Clicky Blue Switch", sku = "ST-65-BLU", inStock = false, stockCount = 0)
            ),
            selectedVarietyName = "Linear Red Switch",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-mouse-05" to Deal(
            id = "prod-mouse-05",
            title = "Vortex Gaming Mouse RGB 12000 DPI",
            brand = "Vortex",
            price = 19.99,
            shippingCost = 4.00,
            totalCost = 23.99,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-mouse-05",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-mouse-05.png",
            rating = 4.5,
            reviewsCount = 98,
            inStock = true,
            stockCount = 30,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Black", sku = "VX-M1-BLK", inStock = true),
                ProductVariety(id = "var-wht", name = "White", sku = "VX-M1-WHT", inStock = true)
            ),
            selectedVarietyName = "Black",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-lamp-06" to Deal(
            id = "prod-lamp-06",
            title = "AuraGlow Smart LED Desk Lamp with 15W Qi Charger",
            brand = "AuraGlow",
            price = 38.50,
            shippingCost = 6.50,
            totalCost = 45.00,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-lamp-06",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-lamp-06.png",
            rating = 4.8,
            reviewsCount = 156,
            inStock = true,
            stockCount = 15,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-gry", name = "Space Gray Aluminum", sku = "AG-LAMP-GRY", inStock = true)
            ),
            selectedVarietyName = "Space Gray Aluminum",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-speaker-07" to Deal(
            id = "prod-speaker-07",
            title = "EchoBass Rugged Portable Bluetooth Speaker IPX7",
            brand = "EchoBass",
            price = 27.99,
            shippingCost = 5.00,
            totalCost = 32.99,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-speaker-07",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-speaker-07.png",
            rating = 4.7,
            reviewsCount = 230,
            inStock = true,
            stockCount = 22,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Storm Black", sku = "EB-MINI-BLK", inStock = true),
                ProductVariety(id = "var-tea", name = "Ocean Teal", sku = "EB-MINI-TEA", inStock = true)
            ),
            selectedVarietyName = "Storm Black",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-webcam-08" to Deal(
            id = "prod-webcam-08",
            title = "ClearView Pro 1080p 60FPS Webcam with Privacy Shutter",
            brand = "ClearView",
            price = 32.00,
            shippingCost = 4.50,
            totalCost = 36.50,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-webcam-08",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-webcam-08.png",
            rating = 4.6,
            reviewsCount = 110,
            inStock = true,
            stockCount = 14,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Black", sku = "CV-1080-BLK", inStock = true)
            ),
            selectedVarietyName = "Black",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-hub-09" to Deal(
            id = "prod-hub-09",
            title = "UltraSlim 7-in-1 USB-C Hub with 4K HDMI & 100W PD",
            brand = "NovaTech",
            price = 22.50,
            shippingCost = 4.00,
            totalCost = 26.50,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-hub-09",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-hub-09.png",
            rating = 4.8,
            reviewsCount = 340,
            inStock = true,
            stockCount = 35,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-slv", name = "Silver", sku = "NT-HUB7-SLV", inStock = true),
                ProductVariety(id = "var-gry", name = "Space Gray", sku = "NT-HUB7-GRY", inStock = true)
            ),
            selectedVarietyName = "Silver",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-fan-10" to Deal(
            id = "prod-fan-10",
            title = "ArcticBreeze Whisper-Quiet USB Desk Mini Fan",
            brand = "ArcticBreeze",
            price = 16.99,
            shippingCost = 4.00,
            totalCost = 20.99,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-fan-10",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-fan-10.png",
            rating = 4.5,
            reviewsCount = 88,
            inStock = true,
            stockCount = 19,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-wht", name = "Matte White", sku = "AB-FAN-WHT", inStock = true),
                ProductVariety(id = "var-sge", name = "Sage Green", sku = "AB-FAN-SGE", inStock = true)
            ),
            selectedVarietyName = "Matte White",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-headphone-11" to Deal(
            id = "prod-headphone-11",
            title = "ZenSound Over-Ear Active Noise Cancelling Headphones",
            brand = "ZenSound",
            price = 58.00,
            shippingCost = 6.00,
            totalCost = 64.00,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-headphone-11",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-headphone-11.png",
            rating = 4.9,
            reviewsCount = 410,
            inStock = true,
            stockCount = 12,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Obsidian Black", sku = "ZS-ANC-BLK", inStock = true),
                ProductVariety(id = "var-slv", name = "Champagne Silver", sku = "ZS-ANC-SLV", inStock = true)
            ),
            selectedVarietyName = "Obsidian Black",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        ),
        "prod-backpack-12" to Deal(
            id = "prod-backpack-12",
            title = "TitanShield Waterproof Anti-Theft Laptop Backpack 15.6\"",
            brand = "TitanShield",
            price = 39.99,
            shippingCost = 6.50,
            totalCost = 46.49,
            currency = "USD",
            storeName = "Demo Market",
            storeUrl = "https://cartwise-demo-market.onrender.com/product/prod-backpack-12",
            imageUrl = "https://cartwise-demo-market.onrender.com/public/images/products/prod-backpack-12.png",
            rating = 4.8,
            reviewsCount = 275,
            inStock = true,
            stockCount = 20,
            reasonWhy = "Extracted from Demo Market share sheet. Ready for PayPal Escrow authorization.",
            varieties = listOf(
                ProductVariety(id = "var-blk", name = "Charcoal Black", sku = "TS-BP-BLK", inStock = true),
                ProductVariety(id = "var-gry", name = "Heather Gray", sku = "TS-BP-GRY", inStock = true)
            ),
            selectedVarietyName = "Charcoal Black",
            safety = WebsiteSafety(domain = "cartwise-demo-market.onrender.com", score = 98, level = "SAFE")
        )
    )
}
