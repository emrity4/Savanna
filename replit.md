# Workspace

## Overview

pnpm workspace monorepo using TypeScript. Each package manages its own dependencies.

## Savanna Browser (Native Android)

A native Android browser app located at `savanna-android/`. Built with Kotlin, Gradle, and Android WebView.

### Architecture
- **Language**: Kotlin
- **Build**: Gradle 8.5 + AGP 8.2.0
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Design**: iOS 26 Liquid Glass aesthetic — OLED black surfaces, frosted white overlays, specular gradients, floating bars
- **No database** — uses SharedPreferences for settings, JSON files for history/bookmarks, in-memory lists for runtime data

### Key Features
- WebView browser with full navigation (back, forward, reload)
- Smart URL/search bar with auto https:// and search engine integration
- **Liquid Glass home page** (`assets/new_tab.html`) — time greeting, favorites grid, recently visited sites with colored domain icons, glass search bar, privacy badge
- **JavaScript bridge** (`NewTabBridge.kt`) — exposes `getRecentHistory()`, `navigate()`, `focusUrlBar()` to the new tab page
- Tab system with tab switcher (bottom and compact modes)
- Browsing history with search + colored domain initial-letter favicon icons
- Bookmarks with persistence
- Privacy report with tracker blocking statistics
- Tracker blocker (analytics, advertising, social, fingerprinting categories)
- Settings: search engine, tab mode, JS toggle, tracker/ad/popup blocking, DNT, clear on exit
- Smooth fragment transition animations
- Fully offline-capable for core features

### Liquid Glass Design System
All UI elements follow iOS 26 Liquid Glass material:
- `url_bar_background.xml` / `url_bar_focused.xml` — glass pill for URL bar
- `bottom_bar_background.xml` — floating dock with top-edge specular line
- `sheet_background.xml` — overlay bottom sheet with drag handle
- `glass_background.xml` — card surfaces for settings/privacy groups
- `menu_item_background.xml` — tappable row tiles with glass + ripple
- All overlays have drag handle pills and white-tinted `#18FFFFFF` separators
- Purple accent (`#B6A8FF`) only on tab card strokes and section headers

### Project Structure
```
savanna-android/
├── app/src/main/assets/
│   └── new_tab.html              # Liquid Glass home page (CSS backdrop-filter)
├── app/src/main/java/com/savanna/browser/
│   ├── MainActivity.kt           # Main orchestrator
│   ├── NewTabBridge.kt           # JS interface for new tab page
│   ├── adapter/                  # RecyclerView adapters (History has colored favicon icons)
│   ├── fragment/                 # UI fragments (Browser, TabSwitcher, History, Bookmarks, Settings, PrivacyReport)
│   ├── manager/                  # Business logic managers
│   ├── model/                    # Data classes
│   └── util/                     # Utilities (UrlUtils)
├── app/src/main/res/
│   ├── drawable/                 # Liquid Glass drawables + SF Symbols icons
│   ├── layout/                   # All layouts updated with glass aesthetic
│   └── values/                   # OLED black colors, glass styles
├── build.gradle.kts
└── settings.gradle.kts
```

### To Build
Open `savanna-android/` in Android Studio, sync Gradle, and run on device/emulator.

EAS Build: `cd savanna-android && eas build --platform android --profile development`

## Monorepo Stack

- **Monorepo tool**: pnpm workspaces
- **Node.js version**: 24
- **Package manager**: pnpm
- **TypeScript version**: 5.9
- **API framework**: Express 5
- **Database**: PostgreSQL + Drizzle ORM
- **Validation**: Zod (`zod/v4`), `drizzle-zod`
- **API codegen**: Orval (from OpenAPI spec)
- **Build**: esbuild (CJS bundle)

## Key Commands

- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- `pnpm --filter @workspace/api-server run dev` — run API server locally

See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details.
