---
name: Key Technical Decisions
description: Simplified RPG system, streak-based ranks (max S), p1=4pts, stats cap 100, AI deferred, Todoist optional
type: project
---

**Points**: p1=4, p2=3, p3=2, p4=1. Every 10 points in a stat category = +1 stat point. Stats cap at 100.
**Ranks**: E→D→C→B→A→S (max). Streak-based: 5 consecutive days = rank up. 5 consecutive breaks = rank down.
**Decay**: 0 tasks in a day = all stats -1, break counter +1. Simple.
**Todoist**: Optional integration, app works standalone. Labels map to stats. Free plan confirmed working.
**AI Agent**: Deferred to v4.0. UI placeholder only in v3.0.
**Stack**: Kotlin/Compose/Room/Ktor/Koin. Dark-only. minSdk 26.

**Why:** User explicitly said "don't overcomplicate things." Original plan had % decay, XP multipliers, label bonuses — all stripped to simple integers.

**How to apply:** When in doubt, pick the simpler approach. No percentage math, no floating point, no multipliers. Integer arithmetic only for points/stats.
