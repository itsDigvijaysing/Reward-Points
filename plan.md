# Reward Points App — Complete Rebuild Plan

## Vision

An anime-inspired **RPG Status Window** app where your real life IS the game. Complete Todoist tasks to level up stats on a hexagonal radar chart, earn points, climb ranks from E to SSS — and watch your stats **decay** if you go idle. Powered by a **Gemini AI agent** with evolving memory as your personal System Narrator, all wrapped in a **Liquid Glass** UI that looks like THE SYSTEM itself.

**The Fantasy**: You are the protagonist. The app is your Status Window. Every task is an XP drop. Every idle day is stat decay. The AI is the voice of the System.

---

## 1. Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | **Kotlin** | Modern, concise, coroutine-native |
| UI Framework | **Jetpack Compose** + Material 3 | Declarative UI, shader support for glass effects |
| Glass Effects | **Kyant0/AndroidLiquidGlass** library + custom AGSL shaders | Real liquid glass blur/refraction on Android 13+, fallback glassmorphism for older |
| Architecture | **MVVM** — ViewModel + StateFlow + Repository pattern | Clean separation, testable, lifecycle-aware |
| Database | **Room** (SQLite) | Proper relational DB for transactions, rewards, missions, AI memory |
| Networking | **Ktor Client** | Lightweight, Kotlin-native HTTP for Todoist + Gemini APIs |
| Serialization | **Kotlinx.serialization** | Type-safe JSON parsing |
| DI | **Koin** | Simple, lightweight, no annotation processing |
| Navigation | **Compose Navigation** | Single-activity, type-safe routes |
| Datastore | **Jetpack DataStore** | For preferences (replaces SharedPreferences) |
| Image | **Coil** | Compose-native image loading |
| Markdown | **Compose Markdown** renderer | For AI chat messages + persona file preview |

### Minimum SDK: 26 (Android 8.0) — covers 95%+ devices, enables blur APIs
### Target SDK: 35 (Android 15)

---

## 2. Design System — "Liquid Glass"

### 2.1 Design Philosophy

Inspired by Apple's iOS 26 Liquid Glass + anime Status Windows (Solo Leveling's "THE SYSTEM", SAO menus, Overlord stat sheets). The glass UI IS the in-universe system interface. It should feel like a protagonist's translucent HUD floating over a dark, living world.

**Core principles:**
- **The System aesthetic** — Every panel looks like it belongs in an RPG status screen
- **Depth through translucency** — Layered glass panels, never flat opaque cards
- **Ambient color** — Soft gradient orbs in the background that the glass distorts
- **Subtle motion** — Parallax shifts, gentle scale/fade on interaction
- **Breathing space** — Generous padding, nothing cramped
- **Minimal chrome** — Let content and glass do the talking
- **Alive** — Stats pulse, the hexagon breathes, decay feels like health draining

### 2.2 Color Palette

#### Primary Background (The Canvas)
The background is NOT solid black. It's a deep dark surface with floating ambient gradient orbs that give the glass something to refract.

```
Background Base:       #0A0A0F  (near-black with blue undertone)
Background Surface:    #12121A  (slightly elevated)
```

#### Ambient Gradient Orbs (behind glass panels)
These are large, soft, blurred circles of color that float behind the UI:

```
Orb 1 — Deep Violet:    #6C3CE1  (top-left region)
Orb 2 — Electric Blue:  #2196F3  (center-right)
Orb 3 — Hot Pink:       #E91E8A  (bottom-left)
Orb 4 — Teal:           #00BFA5  (bottom-right, subtle)
```

#### Glass Panel Colors
```
Glass Fill:             #FFFFFF at 8-12% opacity
Glass Border:           #FFFFFF at 15-20% opacity (thin 1dp)
Glass Blur Radius:      24dp - 40dp
Glass Inner Highlight:  #FFFFFF at 5% (top edge glow)
```

#### Accent & Semantic Colors
```
Primary Accent:         #7C4DFF  (vibrant purple — main actions)
Secondary Accent:       #00E5FF  (cyan — secondary actions, links)
Points Gold:            #FFD740  (warm gold — point counters, earnings)
Success:                #69F0AE  (mint green — completions, earned)
Warning:                #FFB74D  (warm amber — alerts)
Error:                  #FF5252  (soft red — destructive)
```

#### Text Colors
```
Text Primary:           #F0F0F5  (near-white, high contrast)
Text Secondary:         #A0A0B8  (muted lavender-gray)
Text Tertiary:          #6B6B80  (for hints, timestamps)
Text On Accent:         #FFFFFF
```

#### Special Colors
```
AI Agent Glow:          #7C4DFF → #00E5FF gradient (the agent's identity)
Todoist Red:            #E44332  (Todoist brand, used in task cards)
Streak Fire:            #FF6D00 → #FFD740 gradient
```

### 2.3 Typography

```
Display / Points Counter:  Inter Black, 48-64sp
Heading 1:                 Inter Bold, 24sp
Heading 2:                 Inter SemiBold, 20sp
Body:                      Inter Medium, 16sp
Body Small:                Inter Regular, 14sp
Caption:                   Inter Regular, 12sp
Monospace (AI/YAML):       JetBrains Mono, 14sp
```

### 2.4 Glass Component Specs

#### Glass Card (Primary Container)
```
Corner Radius:       24dp
Background:          #FFFFFF at 10% opacity
Blur:                32dp backdrop blur
Border:              1dp #FFFFFF at 18% opacity
Shadow:              0dp 8dp 32dp #000000 at 25%
Inner Highlight:     Top 1dp gradient #FFFFFF 8% → 0%
Padding:             20dp
Margin:              12dp horizontal, 8dp vertical
```

#### Glass Card Elevated (for important items like point counter)
```
Corner Radius:       28dp
Background:          #FFFFFF at 14% opacity
Blur:                40dp backdrop blur
Border:              1dp #FFFFFF at 22% opacity
Shadow:              0dp 12dp 40dp #7C4DFF at 15%
Glow:                Subtle purple (#7C4DFF at 8%) outer glow
```

#### Glass Bottom Navigation Bar
```
Height:              72dp
Corner Radius:       0dp top, or floating with 28dp all
Background:          #FFFFFF at 8% opacity
Blur:                24dp
Border Top:          1dp #FFFFFF at 12%
Icon Size:           24dp
Label Size:          12sp
Active Icon Tint:    #7C4DFF (primary accent)
Inactive Icon Tint:  #A0A0B8
Active Indicator:    Pill shape, #7C4DFF at 15%, 48dp x 32dp
```

#### Glass Button (Primary)
```
Corner Radius:       16dp
Background:          #7C4DFF at 85% opacity
Blur:                8dp (subtle)
Text:                White, Inter SemiBold, 16sp
Height:              52dp
Press State:         Scale to 0.97, brightness +10%
```

#### Glass Input Field
```
Corner Radius:       16dp
Background:          #FFFFFF at 6% opacity
Border:              1dp #FFFFFF at 12%
Focus Border:        1dp #7C4DFF at 60%
Text:                #F0F0F5
Hint:                #6B6B80
Padding:             16dp
```

### 2.5 Iconography
- Use **Phosphor Icons** (thin/light weight) — they complement the glassy aesthetic
- 24dp standard, 20dp in compact contexts
- Tinted with current context color, never solid black

### 2.6 Animations & Motion

| Interaction | Animation |
|---|---|
| Screen transition | Shared element + fade (300ms, EaseInOutCubic) |
| Card appear | Fade up from 20dp below + scale 0.95→1.0 (400ms stagger 50ms) |
| Card press | Scale 0.97 + brightness shift (150ms) |
| Points earned | Counter rolls up + gold particle burst |
| Reward redeemed | Card dissolves with glass shatter particles |
| AI typing | Pulsing gradient glow on chat bubble |
| Tab switch | Crossfade content + indicator slide (250ms) |
| Pull to refresh | Glass orb stretch + release snap |
| Streak counter | Fire emoji scale pulse + glow intensify |

### 2.7 Background System

The ambient background is a **Compose Canvas** drawing 3-4 large radial gradients (the "orbs") that:
- Very slowly drift in position (30-60 second cycle)
- React subtly to scroll position (parallax)
- Are rendered once and cached as a bitmap layer behind the glass panels

---

## 3. RPG Status System — The Core Identity

This is what makes the app unique. Not just points — a full RPG character sheet for your real life.

### 3.1 The Six Stats (Hexagonal Radar Chart)

| Stat | Full Name | Real-Life Mapping | Grows From |
|---|---|---|---|
| **STR** | Strength | Physical discipline & health | Exercise tasks, gym, walks, sports |
| **INT** | Intelligence | Learning & deep work | Work tasks, study, coding, reading |
| **WIS** | Wisdom | Reflection & consistency | Streaks, mood check-ins, journaling, meditation |
| **DEX** | Dexterity | Speed & productivity | Tasks completed before deadline, daily missions done early |
| **CHA** | Charisma | Social & creative output | Social tasks, creative projects, helping others |
| **VIT** | Vitality | Overall wellness & balance | Sleep, self-care, taking breaks, redeeming rewards |

**How users map tasks to stats:**
- In Settings, users assign their **Todoist projects or labels** to stat categories
- E.g., Project "Work" → INT, Label "gym" → STR, Project "Personal" → CHA
- Tasks without mapping get points distributed to a default stat (user's choice)
- Manual point entries also pick a stat category

### 3.2 Stats & Points — How It Works

**Simple and clean:**
- Stats range from **0 to 100** (hard cap)
- Every **10 reward points** earned in a category = **+1 stat point** in that stat
- Stat points are the currency that fills the hexagon (0-100 scale)

Example: You complete a Todoist p1 task tagged "gym" → earn 4 reward points → those 4 points accumulate toward STR. After 10 total STR points, STR stat goes from e.g. 45 → 46.

### 3.3 Reward Points (the currency)

Earned from completing tasks. Simple values:

| Task Priority | Reward Points |
|---|---|
| **p1** (urgent, red) | **4 points** |
| **p2** (high, orange) | **3 points** |
| **p3** (medium, blue) | **2 points** |
| **p4** (normal, no color) | **1 point** |

**Note on Todoist API**: Todoist inverts priority internally — p1 (urgent) = `priority: 4` in the API, p4 (normal) = `priority: 1`. We map this correctly in code.

Additional earning:
| Action | Reward Points |
|---|---|
| Daily mood check-in | 2 points (→ WIS) |
| Complete daily mission | Mission's points (→ Mission's stat) |

### 3.4 Rank System — Earned Through Consistency

**6 Ranks** (max is S):

| Rank | Title | Color |
|---|---|---|
| **E** | Novice | #9E9E9E (gray) |
| **D** | Apprentice | #8D6E63 (bronze) |
| **C** | Warrior | #66BB6A (green) |
| **B** | Elite | #42A5F5 (blue) |
| **A** | Champion | #AB47BC (purple) |
| **S** | Master | #FFD740 (gold) |

**Rank-up rule**: Maintain a **continuous 5-day streak** → rank upgrades by one level.

**Rank-down rule**: If streak breaks, a **break counter** increments. **5 consecutive streak breaks** → rank degrades by one level. Break counter resets when you complete a day successfully.

```
Rank-up:   5 consecutive streak days → rank +1
Rank-down: 5 consecutive streak breaks → rank -1
Break counter resets on any successful day
Minimum rank: E (can't go below)
Maximum rank: S
```

Each rank-up triggers a **dramatic full-screen animation** — the glass shatters and reforms, rank letter glows, particles burst.

### 3.5 Streak & Decay — Simple Rules

**Streak**: Increments by 1 for each day you complete at least 1 task. Resets to 0 if a day passes with 0 completions.

**Decay**: When streak breaks (0 tasks in a day):
```
- All stats lose 1 point (if above 0)
- Break counter increments by 1
- If break counter reaches 5 → rank degrades
- Status window shows warning visual
```

**Decay is checked at midnight (local time)** via WorkManager.

No overcomplicated percentage-based decay — just lose 1 stat point per break day. Simple and punishing enough to motivate.

### 3.5 Status Window UI

The centerpiece of the app. A translucent, glowing panel that looks ripped from an anime:

```
┌─────────────────────────────────────┐
│          ╔═══════════════╗          │
│          ║  STATUS WINDOW ║          │
│          ╚═══════════════╝          │
│                                     │
│         ┌─── RANK: A ───┐          │
│         │   CHAMPION     │          │
│         └────────────────┘          │
│                                     │
│          Player: King               │
│          Level: 42                  │
│          Total XP: 176,400          │
│                                     │
│              STR                    │
│            ╱    ╲                   │
│        VIT ─  ◆  ─ INT             │
│            ╲    ╱                   │
│        CHA ── ── DEX               │
│              WIS                    │
│                                     │
│    STR: 67/100  ████████████░░░     │
│    INT: 89/100  ██████████████░     │
│    WIS: 45/100  █████████░░░░░░     │
│    DEX: 72/100  ████████████░░░     │
│    CHA: 38/100  ███████░░░░░░░░     │
│    VIT: 55/100  ██████████░░░░░     │
│                                     │
│    🔥 Streak: 12 days              │
│    📊 Today: +8 pts                │
│    ⚔️  Rank up in: 3 streak days   │
│                                     │
│         [View Full Stats]           │
└─────────────────────────────────────┘
```

**Visual details:**
- The hexagon is drawn with **animated gradient lines** (glow effect on edges)
- Each stat vertex pulses slightly (breathing animation, 3s cycle)
- The filled area inside the hexagon uses a **gradient mesh** matching stat colors
- Rank letter has an **outer glow** in its rank color
- The entire panel has the liquid glass treatment (blur, refraction, border glow)
- On decay, the hexagon subtly **shrinks inward** with a red pulse
- On XP gain, the affected stat vertex **pulses outward** with a gold flash
- Cracked glass overlay appears during extended inactivity (72h+)

**Stat colors on the hexagon:**
```
STR:  #FF5252  (red)
INT:  #448AFF  (blue)
WIS:  #AB47BC  (purple)
DEX:  #FFD740  (gold)
CHA:  #FF4081  (pink)
VIT:  #69F0AE  (green)
```

### 3.6 Tag-Based Stat Routing (Todoist Labels)

This is how the app knows which stat to boost. Users tag their Todoist tasks with labels, and the app maps labels to stats.

**Setup (one-time in Settings):**
```
Label "gym"     → STR
Label "study"   → INT
Label "journal" → WIS
Label "work"    → DEX
Label "social"  → CHA
Label "health"  → VIT
```

**How it works:**
1. User creates labels in Todoist (free plan allows 500 labels)
2. In our app's Settings, user maps each label to a stat category
3. When a task is completed, app reads its `labels` array (string array from API)
4. First matching label determines the stat that gets the points
5. If no label matches any mapping → points go to a user-configured default stat
6. Tasks can only boost ONE stat (first match wins, keeps it simple)

**Todoist API confirms**: The `labels` field is a **string array** on the task object (e.g., `["gym", "morning"]`). Available on free plan. No issue.

### 3.7 Titles & Achievements (RPG Style)

Beyond rank, special titles unlock based on specific milestones:

```
"Iron Will"         — 30-day streak
"Unbreakable"       — 100-day streak
"Scholar"           — INT reaches 80
"Warrior Spirit"    — STR reaches 80
"Renaissance"       — All stats above 50
"Min-Maxer"         — Any single stat reaches 100
"Balanced Build"    — All stats within 10 points of each other
"Comeback King"     — Recover from rank-down 3 times
"Generous Soul"     — Redeem 50 rewards
```

These display as **glass badges** on the profile, each with a unique icon and glow color.

---

## 4. App Architecture

```
com.rewardpoints.app/
├── di/                          # Koin modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── RewardDao.kt
│   │   │   │   ├── TransactionDao.kt
│   │   │   │   ├── MissionDao.kt
│   │   │   │   ├── PlayerStatsDao.kt
│   │   │   │   ├── StatMappingDao.kt
│   │   │   │   └── AiMemoryDao.kt
│   │   │   └── entity/
│   │   │       ├── RewardEntity.kt
│   │   │       ├── TransactionEntity.kt
│   │   │       ├── MissionEntity.kt
│   │   │       ├── AchievementEntity.kt
│   │   │       ├── PlayerStatsEntity.kt
│   │   │       ├── StatMappingEntity.kt
│   │   │       ├── DecayLogEntity.kt
│   │   │       └── AiMemoryEntity.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   ├── remote/
│   │   ├── todoist/
│   │   │   ├── TodoistApi.kt
│   │   │   ├── TodoistModels.kt
│   │   │   └── TodoistRepository.kt
│   │   └── gemini/
│   │       ├── GeminiApi.kt
│   │       ├── GeminiModels.kt
│   │       └── GeminiRepository.kt
│   └── repository/
│       ├── PointsRepository.kt
│       ├── RewardRepository.kt
│       ├── MissionRepository.kt
│       ├── PlayerRepository.kt
│       └── AiAgentRepository.kt
├── domain/
│   ├── model/
│   │   ├── Reward.kt
│   │   ├── Transaction.kt
│   │   ├── Mission.kt
│   │   ├── Achievement.kt
│   │   ├── PlayerStats.kt
│   │   ├── Rank.kt
│   │   ├── StatType.kt
│   │   ├── TodoistTask.kt
│   │   ├── AiMessage.kt
│   │   └── AiPersona.kt
│   └── usecase/
│       ├── EarnXpUseCase.kt
│       ├── CalculateDecayUseCase.kt
│       ├── CalculateRankUseCase.kt
│       ├── RedeemRewardUseCase.kt
│       ├── SyncTodoistUseCase.kt
│       └── ChatWithAgentUseCase.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt              # LiquidGlass theme
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   └── GlassTokens.kt        # Glass-specific design tokens
│   ├── components/
│   │   ├── glass/
│   │   │   ├── GlassCard.kt
│   │   │   ├── GlassButton.kt
│   │   │   ├── GlassTextField.kt
│   │   │   ├── GlassBottomBar.kt
│   │   │   ├── GlassTopBar.kt
│   │   │   └── GlassDialog.kt
│   │   ├── rpg/
│   │   │   ├── HexagonRadarChart.kt    # The stat hexagon (Canvas)
│   │   │   ├── StatusWindow.kt         # Full status window composable
│   │   │   ├── RankBadge.kt            # Rank letter with glow
│   │   │   ├── StatBar.kt              # Individual stat progress bar
│   │   │   ├── LevelIndicator.kt       # Level + XP progress
│   │   │   ├── DecayOverlay.kt         # Cracked glass for inactivity
│   │   │   └── RankUpAnimation.kt      # Full-screen rank-up ceremony
│   │   ├── AmbientBackground.kt        # The floating gradient orbs
│   │   ├── PointsCounter.kt            # Animated rolling counter
│   │   ├── StreakIndicator.kt
│   │   ├── RewardCard.kt
│   │   ├── TaskCard.kt
│   │   ├── MissionCard.kt
│   │   ├── AiChatBubble.kt
│   │   └── AchievementBadge.kt
│   ├── screen/
│   │   ├── status/
│   │   │   ├── StatusScreen.kt          # THE status window (home)
│   │   │   ├── FullStatsScreen.kt       # Detailed stat breakdown
│   │   │   └── StatusViewModel.kt
│   │   ├── tasks/
│   │   │   ├── TasksScreen.kt
│   │   │   └── TasksViewModel.kt
│   │   ├── rewards/
│   │   │   ├── RewardsScreen.kt
│   │   │   ├── CreateRewardScreen.kt
│   │   │   └── RewardsViewModel.kt
│   │   ├── agent/
│   │   │   ├── AgentChatScreen.kt
│   │   │   ├── PersonaEditorScreen.kt
│   │   │   └── AgentViewModel.kt
│   │   ├── history/
│   │   │   ├── HistoryScreen.kt
│   │   │   └── HistoryViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── navigation/
│       ├── AppNavigation.kt
│       └── Routes.kt
├── ai/
│   ├── AgentEngine.kt             # Core agent logic
│   ├── MemoryManager.kt           # Read/write/evolve memory
│   ├── PersonaParser.kt           # Parse YAML/MD persona files
│   └── PromptBuilder.kt           # Build prompts with persona + memory + context
├── rpg/
│   ├── StatsEngine.kt             # XP calculation, stat updates
│   ├── DecayEngine.kt             # Decay calculation logic
│   ├── RankCalculator.kt          # Level + rank determination
│   └── TitleChecker.kt            # Achievement title unlock checks
├── sync/
│   ├── TodoistSyncWorker.kt       # WorkManager periodic sync
│   ├── DecayWorker.kt             # WorkManager midnight decay check
│   └── SyncManager.kt
└── util/
    ├── DateUtils.kt
    ├── PointsCalculator.kt
    └── Extensions.kt
```

---

## 5. Screens & UX Flow

### 5.1 Status Window (Home Screen) — THE MAIN SCREEN

The hero screen. Everything at a glance.

The home screen IS the Status Window. This is what users see first — their character sheet.

```
┌───────────────────────────────────┐
│    [ambient gradient orbs bg]     │
│                                   │
│    ╔═════════════════════════╗    │
│    ║    S T A T U S          ║    │
│    ╚═════════════════════════╝    │
│                                   │
│    ┌───── RANK: A ─────┐         │
│    │     CHAMPION       │         │
│    │     Level 42       │         │
│    └───────────────────┘         │
│                                   │
│    Player: King                   │
│    Total XP: 176,400              │
│    🔥 Streak: 12 days             │
│                                   │
│           ╱ STR ╲                 │
│       VIT ┤  ◆  ├ INT            │
│       CHA ┤     ├ DEX            │
│           ╲ WIS ╱                 │
│                                   │
│    STR 67 ████████████░░░        │
│    INT 89 ██████████████░        │
│    WIS 45 █████████░░░░░░        │
│    DEX 72 ████████████░░░        │
│    CHA 38 ███████░░░░░░░░        │
│    VIT 55 ██████████░░░░░        │
│                                   │
│    📊 Today: +180 XP              │
│    ⚔️  Next Rank: 1,200 XP away   │
│                                   │
│    ┌──────────────────────────┐   │
│    │ ⚔️  Rank up in 3 streak  │   │
│    │  days. Keep pushing!     │   │
│    └──────────────────────────┘   │
│                                   │
│   ┌─────┐ ┌─────┐ ┌─────┐       │
│   │ Mood │ │+ XP │ │Stats│       │
│   │ +25  │ │     │ │ ▶   │       │
│   └─────┘ └─────┘ └─────┘       │
│                                   │
│ ┌──┐  ┌──┐  ┌──┐  ┌──┐  ┌──┐   │
│ │⚔️│  │📋│  │🎁│  │🤖│  │⚙│   │
│ │Stat│ │Task│ │Rwrd│ │AI │ │Set│  │
│ └──┘  └──┘  └──┘  └──┘  └──┘   │
└───────────────────────────────────┘
```

**Components:**
- **Status Window header** — "STATUS" text in glass panel with glow, rank badge with color
- **Player info** — Name, level, total XP, streak with fire animation
- **Hexagonal radar chart** — 6 stats, animated gradient fill, breathing vertices
- **Stat bars** — Each stat with color-coded progress bar + numeric value
- **Today's points summary** + distance to next rank (streak days remaining)
- **Info card** — Rank progress, streak status, or system messages
- **Quick action pills** — Mood check-in, manual points, view full stats
- **Glass bottom nav** — 5 tabs: Status, Tasks, Rewards, Agent, Settings
- **Decay overlay** — If idle 72h+, cracked glass effect on the entire status window

**Interaction details:**
- Tapping the hexagon opens **Full Stats screen** (detailed breakdown per stat, XP history graph, decay log)
- Tapping the rank badge shows rank progression with all ranks and your position
- Stat bars have subtle shimmer animation on the filled portion
- The entire status window has parallax — tilts slightly with phone gyroscope (optional, togglable)

### 5.2 Tasks Screen (Todoist Integration)

```
┌─────────────────────────────────┐
│  Tasks                    🔄    │
│  ┌──────────────────────────┐   │
│  │ 📊 Today: 3/7 done      │   │
│  │ ████████░░░ +150pts      │   │
│  └──────────────────────────┘   │
│                                 │
│  Today's Tasks                  │
│  ┌──────────────────────────┐   │
│  │ ○ Review PR #42   +30   │   │
│  │ ✓ Write unit tests +25  │   │
│  │ ○ Deploy staging  +50   │   │
│  └──────────────────────────┘   │
│                                 │
│  Upcoming                       │
│  ┌──────────────────────────┐   │
│  │ ○ Sprint planning  +40  │   │
│  │ ○ 1:1 with team   +20  │   │
│  └──────────────────────────┘   │
│                                 │
│  ┌──────────────────────────┐   │
│  │ ⚙ Point Rules            │   │
│  │ Priority 1: +50 pts     │   │
│  │ Priority 2: +30 pts     │   │
│  │ Priority 3: +20 pts     │   │
│  │ Priority 4: +10 pts     │   │
│  └──────────────────────────┘   │
└─────────────────────────────────┘
```

**Features:**
- Auto-sync with Todoist (pull tasks, detect completions)
- Point value based on task priority (p1=50, p2=30, p3=20, p4=10)
- Optional: label-based bonus multipliers (e.g., "hard" label = 2x)
- Progress bar for today's task completion
- Manual sync button + background WorkManager sync every 15 min
- Completing a task here marks it done in Todoist AND earns points

### 5.3 Rewards Screen

```
┌─────────────────────────────────┐
│  Rewards                  + New │
│                                 │
│  Your Points: ✨ 2,450          │
│                                 │
│  ┌──────────────────────────┐   │
│  │ 🎬 Movie Night           │   │
│  │ 200 pts    [REDEEM]      │   │
│  │ "Watch any movie guilt-  │   │
│  │  free"                   │   │
│  └──────────────────────────┘   │
│                                 │
│  ┌──────────────────────────┐   │
│  │ 🍕 Pizza Night            │   │
│  │ 150 pts    [REDEEM]      │   │
│  └──────────────────────────┘   │
│                                 │
│  ┌──────────────────────────┐   │
│  │ 🎮 Gaming Session        │   │
│  │ 500 pts    ████░░ 49%    │   │
│  │ Need 255 more pts        │   │
│  └──────────────────────────┘   │
│                                 │
│  History                        │
│  ┌──────────────────────────┐   │
│  │ 🎬 Movie Night  -200pts  │   │
│  │ Mar 28, 2026             │   │
│  └──────────────────────────┘   │
└─────────────────────────────────┘
```

**Features:**
- Glass reward cards with category emoji, cost, description
- Inline progress bar for rewards you can't yet afford
- One-tap redeem with confirmation dialog (glass modal)
- Redeem animation: glass card shatters into particles
- Create reward: name, cost, category, description, emoji
- Redemption history section

### 5.4 AI Agent Chat Screen

```
┌─────────────────────────────────┐
│  ← Your Agent        ⚙ Persona │
│  ┌──────────────────────────┐   │
│  │ 🤖 "Kai" — Your AI      │   │
│  │ Motivation Coach         │   │
│  │ Memory: 23 entries       │   │
│  └──────────────────────────┘   │
│                                 │
│        ┌────────────────────┐   │
│        │ Hey! You've been   │   │
│        │ crushing it lately.│   │
│        │ 12-day streak!     │   │
│        └────────────────────┘   │
│  ┌──────────────┐               │
│  │ What should I │               │
│  │ focus on today│               │
│  └──────────────┘               │
│        ┌────────────────────┐   │
│        │ Based on your      │   │
│        │ Todoist, you have  │   │
│        │ 3 priority tasks.  │   │
│        │ I'd start with...  │   │
│        └────────────────────┘   │
│                                 │
│  ┌──────────────────────── ↑┐   │
│  │ Type a message...    [→]│   │
│  └──────────────────────────┘   │
└─────────────────────────────────┘
```

**Features:**
- Chat interface with glass bubbles (user = right, agent = left with glow)
- Agent has access to: your points, streak, recent tasks, rewards, missions
- Agent persona defined in editable YAML/markdown file
- Agent memory stored in Room DB — evolves over conversations
- Persona editor screen (markdown editor with preview)
- Agent typing indicator: pulsing gradient glow on bubble
- Suggested quick replies as glass pills

### 5.5 Settings Screen

```
┌─────────────────────────────────┐
│  ← Settings                     │
│                                 │
│  Profile                        │
│  ┌──────────────────────────┐   │
│  │ Username     [King     ] │   │
│  └──────────────────────────┘   │
│                                 │
│  Integrations                   │
│  ┌──────────────────────────┐   │
│  │ Todoist API Token        │   │
│  │ [••••••••••••]  [Test]   │   │
│  │ Status: ✅ Connected      │   │
│  │ Sync: Every 15 min       │   │
│  ├──────────────────────────┤   │
│  │ Gemini API Key           │   │
│  │ [••••••••••••]  [Test]   │   │
│  │ Status: ✅ Connected      │   │
│  │ Model: gemini-2.5-flash  │   │
│  └──────────────────────────┘   │
│                                 │
│  Point Rules                    │
│  ┌──────────────────────────┐   │
│  │ Task Priority 1:  [50]   │   │
│  │ Task Priority 2:  [30]   │   │
│  │ Task Priority 3:  [20]   │   │
│  │ Task Priority 4:  [10]   │   │
│  │ Daily Check-in:   [25]   │   │
│  │ Mood Bonus:       [ON]   │   │
│  └──────────────────────────┘   │
│                                 │
│  AI Agent                       │
│  ┌──────────────────────────┐   │
│  │ [Edit Persona File]      │   │
│  │ [View Agent Memory]      │   │
│  │ [Reset Agent Memory]     │   │
│  └──────────────────────────┘   │
│                                 │
│  Data                           │
│  ┌──────────────────────────┐   │
│  │ [Export Data]             │   │
│  │ [Reset All Data]         │   │
│  └──────────────────────────┘   │
│                                 │
│  v2.0.0                         │
└─────────────────────────────────┘
```

---

## 6. Todoist Integration — Technical Design

### 6.1 Key Facts (Researched)
- **Free plan works fine**: 5 projects, 500 labels, API access unrestricted
- **Task object fields**: `id`, `content`, `description`, `priority` (1-4), `labels` (string array), `project_id`, `due`, `is_completed`
- **Labels are strings**: e.g., `["gym", "morning"]` — perfect for stat mapping
- **Priority is inverted in API**: p1 (urgent/red) = `priority: 4`, p4 (normal) = `priority: 1`
- **Completed tasks endpoint**: `GET /api/v1/tasks/completed_by_completion_date` — free plan: last 1 week
- **Rate limit**: 1000 requests per 15 min per user — plenty
- **Auth**: `Authorization: Bearer {personal_api_token}`

### 6.2 Authentication
- User pastes their **personal API token** from Todoist settings
- Token stored in **DataStore** (encrypted)
- Test button validates token with a `GET /api/v1/projects` call
- App works fully without Todoist — it's an optional integration, not a dependency

### 6.3 Sync Flow
```
1. WorkManager schedules periodic sync (every 15 min)
2. SyncTodoistWorker runs:
   a. GET /api/v1/tasks/completed_by_completion_date — fetch recently completed
   b. Compare with local cache (Room DB) — find new completions
   c. For each newly completed task:
      - Read task.priority → map to reward points (see below)
      - Read task.labels → find first matching stat mapping
      - Create Transaction(type=EARN, source="Todoist", stat_type=mapped_stat)
      - Add reward points to that stat's accumulator
      - If accumulator >= 10 → stat +1, accumulator resets remainder
   d. Also GET /api/v1/tasks to cache today's active tasks for display
   e. Update local cache
3. Manual sync button triggers immediate worker run
4. On app open → also trigger sync if last sync > 5 min ago
```

### 6.4 Point Calculation — Simple
```kotlin
fun calculateTaskPoints(apiPriority: Int): Int {
    // Todoist API inverts: p1 (urgent) = priority 4, p4 (normal) = priority 1
    return when (apiPriority) {
        4 -> 4  // p1 urgent → 4 reward points
        3 -> 3  // p2 high   → 3 reward points
        2 -> 2  // p3 medium → 2 reward points
        1 -> 1  // p4 normal → 1 reward point
        else -> 1
    }
}

fun routeToStat(labels: List<String>, mappings: Map<String, StatType>): StatType {
    // First matching label wins
    for (label in labels) {
        mappings[label]?.let { return it }
    }
    return userDefaultStat  // fallback if no label matches
}
```

### 6.5 Todoist API Endpoints Used
```
Auth:   Authorization: Bearer {token}
Base:   https://api.todoist.com

GET  /api/v1/tasks                              — list active tasks (for display)
GET  /api/v1/tasks/completed_by_completion_date  — get recently completed tasks
POST /api/v1/tasks/{id}/close                    — complete a task from our app
GET  /api/v1/projects                            — list projects (for validation + mapping)
GET  /api/v1/labels                              — list user's labels (for stat mapping setup)
```

### 6.6 Not Dependent on Todoist
The app must work fully without Todoist connected:
- Manual point entry still works (pick stat, enter points)
- Daily missions still work
- Mood check-in still works
- Todoist is just one source of reward points, not the only one

---

## 7. Gemini AI Agent — FUTURE VERSION (v4.0)

> **NOT built in v3.0.** UI placeholder only. Tapping the Agent tab shows a glass card:
> "AI Agent coming in next version. Stay tuned, {rank_title}."

The full AI agent design is documented here for future reference. All the architecture (persona YAML, evolving memory, prompt system) will be implemented in a future version. The `ai/` package, `ai_memory` and `ai_conversations` Room tables, and Agent screen UI shell are created now but left non-functional.

### 7.1 Persona System (future)
YAML-based persona file defining agent name, personality, tone, and boundaries. Editable in-app.

### 7.2 Evolving Memory System (future)
Room DB storage for agent memories (preference/pattern/fact/goal/feedback categories). Max 100 entries, pruned by relevance.

### 7.3 Prompt Architecture (future)
System prompt includes: persona + RPG stats context (rank, stats, streak, decay) + memory + conversation history. Agent speaks as "THE SYSTEM" narrator.

### 7.4 Daily Insight (future)
Auto-generated motivational message on Status Window based on stats/streak.

### 7.5 Gemini API (future)
```
Endpoint:  https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
Model:     gemini-2.5-flash (default)
Auth:      User's own API key
```

---

## 8. Database Schema (Room)

```sql
-- Core tables
CREATE TABLE rewards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    points_cost INTEGER NOT NULL,
    category TEXT NOT NULL,
    emoji TEXT,
    is_active INTEGER DEFAULT 1,
    created_at INTEGER NOT NULL,
    times_redeemed INTEGER DEFAULT 0
);

CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,           -- 'EARN' or 'REDEEM'
    source TEXT NOT NULL,         -- 'Todoist', 'Manual', 'Mission', 'Mood', 'Reward'
    description TEXT,
    points INTEGER NOT NULL,
    stat_type TEXT,               -- 'STR','INT','WIS','DEX','CHA','VIT' (which stat got XP)
    related_id TEXT,              -- task ID, reward ID, etc.
    created_at INTEGER NOT NULL
);

CREATE TABLE missions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    points_reward INTEGER NOT NULL,
    is_daily INTEGER DEFAULT 1,  -- daily vs one-time
    is_completed_today INTEGER DEFAULT 0,
    last_completed_at INTEGER,
    streak INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL
);

CREATE TABLE achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    target_points INTEGER,
    emoji TEXT,
    is_unlocked INTEGER DEFAULT 0,
    unlocked_at INTEGER,
    created_at INTEGER NOT NULL
);

CREATE TABLE todoist_tasks (
    id TEXT PRIMARY KEY,          -- Todoist task ID
    content TEXT NOT NULL,
    description TEXT,
    priority INTEGER,
    project_id TEXT,
    labels TEXT,                  -- JSON array
    due_date TEXT,
    is_completed INTEGER DEFAULT 0,
    points_earned INTEGER DEFAULT 0,
    synced_at INTEGER NOT NULL
);

CREATE TABLE ai_memory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category TEXT NOT NULL,
    content TEXT NOT NULL,
    confidence REAL DEFAULT 0.8,
    created_at INTEGER NOT NULL,
    last_accessed_at INTEGER NOT NULL,
    access_count INTEGER DEFAULT 0,
    source TEXT NOT NULL
);

CREATE TABLE ai_conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role TEXT NOT NULL,           -- 'user' or 'assistant'
    content TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

-- RPG System tables
CREATE TABLE player_stats (
    id INTEGER PRIMARY KEY DEFAULT 1,  -- singleton row
    str_stat INTEGER DEFAULT 0,        -- 0-100
    int_stat INTEGER DEFAULT 0,
    wis_stat INTEGER DEFAULT 0,
    dex_stat INTEGER DEFAULT 0,
    cha_stat INTEGER DEFAULT 0,
    vit_stat INTEGER DEFAULT 0,
    str_points_acc INTEGER DEFAULT 0,  -- accumulator (resets at 10 → +1 stat)
    int_points_acc INTEGER DEFAULT 0,
    wis_points_acc INTEGER DEFAULT 0,
    dex_points_acc INTEGER DEFAULT 0,
    cha_points_acc INTEGER DEFAULT 0,
    vit_points_acc INTEGER DEFAULT 0,
    total_points_earned INTEGER DEFAULT 0,
    rank TEXT DEFAULT 'E',             -- E, D, C, B, A, S
    streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    rank_up_streak_counter INTEGER DEFAULT 0,  -- consecutive streak days toward next rank (need 5)
    rank_down_break_counter INTEGER DEFAULT 0, -- consecutive break days toward rank down (5 = degrade)
    last_activity_at INTEGER,
    updated_at INTEGER NOT NULL
);

CREATE TABLE stat_mappings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source_type TEXT NOT NULL,     -- 'project', 'label'
    source_id TEXT NOT NULL,       -- Todoist project/label ID
    source_name TEXT NOT NULL,     -- Display name
    stat_type TEXT NOT NULL,       -- 'STR', 'INT', 'WIS', 'DEX', 'CHA', 'VIT'
    created_at INTEGER NOT NULL
);

CREATE TABLE decay_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    str_lost INTEGER DEFAULT 0,
    int_lost INTEGER DEFAULT 0,
    wis_lost INTEGER DEFAULT 0,
    dex_lost INTEGER DEFAULT 0,
    cha_lost INTEGER DEFAULT 0,
    vit_lost INTEGER DEFAULT 0,
    idle_hours INTEGER,
    reason TEXT,                   -- '24h_idle', '48h_idle', '72h_idle'
    created_at INTEGER NOT NULL
);

CREATE TABLE titles (
    id TEXT PRIMARY KEY,           -- 'early_bird', 'iron_will', etc.
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    emoji TEXT,
    is_unlocked INTEGER DEFAULT 0,
    unlocked_at INTEGER,
    progress INTEGER DEFAULT 0,   -- current progress toward unlock
    target INTEGER NOT NULL       -- target to unlock
);
```

---

## 9. Build Phases

### Phase 1 — Foundation + Glass UI + RPG Core (Week 1-2)
- [ ] Project setup: Kotlin, Compose, Room, Koin, Navigation
- [ ] Design system: Theme, Colors, Typography, GlassTokens
- [ ] Glass components: GlassCard, GlassButton, GlassTextField, GlassBottomBar
- [ ] Ambient background with gradient orbs
- [ ] Room database setup with ALL entities and DAOs (including RPG tables)
- [ ] DataStore for user preferences
- [ ] RPG engine: StatsEngine, RankCalculator, DecayEngine
- [ ] HexagonRadarChart composable (Canvas-drawn, animated)
- [ ] StatusWindow composable (the main character sheet)
- [ ] RankBadge + StatBar components
- [ ] Status Screen (home) with static/mock data
- [ ] Bottom navigation (5 tabs: Status, Tasks, Rewards, Agent, Settings)

### Phase 2 — Core Features + XP System (Week 2-3)
- [ ] XP earning flow: EarnXpUseCase with stat routing
- [ ] Stat mapping UI in settings (map Todoist projects/labels → stats)
- [ ] Level calculation + rank determination
- [ ] Rank-up animation (full-screen ceremony)
- [ ] Decay system: DecayWorker (midnight), cracked glass overlay
- [ ] Comeback bonus mechanic (1.5x after idle)
- [ ] Rewards: create, list, redeem with animations
- [ ] Missions: daily missions with stat assignment, completion tracking, streaks
- [ ] Titles/Achievements: unlock system with RPG titles, glass badges
- [ ] Transaction history: filterable list with glass cards, stat column
- [ ] Mood check-in: daily mood selector → WIS XP
- [ ] Full Stats screen (detailed breakdown, XP history per stat, decay log)
- [ ] Settings screen: profile, point rules, data management

### Phase 3 — Todoist Integration (Week 3-4)
- [ ] Todoist API client with Ktor
- [ ] Settings: API token input, validation, connection status
- [ ] Task sync: fetch, cache, detect completions
- [ ] WorkManager periodic sync (every 15 min)
- [ ] Tasks screen: today's tasks, upcoming, stat tag on each task
- [ ] Point calculation: priority-based + label multipliers → routed to mapped stat
- [ ] Stat mapping screen: assign Todoist projects/labels to STR/INT/WIS/DEX/CHA/VIT

### Phase 4 — Polish & Refinement (Week 4-5)
- [ ] All animations: hexagon breathing, stat vertex pulse, rank-up particles, decay warning
- [ ] Glass shader refinement (test on multiple devices)
- [ ] Fallback UI for Android < 13 (no AGSL shaders — use simple translucency)
- [ ] Error states: no internet, invalid API token, sync failures
- [ ] Empty states with RPG-themed messaging
- [ ] Agent tab placeholder: glass card with "Coming in next version" message
- [ ] App icon design (glass-style)
- [ ] Performance optimization: lazy loading, bitmap caching
- [ ] Testing: unit tests for ViewModels, integration tests for sync

---

## 10. Key Files at Project Root

```
/llm_memory.md         — Compressed project memory for LLM collaboration
/plan.md               — This file
/persona.yaml          — Default AI agent persona (future version, not created yet)
```

---

## 11. Migration Strategy

This is a **full rewrite**, not an incremental migration. However:

1. **Data export**: Before starting, the existing app's SharedPreferences data (points, rewards, missions) can be exported as JSON
2. **Data import**: The new app can optionally import this JSON into Room on first launch
3. **Same package name**: Keep `com.rewardpoints.app` so existing users get an update, not a separate install
4. **Version bump**: Jump to v3.0.0 to signal the major rewrite

---

## 12. Non-Functional Requirements

| Requirement | Target |
|---|---|
| Cold start | < 2 seconds |
| Glass rendering | 60fps on mid-range (Snapdragon 7 series+) |
| Todoist sync | < 3 seconds per sync |
| AI response | < 5 seconds (depends on Gemini API) |
| Offline mode | Full functionality except Todoist sync and AI chat |
| APK size | < 15MB |
| Min Android | 8.0 (API 26) |
| Battery | WorkManager respects Doze mode, no aggressive background |

**Points**: p1=4, p2=3, p3=2, p4=1. Every 10 points in a stat category = +1 stat point. Stats cap at 100.
**Ranks**: E→D→C→B→A→S (max). Streak-based: 5 consecutive days = rank up. 5 consecutive breaks = rank down.
**Decay**: 0 tasks in a day = all stats -1, break counter +1. Simple.
**Todoist**: Optional integration, app works standalone. Labels map to stats. Free plan confirmed working.
**AI Agent**: Deferred to v4.0. UI placeholder only in v3.0.
**Stack**: Kotlin/Compose/Room/Ktor/Koin. Dark-only. minSdk 26.

**Why:** User explicitly said "don't overcomplicate things." Original plan had % decay, XP multipliers, label bonuses — all stripped to simple integers.

**How to apply:** When in doubt, pick the simpler approach. No percentage math, no floating point, no multipliers. Integer arithmetic only for points/stats.
