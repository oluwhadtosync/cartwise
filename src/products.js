// Demo Market Products Catalog
// 12 products with rich metadata, realistic pricing, stock counts, SVG graphics, and bilingual (EN/ZH) search keywords
// Completely immune to VPN/geolocation misrouting

const products = [
  {
    id: "prod-earbuds-01",
    title: "SoundPulse Air Wireless Noise-Cancelling Earbuds",
    titleZh: "SoundPulse Air 无线降噪耳机",
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
    descriptionZh: "高品质无线降噪耳塞，支持混合主动降噪，32小时超长续航，IPX5级防水与清晰通话降噪。",
    keywordsZh: ["耳机", "降噪耳机", "无线耳机", "蓝牙耳机", "主动降噪", "降噪", "耳塞", "入耳式", "soundpulse", "earbuds"],
    features: [
      "Hybrid Active Noise Cancellation (up to 35dB)",
      "32 Hours Total Playback with USB-C Charging Case",
      "Bluetooth 5.3 Low Latency Mode",
      "Dual Beamforming Mics for Clear Calls",
      "Touch Controls & Voice Assistant Support"
    ],
    variants: [
      { name: "Matte Black", sku: "SP-AIR-BLK", stockCount: 25, inStock: true },
      { name: "Pearl White", sku: "SP-AIR-WHT", stockCount: 12, inStock: true },
      { name: "Deep Navy", sku: "SP-AIR-NVY", stockCount: 2, inStock: true },
      { name: "Rose Gold", sku: "SP-AIR-GLD", stockCount: 0, inStock: false }
    ],
    iconType: "earbuds"
  },
  {
    id: "prod-tracker-02",
    title: "NovaFit Pro Smart Fitness Tracker Band",
    titleZh: "NovaFit Pro 智能运动手环",
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
    descriptionZh: "高清AMOLED彩色曲面屏智能手环，全天候心率血氧监测，睡眠追踪，14天超长待机。",
    keywordsZh: ["手环", "智能手环", "运动手环", "健康手环", "运动手表", "novafit", "tracker", "band"],
    features: [
      "1.47-inch Vivid AMOLED Curved Display",
      "24/7 Heart Rate, SpO2 & Stress Tracking",
      "30+ Sport Activity Modes",
      "5ATM Water Resistant (Swim-proof)",
      "14-Day Ultra-Long Battery Life"
    ],
    variants: [
      { name: "Midnight Black", sku: "NF-PRO-BLK", stockCount: 18, inStock: true },
      { name: "Olive Green", sku: "NF-PRO-GRN", stockCount: 5, inStock: true },
      { name: "Sunset Coral", sku: "NF-PRO-CRL", stockCount: 0, inStock: false }
    ],
    iconType: "watch"
  },
  {
    id: "prod-powerbank-03",
    title: "HyperCharge 20000mAh Ultra-Fast Power Bank 65W",
    titleZh: "HyperCharge 20000mAh 超快充移动电源",
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
    descriptionZh: "20000mAh大容量65W超级闪充移动电源，兼容笔记本、平板和手机，支持多设备同时快充。",
    keywordsZh: ["充电宝", "移动电源", "便携电源", "快充", "外接电池", "hypercharge", "power bank", "powerbank"],
    features: [
      "65W USB-C PD 3.0 Fast Output",
      "Charges iPhone 15 to 60% in 30 minutes",
      "3 Ports: 2x USB-C + 1x USB-A",
      "Digital LED Percentage Battery Indicator",
      "Airline Approved 74Wh Capacity"
    ],
    variants: [
      { name: "Gunmetal Gray", sku: "HC-20K-GRY", stockCount: 40, inStock: true },
      { name: "Stealth Black", sku: "HC-20K-BLK", stockCount: 15, inStock: true },
      { name: "Titanium White", sku: "HC-20K-WHT", stockCount: 0, inStock: false }
    ],
    iconType: "powerbank"
  },
  {
    id: "prod-keyboard-04",
    title: "SwiftType Compact Mechanical Gaming Keyboard (Red Switch)",
    titleZh: "SwiftType 紧凑机械游戏键盘 (红轴)",
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
    descriptionZh: "65%精简配列热插拔机械键盘，厂润线性红轴，独立RGB背光，消音夹心棉降噪。",
    keywordsZh: ["键盘", "机械键盘", "游戏键盘", "红轴", "电脑键盘", "客制化键盘", "swifttype", "keyboard"],
    features: [
      "Smooth Linear Red Mechanical Switches",
      "Hot-Swappable 3-Pin / 5-Pin PCB",
      "Customizable RGB Lighting with 18 Presets",
      "Detachable Braided Type-C Cable",
      "Double-shot PBT Shine-through Keycaps"
    ],
    variants: [
      { name: "Linear Red Switch", sku: "ST-65-RED", stockCount: 0, inStock: false },
      { name: "Clicky Blue Switch", sku: "ST-65-BLU", stockCount: 0, inStock: false }
    ],
    iconType: "keyboard"
  },
  {
    id: "prod-mouse-05",
    title: "Vortex Gaming Mouse RGB 12000 DPI",
    titleZh: "Vortex RGB 游戏无线鼠标",
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
    descriptionZh: "轻量化洞洞人体工学电竞鼠标，12000 DPI高精度光学传感器，RGB幻彩灯效与微动防抖。",
    keywordsZh: ["鼠标", "游戏鼠标", "无线鼠标", "电竞鼠标", "有线鼠标", "vortex", "mouse"],
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
    titleZh: "AuraGlow 智能无线充护眼台灯",
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
    descriptionZh: "现代简约护眼台灯，无频闪减蓝光，5段色温无级调光，底座内置15W Qi手机无线快充。",
    keywordsZh: ["台灯", "护眼台灯", "智能台灯", "无线充台灯", "桌面灯", "auraglow", "lamp"],
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
    titleZh: "EchoBass 便携防水蓝牙低音音箱",
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
    descriptionZh: "360度环绕立体声，被动低音振膜，IPX7全防水可漂浮设计，户外旅行聚会便携低音炮。",
    keywordsZh: ["音箱", "蓝牙音箱", "低音音箱", "便携音箱", "户外音箱", "低音炮", "echobass", "speaker"],
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
    titleZh: "ClearView Pro 1080P 超清网络直播摄像头",
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
    descriptionZh: "1080P 60帧全高清直播摄像头，自动弱光补偿，双立体声降噪麦克风，物理隐私防窥滑盖。",
    keywordsZh: ["摄像头", "网络摄像头", "电脑摄像头", "直播摄像头", "高清摄像头", "clearview", "webcam"],
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
    titleZh: "UltraSlim 7合1 USB-C 多功能拓展坞",
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
    descriptionZh: "单口USB-C扩展4K@60Hz HDMI视频输出，100W PD闪充供电，3个USB 3.0高速接口及双卡槽读取。",
    keywordsZh: ["拓展坞", "转接头", "集线器", "usb-c", "转换器", "novatech", "hub"],
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
    titleZh: "ArcticBreeze 桌面静音USB迷你小风扇",
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
    descriptionZh: "无刷静音电机桌面小风扇，三档风速调节，360度角度俯仰，内置4000mAh充电脑长效送风。",
    keywordsZh: ["风扇", "小风扇", "桌面风扇", "静音风扇", "迷你风扇", "arcticbreeze", "fan"],
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
    titleZh: "ZenSound 头戴式主动降噪蓝牙耳机",
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
    descriptionZh: "录音室级无线头戴式耳机，40mm钛金属振膜单元，高深主动降噪，蛋白质亲肤记忆棉，50小时超长续航。",
    keywordsZh: ["头戴式耳机", "头戴耳机", "大耳机", "主动降噪", "降噪耳机", "zensound", "headphones"],
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
    titleZh: "TitanShield 防水防盗商务笔记本背包",
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
    descriptionZh: "牛津防泼水耐磨商务电脑包，独立16寸防震电脑仓，隐形防盗后贴身口袋，外置USB应急充电接口。",
    keywordsZh: ["背包", "电脑包", "双肩包", "书包", "旅行包", "titanshield", "backpack"],
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
