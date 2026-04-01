# LLM Project Memory — Stat Up App

> Compressed memory for AI collaboration. Read this first every session.

## Identity
- **App**: Stat Up — anime-inspired RPG Status Window for real life
- **User**: King — Android developer, values premium UI, wants simple but effective gamification
- **Repo**: /home/king/Documents/Projects/Reward-Points
- **License**: GPL-3.0 | **Package**: com.rewardpoints.app

## Current State (as of 2026-04-01)
- **Status**: v3.1.2 (versionCode 10) — production ready
- **Stack**: Kotlin, Compose, Room, Ktor, Koin, DataStore, WorkManager
- **DB version**: 3 (migration 2→3 adds rewardPoints to titles table)
- **76 Kotlin files**, app builds and runs on Waydroid

## What Works
- ✅ Glass UI (cards, buttons, bottom bar, ambient background, no click rectangles)
- ✅ Status screen — player name + total pts (no rank title), hexagon chart, rank info popup on tap
- ✅ Manual task/mission creation with stat assignment
- ✅ Rewards create/redeem/delete, points card → History navigation
- ✅ History with streak calendar (full month view with navigation, color coded)
- ✅ Mood check-in (+2 WIS)
- ✅ Manual points addition with stat selection
- ✅ Settings with full reset (reinitializes stats + achievements), hexagon style
- ✅ Decay worker (midnight check for activity → streak/decay)
- ✅ Rank-up animations (particle effects + animated badge)
- ✅ Collapsible stat bars with full stat names
- ✅ Achievements system (built-in + custom create/delete, custom reward points, no-goal manual completion)
- ✅ Per-stat detailed breakdown screen (3x2 selector, weekly chart, top sources)
- ✅ Calendar with month navigation (green=done, red=missed, today highlight)
- ✅ Todoist sync — flexible JSON parsing, token validation, active tasks display
- ✅ Todoist completed tasks → points (with labels → stat routing, without labels → points only)
- ✅ App initialization: stats + achievements seeded in Application.onCreate()

## What's Remaining (v4.0)
- ❌ AI Agent (provider TBD — not locked to any provider)
- ❌ Real backdrop blur glass effect (bottom bar + cards)
- ❌ Push notifications for sync
- ❌ Widget support

## Points & Stats
- **Reward points**: p1=4, p2=3, p3=2, p4=1
- **Stats cap at 100**. Every **10 pts** = **+1 stat point**
- **Base stat**: 5 each (decay stops at base)
- **6 stats**: STR(#FF5252) INT(#448AFF) WIS(#AB47BC) DEX(#FFD740) CHA(#FF4081) VIT(#69F0AE)

## Rank System (Star Lines)
- **6 ranks**: E → D → C → B → A → S (lowest to highest)
- **Star Lines**: accumulate toward 5
- Each **streak day** = +1 star line
- Each **break day** = -1 star line
- **5 lines** = **RANK UP** (lines reset to 0)
- **Lines go below 0** = immediate **RANK DOWN** (lines reset to 5)
- At rank E, lines clamp at 0 (can't go below E)
- Visual: ★★★☆☆ shows 3/5 lines progress

## Architecture
MVVM: Screen→ViewModel→Repository→(Room|Ktor)
5 tabs: Status | Tasks | Rewards | Agent(placeholder) | Settings

## Todoist Integration
- **Base URL**: `https://api.todoist.com/api/v1`
- **Active tasks**: POST `$BASE_URL/sync` with `resource_types=["items"]` (Sync API, submitForm)
- **Completed tasks**: GET `$BASE_URL/tasks/completed/by_completion_date` (v1 REST)
- **Flexible parsing**: response parsed as JsonElement, handles `items`, `results`, or raw array
- **No tags → points only** (no stat allocated), **with labels → routed to stat via StatMapping**
- **Token validation**: `testConnection()` via Sync API user resource
- Background sync: TodoistSyncWorker every 15 min

## Background Workers
- **DecayWorker**: Runs at midnight, checks if earned points yesterday
  - Yes → recordSuccessfulDay() → streak+1, starLine+1
  - No → applyDecay() → stats-1 (min base 5), starLine-1
- **TodoistSyncWorker**: Every 15 minutes, syncs completed tasks from Todoist

## Design Tokens
- BG: #0A0A0F (all Material3 surface colors also set to this — no dark rectangles)
- Glass: white@10% fill, white@18% border, 24dp radius (no shadow/elevation — causes dark rect artifacts)
- Accent: #7C4DFF | Gold: #FFD740 | Success: #69F0AE | Error: #FF5252
- Text: #F0F0F5 primary, #A0A0B8 secondary
- Font: Inter

## Key Files
- `ui/screen/status/StatusScreen.kt` - Main home screen
- `ui/screen/tasks/TasksScreen.kt` - Mission management + Todoist section
- `ui/screen/tasks/TasksViewModel.kt` - Manages missions + Todoist tasks/sync
- `ui/screen/stats/StatsScreen.kt` - Detailed stat breakdown
- `ui/screen/achievements/AchievementsScreen.kt` - Achievement badges
- `ui/screen/settings/SettingsScreen.kt` - Todoist connection + preferences
- `ui/screen/settings/SettingsViewModel.kt` - Token validation + preferences
- `ui/components/rpg/StatusWindow.kt` - Character sheet with collapsible stats
- `ui/components/rpg/HexagonRadarChart.kt` - Stats visualization (Simple/Glow)
- `rpg/DecayEngine.kt` - Stat decay & streak logic
- `sync/TodoistApi.kt` - Todoist API client (flexible JSON parsing)
- `sync/TodoistSyncManager.kt` - Sync logic + active tasks fetch
- `StatUpApp.kt` - App initialization (stats + achievements seeding)
- `di/AppModule.kt` - All Koin DI wiring (includes 15s HTTP timeout)

## Testing
Waydroid multi-window mode: `waydroid prop set persist.waydroid.multi_windows true`
Build debug: `./gradlew assembleDebug`
Build release: `./gradlew assembleRelease`
Install: `waydroid app install app/build/outputs/apk/release/app-release.apk`

## Rules
- Dark-only, minSdk 26, targetSdk 35
- Keep things simple (no multipliers, no label bonuses)
- App works standalone without Todoist
- AI Agent deferred to v4.0
