# AGENTS.md

Coding agent guidelines for the Sclat Spigot 1.14.4 plugin project.

## Project Overview

Sclat is a Minecraft Spigot 1.14.4 plugin implementing a squid painting game. Written in Kotlin, targeting Java 11, using Gradle with version catalogs.

## Build Commands

```bash
# Build the plugin JAR (outputs build/libs/Sclat.jar)
./gradlew build

# Build without running tests
./gradlew build -x test

# Create shadow JAR only (fat JAR with relocated dependencies)
./gradlew shadowJar
```

## Lint Commands

```bash
# Check code style with ktlint
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Run all checks (includes ktlint and tests)
./gradlew check
```

## Test Commands

```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "be4rjp.sclat.animation.text.ReadyAnimationTest"

# Run a single test method
./gradlew test --tests "be4rjp.sclat.animation.text.ReadyAnimationTest.READYのアニメーション遷移"

# Run tests with verbose output
./gradlew test --info
```

## Development Setup

1. Build the plugin with `./gradlew build`
2. Copy `build/libs/Sclat.jar` to your Spigot server's `plugins/` directory
3. Install required plugins: [DADADAChecker](https://github.com/bea4dev/DADADAChecker/releases/tag/1.0.0)

## Code Style Guidelines

### Imports

- Imports are organized alphabetically
- Use explicit imports; avoid wildcard imports except in tests
- Order: standard library, third-party libraries, project classes
- Use `@file:Suppress("DEPRECATION")` at file top when using deprecated Bukkit APIs

```kotlin
import be4rjp.sclat.data.Match
import org.bukkit.entity.Player
import java.util.UUID
```

### Formatting

- Indentation: 4 spaces (no tabs)
- Maximum line length: 120 characters
- One statement per line
- Blank line between logical sections
- Opening brace on same line for functions/classes
- Use trailing commas in multi-line collections/parameter lists

### Types and Nullability

- Use Kotlin nullable types explicitly (`Type?`)
- Prefer safe calls (`?.`) over null checks with `!!`
- Use `require()` for argument validation in constructors/functions
- Use `@JvmField` for fields accessed from Java code
- Use `lateinit var` for lateinit properties

```kotlin
class ReadyAnimation(private val text: String) {
    init {
        require(text.isNotBlank()) { "text must not be blank" }
    }
}
```

### Naming Conventions

- **Classes/Objects/Interfaces**: PascalCase (`Match`, `MatchMgr`, `PlayerData`)
- **Functions/Methods**: camelCase (`playerJoinMatch`, `addPlayerCount`)
- **Properties/Variables**: camelCase (`playerCount`, `isFinished`)
- **Constants**: SCREAMING_SNAKE_CASE in companion objects (`VERSION`)
- **Package names**: lowercase (`be4rjp.sclat.manager`)
- **Extension functions**: Place in `extension` package (`Component.kt`, `String.kt`)
- **Manager objects**: Use `object` declaration with `Mgr` suffix (`MatchMgr`, `ColorMgr`)

### Singleton Pattern

Use Kotlin `object` declarations for managers and utilities:

```kotlin
object MatchMgr {
    @JvmField
    var matchcount: Int = 0
    
    @JvmStatic
    fun playerJoinMatch(player: Player) { ... }
}
```

### Error Handling

- Use `try-catch` around operations that may throw
- Log errors with appropriate severity
- Use `require()` for precondition checks
- Avoid catching generic `Exception` unless necessary

```kotlin
try {
    shutdownAll()
} catch (e: Exception) {
    e.printStackTrace()
}
```

### Bukkit-specific Conventions

- Register events in `onEnable()`
- Use `BukkitRunnable` for async/repeating tasks
- Store plugin reference in companion object: `internal lateinit var plugin: Sclat`
- Access via `plugin` import alias from `Variables.kt`
- Use Adventure API for text components: `net.kyori.adventure.text.Component`

## Testing Conventions

- Use Kotest with `StringSpec` style
- Use Mockk for mocking
- Test class names end with `Test`
- Place tests in `src/test/kotlin/` mirroring source structure
- **Test case names must be in Japanese for visibility** - use descriptive Japanese names in quotes

```kotlin
class ReadyAnimationTest : StringSpec({
    "READYのアニメーション遷移" {
        val anim = ReadyAnimation("READY")
        anim.next() shouldBe "§7READY"
    }
    
    "空白文字を渡すと例外を投げること" {
        shouldThrow<IllegalArgumentException> {
            ReadyAnimation(" ")
        }
    }
})
```

### Test Dependencies

- **Kotest**: `kotest-assertions-core`, `kotest-runner-junit5`, `kotest-property`
- **Mockk**: `mockk` for mocking Kotlin classes

### Data Classes and POJOs

- Use regular classes with properties for data holders
- Use `@JvmField` for Java interoperability
- Provide getter/setter methods when additional logic needed

```kotlin
class Match(private val id: Int) {
    @JvmField
    var team0: Team? = null
    
    var playerCount: Int = 0
        private set
}
```

### Extension Functions

Keep extension functions minimal and focused. Place in dedicated files:

```kotlin
// extension/Component.kt
fun Component.serializeString() = BukkitComponentSerializer.legacy().serialize(this)

// extension/String.kt
fun String.toMaterial(): Material? = Material.getMaterial(this)
```

### Comments and Documentation

- Use KDoc (`/** */`) for public APIs
- Include `@author` tag for major classes
- Keep comments concise and in English or Japanese
- Avoid redundant comments that repeat code

## Project Structure

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

## Dependencies

- Spigot 1.14.4-R0.1-SNAPSHOT (compileOnly, local Maven)
- ProtocolLib (compileOnly)
- Adventure API (implementation)
- Kotest (testImplementation)
- Mockk (testImplementation)
- See `gradle/libs.versions.toml` for all dependencies

## After Making Changes

Run these commands after modifying code:

```bash
./gradlew ktlintCheck && ./gradlew test
```
