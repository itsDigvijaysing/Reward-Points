# LLM Project Memory — Reward Points App

> Compressed memory for AI collaboration. Read this first every session.

## Identity
- **App**: Reward Points — anime-inspired RPG Status Window for real life
- **User**: King — Android developer, values premium UI, wants simple but effective gamification
- **Repo**: /home/king/Documents/Projects/Reward-Points
- **License**: GPL-3.0 | **Package**: com.rewardpoints.app

## Current State (as of 2026-03-31)
- **Status**: PRE-REBUILD. Old code exists (Java/XML/SharedPrefs). Full rewrite planned.
- **Old stack**: Native Android Java, Material Design 3, XML layouts, EncryptedSharedPreferences
- **Old version**: v2.1.0 (versionCode 6)
- **Plan file**: /plan.md (12 sections)

## New Stack
Kotlin | Jetpack Compose | Room | Ktor | Koin | Compose Navigation | DataStore | Kotlinx.serialization | Coil | AGSL shaders

## Core Concept
RPG Status Window. Complete real-life tasks → earn reward points → stats grow → rank up.
Idle = decay. Consistency = rank promotion. The app IS the protagonist's status screen.

## Points & Stats (SIMPLE — don't overcomplicate)
- **Reward points** from tasks: p1=4, p2=3, p3=2, p4=1
- **Stats cap at 100**. Every **10 reward points** in a category = **+1 stat point**
- **6 stats**: STR(#FF5252) INT(#448AFF) WIS(#AB47BC) DEX(#FFD740) CHA(#FF4081) VIT(#69F0AE)
- Stat routing: Todoist task labels mapped to stats in settings (e.g., label "gym"→STR)
- Tasks with no matching label → user-configured default stat
- Mood check-in = 2pts → WIS. Manual entry picks stat.

## Rank System (streak-based, NOT XP-based)
- **6 ranks max**: E → D → C → B → A → S (S is max, no SS/SSS)
- **Rank UP**: 5 continuous streak days → rank +1
- **Rank DOWN**: 5 consecutive streak breaks → rank -1
- Break counter resets on any successful day
- Rank colors: E=#9E9E9E, D=#8D6E63, C=#66BB6A, B=#42A5F5, A=#AB47BC, S=#FFD740

## Decay (simple)
- 0 tasks in a day → streak breaks → all stats lose 1 point → break counter +1
- 5 breaks → rank degrades
- Checked at midnight via WorkManager

## Todoist Integration (researched, confirmed working)
- **Free plan works**: 500 labels, API unrestricted, 1000 req/15min
- **API**: v1 endpoints. Auth: Bearer token. Labels = string array on task object.
- **Priority inverted in API**: p1(urgent)=`priority:4`, p4(normal)=`priority:1`
- **Completed tasks**: `GET /api/v1/tasks/completed_by_completion_date` (free: 1 week history)
- **Sync**: WorkManager every 15min + on app open. Poll, not webhooks.
- **NOT dependent on Todoist** — app works fully standalone. Manual points, missions, mood all work without it.

## AI Agent — DEFERRED TO v4.0
- Keep UI placeholder (Agent tab → "Coming in next version" glass card)
- Keep Room tables (ai_memory, ai_conversations) in schema for future
- Keep ai/ package structure planned but empty
- Gemini API key input in settings — hidden/disabled for now

## Architecture
MVVM: Screen→ViewModel→UseCase→Repository→(Room|Ktor)
Packages: di/ data/ domain/ ui/ rpg/ sync/ util/ (ai/ for future)
Single Activity + Compose Navigation
5 tabs: Status | Tasks | Rewards | Agent(placeholder) | Settings

## Design Tokens
- BG: #0A0A0F, Surface: #12121A
- Glass: white@10% fill, white@18% border, 32dp blur, 24dp radius
- Accent: #7C4DFF | Secondary: #00E5FF | Gold: #FFD740 | Success: #69F0AE
- Text: #F0F0F5 primary, #A0A0B8 secondary, #6B6B80 tertiary
- Font: Inter (UI) + JetBrains Mono (code/YAML)
- Icons: Phosphor (light weight)

## Build Phases
P1: Foundation+Glass+RPG core → P2: XP system+rewards+missions+decay → P3: Todoist → P4: Polish
(AI Agent = future version, not in these phases)

## Key Decisions
- minSdk 26, targetSdk 35
- Dark-only app (no light mode)
- Todoist personal API token (not OAuth)
- Room + DataStore (not SharedPrefs)
- Full rewrite → v3.0.0, same package name
- KEEP THINGS SIMPLE. User explicitly said don't overcomplicate.

## What NOT to do
- Don't use Java/XML/SharedPreferences
- Don't overcomplicate point calculations (no multipliers, no label bonuses — just p1=4,p2=3,p3=2,p4=1)
- Don't build AI agent functionality yet (UI shell only)
- Don't make app dependent on Todoist (works standalone)
- Don't over-log like the old app
- Don't add ranks beyond S
- Don't use XP-based ranking (it's streak-based)
