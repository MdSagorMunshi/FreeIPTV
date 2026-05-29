# FreeIPTV — Image Asset Prompts

> **Instructions:** Generate each image using the prompt below. Save the output file with the exact filename listed at the bottom of each section into `FreeIPTV/assets/` folder. Once all images are ready, they will be resized to proper Android densities and integrated into the project.

---

## Color Reference (Match These in Every Image)

| Role | Hex | Description |
|------|-----|-------------|
| Primary Purple | `#6C5CE7` | Main brand purple |
| Secondary Pink | `#EC4899` | Accent pink |
| Deep Purple | `#4338CA` | Darker shade |
| Light Purple | `#A78BFA` | Lighter shade |
| Background Dark | `#07070E` | App background (near-black with purple tint) |
| Surface Dark | `#0F0F1A` | Card/panel backgrounds |
| Live Red | `#EF4444` | Live indicator color |
| Online Green | `#22C55E` | Status active color |

---

## 1. App Icon — Foreground

**Prompt:**
A modern, premium app icon foreground for an IPTV / live TV streaming application called "FreeIPTV." The design should be a bold, stylized play-button triangle merged seamlessly with a classic old-school TV antenna silhouette, forming one unified symbol. The play button is angled slightly to the right, suggesting motion and streaming. The antenna prongs extend upward from the top-left corner of the play button. The entire symbol should be rendered in a smooth purple-to-pink gradient (from `#6C5CE7` on the left to `#EC4899` on the right). The icon should have subtle inner glow or glass-like highlight to convey depth and premium quality. The background behind the symbol is fully transparent (alpha channel). The symbol should sit centered within a 108×108 safe zone with generous padding (the icon shape occupies roughly the center 66×66 area, leaving room for adaptive icon masking). Clean vector style, no text, no border, no drop shadow outside the shape. Ultra-crisp edges suitable for rendering at small sizes.

**Orientation:** Square (1:1)
**Canvas Size:** 1024 × 1024 px (will be scaled down)
**Background:** Transparent / alpha

📁 **Filename:** `ic_launcher_foreground.png`

---

## 2. App Icon — Background

**Prompt:**
A rich, deep, seamless gradient background for an Android adaptive app icon. The gradient sweeps diagonally from the bottom-left to the top-right, transitioning from a deep near-black purple `#07070E` through a saturated mid-purple `#4338CA` to a vivid electric purple `#6C5CE7`. Include a very subtle, almost imperceptible radial light bloom near the center (a soft glow of `#A78BFA` at 8-10% opacity) that gives the background a sense of depth without adding any visible pattern. No text, no shapes, no patterns — pure smooth gradient. The result should feel like a polished gemstone surface when paired with the foreground icon.

**Orientation:** Square (1:1)
**Canvas Size:** 1024 × 1024 px

📁 **Filename:** `ic_launcher_background.png`

---

## 3. Splash / Launch Screen Logo

**Prompt:**
A centered brand logo for a splash screen of a premium IPTV streaming app called "FreeIPTV." The design shows the same play-button-with-antenna icon from the app icon (see Image 1 description), rendered larger and with a subtle neon-like outer glow in purple (`#6C5CE7` at 30% opacity, spread ~20px). Below the icon symbol, the text "FreeIPTV" is displayed in a clean, modern sans-serif font (similar to Inter or Outfit bold weight), with "Free" in white `#F0F0F5` and "IPTV" in the gradient purple-to-pink (`#6C5CE7` → `#EC4899`). Beneath the app name, a thin tagline reads "Live TV. Unlimited. Free." in a lighter weight, colored `#8888A0`. The entire composition is centered on a solid dark background of `#07070E`. The logo+text block is vertically centered. Minimal, luxurious, and modern feel — like a streaming service brand.

**Orientation:** Portrait (9:16)
**Canvas Size:** 1080 × 1920 px

📁 **Filename:** `splash_logo.png`

---

## 4. Google Play Store Feature Graphic

**Prompt:**
A wide promotional banner for the Google Play Store listing of "FreeIPTV." The background is a dramatic deep-space-inspired gradient from `#07070E` at the edges to a glowing center of `#4338CA` with subtle lens-flare-like light streaks in purple and pink. On the left third of the banner, the FreeIPTV icon (play-button-with-antenna, purple-to-pink gradient) floats with a soft glow halo. To the right of the icon, large bold white text reads "FreeIPTV" with "Free" in white and "IPTV" in the purple-pink gradient. Below that, a subtitle in `#8888A0` reads "Stream Live TV Channels — Completely Free." Scattered faintly in the dark background are tiny abstract dots or constellation-like patterns suggesting a vast network of channels. The overall mood is cinematic, premium, and inviting. No device mockups, no screenshots — pure brand graphic.

**Orientation:** Landscape (16:9)
**Canvas Size:** 1024 × 500 px

📁 **Filename:** `feature_graphic.png`

---

## 5. Empty State — No Channels Found

**Prompt:**
An illustration for an empty state screen inside a dark-themed IPTV app, shown when the user's search returns no results. The scene features a minimalist, semi-flat illustration of a vintage TV set (rounded-rectangle body with two antenna prongs sticking up) displayed on a subtle surface. The TV screen shows a static/snow pattern rendered as small dots in `#55556A`. The TV body is colored `#1C1C34` with a thin border of `#22223A`. Floating around the TV are 2-3 small question marks or magnifying glass outlines in `#8888A0` at reduced opacity, conveying "not found." The background is a solid `#07070E`. The overall style is minimal, friendly, and consistent with a premium dark UI. No text in the illustration itself — text will be added programmatically. Centered composition.

**Orientation:** Square (1:1)
**Canvas Size:** 512 × 512 px
**Background:** Transparent / alpha

📁 **Filename:** `empty_no_results.png`

---

## 6. Empty State — No Favorites

**Prompt:**
An illustration for an empty state screen inside a dark-themed IPTV app, shown when the user has no favorite channels saved yet. The scene shows a minimalist outlined heart shape (like a love/favorite icon) drawn with a dashed or dotted purple stroke (`#6C5CE7`) on a dark background `#07070E`. Inside the heart, a tiny simplified TV antenna icon sits in `#8888A0`, suggesting "add your favorite channels." A few small star sparkles (3-4) in `#A78BFA` at 40% opacity float around the heart for a subtle magical feel. Clean, minimal, friendly illustration. No text in the illustration. Centered composition. Transparent background.

**Orientation:** Square (1:1)
**Canvas Size:** 512 × 512 px
**Background:** Transparent / alpha

📁 **Filename:** `empty_no_favorites.png`

---

## 7. Empty State — Loading / Buffering

**Prompt:**
An illustration for a loading or buffering state inside a dark-themed IPTV app. The scene shows a minimalist TV set outline (like Image 5's TV but slightly more abstract/geometric) with its screen area filled with a swirling, soft gradient ring animation (like a loading spinner), colored in the purple-to-pink gradient (`#6C5CE7` → `#EC4899`). A few small concentric signal wave arcs radiate outward from the TV's antenna tips in `#A78BFA` at 30% opacity, suggesting "connecting" or "loading signal." The background is solid `#07070E`. Clean, modern, minimal vector style. No text. Centered. Transparent background.

**Orientation:** Square (1:1)
**Canvas Size:** 512 × 512 px
**Background:** Transparent / alpha

📁 **Filename:** `empty_loading.png`

---

## 8. Channel Placeholder / Default Channel Logo

**Prompt:**
A small, clean placeholder icon to represent a TV channel that has no logo available. The design is a simple, filled rounded-rectangle TV screen shape (like a monitor with slightly rounded corners) in `#1C1C34` with a thin 1px border of `#22223A`. Inside the screen area, centered, a small play triangle in `#55556A` sits. The overall shape should be compact and balanced, suitable for display at very small sizes (48×48 dp). No text, no antenna, ultra-minimal. Transparent background.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `channel_placeholder.png`

---

## 9. Category Icon — Sports

**Prompt:**
A small, flat-style category icon for a "Sports" channel group inside a dark-themed IPTV app. The icon shows a simplified soccer ball (pentagon-hexagon pattern) rendered in a single color: white `#F0F0F5`. The style is minimal outline/filled-flat, not realistic — think Material Design or Phosphor Icons. No background fill, transparent background. The icon should be crisp and legible at 24×24 dp. Centered within the canvas.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_sports.png`

---

## 10. Category Icon — News

**Prompt:**
A small, flat-style category icon for a "News" channel group inside a dark-themed IPTV app. The icon shows a simplified newspaper or broadcast microphone rendered in a single color: white `#F0F0F5`. Minimal outline/filled-flat style, similar to Material Design icons. No background fill, transparent background. Crisp and legible at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_news.png`

---

## 11. Category Icon — Entertainment

**Prompt:**
A small, flat-style category icon for an "Entertainment" channel group inside a dark-themed IPTV app. The icon shows a simplified movie clapperboard or a popcorn bucket rendered in a single color: white `#F0F0F5`. Minimal outline/filled-flat style, Material Design aesthetic. No background fill, transparent background. Crisp at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_entertainment.png`

---

## 12. Category Icon — Music

**Prompt:**
A small, flat-style category icon for a "Music" channel group inside a dark-themed IPTV app. The icon shows a simplified music note (single eighth note or double eighth notes) rendered in a single color: white `#F0F0F5`. Minimal outline/filled-flat style. No background, transparent background. Crisp at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_music.png`

---

## 13. Category Icon — Kids

**Prompt:**
A small, flat-style category icon for a "Kids" channel group inside a dark-themed IPTV app. The icon shows a simplified teddy bear face or a cartoon star with a smiley face, rendered in a single color: white `#F0F0F5`. Friendly, rounded shapes. Minimal flat style. No background, transparent. Crisp at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_kids.png`

---

## 14. Category Icon — Movies

**Prompt:**
A small, flat-style category icon for a "Movies" channel group inside a dark-themed IPTV app. The icon shows a simplified film reel or film strip frame rendered in a single color: white `#F0F0F5`. Clean, minimal, Material Design flat style. No background, transparent. Crisp at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_movies.png`

---

## 15. Category Icon — General / All

**Prompt:**
A small, flat-style category icon for an "All Channels" or general category inside a dark-themed IPTV app. The icon shows a simplified grid layout (2×2 squares like a dashboard/grid view) rendered in a single color: white `#F0F0F5`. Minimal flat style. No background, transparent. Crisp at 24×24 dp. Centered.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_category_all.png`

---

## 16. Notification Icon (Monochrome)

**Prompt:**
A monochrome notification icon for an Android IPTV streaming app. The design is the simplified play-button-with-antenna symbol (same shape as the app icon foreground) rendered as a solid white silhouette on a transparent background. The shape must be a single flat fill with no gradients, no gray tones — pure white `#FFFFFF` only. This is required for Android notification tray where only alpha channel is used. The shape should be well-padded within the canvas (occupying about 60-70% of the space). Ultra-clean edges.

**Orientation:** Square (1:1)
**Canvas Size:** 96 × 96 px
**Background:** Transparent / alpha

📁 **Filename:** `ic_notification.png`

---

## 17. Player Overlay — Watermark / Branding

**Prompt:**
A very subtle, minimal watermark logo to be displayed semi-transparently in the corner of the video player screen. The design is the play-button-with-antenna icon symbol only (no text), rendered in white `#FFFFFF` at full opacity — transparency will be applied programmatically at around 10-15% in the app. The shape should be compact and recognizable even at very small sizes (24-32 dp). Transparent background. Extremely clean and simple vector-like rendering.

**Orientation:** Square (1:1)
**Canvas Size:** 256 × 256 px
**Background:** Transparent / alpha

📁 **Filename:** `watermark_logo.png`

---

## 18. Onboarding — Screen 1: Welcome

**Prompt:**
An onboarding illustration for the first screen of a premium dark-themed IPTV app. The scene shows a large, modern smart TV floating slightly at an angle in a dark void `#07070E`, with its screen glowing vividly — displaying a colorful mosaic grid of tiny channel thumbnails in various hues (blues, purples, pinks, greens) suggesting hundreds of live channels. Purple-to-pink gradient light rays (`#6C5CE7` → `#EC4899`) emanate softly from behind the TV like a sunrise. A few tiny satellite/signal wave icons float in the upper portion. The style is clean, semi-flat with subtle depth (soft shadows, gentle glows). Premium, aspirational, and exciting. No text in illustration.

**Orientation:** Portrait (3:4)
**Canvas Size:** 900 × 1200 px
**Background:** Solid `#07070E`

📁 **Filename:** `onboarding_welcome.png`

---

## 19. Onboarding — Screen 2: Browse Categories

**Prompt:**
An onboarding illustration for the second screen of a premium dark-themed IPTV app, showing the concept of browsing channel categories. The scene shows 4-5 floating rounded-rectangle category cards arranged in a fan or stacked layout, each card in a slightly different shade of the app's palette (dark purple `#1C1C34`, deep surface `#16162A`, etc.) with a small white icon on each (sports ball, music note, news mic, movie clapboard, star). The cards have a subtle purple glow edge (`#6C5CE7` at 20% opacity). The background is `#07070E`. A soft, defocused grid of dots behind the cards suggests the channel grid. Clean, minimal, premium style. No text.

**Orientation:** Portrait (3:4)
**Canvas Size:** 900 × 1200 px
**Background:** Solid `#07070E`

📁 **Filename:** `onboarding_categories.png`

---

## 20. Onboarding — Screen 3: Watch Anywhere

**Prompt:**
An onboarding illustration for the third screen of a premium dark-themed IPTV app, conveying the idea of "watch anywhere, anytime." The scene shows a smartphone held at a slight tilt, with its screen displaying a vibrant live TV stream (a glowing, colorful abstract video frame). Around the phone, small Wi-Fi signal arcs and a small globe icon float, symbolizing internet streaming. A subtle clock or "24/7" badge sits in the upper corner. The phone emits a soft purple glow (`#6C5CE7`). Background is `#07070E`. Clean, semi-flat style with gentle glows and shadows. No text.

**Orientation:** Portrait (3:4)
**Canvas Size:** 900 × 1200 px
**Background:** Solid `#07070E`

📁 **Filename:** `onboarding_watch_anywhere.png`

---

## Summary — All Files

| # | Filename | Size (px) | Orientation | Transparency |
|---|----------|-----------|-------------|--------------|
| 1 | `ic_launcher_foreground.png` | 1024×1024 | Square | ✅ Yes |
| 2 | `ic_launcher_background.png` | 1024×1024 | Square | ❌ No |
| 3 | `splash_logo.png` | 1080×1920 | Portrait | ❌ No |
| 4 | `feature_graphic.png` | 1024×500 | Landscape | ❌ No |
| 5 | `empty_no_results.png` | 512×512 | Square | ✅ Yes |
| 6 | `empty_no_favorites.png` | 512×512 | Square | ✅ Yes |
| 7 | `empty_loading.png` | 512×512 | Square | ✅ Yes |
| 8 | `channel_placeholder.png` | 256×256 | Square | ✅ Yes |
| 9 | `ic_category_sports.png` | 256×256 | Square | ✅ Yes |
| 10 | `ic_category_news.png` | 256×256 | Square | ✅ Yes |
| 11 | `ic_category_entertainment.png` | 256×256 | Square | ✅ Yes |
| 12 | `ic_category_music.png` | 256×256 | Square | ✅ Yes |
| 13 | `ic_category_kids.png` | 256×256 | Square | ✅ Yes |
| 14 | `ic_category_movies.png` | 256×256 | Square | ✅ Yes |
| 15 | `ic_category_all.png` | 256×256 | Square | ✅ Yes |
| 16 | `ic_notification.png` | 96×96 | Square | ✅ Yes |
| 17 | `watermark_logo.png` | 256×256 | Square | ✅ Yes |
| 18 | `onboarding_welcome.png` | 900×1200 | Portrait | ❌ No |
| 19 | `onboarding_categories.png` | 900×1200 | Portrait | ❌ No |
| 20 | `onboarding_watch_anywhere.png` | 900×1200 | Portrait | ❌ No |

---

> **Save all files to:** `FreeIPTV/assets/`
> Once done, notify me and I will generate proper Android density buckets (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi) and integrate them into the project's `res/` folders.
