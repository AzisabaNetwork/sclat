<!--
  SYNC IMPACT REPORT
  ==================
  Version change: (new) 1.0.0
  
  Modified principles: N/A (initial constitution)
  Added sections: All (initial creation)
  Removed sections: N/A
  
  Templates requiring updates:
  - .specify/templates/plan-template.md: ✅ Compatible (Constitution Check section references constitution)
  - .specify/templates/spec-template.md: ✅ Compatible (no direct constitution references)
  - .specify/templates/tasks-template.md: ✅ Compatible (no direct constitution references)
  
  Follow-up TODOs: None
-->

# Sclat Constitution

## Core Principles

### I. Kotlin Idioms & Type Safety

- All code MUST use Kotlin nullable types explicitly (`Type?`)
- Safe calls (`?.`) MUST be preferred over force-null assertions (`!!`)
- `require()` MUST be used for argument validation in constructors/functions
- Extension functions MUST be minimal, focused, and placed in the `extension` package
- Manager objects MUST use Kotlin `object` declaration with `Mgr` suffix

**Rationale**: Kotlin's null safety and extension functions are core language features that reduce runtime errors and improve code expressiveness.

### II. Code Style Consistency

- All code MUST pass `ktlintCheck` before commit
- Indentation MUST be 4 spaces (no tabs)
- Maximum line length MUST be 120 characters
- Imports MUST be organized alphabetically with explicit imports (no wildcards except in tests)
- Opening braces MUST be on the same line for functions/classes
- Trailing commas MUST be used in multi-line collections/parameter lists

**Rationale**: Consistent code style reduces cognitive load and makes code reviews more efficient.

### III. Bukkit Compatibility

- Events MUST be registered in `onEnable()`
- Async/repeating tasks MUST use `BukkitRunnable`
- Deprecated Bukkit APIs MUST be suppressed with `@file:Suppress("DEPRECATION")` at file top
- Text components MUST use Adventure API (`net.kyori.adventure.text.Component`)
- Plugin reference MUST be stored in companion object and accessed via `plugin` import alias

**Rationale**: Bukkit/Spigot APIs have specific lifecycle and threading requirements that must be followed for stable plugin operation.

### IV. Testing Discipline

- Tests MUST use Kotest with `StringSpec` style
- Test class names MUST end with `Test`
- Tests MUST be placed in `src/test/kotlin/` mirroring source structure
- Test names MUST be descriptive (Japanese acceptable)
- `./gradlew ktlintCheck && ./gradlew test` MUST pass after code modifications

**Rationale**: Tests serve as living documentation and catch regressions early. Consistent test structure makes tests discoverable and maintainable.

### V. Documentation & Comments

- Public APIs MUST use KDoc (`/** */`) documentation
- Major classes MUST include `@author` tag
- Comments MUST be concise and in English or Japanese
- Redundant comments that repeat code MUST be avoided
- Comments MUST NOT be added unless explicitly requested

**Rationale**: Documentation should add value, not noise. Over-commenting degrades code readability.

## Technology Constraints

### Platform & Runtime

- **Target**: Spigot 1.14.4-R0.1-SNAPSHOT
- **Language**: Kotlin targeting Java 11
- **Build System**: Gradle with version catalogs
- **Output**: Plugin JAR at `build/libs/Sclat.jar`

### Dependencies

- Spigot API: compileOnly, local Maven
- ProtocolLib: compileOnly
- Adventure API: implementation
- Kotest: testImplementation

### Project Structure

```
src/main/kotlin/be4rjp/sclat/
├── animation/      # Animation classes
├── api/            # Core API and utilities
├── commands/       # Command executors
├── config/         # Configuration handling
├── data/           # Data classes and models
├── extension/      # Kotlin extension functions
├── gui/            # GUI/menu handling
├── listener/       # Bukkit event listeners
├── manager/        # Manager singletons (business logic)
├── protocollib/    # ProtocolLib packet handlers
├── server/         # Server communication
├── weapon/         # Weapon implementations
└── Sclat.kt        # Main plugin class
```

## Development Workflow

### Pre-commit Checks

1. Run `./gradlew ktlintCheck` to verify code style
2. Run `./gradlew test` to verify all tests pass
3. Verify build succeeds with `./gradlew build`

### Code Review Requirements

- Changes MUST follow naming conventions (PascalCase for classes, camelCase for functions/properties)
- `@JvmField` MUST be used for fields accessed from Java code
- Error handling MUST use `try-catch` around operations that may throw
- Exceptions MUST be logged with appropriate severity

### Build & Deployment

1. Build with `./gradlew build`
2. Copy `build/libs/Sclat.jar` to server `plugins/` directory
3. Ensure DADADAChecker plugin is installed on server

## Governance

This constitution establishes the non-negotiable rules for the Sclat project. All code changes MUST comply with these principles.

### Amendment Procedure

1. Propose amendment with clear rationale
2. Document impact on existing code
3. Increment version following semantic versioning:
   - MAJOR: Backward incompatible principle changes
   - MINOR: New principles or materially expanded guidance
   - PATCH: Clarifications, wording fixes
4. Update dependent templates if affected

### Compliance

- All pull requests MUST pass ktlint and test checks
- Complexity beyond established patterns MUST be justified
- Use `AGENTS.md` for runtime development guidance

**Version**: 1.0.0 | **Ratified**: 2026-02-19 | **Last Amended**: 2026-02-19
