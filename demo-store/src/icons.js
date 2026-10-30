// Inline SVG generators for Demo Market products
// 100% self-contained, no external CDN or image hosting needed

function getProductSvg(type, color = "#6366f1") {
  const svgs = {
    earbuds: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#1e1b4b"/>
            <stop offset="100%" stop-color="#312e81"/>
          </linearGradient>
          <linearGradient id="glow" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#818cf8"/>
            <stop offset="100%" stop-color="#4f46e5"/>
          </linearGradient>
        </defs>
        <rect width="200" height="200" rx="24" fill="url(#bg)"/>
        <!-- Case -->
        <rect x="50" y="80" width="100" height="75" rx="30" fill="#0f172a" stroke="#475569" stroke-width="3"/>
        <line x1="50" y1="110" x2="150" y2="110" stroke="#334155" stroke-width="2"/>
        <circle cx="100" cy="125" r="4" fill="#38bdf8"/>
        <!-- Earbud Left -->
        <circle cx="75" cy="55" r="18" fill="url(#glow)"/>
        <path d="M72 65 L72 88 A6 6 0 0 0 84 88 L84 65 Z" fill="#64748b"/>
        <!-- Earbud Right -->
        <circle cx="125" cy="55" r="18" fill="url(#glow)"/>
        <path d="M122 65 L122 88 A6 6 0 0 0 134 88 L134 65 Z" fill="#64748b"/>
        <!-- Sound waves -->
        <path d="M40 50 A 25 25 0 0 0 40 70" stroke="#38bdf8" stroke-width="2.5" fill="none" stroke-linecap="round"/>
        <path d="M160 50 A 25 25 0 0 1 160 70" stroke="#38bdf8" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      </svg>`,

    watch: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#064e3b"/>
        <!-- Straps -->
        <rect x="82" y="20" width="36" height="50" rx="8" fill="#0f172a"/>
        <rect x="82" y="130" width="36" height="50" rx="8" fill="#0f172a"/>
        <!-- Body -->
        <rect x="70" y="55" width="60" height="90" rx="16" fill="#1e293b" stroke="#34d399" stroke-width="2.5"/>
        <!-- Screen Content -->
        <text x="100" y="85" font-family="system-ui" font-size="14" font-weight="bold" fill="#34d399" text-anchor="middle">10:42</text>
        <!-- Heart Icon -->
        <path d="M95 100 A3 3 0 0 1 100 97 A3 3 0 0 1 105 100 L100 106 Z" fill="#f43f5e"/>
        <text x="100" y="118" font-family="system-ui" font-size="9" fill="#94a3b8" text-anchor="middle">72 BPM</text>
        <circle cx="100" cy="132" r="3" fill="#34d399"/>
      </svg>`,

    powerbank: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#18181b"/>
        <rect x="60" y="45" width="80" height="120" rx="14" fill="#27272a" stroke="#71717a" stroke-width="2"/>
        <!-- Display -->
        <rect x="75" y="65" width="50" height="24" rx="4" fill="#09090b" stroke="#3f3f46" stroke-width="1.5"/>
        <text x="100" y="81" font-family="system-ui" font-size="12" font-weight="bold" fill="#22c55e" text-anchor="middle">98%</text>
        <!-- Lightning -->
        <polygon points="100,105 92,120 99,120 97,135 108,118 101,118" fill="#eab308"/>
        <!-- Top Ports -->
        <rect x="75" y="40" width="12" height="6" rx="2" fill="#52525b"/>
        <rect x="94" y="40" width="12" height="6" rx="2" fill="#52525b"/>
        <rect x="113" y="40" width="12" height="6" rx="2" fill="#52525b"/>
      </svg>`,

    keyboard: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#1e1b4b"/>
        <rect x="35" y="65" width="130" height="75" rx="10" fill="#0f172a" stroke="#ec4899" stroke-width="2"/>
        <!-- Keys Grid -->
        <g fill="#334155" stroke="#ec4899" stroke-width="0.8">
          <rect x="42" y="72" width="18" height="12" rx="2" fill="#ec4899"/>
          <rect x="64" y="72" width="18" height="12" rx="2"/>
          <rect x="86" y="72" width="18" height="12" rx="2"/>
          <rect x="108" y="72" width="18" height="12" rx="2"/>
          <rect x="130" y="72" width="28" height="12" rx="2"/>
          
          <rect x="42" y="88" width="22" height="12" rx="2"/>
          <rect x="68" y="88" width="18" height="12" rx="2"/>
          <rect x="90" y="88" width="18" height="12" rx="2" fill="#a855f7"/>
          <rect x="112" y="88" width="18" height="12" rx="2"/>
          <rect x="134" y="88" width="24" height="12" rx="2"/>

          <rect x="42" y="104" width="26" height="12" rx="2"/>
          <rect x="72" y="104" width="55" height="12" rx="3" fill="#ec4899"/>
          <rect x="131" y="104" width="27" height="12" rx="2"/>
        </g>
        <circle cx="155" cy="70" r="2.5" fill="#22c55e"/>
      </svg>`,

    mouse: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#0f172a"/>
        <!-- Mouse Body -->
        <path d="M75 90 C75 55 125 55 125 90 L125 130 C125 155 75 155 75 130 Z" fill="#1e293b" stroke="#38bdf8" stroke-width="2.5"/>
        <line x1="100" y1="57" x2="100" y2="85" stroke="#38bdf8" stroke-width="2"/>
        <!-- Scroll Wheel -->
        <rect x="96" y="70" width="8" height="16" rx="4" fill="#38bdf8"/>
        <!-- Honeycomb dots -->
        <circle cx="100" cy="115" r="3" fill="#0284c7"/>
        <circle cx="92" cy="122" r="3" fill="#0284c7"/>
        <circle cx="108" cy="122" r="3" fill="#0284c7"/>
        <circle cx="100" cy="130" r="3" fill="#0284c7"/>
      </svg>`,

    lamp: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#1c1917"/>
        <!-- Base -->
        <ellipse cx="100" cy="155" rx="40" ry="12" fill="#292524" stroke="#eab308" stroke-width="2"/>
        <circle cx="100" cy="155" r="8" fill="none" stroke="#78716c" stroke-width="1.5" stroke-dasharray="3,2"/>
        <!-- Stem -->
        <path d="M100 155 L100 90 L140 60" stroke="#78716c" stroke-width="5" fill="none" stroke-linecap="round"/>
        <!-- Lamp Head -->
        <rect x="135" y="52" width="45" height="12" rx="4" fill="#eab308"/>
        <!-- Light Cone -->
        <polygon points="140,64 100,140 170,140" fill="#eab308" fill-opacity="0.15"/>
      </svg>`,

    speaker: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#042f2e"/>
        <!-- Speaker Body -->
        <rect x="65" y="45" width="70" height="110" rx="22" fill="#0f172a" stroke="#14b8a6" stroke-width="3"/>
        <!-- Grill lines -->
        <g stroke="#14b8a6" stroke-width="2" stroke-linecap="round">
          <line x1="78" y1="75" x2="122" y2="75"/>
          <line x1="78" y1="85" x2="122" y2="85"/>
          <line x1="78" y1="95" x2="122" y2="95"/>
          <line x1="78" y1="105" x2="122" y2="105"/>
          <line x1="78" y1="115" x2="122" y2="115"/>
          <line x1="78" y1="125" x2="122" y2="125"/>
        </g>
        <circle cx="100" cy="60" r="5" fill="#f43f5e"/>
      </svg>`,

    webcam: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#111827"/>
        <!-- Stand Base -->
        <rect x="80" y="145" width="40" height="15" rx="4" fill="#374151"/>
        <line x1="100" y1="120" x2="100" y2="145" stroke="#4b5563" stroke-width="5"/>
        <!-- Camera Body -->
        <rect x="50" y="70" width="100" height="50" rx="16" fill="#1f2937" stroke="#60a5fa" stroke-width="2"/>
        <circle cx="100" cy="95" r="18" fill="#0f172a" stroke="#3b82f6" stroke-width="3"/>
        <circle cx="100" cy="95" r="9" fill="#1d4ed8"/>
        <circle cx="103" cy="92" r="3" fill="#ffffff" fill-opacity="0.8"/>
        <circle cx="135" cy="95" r="3" fill="#10b981"/>
      </svg>`,

    hub: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#1e293b"/>
        <!-- Cable -->
        <path d="M100 45 L100 70" stroke="#94a3b8" stroke-width="5" stroke-linecap="round"/>
        <rect x="94" y="32" width="12" height="15" rx="3" fill="#64748b"/>
        <!-- Hub Body -->
        <rect x="65" y="70" width="70" height="95" rx="10" fill="#334155" stroke="#94a3b8" stroke-width="2"/>
        <!-- Ports -->
        <rect x="75" y="85" width="22" height="7" rx="2" fill="#0f172a"/>
        <rect x="75" y="100" width="22" height="7" rx="2" fill="#0f172a"/>
        <rect x="75" y="115" width="22" height="7" rx="2" fill="#0f172a"/>
        <rect x="75" y="135" width="35" height="10" rx="2" fill="#0284c7"/>
      </svg>`,

    fan: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#064e3b"/>
        <!-- Base -->
        <rect x="75" y="145" width="50" height="15" rx="6" fill="#022c22" stroke="#10b981" stroke-width="2"/>
        <line x1="100" y1="115" x2="100" y2="145" stroke="#022c22" stroke-width="5"/>
        <!-- Fan Circle -->
        <circle cx="100" cy="80" r="35" fill="#022c22" stroke="#34d399" stroke-width="2.5"/>
        <circle cx="100" cy="80" r="8" fill="#10b981"/>
        <!-- Blades -->
        <path d="M100 80 Q 90 55 100 48 Q 110 55 100 80" fill="#34d399"/>
        <path d="M100 80 Q 125 70 132 80 Q 125 90 100 80" fill="#34d399"/>
        <path d="M100 80 Q 110 105 100 112 Q 90 105 100 80" fill="#34d399"/>
        <path d="M100 80 Q 75 90 68 80 Q 75 70 100 80" fill="#34d399"/>
      </svg>`,

    headphones: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#172554"/>
        <!-- Headband -->
        <path d="M60 110 A 45 45 0 0 1 140 110" stroke="#60a5fa" stroke-width="7" fill="none" stroke-linecap="round"/>
        <!-- Ear Cushions -->
        <rect x="50" y="95" width="18" height="40" rx="9" fill="#1e40af" stroke="#93c5fd" stroke-width="2"/>
        <rect x="132" y="95" width="18" height="40" rx="9" fill="#1e40af" stroke="#93c5fd" stroke-width="2"/>
        <circle cx="100" cy="65" r="4" fill="#38bdf8"/>
      </svg>`,

    backpack: `
      <svg viewBox="0 0 200 200" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" rx="24" fill="#1f2937"/>
        <!-- Handle -->
        <path d="M85 55 A 15 15 0 0 1 115 55" stroke="#9ca3af" stroke-width="4" fill="none"/>
        <!-- Backpack Body -->
        <rect x="65" y="55" width="70" height="95" rx="18" fill="#374151" stroke="#f97316" stroke-width="2.5"/>
        <!-- Front Pocket -->
        <rect x="75" y="95" width="50" height="45" rx="8" fill="#1f2937" stroke="#f97316" stroke-width="1.5"/>
        <line x1="75" y1="95" x2="125" y2="95" stroke="#f97316" stroke-width="2"/>
        <circle cx="120" cy="102" r="3" fill="#f97316"/>
      </svg>`
  };

  return svgs[type] || svgs.earbuds;
}

module.exports = { getProductSvg };
