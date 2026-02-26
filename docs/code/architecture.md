# Architecture Overview

This document maps high-level components to their packages and responsibilities to help contributors find where to change behavior.

- Plugin bootstrap
  - be4rjp.sclat.Sclat.kt — main plugin class, registers commands and listeners.

- Core API
  - be4rjp.sclat.api — utility APIs (holograms, packet helpers, async tasks, raytrace, config serializers).

- Managers and game logic
  - be4rjp.sclat.manager — singletons controlling game state (MatchMgr, GameMgr, MapDataMgr, PaintMgr, etc.).
  - be4rjp.sclat.match — match types and states.

- Data and models
  - be4rjp.sclat.data — Match, MapData, PaintData, RegionBlocks and other domain models.

- Weapons and combat
  - be4rjp.sclat.weapon — weapon base classes and implementations (Blaster, Charger, Roller, SPWeapon, subweapons/).

- Listeners and ProtocolLib integration
  - be4rjp.sclat.listener — Bukkit event listeners.
  - be4rjp.sclat.protocollib — packet listeners and handlers.

- Utilities and extensions
  - be4rjp.sclat.extension — Kotlin extension functions used across the codebase.

Notes:
- Follow coding conventions in AGENTS.md (ktlint, formatting, naming) when contributing.
