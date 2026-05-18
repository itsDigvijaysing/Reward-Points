# Stat Up - RPG Gamification for Real Life

<div align="center">

![Version](https://img.shields.io/badge/version-3.1.2-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-GPL--3.0-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-green.svg)

**An anime-inspired RPG Status Window app that gamifies your daily tasks and achievements with a premium liquid glass UI.**

[Features](#-features) • [Screenshots](#-screenshots) • [Building](#-building) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

## 📱 Overview

Stat Up transforms your daily life into an RPG experience. Complete tasks to earn points, level up your character stats, maintain streaks to rank up, and redeem rewards you create. All with a beautiful liquid glass aesthetic inspired by iOS and anime "Status Window" interfaces.

### Why Stat Up?

- 🎮 **Gamified Productivity** - Turn boring tasks into exciting quests
- ✨ **Premium UI** - Liquid glass effects, hexagon stat charts, animated rank badges
- 🔒 **Privacy First** - Offline-first core, no accounts, no tracking; Todoist and AI Coach are opt-in only
- 🎯 **Simple Yet Effective** - No complex systems, just points, stats, and ranks

## ✨ Features

### 🎯 **Core Gamification**
- **RPG Stats System** - 6 stats: Strength, Intelligence, Wisdom, Dexterity, Charisma, Vitality
- **Hexagon Radar Chart** - Visual representation of your stats with tap-to-expand details
- **Rank System** - Progress from E → D → C → B → A → S based on streak consistency
- **Star Lines Progress** - 5 streak days = ⭐ = Rank Up; breaks can cause rank down
- **Points Economy** - Earn points from tasks, spend on custom rewards

### 📋 **Task Management**
- **Manual Tasks** - Create custom tasks with priority (P1-P4) and stat assignment
- **Todoist Integration** - Connect your Todoist account, sync completed tasks with label-to-stat routing
- **Priority Points** - P1=4pts, P2=3pts, P3=2pts, P4=1pt
- **Background Sync** - TodoistSyncWorker runs every 15 minutes

### 🎁 **Rewards System**
- **Custom Rewards** - Create personal rewards (e.g., "Watch an episode" = 50pts)
- **Point Redemption** - Spend earned points on your rewards
- **Transaction History** - Track all earnings and spendings

### 🏆 **Achievements**
- **Built-in Achievements** - 12 prebuilt achievements across streak, points, stats, tasks, rank, and special categories
- **Custom Achievements** - Create your own with custom emoji, target goal, and reward points
- **No-Goal Achievements** - Manual completion option for open-ended goals
- **Delete Any Achievement** - Full control over your achievement list

### 📊 **Progress Tracking**
- **Streak Calendar** - 28-day view with month label showing your activity patterns
- **Stat Decay** - Midnight check: idle days lose 1 stat point per stat and 1 star line
- **Mood Check-in** - Daily mood logging with +2 WIS bonus
- **History Log** - Complete transaction history with filtering and pagination
- **Per-Stat Breakdown** - Detailed view with weekly chart and top sources for each stat

### 🎨 **Premium UI**
- **Liquid Glass Design** - Translucent cards, glowing borders, blur effects
- **Dark Theme** - Eye-friendly dark interface
- **Smooth Animations** - AnimatedVisibility, progress animations, transitions
- **RPG Aesthetic** - Status window design inspired by Solo Leveling and other anime

## 🏗️ Building

### Prerequisites
- **JDK 17** (Gradle 8.13 does not run on JDK 24+; pure JDK 17 is what AGP 8.11.2 expects)
- **Android SDK** with platforms `android-35` + `android-36`, build-tools `35.0.0` + `36.0.0`, platform-tools (compileSdk 36, minSdk 26)
- Gradle 8.13 (wrapper included)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/yourusername/Stat-Up.git
cd Stat-Up

# Build debug APK
./gradlew assembleDebug

# Build release APK
# Without a real keystore this falls back to debug signing.
# To produce a Play-ready APK, copy keystore.properties.template → keystore.properties
# and fill in the real values (see template comments).
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/`

### Testing on Waydroid (Linux)
```bash
# Enable multi-window mode
waydroid prop set persist.waydroid.multi_windows true
systemctl restart waydroid-container

# Install and run
waydroid app install app/build/outputs/apk/debug/app-debug.apk
waydroid app launch com.rewardpoints.app.debug
```

## 🏛️ Architecture

### Tech Stack
- **Language**: Kotlin 2.1.20
- **UI**: Jetpack Compose (Compose BOM 2025.03.01) with Material 3 + [Haze 1.7.2](https://chrisbanes.github.io/haze/) for real GPU backdrop blur on API 31+
- **Architecture**: MVVM with Repository pattern
- **DI**: Koin 3.5.6
- **Database**: Room 2.7.1
- **Preferences**: DataStore 1.1.4 (plain) + AndroidX Security 1.1.0-alpha06 (`EncryptedSharedPreferences`, AES-256-GCM, for Todoist + Gemini tokens)
- **Network**: Ktor 3.1.2 (Todoist API + Gemini Generative Language API)
- **Background**: WorkManager 2.10.1 (DecayWorker, TodoistSyncWorker)
- **Widget**: AppWidgetProvider + RemoteViews (4×2 home-screen widget showing rank/balance/streak/today)

### Project Structure
```
app/src/main/java/com/rewardpoints/app/
├── data/
│   ├── local/
│   │   ├── db/          # Room database, DAOs, entities
│   │   └── datastore/   # User preferences
│   └── repository/      # Data repositories
├── domain/
│   └── model/           # Domain models (PlayerStats, Rank, StatType)
├── rpg/                 # Game mechanics (StatsEngine, RankCalculator, DecayEngine)
├── ui/
│   ├── components/
│   │   ├── glass/       # Reusable glass UI components
│   │   └── rpg/         # RPG-specific components (StatusWindow, HexagonChart)
│   ├── screen/          # Screen composables and ViewModels
│   └── theme/           # Colors, typography, design tokens
└── di/                  # Koin modules
```

### Design Tokens
```kotlin
// Colors
BackgroundBase = #0A0A0F
SurfaceBase = #12121A
AccentPrimary = #7C4DFF
PointsGold = #FFD740

// Glass Effect
GlassFill = white @ 10% alpha
GlassBorder = white @ 18% alpha
GlassRadius = 24.dp
```

## 🔒 Privacy

Your privacy matters:

- **Offline First** - Core gamification (stats, tasks, rewards, achievements, decay) works fully offline. The only network use is opt-in Todoist sync, which requires you to manually enter your own API token.
- **Local Data Only** - All data stored on-device using Room database. Todoist API tokens are stored in `EncryptedSharedPreferences` (AndroidX Security).
- **No Analytics** - Zero tracking, telemetry, or crash reporting
- **No Ads** - Completely ad-free
- **No Accounts** - No registration or cloud sync
- **Open Source** - Fully auditable code

## 🗺️ Roadmap

### ✅ Completed (v3.1.2)
- [x] Complete Kotlin/Compose rewrite
- [x] Liquid glass UI system
- [x] Hexagon radar chart (Simple/Glow styles)
- [x] Rank system with star lines (E → S)
- [x] Manual task creation with stat assignment
- [x] Rewards create/redeem/delete
- [x] Streak calendar with month display
- [x] Mood check-in (+2 WIS)
- [x] Settings with full reset
- [x] Todoist integration (sync, label→stat routing, token validation)
- [x] Stat decay system (midnight DecayWorker)
- [x] Rank-up animations (particles + badge)
- [x] Achievements system (built-in + custom + no-goal)
- [x] Per-stat detailed breakdown (weekly chart, top sources)
- [x] Collapsible stat bars with full stat names

### ✅ v4.0 (merged, build verified 2026-05-14)
- [x] AI Agent — Gemini chat (free tier `gemini-2.0-flash`), ephemeral transcript, persona + fresh player-state injected per send. Gated in Settings by an encrypted API key.
- [x] Real backdrop blur — Haze 1.7.2; real GPU blur on API 31+, auto-falls-back to a translucent scrim on older devices.
- [x] Push notifications — channels for sync results + auth failures; declines handled silently.
- [x] Home-screen widget — 4×2 read-only widget with rank/balance/streak/today; refreshes are push-driven (no polling).

### 🔭 Roadmap beyond v4.0
- [ ] Decay & rank-up notifications (channels already created, just need wiring)
- [ ] Widget configure activity for a few size variants
- [ ] AI Agent: persist conversations (table already exists, currently dormant)

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Quick Start
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make changes and test
4. Commit (`git commit -m 'Add amazing feature'`)
5. Push and open a Pull Request

## 📄 License

GNU General Public License v3.0 - see [LICENSE](LICENSE) for details.

---

<div align="center">

**Built with ❤️ for productivity enthusiasts and RPG lovers**

[⬆ Back to Top](#stat-up---rpg-gamification-for-real-life)

</div>
