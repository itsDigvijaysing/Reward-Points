# Reward Points - RPG Gamification for Real Life

<div align="center">

![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-GPL--3.0-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-green.svg)

**An anime-inspired RPG Status Window app that gamifies your daily tasks and achievements with a premium liquid glass UI.**

[Features](#-features) • [Screenshots](#-screenshots) • [Building](#-building) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

## 📱 Overview

Reward Points transforms your daily life into an RPG experience. Complete tasks to earn points, level up your character stats, maintain streaks to rank up, and redeem rewards you create. All with a beautiful liquid glass aesthetic inspired by iOS and anime "Status Window" interfaces.

### Why Reward Points?

- 🎮 **Gamified Productivity** - Turn boring tasks into exciting quests
- ✨ **Premium UI** - Liquid glass effects, hexagon stat charts, animated rank badges
- 🔒 **Privacy First** - 100% offline, no accounts, no tracking
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
- **Todoist Integration** - Connect your Todoist account to sync tasks (coming soon)
- **Priority Points** - P1=4pts, P2=3pts, P3=2pts, P4=1pt

### 🎁 **Rewards System**
- **Custom Rewards** - Create personal rewards (e.g., "Watch an episode" = 50pts)
- **Point Redemption** - Spend earned points on your rewards
- **Transaction History** - Track all earnings and spendings

### 📊 **Progress Tracking**
- **Streak Calendar** - 28-day view showing your activity patterns
- **Mood Check-in** - Daily mood logging with +2 WIS bonus
- **History Log** - Complete transaction history with filtering

### 🎨 **Premium UI**
- **Liquid Glass Design** - Translucent cards, glowing borders, blur effects
- **Dark Theme** - Eye-friendly dark interface
- **Smooth Animations** - AnimatedVisibility, progress animations, transitions
- **RPG Aesthetic** - Status window design inspired by Solo Leveling and other anime

## 🏗️ Building

### Prerequisites
- JDK 17 or newer
- Android SDK with API level 26+
- Gradle 8.x (wrapper included)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/yourusername/Reward-Points.git
cd Reward-Points

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
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
- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **DI**: Koin
- **Database**: Room
- **Preferences**: DataStore
- **Network**: Ktor (for Todoist API)

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

- **100% Offline** - No internet required for core functionality
- **Local Data Only** - All data stored on-device using Room database
- **No Analytics** - Zero tracking, telemetry, or crash reporting
- **No Ads** - Completely ad-free
- **No Accounts** - No registration or cloud sync
- **Open Source** - Fully auditable code

## 🗺️ Roadmap

### ✅ Completed (v3.0)
- [x] Complete Kotlin/Compose rewrite
- [x] Liquid glass UI system
- [x] Hexagon radar chart with labels
- [x] Rank system with star lines progress
- [x] Manual task creation
- [x] Rewards create/redeem
- [x] Streak calendar
- [x] Mood check-in
- [x] Settings with full reset

### 🚧 In Progress
- [ ] Todoist API integration
- [ ] Stat decay system (daily decay at midnight)
- [ ] Rank-up animations

### 📋 Planned
- [ ] Achievements/Titles system
- [ ] Per-stat detailed breakdown
- [ ] AI Agent for task suggestions
- [ ] Widget support

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

[⬆ Back to Top](#reward-points---rpg-gamification-for-real-life)

</div>
