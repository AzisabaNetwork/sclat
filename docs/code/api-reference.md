# API Reference — Key Entry Points

This quick reference points to commonly used public classes and where to find them.

- be4rjp.sclat.Sclat — plugin entry, lifecycle (onEnable/onDisable).
- be4rjp.sclat.api.GaugeAPI, GlowingAPI, Hologram utilities — in api/ for UI helpers.
- be4rjp.sclat.manager.MatchMgr — core match lifecycle and player management.
- be4rjp.sclat.manager.PaintMgr — painting and block updates (see data/BlockUpdater).
- be4rjp.sclat.api.packet.WorldPackets, Packets — packet creation utilities and helpers.
- be4rjp.sclat.api.raytrace.RayTrace — ray tracing utilities used by weapons and targeting.
- be4rjp.sclat.data.MapData, MapDataMgr — map definitions and management.
- be4rjp.sclat.commands.SclatCommandExecutor — central command handling.

Tests of interest:
- src/test/kotlin/.../ReadyAnimationTest.kt and TextAnimationTest.kt — animation behavior tests.
- src/test/kotlin/.../MatchWorldSelectorTest.kt — world selection tests.

Recommended next steps for contributors:
- Run ./gradlew ktlintCheck && ./gradlew test before opening PRs.
- Read AGENTS.md for style and testing conventions.
