// Demo Market Products Catalog
// 12 products with rich metadata, realistic pricing, stock counts, and SVG graphics

const products = [
  {
    id: "prod-earbuds-01",
    title: "SoundPulse Air Wireless Noise-Cancelling Earbuds",
    brand: "SoundPulse",
    price: 34.99,
    currency: "USD",
    category: "audio",
    rating: 4.8,
    reviewsCount: 312,
    inStock: true,
    stockCount: 25,
    shippingCost: 5.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH", "KE"],
    description: "Premium wireless earbuds with hybrid active noise cancellation, 32-hour battery life, IPX5 water resistance, and crystal-clear microphone for calls.",
    features: [
      "Hybrid Active Noise Cancellation (up to 35dB)",
      "32 Hours Total Playback with USB-C Charging Case",
      "Bluetooth 5.3 Low Latency Mode",
      "Dual Beamforming Mics for Clear Calls",
      "Touch Controls & Voice Assistant Support"
    ],
    variants: [
      { name: "Matte Black", sku: "SP-AIR-BLK" },
      { name: "Pearl White", sku: "SP-AIR-WHT" },
      { name: "Deep Navy", sku: "SP-AIR-NVY" }
    ],
    iconType: "earbuds"
  },
  {
    id: "prod-tracker-02",
    title: "NovaFit Pro Smart Fitness Tracker Band",
    brand: "NovaFit",
    price: 29.99,
    currency: "USD",
    category: "wearables",
    rating: 4.6,
    reviewsCount: 184,
    inStock: true,
    stockCount: 18,
    shippingCost: 4.50,
    shipsTo: ["NG", "US", "UK", "CA", "GH"],
    description: "Sleek AMOLED fitness band with continuous heart rate monitoring, SpO2 blood oxygen, sleep quality analysis, and 14-day battery life.",
    features: [
      "1.47-inch Vivid AMOLED Curved Display",
      "24/7 Heart Rate, SpO2 & Stress Tracking",
      "30+ Sport Activity Modes",
      "5ATM Water Resistant (Swim-proof)",
      "14-Day Ultra-Long Battery Life"
    ],
    variants: [
      { name: "Midnight Black", sku: "NF-PRO-BLK" },
      { name: "Olive Green", sku: "NF-PRO-GRN" }
    ],
    iconType: "watch"
  },
  {
    id: "prod-powerbank-03",
    title: "HyperCharge 20000mAh Ultra-Fast Power Bank 65W",
    brand: "HyperCharge",
    price: 24.99,
    currency: "USD",
    category: "accessories",
    rating: 4.9,
    reviewsCount: 520,
    inStock: true,
    stockCount: 40,
    shippingCost: 5.50,
    shipsTo: ["NG", "US", "UK", "CA", "GH", "KE"],
    description: "High-capacity 20000mAh battery pack with 65W Power Delivery fast charging. Powers laptops, tablets, and smartphones on the go.",
    features: [
      "65W USB-C PD 3.0 Fast Output",
      "Charges iPhone 15 to 60% in 30 minutes",
      "3 Ports: 2x USB-C + 1x USB-A",
      "Digital LED Percentage Battery Indicator",
      "Airline Approved 74Wh Capacity"
    ],
    variants: [
      { name: "Gunmetal Gray", sku: "HC-20K-GRY" },
      { name: "Stealth Black", sku: "HC-20K-BLK" }
    ],
    iconType: "powerbank"
  },
  {
    id: "prod-keyboard-04",
    title: "SwiftType Compact Mechanical Gaming Keyboard (Red Switch)",
    brand: "SwiftType",
    price: 45.00,
    currency: "USD",
    category: "electronics",
    rating: 4.7,
    reviewsCount: 142,
    inStock: false, // Intentionally out of stock to demonstrate failure/guardrail path!
    stockCount: 0,
    shippingCost: 6.00,
    shipsTo: ["NG", "US", "UK", "CA"],
    description: "65% hot-swappable mechanical keyboard with pre-lubed linear red switches, per-key RGB backlighting, and sound-dampening foam.",
    features: [
      "Smooth Linear Red Mechanical Switches",
      "Hot-Swappable 3-Pin / 5-Pin PCB",
      "Customizable RGB Lighting with 18 Presets",
      "Detachable Braided Type-C Cable",
      "Double-shot PBT Shine-through Keycaps"
    ],
    variants: [
      { name: "White/Grey", sku: "ST-65-WHT" },
      { name: "Cyberpunk Edition", sku: "ST-65-CYB" }
    ],
    iconType: "keyboard"
  },
  {
    id: "prod-mouse-05",
    title: "Vortex Gaming Mouse RGB 12000 DPI",
    brand: "Vortex",
    price: 19.99,
    currency: "USD",
    category: "gaming",
    rating: 4.5,
    reviewsCount: 98,
    inStock: true,
    stockCount: 30,
    shippingCost: 4.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH"],
    description: "Ergonomic lightweight gaming mouse with 12000 DPI optical sensor, honeycomb shell design, and ultra-flexible paracord cable.",
    features: [
      "High-precision 12000 DPI Optical Sensor",
      "Ultralight 68g Honeycomb Ergonomic Shell",
      "Omron Switches Rated for 20 Million Clicks",
      "6 Programmable Buttons with Onboard Memory",
      "Dynamic 16.8M Color RGB Chroma Lighting"
    ],
    variants: [
      { name: "Black", sku: "VX-M1-BLK" },
      { name: "White", sku: "VX-M1-WHT" }
    ],
    iconType: "mouse"
  },
  {
    id: "prod-lamp-06",
    title: "AuraGlow Smart LED Desk Lamp with 15W Qi Charger",
    brand: "AuraGlow",
    price: 38.50,
    currency: "USD",
    category: "home-office",
    rating: 4.8,
    reviewsCount: 156,
    inStock: true,
    stockCount: 15,
    shippingCost: 6.50,
    shipsTo: ["NG", "US", "UK", "CA"],
    description: "Modern architectural desk lamp featuring flicker-free eye care lighting, 5 color temperature modes, and integrated 15W Qi wireless fast charger in base.",
    features: [
      "Integrated 15W Qi Fast Wireless Charging Base",
      "5 Color Temperatures & Stepless Dimming",
      "No Blue-Light Hazard / Flicker-Free Certified",
      "Flexible Multi-Angle Foldable Aluminum Arm",
      "45-Minute Auto-Off Sleep Timer"
    ],
    variants: [
      { name: "Space Gray Aluminum", sku: "AG-LAMP-GRY" }
    ],
    iconType: "lamp"
  },
  {
    id: "prod-speaker-07",
    title: "EchoBass Rugged Portable Bluetooth Speaker IPX7",
    brand: "EchoBass",
    price: 27.99,
    currency: "USD",
    category: "audio",
    rating: 4.7,
    reviewsCount: 230,
    inStock: true,
    stockCount: 22,
    shippingCost: 5.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH", "KE"],
    description: "Punchy 360-degree sound with deep passive bass radiators. 100% waterproof IPX7 construction floats in water, ideal for travel and outdoor sessions.",
    features: [
      "16W Crisp 360-degree Stereo Sound",
      "IPX7 Fully Waterproof & Floating Design",
      "Up to 16 Hours Playback at 60% Volume",
      "TWS Pairing: Connect 2 Speakers for True Stereo",
      "Built-in Carabiner & Shockproof Silicone Bumper"
    ],
    variants: [
      { name: "Storm Black", sku: "EB-MINI-BLK" },
      { name: "Ocean Teal", sku: "EB-MINI-TEA" }
    ],
    iconType: "speaker"
  },
  {
    id: "prod-webcam-08",
    title: "ClearView Pro 1080p 60FPS Webcam with Privacy Shutter",
    brand: "ClearView",
    price: 32.00,
    currency: "USD",
    category: "electronics",
    rating: 4.6,
    reviewsCount: 110,
    inStock: true,
    stockCount: 14,
    shippingCost: 4.50,
    shipsTo: ["NG", "US", "UK", "CA"],
    description: "Full HD 1080p 60fps streaming webcam with auto low-light correction, stereo noise-reducing microphones, and physical privacy slide cover.",
    features: [
      "Smooth 1080p 60fps Video with Autofocus",
      "Physical Built-in Privacy Slider",
      "Dual Stereo Noise-Cancelling Microphones",
      "Plug & Play USB 2.0 / USB-C (Adapter included)",
      "Tripod-ready Universal Monitor Clamp"
    ],
    variants: [
      { name: "Black", sku: "CV-1080-BLK" }
    ],
    iconType: "webcam"
  },
  {
    id: "prod-hub-09",
    title: "UltraSlim 7-in-1 USB-C Hub with 4K HDMI & 100W PD",
    brand: "NovaTech",
    price: 22.50,
    currency: "USD",
    category: "accessories",
    rating: 4.8,
    reviewsCount: 340,
    inStock: true,
    stockCount: 35,
    shippingCost: 4.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH", "KE"],
    description: "Expand single USB-C port to 4K HDMI @ 60Hz, 100W Power Delivery pass-through, 3x USB 3.0 ports (5Gbps), and SD/TF card readers.",
    features: [
      "4K @ 60Hz HDMI Video Output",
      "100W USB-C Power Delivery Fast Pass-Through",
      "3x USB 3.0 Ports with 5Gbps Data Transfer",
      "SD & MicroSD Card Slots Dual-Read",
      "Heat-dissipating Precision Aluminum Casing"
    ],
    variants: [
      { name: "Silver", sku: "NT-HUB7-SLV" },
      { name: "Space Gray", sku: "NT-HUB7-GRY" }
    ],
    iconType: "hub"
  },
  {
    id: "prod-fan-10",
    title: "ArcticBreeze Whisper-Quiet USB Desk Mini Fan",
    brand: "ArcticBreeze",
    price: 16.99,
    currency: "USD",
    category: "home-office",
    rating: 4.5,
    reviewsCount: 88,
    inStock: true,
    stockCount: 19,
    shippingCost: 4.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH"],
    description: "Ultra-quiet brushless motor mini desk fan with 3 speed levels, 360-degree tilt oscillation, and rechargeable 4000mAh battery.",
    features: [
      "Whisper-Quiet Brushless Motor (<25dB)",
      "Rechargeable 4000mAh Battery (8-12 Hours Run)",
      "3 Speed Settings: Gentle, Natural, Turbo",
      "360-degree Manual Tilt Adjustment",
      "Removable Front Grill for Easy Cleaning"
    ],
    variants: [
      { name: "Matte White", sku: "AB-FAN-WHT" },
      { name: "Sage Green", sku: "AB-FAN-SGE" }
    ],
    iconType: "fan"
  },
  {
    id: "prod-headphone-11",
    title: "ZenSound Over-Ear Active Noise Cancelling Headphones",
    brand: "ZenSound",
    price: 58.00,
    currency: "USD",
    category: "audio",
    rating: 4.9,
    reviewsCount: 410,
    inStock: true,
    stockCount: 12,
    shippingCost: 6.00,
    shipsTo: ["NG", "US", "UK", "CA", "GH"],
    description: "Studio-grade wireless over-ear headphones with 40mm titanium drivers, advanced hybrid ANC, plush memory foam earcups, and 50-hour battery life.",
    features: [
      "Advanced Hybrid ANC (reduces 95% ambient noise)",
      "Hi-Res Audio Certified with LDAC Support",
      "50-Hour Playback with Fast Charge (5 min = 4 hrs)",
      "Ultra-Soft Protein Leather Memory Foam Cushions",
      "Multi-point Connection (Connects to 2 devices)"
    ],
    variants: [
      { name: "Obsidian Black", sku: "ZS-ANC-BLK" },
      { name: "Champagne Silver", sku: "ZS-ANC-SLV" }
    ],
    iconType: "headphones"
  },
  {
    id: "prod-backpack-12",
    title: "TitanShield Waterproof Anti-Theft Laptop Backpack 15.6\"",
    brand: "TitanShield",
    price: 39.99,
    currency: "USD",
    category: "bags",
    rating: 4.8,
    reviewsCount: 275,
    inStock: true,
    stockCount: 20,
    shippingCost: 6.50,
    shipsTo: ["NG", "US", "UK", "CA", "GH", "KE"],
    description: "Weather-resistant Oxford fabric backpack with padded compartment for up to 16-inch laptops, hidden anti-theft back pocket, and integrated USB charging port.",
    features: [
      "Waterproof & Scratch-Resistant Oxford Fabric",
      "Shockproof Padded Pocket Fits Up to 16\" Laptops",
      "Hidden Anti-Theft Back Zipper Pocket",
      "External USB Charging Port with Internal Cable",
      "Luggage Strap for Attaching to Rolling Suitcases"
    ],
    variants: [
      { name: "Charcoal Black", sku: "TS-BP-BLK" },
      { name: "Heather Gray", sku: "TS-BP-GRY" }
    ],
    iconType: "backpack"
  }
];

module.exports = products;
