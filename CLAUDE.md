# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Snapshot

**Stat Up** — an Android RPG-themed productivity app. Users complete tasks (manual or Todoist-synced) to earn points, which level up six character stats (STR/INT/WIS/DEX/CHA/VIT), accumulate streak "star lines" that drive a rank ladder (E→D→C→B→A→S), and can be spent on user-defined rewards. The app is offline-first; Todoist sync is opt-in.

- Package: `com.rewardpoints.app` (historical — debug variant adds `.debug` suffix). Do **not** rename the package without writing a migration: the Room DB filename `reward_points_db`, DataStore name `user_preferences`, and the encrypted prefs file `secret_prefs` are tied to this identity.
- versionCode 10 / versionName 3.1.2.
- minSdk 26, targetSdk 35, **compileSdk 36** (bumped 2026-05-14 — transitive `androidx.activity:1.12.2` + `navigationevent:1.0.1` require API 36 to compile against), JDK 17, AGP 8.11.2, Gradle 8.13 (wrapper), Kotlin 2.1.20, Compose BOM 2025.03.01, Room 2.7.1, Ktor 3.1.2, Koin 3.5.6, AndroidX Security 1.1.0-alpha06, WorkManager 2.10.1, **AndroidX Core SplashScreen 1.0.1**, **AndroidX Lifecycle-Runtime-Compose 2.9.0**, **Haze 1.7.2** (backdrop blur).

## Common Commands

```bash
# Build
./gradlew assembleDebug                 # debug APK → app/build/outputs/apk/debug/
./gradlew assembleRelease               # release APK (signing — see Gotchas)
./gradlew clean

# Tests
./gradlew test                          # unit tests under app/src/test (Kotlin, JUnit 4)
./gradlew connectedAndroidTest          # instrumented tests under app/src/androidTest
./gradlew :app:testDebugUnitTest --tests "com.rewardpoints.app.rpg.RankCalculatorTest"

# Lint
./gradlew lintDebug                     # report at app/build/reports/lint-results-debug.html

# Install / launch on Waydroid
waydroid app install app/build/outputs/apk/debug/app-debug.apk
waydroid app launch com.rewardpoints.app.debug
```

**Waydroid windowed-phone setup (verified 2026-05-14)**: by default Waydroid opens a full-screen Android home — for app development we want each app in its own mobile-portrait desktop window. One-time setup (props persist across reboots in `/var/lib/waydroid/waydroid.cfg`):

```bash
waydroid prop set persist.waydroid.multi_windows true   # each app gets its own window
waydroid prop set persist.waydroid.width 720            # 9:16-ish phone portrait
waydroid prop set persist.waydroid.height 1024
waydroid session stop && waydroid session start         # apply (no sudo needed)
```

Verify: `waydroid prop get persist.waydroid.multi_windows`. Resize later by changing the props and `session stop && session start` — sudo is **not** required for prop changes or session restart. Revert to full Android UI with `multi_windows=false` + `waydroid show-full-ui`.

KSP generates Room code and exports schemas to `app/schemas/`. Bumping `AppDatabase.version` requires either a `Migration` (see `AppDatabase.MIGRATION_2_3` / `MIGRATION_3_4`) or accepting that `fallbackToDestructiveMigration(false)` will throw on mismatch. Current DB version: **4**. Registered migrations: 1→2 (no-op; v1 and v2 have identical identity hashes), 2→3 (adds `titles.rewardPoints`), 3→4 (dedupes any pre-existing duplicate `externalId` rows in `transactions` then adds a unique index).

**Build env requirements**: gradle 8.13 requires **JDK 17** (not 24+). Android SDK needs platforms `android-35`+`android-36` and build-tools `35.0.0`+`36.0.0` installed via `sdkmanager`. Put `sdk.dir=...` in a `local.properties` at the repo root, or export `ANDROID_HOME`. Last verified clean build: 2026-05-14 (`app-debug.apk` ~25 MB; 13/13 unit tests pass).

**Release signing**: copy `keystore.properties.template` to `keystore.properties` (gitignored) and fill in real values before producing Play-ready APKs. If `keystore.properties` is absent, release builds fall back to the debug keystore (loaded conditionally in `app/build.gradle`).

## Architecture (Big Picture)

MVVM with explicit layers; Koin wires everything in `di/AppModule.kt`. Read that file first when adding a new screen — every new ViewModel, repository, or engine must be registered there.

```
UI (Compose)              ui/screen/*, ui/components/*
  ↕ collectAsState
ViewModel                 ui/screen/<feature>/<Feature>ViewModel.kt
  ↕ suspend / Flow
Repository                data/repository/*
  ↕
Room DAO + DataStore      data/local/db/dao/*, data/local/datastore/UserPreferences.kt
SecretStorage             data/local/datastore/SecretStorage.kt  (AES-256-GCM token store)
  +
RPG engines               rpg/StatsEngine, rpg/DecayEngine, rpg/RankCalculator, rpg/AchievementTracker
Background workers        sync/DecayWorker, sync/TodoistSyncWorker (both KoinComponent)
Network                   sync/TodoistApi (Ktor)
```

**Navigation** (`ui/navigation/AppNavigation.kt`): five bottom-bar destinations (Status / Tasks / Rewards / Agent / Settings) plus three hidden routes reachable by deep navigation (History, FullStats, Achievements). `Routes.CREATE_REWARD` and `Routes.EDIT_REWARD` are defined but currently unused — reward creation is dialog-driven from `RewardsScreen`.

**Application bootstrap** (`StatUpApp.onCreate`): starts Koin, then launches four init coroutines on a `SupervisorJob + Dispatchers.IO + CoroutineExceptionHandler` scope. The exception handler is required — `SupervisorJob` alone keeps siblings alive but does **not** swallow exceptions (uncaught throwables would still hit the global thread handler and crash app startup). The four init tasks: seed `player_stats` row id=1, seed default achievements (`Achievements.ALL`), seed six STR/INT/WIS/DEX/CHA/VIT label→stat mappings, and call `UserPreferences.loadSecretsIfNeeded()` to populate the encrypted token cache. Both periodic workers are scheduled here unconditionally.

## RPG Mechanics (Invariants — change cautiously)

These constants live in `domain/model/PlayerStats.kt` and `domain/model/Rank.kt`:

- Stat range: `[BASE_STAT=5, MAX_STAT=100]`. Decay clamps at base; gain coerces at max.
- Points→stat conversion: `POINTS_PER_STAT = 10`. `PointsRepository.updateStatAccumulator` accumulates into per-stat `*PointsAcc` columns; every 10 points = +1 stat, remainder kept in the accumulator.
- Task points by Todoist API priority (note: Todoist inverts — `priority=4` is "p1 urgent"): p1→4, p2→3, p3→2, p4→1. **Single source of truth**: `StatsEngine.calculateTaskPoints`. `TodoistSyncManager` calls it statically.
- Mood check-in: `MOOD_POINTS = 2` → WIS. **Capped at one check-in per local day** (enforced in `StatusViewModel.checkInMood` + UI dims the Mood quick-action card via `hasCheckedInMoodToday`).
- Rank ladder (asymmetric star-line model — single counter, intentionally lop-sided):
  - `rankUpStreakCounter` is the source of truth. Each active day `+1`, each idle day `-1`.
  - **Up:** counter `>= +STREAK_DAYS_TO_RANK_UP` (5) → rank-up; counter resets to **0** at the new rank. Going up takes 5 hard days of work, with no carry-over.
  - **Down:** counter `< 0` (drops to `-1`) → rank-down immediately, counter resets to **+STREAK_DAYS_TO_RANK_UP** (5) at the lower rank. The `+5` is intentional — a "near-miss" cushion so a single active day at the lower rank (`+5 → +6 ≥ 5`) bounces straight back up to where you were.
  - **No grace period after promotion**: just-promoted at counter 0 + 1 idle day = counter -1 = immediate demotion. Slack off the day after you ranked up and you fall straight back. (Walkthrough: at D with counter `+1`, 1 idle → 0, 2 idle → -1 → demote to E with counter `+5`.)
  - At rank E (the floor) the counter clamps at 0 — no negative accumulation, so the first active day always starts climbing toward D.
  - Pure transition logic: [rpg/RankLogic.kt](app/src/main/java/com/rewardpoints/app/rpg/RankLogic.kt). Side-effecting caller: [DecayEngine](app/src/main/java/com/rewardpoints/app/rpg/DecayEngine.kt). Threshold/floor tests: [RankLogicTest](app/src/test/java/com/rewardpoints/app/rpg/RankLogicTest.kt).
  - `rankDownBreakCounter` column exists in `player_stats` but is no longer read by any live code path. Column retained to avoid a DB migration.

## Background Work

Both workers are `CoroutineWorker` + `KoinComponent` — they pull dependencies via `by inject()` at runtime, so **do not register a custom `WorkerFactory`** (the default no-arg constructor is what `WorkManager` needs). If a worker constructor ever takes parameters, a `WorkManager.Configuration.Provider` setup becomes mandatory.

- `DecayWorker` (`sync/DecayWorker.kt`): periodic 24h, initial delay aligned to next midnight. Calls `DecayEngine.applyDailyDecay()`, which **snapshots today's midnight once** and uses `[today−24h, today)` as the "yesterday" window. Robust to WorkManager firing late (e.g., 02:15). **Idempotent within a local day**: gated on `UserPreferences.lastDecayDay` so retries / debug `runNow()` / overlapping schedules can't double-apply decay or skip-a-day. Returns `DailyDecayResult.AlreadyApplied` when called twice in the same local day.
- `TodoistSyncWorker` (`sync/TodoistSyncWorker.kt`): periodic 15 min, `NetworkType.CONNECTED`. Dedupes by `TransactionEntity.externalId` — enforced at the DB level via `UNIQUE INDEX index_transactions_externalId` (added in MIGRATION_3_4) so concurrent sync runs cannot double-award. The dedup path uses `PointsRepository.tryEarnExternalPoints` which calls `TransactionDao.insertIgnore` (returns -1 on conflict) and skips the stat/totals update on miss. First sync fetches 200 completed tasks, subsequent syncs fetch 30.
  - **Retry semantics:** transient errors → `Result.retry()`; **auth failures (401/403)** → `Result.success()` (no retry — token invalid, user must re-enter); `NotConnected` (no token) → `Result.success()`.

## Todoist Integration

- Base URL: `https://api.todoist.com/api/v1` (Sync v1).
- Active tasks: `POST /sync` with form `resource_types=["items"]`, parsed as `SyncResponse`.
- Completed tasks: `GET /tasks/completed?limit=N&annotate_items=true`, parsed manually as `JsonElement` (response shape varies — code only accepts a `JsonObject` containing `items`).
- Token validation: `POST /sync` with `resource_types=["user"]`.
- **Auth handling**: `TodoistApi` returns `Result.failure(TodoistAuthException(...))` on 401/403. `TodoistSyncManager` maps this to `SyncResult.AuthFailed` so the worker can skip retry.
- **Label routing performance**: `TodoistSyncManager` loads stat mappings **once per sync run** via `PointsRepository.loadStatMappings()` and reuses via `routeToStatCached()` — avoids N DB round trips for N tasks.
- HTTP client (in `di/AppModule.kt`): 10s connect / 15s request+socket, `expectSuccess = false` (we check status codes manually). The Ktor `Logging` plugin was removed — no logging overhead.

## Secrets & Preferences

- Plain preferences live in DataStore (`user_preferences`).
- **Secrets** (Todoist token, future Gemini API key) live in `EncryptedSharedPreferences` ("secret_prefs", AES-256-GCM, AndroidX Security 1.1.0-alpha06) via `SecretStorage`.
- `UserPreferences` exposes secrets as `Flow<String?>` (`todoistToken`, `geminiApiKey`) backed by in-memory `MutableStateFlow`s. The flows start as `null`; `loadSecretsIfNeeded()` (called at app init) populates them.
- **Suspend callers should prefer** `userPreferences.getTodoistToken()` / `getGeminiApiKey()` — these guarantee the load has completed before returning.
- **Legacy migration**: `migrateLegacySecret()` reads the old plain DataStore key (if present), copies its value into `SecretStorage`, and removes it from DataStore. Happens automatically on first `loadSecretsIfNeeded()`.

## Data Model Notes

- `AppDatabase` declares **10 entities** and exposes **8 DAOs**:
  - Live entities (with DAO + active consumers): `RewardEntity`, `TransactionEntity`, `MissionEntity`, `PlayerStatsEntity`, `StatMappingEntity`, `DecayLogEntity`, `TitleEntity` (achievements).
  - **Dormant** (table created, no DAO, no consumer): `TodoistTaskEntity` (table `todoist_tasks`) and `AiConversationEntity` (table `ai_conversations`).
  - **Half-dormant** (DAO exists but no class injects it): `AiMemoryEntity` ↔ `AiMemoryDao`. Registered in Koin (`get<AppDatabase>().aiMemoryDao()`) for the planned v4.0 AI Agent.
  - Dropping any of these requires a DB version bump + migration. They're kept as v4.0 staging.
- **Atomicity**: every multi-write earn/redeem flow is wrapped in `database.withTransaction { … }`. `PointsRepository.addPoints` (insert + totals + stat accumulator) is atomic. `RewardRepository.redeemReward(reward)` re-reads the live balance via SQL inside its transaction (the old ViewModel-supplied balance was racy) and routes through `PointsRepository.redeemPoints` so the home-screen widget refreshes for free.
- **Todoist dedup**: `transactions.externalId` carries a `UNIQUE INDEX` (since v4). `PointsRepository.tryEarnExternalPoints(externalId, …)` uses `OnConflictStrategy.IGNORE` semantics and only awards points/stats when the insert actually lands (`id != -1`). Concurrent sync runs can't double-award.
- `player_stats` is a singleton row (`id = 1`). All updates target that row.
- `TransactionEntity.externalId` is the dedup key for Todoist sync. Manual transactions leave it null.
- **Balance** is computed via SQL in `TransactionDao.getBalance()` — a `Flow<Int>` from a single-pass `SUM(CASE WHEN type='EARN' THEN points WHEN type='REDEEM' THEN -points ELSE 0 END)` against `transactions`. Replaces the older in-memory `filter+sum` derivation. Single table scan; Room's invalidation tracker correctly observes the `transactions` table via the explicit `FROM transactions`.
- **Today's mood check** uses `TransactionDao.countBySourceInRange(MOOD, todayStart, todayEnd)` returning `Flow<Int>` — UI reacts immediately when the daily mood transaction is inserted.

## UI / Theming Conventions

- Dark-only. All Material 3 surface colors and `BackgroundBase` are forced to `#0A0A0F` in `ui/theme/Theme.kt`.
- Glass primitives in `ui/components/glass/` use `GlassTokens` (10% fill, 18% border, 24dp radius, **no** elevation — adding `shadow` causes dark-rectangle artifacts).
- `GlassCard.onClick` is nullable; pass `null` to disable the clickable modifier (no press animation). `QuickActionCard(enabled = false)` uses this pattern.
- `GlassTextField` accepts a `keyboardType` parameter (default `KeyboardType.Text`); pass `KeyboardType.Number` for numeric input fields.
- Stat / Rank colors live in `ui/theme/Color.kt` and are referenced by enum constants in `domain/model/StatType.kt` and `Rank.kt` — keep colors in the theme file, not at the enum site.

### Backdrop blur (Haze)

Real GPU backdrop blur for glass effects is wired via [Haze 1.7.2](https://chrisbanes.github.io/haze/):

- A single `HazeState` is created in `AppNavigation` via `rememberHazeState()` and provided via `LocalHazeState` (see `ui/components/glass/HazeWiring.kt`).
- The `NavHost` content is the **source** — `Modifier.hazeSourceOrFallback()`.
- `GlassCard`, `GlassCardWithHighlight`, and `GlassBottomBar` apply `Modifier.hazeEffectOrFallback()` — blurs whatever's underneath in the source layer.
- On **Android 12+** (API 31+) this is real-time GPU blur. On **older devices** Haze automatically falls back to a translucent scrim (no extra code path needed).
- The fake-glass gradient tint is kept on top of the blur at reduced alpha so the brand colour still reads.
- New screens get this for free — wrap any glass element with the existing primitives. To opt out (e.g., a fully opaque card), wrap the call site with `CompositionLocalProvider(LocalHazeState provides null) { ... }`.

### Splash screen

Cold-start splash is handled by `androidx.core:core-splashscreen`:
- `Theme.StatUp.Splash` in `res/values/themes.xml` sets `windowSplashScreenBackground=#0A0A0F` and `windowSplashScreenAnimatedIcon=@drawable/ic_launcher_foreground` (the same hexagon as the launcher icon).
- `AndroidManifest.xml` lists `Theme.StatUp.Splash` as the launcher activity's theme.
- `MainActivity.onCreate()` calls `installSplashScreen()` **before** `super.onCreate()` — this swaps the splash theme out for `Theme.StatUp` (post-splash theme) when the first frame is ready.

### Pull-to-refresh + lifecycle-aware refresh

- The Tasks screen wraps its `LazyColumn` in a Material 3 `PullToRefreshBox` (Compose 1.7+). The handler `TasksViewModel.refreshAll()` is suspend-friendly so the spinner stays visible until both the daily-mission reset and the Todoist active-task fetch complete.
- `TasksScreen` uses `lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED)` to re-fetch Todoist active tasks every time the tab is re-entered (not just on first composition). Add this pattern to any screen that needs "always fresh on tab open" behaviour.
- The Todoist section in `TasksScreen` is **collapsed by default** (`showTodoistTasks = false`) and rendered **last** in the list, below the user's missions. Missions are the primary surface; Todoist is an integration.

## AI Agent (Gemini)

The Agent tab is no longer a placeholder. Implemented in `ai/`:

- **`AgentApi` interface + `GeminiAgentApi` impl** — talks to `generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent`. Sends `system_instruction` + ordered `contents` (user/model turns). Returns `Result<String>`; non-2xx is mapped to typed exceptions (`AgentAuthException` for 401/403, `AgentRateLimitException` for 429, `AgentSafetyException` for `finishReason=SAFETY|RECITATION`). Reads the API key lazily via a `suspend () -> String?` provider so updates in Settings take effect without recreating the Koin singleton. **Model history note**: bumped from `gemini-2.0-flash` to `gemini-2.5-flash` on 2026-05-17 — the 2.0-flash line was retired on 2026-03-03 and existing apps started receiving 4xx (often surfacing as 429 in API Studio dashboards) instead of replies.
- **`AgentRepository`** — thin orchestrator: builds the system instruction (`AgentPersona.SYSTEM_PROMPT` + `AgentContextBuilder.build()`) then delegates to `AgentApi`. Stateless.
- **`AgentContextBuilder`** — snapshots fresh player state every send: name, rank, star-line counter, streak, six stats, total earned, last 8 EARN transactions, up to 6 active missions. Kept ~500 tokens. No caching.
- **`AgentPersona.SYSTEM_PROMPT`** — pins the persona to Stat Up's domain (six stats, rank ladder E→S, star-line rules) and the response rules (concise, grounded in player state, suggest missions textually but cannot create them).
- **`AgentMessage`** — in-memory data class with `Role(USER | MODEL)`, `content`, `isPending`. Never persisted (the dormant `AiConversationEntity` table stays unused).
- **`AgentViewModel`** — holds the transcript in `MutableStateFlow<AgentUiState>`. Gates the screen on `isConfigured` (true iff Gemini key non-blank). On send, appends user msg + pending placeholder, awaits the API, replaces the placeholder with the reply or surfaces a friendly error. Exposes `clearChat`, `dismissError`, `refreshConfigured` (called from screen on resume so Settings round-trips reflect immediately).
- **`AgentScreen`** — gated "Connect AI Coach" empty state when not configured, then a chat surface (4 starter prompts, message bubbles, auto-scroll to bottom, error banner, send-disabled-while-pending input).
- **Settings**: `GeminiConnectionDialog` accepts a key, stores via `userPreferences.setGeminiApiKey(...)` → `SecretStorage` (AES-256). No validation on save (Gemini has no cheap ping endpoint; cost-free saves means a typo only surfaces when the user first chats).

## Home-screen widget

4×2 widget in `widget/StatsWidgetProvider` displaying rank badge, rank title, balance, streak, today's earned points. Read-only — taps the root to open `MainActivity`.

- **Registration**: `<receiver>` in `AndroidManifest.xml` with `widget_stats_info.xml` metadata.
- **Layout**: `res/layout/widget_stats.xml` (RemoteViews only — no Compose, no custom views). Static dark palette matching the app.
- **Refresh model**: `updatePeriodMillis=0` (no AlarmManager polling). Refreshes are pushed by `widget/StatsWidgetUpdater`, a Koin singleton that sends an `ACTION_APPWIDGET_UPDATE` broadcast for the live widget IDs (early-outs if none on the home screen).
- **Trigger points**: injected as `StatsWidgetUpdater?` into `PointsRepository` (refreshes after every `addPoints` and `redeemPoints`) and `DecayEngine` (refreshes after `applyDailyDecay`). `StatUpApp.initializeData()` also calls `refresh()` once at app start so background changes propagate when the user reopens.
- **Snapshot reads**: `StatsWidgetProvider.onUpdate` runs on a binder thread and uses `runBlocking` to read `PlayerStatsEntity` + `getTotalEarned`/`getTotalRedeemed` + `getEarnedInRange(todayStart, todayEnd)`. Fast enough — singleton row + tiny SUM queries.

## Notifications

`com.rewardpoints.app.notifications.Notifier` is the single entry point for all app notifications:
- Channels created in `init`: `CHANNEL_SYNC` (Todoist results, IMPORTANCE_LOW, no badge) and `CHANNEL_REMINDERS` (IMPORTANCE_DEFAULT, reserved for future decay warnings / rank-up alerts).
- `Notifier` is resolved eagerly in `StatUpApp.initializeData()` so the channels exist before any worker can fire.
- All notify calls re-check `arePermissionsGranted()` first; declined permission silently disables notifications (no crash).
- `MainActivity` requests `POST_NOTIFICATIONS` once on Android 13+ via `rememberLauncherForActivityResult` — refusal is fine; the runtime check above keeps everything stable.
- Wired into `TodoistSyncWorker`: `Success → showSyncResult(tasksProcessed, pointsEarned)` (skipped when `tasksProcessed == 0`); `AuthFailed → showSyncAuthFailure()`.
- Small icon is `ic_launcher_monochrome` so notifications match the system's themed-icon style on Android 13+.

## App Icon

Adaptive icon (API 26+) lives in:
- `res/mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml` — adaptive icon manifests.
- `res/drawable/ic_launcher_background.xml` — radial purple→black gradient.
- `res/drawable/ic_launcher_foreground.xml` — hexagon outline with gold inner hex, upward chevron (stat-up motif), and six colored vertex dots (one per stat color).
- `res/drawable/ic_launcher_monochrome.xml` — Android 13+ themed-icon silhouette.

The PNG `mipmap-*dpi/ic_launcher*.png` files (including under `app/src/debug/res/`) are now dead weight — adaptive icons in `mipmap-anydpi-v26/` take precedence on every supported device (minSdk 26+). Safe to delete the PNGs to shave a few KB off the APK, but harmless to leave. Tweak colors and shape in the foreground/background XML files.

## Rewards UI

The reward creation dialog (`ui/screen/rewards/RewardsScreen.kt::CreateRewardDialog`) offers:
- 7 preset cost chips: 5 / 10 / 20 / 50 / 100 / 200 / 500.
- A **custom amount** numeric input field below the chips. When non-empty (any positive int up to 999,999), it overrides whichever preset is selected.
- `effectiveCost = customCost.toIntOrNull()?.takeIf { it > 0 } ?: cost.toIntOrNull() ?: 10` resolves the final value.

## Source-of-Truth Files When Onboarding

Read these in order to ramp on a feature:

1. `di/AppModule.kt` — wiring graph.
2. `domain/model/PlayerStats.kt`, `Rank.kt`, `StatType.kt` — invariants.
3. `data/repository/PointsRepository.kt` — earn/redeem + stat accumulation.
4. `rpg/DecayEngine.kt` — daily tick logic + rank transitions.
5. `sync/TodoistSyncManager.kt` — external task ingestion.
6. `ui/navigation/AppNavigation.kt` — route map.
7. `data/local/datastore/UserPreferences.kt` + `SecretStorage.kt` — preferences/secrets split.

`llm_memory.md` is a complementary running log of feature state — useful for historical context but may lag the code.

## v4.0 Roadmap Status

| Item | State | Notes |
| --- | --- | --- |
| AI Agent | ✅ Done | Gemini-backed chat (`ai/GeminiAgentApi`, model `gemini-2.5-flash`, free tier 10 RPM / 250k TPM / 500 RPD as of 2026-05). `AgentRepository` builds a system instruction = persona + fresh player-state snapshot per send. `AgentViewModel` keeps the transcript in memory only (no DB persistence — `AiConversationEntity` remains dormant). Settings has a `GeminiConnectionDialog` to add/remove the encrypted key. `AgentScreen` shows a gated "connect" state when no key is set, then a chat surface with 4 starter prompts. Errors surface as `AgentAuthException` / `AgentRateLimitException` / `AgentSafetyException`. For local key validation outside the app, `scripts/verify_gemini_key.sh` reads `.env` (`GOOGLE_API_KEY=`) and probes ListModels + generateContent on the current Flash models. |
| Backdrop blur (glass) | ✅ Done | Haze 1.7.2 — see "Backdrop blur" section above. |
| Splash polish | ✅ Done | Dark `Theme.StatUp.Splash` + hexagon icon. |
| Push notifications (sync) | ✅ Scaffolded | `Notifier` infrastructure + sync-result + auth-failure flows live. Decay/rank notifications wired up = TODO. |
| Widget | ✅ Done | 4×2 read-only widget in `widget/StatsWidgetProvider` showing rank + rank title + balance + streak + today's earned points. Registered in `AndroidManifest` (`<receiver>`). `updatePeriodMillis=0` — refreshes are pushed by `widget/StatsWidgetUpdater`, a Koin singleton injected into `PointsRepository` and `DecayEngine`. Trigger points: every earn/redeem (`PointsRepository.addPoints` + `redeemPoints`), every daily tick (`DecayEngine.applyDailyDecay`), and app start (`StatUpApp.initializeData`). Click target opens `MainActivity`. |

## Gotchas

- **JDK 17 required.** Gradle 8.13 does not run on JDK 24+. On Ubuntu 26.04 (which ships only JDK 25), install JDK 17: `sudo apt install openjdk-17-jdk` and select it via `sudo update-alternatives --config java`.
- **Release signing falls back to `debug.keystore`** if `keystore.properties` is absent. Copy `keystore.properties.template` → `keystore.properties` and fill in real values before producing Play-ready APKs.
- **`AndroidManifest.xml` declares `INTERNET`** because Todoist sync **and** Gemini chat need it. The privacy stance is "offline-first" — both Todoist sync and the AI Agent are opt-in (require a user-supplied API key); the rest of the app works fully offline.
- **`compileSdk 36` is required**, not optional — transitive deps `androidx.activity:1.12.2` and `androidx.navigationevent:1.0.1` (pulled in by `lifecycle-runtime-compose:2.9.0` / `navigation-compose:2.9.0`) fail the build with "compile against version 36 or later" if `compileSdk` is dropped back to 35. `targetSdk` remains 35 — that's intentional, do not bump without testing API-36 runtime behavior changes.
- **AI Agent transcript is in-memory only.** The `AiConversationEntity` table is declared in Room but has no DAO and no live consumer. If you want chat persistence, you'd need to add a DAO and have `AgentViewModel` rehydrate on init.
- **Don't rename the package, DB name, DataStore name, or secret prefs name** without a migration plan; existing users' Room DB, DataStore, and `EncryptedSharedPreferences` file would orphan.
- **`StatsEngine.calculateTaskPoints` is the only priority→points mapping** — `TodoistSyncManager` delegates to it. Instance methods on `StatsEngine` (`earnPointsFromTask`, `earnManualPoints`, `earnMoodPoints`, `earnMissionPoints`) are currently unused — ViewModels call `PointsRepository.addPoints` / `earnPoints` directly. Treat as inert until a planned consumer appears.
- **`rankDownBreakCounter` column** in `player_stats` is unread by live code (star-line model in `DecayEngine` uses `rankUpStreakCounter` only). Column is retained to avoid a DB migration.
- **Mood check-in is once per local day.** Resets at midnight (local time) automatically via `LocalDate.now()`-based query window. If the user keeps the screen open across midnight, the flag won't refresh until the ViewModel is re-created — acceptable for the use case.
- **`Routes.CREATE_REWARD` and `Routes.EDIT_REWARD`** are defined but unused. Reward creation is a dialog inside `RewardsScreen`, not a separate route.

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
