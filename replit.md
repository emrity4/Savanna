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
- **Design**: Dark liquid-glass aesthetic with purple accents (iOS 26-style)
- **No database** — uses SharedPreferences for settings, JSON files for history/bookmarks, in-memory lists for runtime data

### Key Features
- WebView browser with full navigation (back, forward, reload)
- Smart URL/search bar with auto https:// and search engine integration
- Tab system with tab switcher (bottom and compact modes)
- Browsing history with search and date grouping
- Bookmarks with persistence
- Privacy report with tracker blocking statistics
- Tracker blocker (analytics, advertising, social, fingerprinting categories)
- Settings: search engine, tab mode, JS toggle, tracker/ad/popup blocking, DNT, clear on exit
- Smooth fragment transition animations
- Fully offline-capable for core features

### Project Structure
```
savanna-android/
├── app/src/main/java/com/savanna/browser/
│   ├── MainActivity.kt          # Main orchestrator
│   ├── adapter/                  # RecyclerView adapters
│   ├── fragment/                 # UI fragments (Browser, TabSwitcher, History, Bookmarks, Settings, PrivacyReport)
│   ├── manager/                  # Business logic (TabManager, HistoryManager, BookmarkManager, SettingsManager, TrackerBlocker)
│   ├── model/                    # Data classes (Tab, HistoryItem, Bookmark, TrackerInfo)
│   └── util/                     # Utilities (UrlUtils)
├── app/src/main/res/             # Layouts, drawables, colors, themes, animations
├── build.gradle.kts              # Project-level Gradle
└── settings.gradle.kts           # Gradle settings
```

### To Build
Open `savanna-android/` in Android Studio, sync Gradle, and run on device/emulator.

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
