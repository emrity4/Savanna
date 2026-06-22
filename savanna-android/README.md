# Savanna

A privacy-focused Android browser with a native, iOS-inspired interface. Minimal, fast, and dark by default.

## Features

- **WebKit-powered** — full-featured `WebView` with tracker blocking, popup control, and JavaScript toggles
- **Dual layout** — standard bottom toolbar or compact mode with floating URL bar
- **Adaptive theming** — instant dark/light toggle with OLED-black dark mode; 75 % frosted glass elements
- **Tab management** — swipeable tab switcher, back/forward history per tab
- **Privacy suite** — per-site permissions (camera, mic, location, notifications), cookie control, clear-on-exit
- **Bookmarks & history** — local SQLite-backed storage with inline search
- **Find in page** — WebView-native text search with match counter
- **Reader mode** — distraction-free reading via injected styles
- **Gesture-friendly** — scroll-to-hide bars, long-press context menu, pull-to-refresh

## Tech stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| Minimum SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| UI | Jetpack fragments + Material 3 |
| Web engine | Android `WebView` (Chrome on device) |
| Storage | SQLite via `SharedPreferences` + raw queries |
| Build | Gradle + Kotlin DSL |

## Building

```bash
./gradlew assembleDebug
```

The debug APK lands at `app/build/outputs/apk/debug/`.

## License

This project is provided for personal and educational use.
