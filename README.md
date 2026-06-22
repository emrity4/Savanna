<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/emrity4/Savanna/main/.github/banner-dark.png">
  <img src="https://raw.githubusercontent.com/emrity4/Savanna/main/.github/banner-light.png" width="100%" alt="Savanna — a browser that breathes">
</picture>

# Savanna

A browser that breathes.  
Minimal. Private. Glassy.

Savanna strips away the noise and puts the web front and center — a native Android browser born from the idea that your browser should feel like part of the OS, not an afterthought. Frosted glass, liquid gradients, and a dark soul.

---

## ✦ At a glance

| What | How |
|------|-----|
| **Engine** | Android `WebView` (Chrome on device) |
| **Soul** | Kotlin, Jetpack fragments, Material 3 |
| **Minimum** | Android 8.0 (API 26) |
| **Target** | Android 14 (API 34) |
| **Storage** | SQLite, `SharedPreferences` |
| **Build** | Gradle + Kotlin DSL |

---

## ✦ What makes it different

**Three skins, one browser.**  
Toggle between SOLID, FROSTED, and GLASS URL bar styles. GLASS mode bends the web page behind the bar through a live OpenGL shader — blur, chromatic aberration, wobble. It's extra. It's unnecessary. It's beautiful.

**Dark by default, light when you need it.**  
Instant dark/light toggle with pure OLED black. Every surface, every chip, every border — themed in one frame.

**Two faces.**  
Standard bottom toolbar when you want everything a tap away. Compact mode with a floating URL bar when you want the web to breathe.

**The details.**  
A scroll pill that hugs your thumb. A progress bar that fades in like a pulse. Tabs that live in a badge. A new tab page that greets you by the hour.

---

## ✦ Feature depth

- **Privacy-first** — per-site permissions (camera, mic, location, notifications), cookie control, clear-on-exit, tracker-blocking WebView
- **Tab management** — swipeable tab switcher, full back/forward stacks per tab
- **Bookmarks & history** — inline search, SQLite-backed, synced across tabs
- **Find in page** — native search with match counter, prev/next navigation
- **Reader mode** — injected styles for distraction-free reading
- **Gesture playground** — scroll-to-hide bars, long-press everywhere, pull-to-refresh, swipe navigation
- **PDF renderer** — open PDFs directly in the browser using Android's native `PdfRenderer`
- **PPTX viewer** — extracts slide text and renders it as clean HTML cards
- **File picker** — tap to open PDFs and presentations from device storage

---

## ✦ Building

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/`.

---

## ✦ Philosophy

Savanna was built in ponytail mode — every feature earns its place. No speculative abstractions. No dead weight. If the standard library can do it, we use it. If a feature isn't pulling its weight, it's cut.

The glass effect exists because it makes you smile. Everything else exists because it makes you faster.

---

> Crafted with care, shipping from the savanna.
