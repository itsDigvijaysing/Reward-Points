# LLM Project Memory — Reward Points App

> Compressed memory for AI collaboration. Read this first every session.

## Identity
- **App**: Reward Points — anime-inspired RPG Status Window for real life
- **User**: King — Android developer, values premium UI, wants simple but effective gamification
- **Repo**: /home/king/Documents/Projects/Reward-Points
- **License**: GPL-3.0 | **Package**: com.rewardpoints.app

## Current State (as of 2026-03-31)
- **Status**: v3.0.1 (versionCode 8) — Phase 4 (Todoist integration) IN PROGRESS
- **Stack**: Kotlin, Compose, Room, Ktor, Koin, DataStore, WorkManager
- **~76 Kotlin files**, app builds and runs on Waydroid

## What Works
- ✅ Glass UI (cards, buttons, bottom bar, ambient background)
- ✅ Status screen with StatusWindow, hexagon chart (Simple/Glow styles), rank badge
- ✅ Manual task/mission creation with stat assignment
- ✅ Rewards create/redeem/delete
- ✅ History with streak calendar (28-day view)
- ✅ Mood check-in (+2 WIS)
- ✅ Manual points addition with stat selection
- ✅ Settings with full reset, hexagon style preference
- ✅ Decay worker (midnight check for activity → streak/decay)
- ✅ Rank-up animations (particle effects + animated badge)
- ✅ Collapsible stat bars with full stat names
- ✅ Achievements system (shows reward points, simplified UI)
- ✅ Per-stat detailed breakdown screen (3x2 selector, weekly chart, top sources)
- ✅ Full Reset in Settings properly reinitializes player stats (base 5)
- ✅ **Todoist token validation** on connect (validates API key before saving)
- ✅ **Todoist active tasks display** (collapsible list with priority badges, due dates, labels)
- ✅ **Sync status display** (last sync time, sync log)
- ✅ **Manual sync button** with loading indicator

## What's NOT Working / In Progress
- ⚠️ **Completed tasks sync** — API response structure mismatch (items field not found)
- ⚠️ **Points from Todoist tasks** — Not awarding because completed sync fails
- ❌ Click points in Rewards → open History (not implemented)

## What's Remaining (v4.0)
- ❌ Push notifications for sync
- ❌ AI Agent (Gemini integration)

## Points & Stats
- **Reward points**: p1=4, p2=3, p3=2, p4=1
- **Stats cap at 100**. Every **10 pts** = **+1 stat point**
- **Base stat**: 5 each (decay stops at base)
- **6 stats**: STR(#FF5252) INT(#448AFF) WIS(#AB47BC) DEX(#FFD740) CHA(#FF4081) VIT(#69F0AE)

## Rank System (Star Lines)
- **6 ranks**: E → D → C → B → A → S (lowest to highest)
- **Star Lines**: Think of making a ⭐ with 5 lines
- Each **streak day** = +1 line
- Each **break day** = -1 line
- **5 lines** = ⭐ complete = **RANK UP** (lines reset to 0)
- **Lines go negative** (below 0) = **RANK DOWN** (lines reset to 5)
- Visual: ★★★☆☆ shows 3/5 lines progress
- Lowest rank is E (can't go below)

## Architecture
MVVM: Screen→ViewModel→Repository→(Room|Ktor)
5 tabs: Status | Tasks | Rewards | Agent(placeholder) | Settings

## Todoist Integration

### API Details
- **IMPORTANT**: Todoist REST v2 API (`/rest/v2/tasks`) is DEPRECATED — returns 410 Gone
- **Must use Sync API v1**: `https://api.todoist.com/api/v1/sync`
- Sync API requires **POST with form data** (submitForm), not GET
- Active items use field `checked` (not `is_completed`)
- Completed tasks endpoint: `/api/v1/sync/completed/get_all`

### Current Issue
- `/api/v1/sync/completed/get_all` returns different JSON structure than expected
- Error: `Field 'items' is required for type CompletedTasksResponse`
- Active tasks (105) load correctly, completed tasks sync fails

### Components
- **SettingsViewModel.validateAndConnectTodoist()** - Validates token via `testConnection()` before saving
- **TodoistConnectionDialog** - Shows validation status, error messages on invalid tokens
- **TasksScreen** - Has collapsible `TodoistTasksSection` showing active tasks
- **TodoistTaskCard** - Shows priority badge (P1-P4), content, due date, labels, points value
- **Manual sync** - Attempts to sync completed tasks, updates sync log, refreshes active tasks
- **Background sync** - TodoistSyncWorker runs every 15 min (but completed sync currently fails)

## Background Workers
- **DecayWorker**: Runs at midnight, checks if earned points yesterday
  - Yes → recordSuccessfulDay() → streak+1, starLine+1
  - No → applyDecay() → stats-1 (min base 5), starLine-1
- **TodoistSyncWorker**: Every 15 minutes, syncs completed tasks from Todoist

## Design Tokens
- BG: #0A0A0F, Surface: #12121A
- Glass: white@10% fill, white@18% border, 32dp blur, 24dp radius
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
- `sync/TodoistApi.kt` - Todoist API client (Sync API v1)
- `sync/TodoistSyncManager.kt` - Sync logic + active tasks fetch
- `di/AppModule.kt` - All Koin DI wiring (includes 15s HTTP timeout)

## Next Steps to Fix Todoist
1. Debug actual JSON response from `/api/v1/sync/completed/get_all`
2. Update `CompletedTasksResponse` data class to match real response structure
3. Test completed tasks sync and points awarding
4. Add click handler: Points in Rewards → navigate to History

## Testing
Waydroid multi-window mode: `waydroid prop set persist.waydroid.multi_windows true`
Build: `./gradlew assembleDebug`
Install: `waydroid app install app/build/outputs/apk/debug/app-debug.apk`

## Rules
- Dark-only, minSdk 26, targetSdk 35
- Keep things simple (no multipliers, no label bonuses)
- App works standalone without Todoist
- AI Agent deferred to v4.0
