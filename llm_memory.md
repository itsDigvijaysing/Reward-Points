# LLM Project Memory — Stat Up App

> Compressed memory for AI collaboration. Read this first every session.

## Identity
- **App**: Stat Up — anime-inspired RPG Status Window for real life
- **User**: King — Android developer, values premium UI, wants simple but effective gamification
- **Repo**: /home/king/Documents/Projects/Stat-Up
- **License**: GPL-3.0 | **Package**: com.rewardpoints.app (historical name; matches DB/DataStore identity)

## Current State (as of 2026-06-12)
- **Status**: v3.1.3 (versionCode 11). Pre-launch hardening + polish batches merged and verified — debug APK + release build clean under R8; **36 JVM unit tests pass**; instrumented `MigrationTest` (2) + `ExampleInstrumentedTest` pass on Waydroid. CI (`.github/workflows/ci.yml`) runs tests/lint/build automatically. **No DB schema change** — `DB_VERSION` stays 5.
- **Stack**: Kotlin 2.1.20, Compose BOM 2025.03.01, Room 2.7.1, Ktor 3.1.2, Koin 3.5.6, DataStore 1.1.4, WorkManager 2.10.1, Haze 1.7.2 (backdrop blur), AndroidX Security 1.1.0 (encrypted prefs — stable release deprecates the `EncryptedSharedPreferences` API; still functional, kept until a storage migration is planned).
- **Android targets**: minSdk 26, **targetSdk 36** (bumped 2026-06-10 — Play requires 36 for updates after Aug 31, 2026), **compileSdk 36**.
- **Local build env (2026-05-14)**: JDK 17.0.18 GA (apt `openjdk-17-jdk`, alongside an unusable JDK 25 EA), Android SDK at `~/Android/Sdk` with `cmdline-tools/latest`, platforms `android-35` + `android-36`, build-tools `35.0.0` + `36.0.0`, platform-tools r37. `local.properties` points `sdk.dir` at `~/Android/Sdk`.
- **DB version**: 5 (`DB_VERSION` in `AppDatabase.kt`; migrations 1→2 no-op, 2→3 adds `titles.rewardPoints`, 3→4 dedupes `transactions.externalId` + unique index, 4→5 adds `player_stats.streakShields`). Builder uses `fallbackToDestructiveMigrationOnDowngrade(false)` — a forgotten upgrade migration **throws** instead of wiping user data; instrumented `MigrationTest` validates every migration path against `app/schemas/*.json`. The `AiConversationEntity` table stays dormant — agent transcript lives in-memory only by design (user opted out of conversation persistence).

## 3.1.3 Changelog (2026-06-12) — pre-launch hardening + polish
- **Data safety**: daily decay is now atomic — `DecayEngine` wraps its `player_stats` read-modify-write in a `Transactor`/`RoomTransactor` transaction and sets the idempotency marker before side-effects; fixes "concurrent earn/buy-shield clobbered by decay" + double-apply-on-retry. Uses `DecayStatsStore`/`DecayDayStore` ports, JVM-tested (`DecayEngineTest`).
- **MissionRepository** extracted; daily-mission reset runs from `DecayWorker` at midnight (once/day via `lastMissionResetDay`), not only on Tasks-tab resume.
- **Achievement checks** hoisted out of the Todoist per-task loop (guarded `runCatching`) → no false "sync failed", far fewer transactions.
- **MigrationTest** gained a data-survival case; **CI + lint baseline** added; `network_security_config` wired; Gemini key sent in header; password IME on secret fields; unused Coil removed.
- **New icon** (neon hexagon) on launcher + splash; old icon vectors + `ICONS/` source + 19 unused drawables/font archived in `OLD_ASSET/` (safe to delete).
- **New features**: 3-step onboarding (gated on `onboardingComplete`), edit-a-reward, re-engagement notifications (rank-down/shield-saved), real haptics (was a dead toggle) + fade-through screen transitions.
- Confirmed already-built: mood once/day, custom reward cost (≤999,999).
- **Publishing**: not yet on Play. Release signing still falls back to the debug keystore until `keystore.properties` + an upload key exist.

## What Works
- ✅ Glass UI (cards, buttons, bottom bar, ambient background, no click rectangles)
- ✅ Status screen — player name + total pts (no rank title), hexagon chart, rank info popup on tap
- ✅ Manual task/mission creation with stat assignment
- ✅ Rewards create/redeem/delete, points card → History navigation (custom cost input + preset chips up to 500)
- ✅ History with streak calendar (full month view with navigation, color coded)
- ✅ Mood check-in (+2 WIS, limited to once per local day)
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
- ✅ App initialization: stats + achievements seeded in Application.onCreate() (SupervisorJob + CoroutineExceptionHandler)
- ✅ Adaptive app icon (hexagon vector with 6 stat-color vertex dots + upward chevron)
- ✅ Daily Quote card on Status (sources: offline pack default / Animechan anime / ZenQuotes motivation / mixed; once-a-day cache; offline fallback; Settings selector)
- ✅ Streak Freeze Shield (30 pts, max 3) — idle day consumes a shield instead of decaying stats/streak/star-lines; 🛡️ card + dialog on Status (DB v5)
- ✅ Equippable achievement titles under the player name (DataStore pref, picker dialog on Status)
- ✅ Todoist connection status is reactive (connect in Settings → sync UI appears immediately on Tasks)
- ✅ Bottom bar no longer shifts on tab tap (fixed-size indicator slot + constant label weight)

## v4.0 (status as of 2026-05-14)
- ✅ AI Agent — Gemini-backed (`gemini-2.5-flash`, free tier; bumped from the retired `gemini-2.0-flash` on 2026-05-17). System instruction = persona + fresh player-state snapshot per send (via the narrow `PlayerStateProvider` interface). Ephemeral chat (no DB persistence). Settings dialog stores the API key encrypted via SecretStorage.
- ✅ Real backdrop blur — Haze 1.7.2; single `HazeState` provided via `LocalHazeState` in `AppNavigation`; cards/bottom bar are effects, NavHost is the source. Auto-falls-back to a scrim on < API 31.
- ✅ Push notifications — `Notifier` channels (sync + reminders), POST_NOTIFICATIONS permission requested in MainActivity, wired into TodoistSyncWorker (sync result + auth failure). Decay/rank notifications still TODO.
- ✅ Widget — 4×2 read-only home-screen widget (rank, balance, streak, today). Registered in AndroidManifest. `StatsWidgetUpdater` pushes refreshes from PointsRepository (earn/redeem), DecayEngine (daily tick), and StatUpApp.onCreate (app start).

## Hardening pass (2026-06)
- Rank transitions extracted to pure `rpg/RankLogic.kt` (full truth table in `RankLogicTest`); `DecayEngine` delegates and fails fast on impossible transitions
- `RankUpNotifier` (Channel-backed, exactly-once) replaces the SharedFlow rank-up event — rank-ups while Status tab is off-screen are no longer dropped
- `SecretStorage.openWithRecovery`: transient Keystore failure → plain retry (no wipe); persistent failure → wipe once + recreate. `secret_prefs.xml` excluded from auto-backup/device-transfer (master key is device-bound, blobs undecryptable after restore)
- Stat accumulator freezes at `MAX_STAT` (post-cap earns no longer silently leak progress)
- `HistoryViewModel`/`StatsViewModel` aggregation off-Main; lost-update race on filter taps fixed (atomic `_uiState.update {}` everywhere)
- Rewards snackbar keyed on a monotonic redemption id (same-reward double-redeem re-fires)
- `Notifier.notify()` is the single permission-gated funnel (`@SuppressLint("MissingPermission")` with the runtime guard — lint can't see through the helper)
- Release builds without `keystore.properties` print a loud debug-keystore warning

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
MVVM: Screen→ViewModel→Repository→(Room|Ktor|EncryptedSharedPreferences|Gemini-via-Ktor)
5 tabs: Status | Tasks | Rewards | **Agent (Gemini chat)** | Settings
Hidden routes (deep nav, no bottom-bar entry): History, FullStats, Achievements.

## Todoist Integration
- **Base URL**: `https://api.todoist.com/api/v1`
- **Active tasks**: POST `$BASE_URL/sync` with `resource_types=["items"]` (Sync API, submitForm)
- **Completed tasks**: GET `$BASE_URL/tasks/completed?limit=N&annotate_items=true` (Sync v1 REST)
- **Parsing**: response parsed as JsonElement, requires object with `items` array
- **No tags → points only** (no stat allocated), **with labels → routed to stat via StatMapping (cached per sync run)**
- **Token validation**: `testConnection()` via Sync API user resource
- **Auth failures (401/403)** → `TodoistAuthException` → `SyncResult.AuthFailed` → worker returns Success (no retry)
- Background sync: TodoistSyncWorker every 15 min
- Token stored encrypted in `SecretStorage` (AES-256-GCM via AndroidX Security)

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
- `ui/screen/settings/SettingsScreen.kt` - Todoist + Gemini connection dialogs + preferences
- `ui/screen/settings/SettingsViewModel.kt` - Token validation + preferences
- `ui/screen/agent/AgentScreen.kt` - Gemini chat surface (gated on `isConfigured`)
- `ui/screen/agent/AgentViewModel.kt` - In-memory transcript + send-flow + typed-error handling
- `ai/GeminiAgentApi.kt` - Ktor client to `generativelanguage.googleapis.com` (gemini-2.5-flash)
- `ai/AgentRepository.kt` + `ai/AgentContextBuilder.kt` - System instruction = persona + fresh player state (via `PlayerStateProvider`)
- `ui/components/rpg/StatusWindow.kt` - Character sheet with collapsible stats
- `ui/components/rpg/HexagonRadarChart.kt` - Stats visualization (Simple/Glow)
- `ui/components/glass/HazeWiring.kt` - `LocalHazeState` + `hazeSourceOrFallback` / `hazeEffectOrFallback`
- `rpg/DecayEngine.kt` - Stat decay & streak logic (calls `StatsWidgetUpdater.refresh()`); rank transitions delegated to pure `rpg/RankLogic.kt`
- `sync/TodoistApi.kt` - Todoist API client (flexible JSON parsing)
- `sync/TodoistSyncManager.kt` - Sync logic + active tasks fetch
- `data/local/datastore/SecretStorage.kt` - AES-256-GCM encrypted prefs for Todoist + Gemini keys
- `notifications/Notifier.kt` - Notification channels + sync result + auth-failure flows
- `widget/StatsWidgetProvider.kt` + `widget/StatsWidgetUpdater.kt` - Home-screen widget + push-refresher
- `StatUpApp.kt` - App initialization (stats + achievements seeding, Notifier channels, widget refresh)
- `di/AppModule.kt` - All Koin DI wiring (15s HTTP timeout, Notifier + StatsWidgetUpdater singletons)

## Build / Test / Install
- Prereqs: JDK 17 (Gradle 8.13 won't run on JDK 24+), Android SDK with platforms `android-35`+`android-36` and build-tools 35.0.0+36.0.0 (env: `ANDROID_HOME=~/Android/Sdk`, also put `local.properties` `sdk.dir=...` in the project root).
- Build debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Build release APK: `./gradlew assembleRelease` (falls back to debug signing if `keystore.properties` is absent)
- Unit tests: `./gradlew :app:testDebugUnitTest` (current: 31/31 passing — `RankLogicTest`, `StatsEngineTest`, `RankCalculatorTest`, `PlayerStatsTest`, `SecretStorageRecoveryTest`, `RankUpNotifierTest`, `AgentContextBuilderTest`, `HistoryViewModelTest`, `QuoteRepositoryTest`)
- Instrumented migration guard: `./gradlew connectedDebugAndroidTest` (`MigrationTest` — run after every DB version bump)
- Waydroid multi-window: `waydroid prop set persist.waydroid.multi_windows true && waydroid session stop && waydroid session start` (no sudo needed)
- Waydroid install/launch: `waydroid app install app/build/outputs/apk/debug/app-debug.apk && waydroid app launch com.rewardpoints.app.debug` (note the `.debug` applicationId suffix on debug variant).

## Rules
- Dark-only, minSdk 26, targetSdk 36, compileSdk 36
- Keep things simple (no multipliers, no label bonuses)
- App works standalone without Todoist and without Gemini
- v4.0 features (AI Agent, Widget, Notifications, Backdrop blur) are complete and verified building
