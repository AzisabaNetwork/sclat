# Sclat — Project Overview

Sclat is a Minecraft Spigot 1.14.4 plugin written in Kotlin that implements a squid painting game. It targets Java 11, uses Gradle, and produces a plugin JAR at build/libs/Sclat.jar.

Key locations:
- Main plugin: src/main/kotlin/be4rjp/sclat/Sclat.kt
- Core API and utilities: src/main/kotlin/be4rjp/sclat/api/
- Game logic and managers: src/main/kotlin/be4rjp/sclat/manager/
- Weapon implementations: src/main/kotlin/be4rjp/sclat/weapon/
- Data models: src/main/kotlin/be4rjp/sclat/data/

Common commands:
- Build: ./gradlew build
- Build without tests: ./gradlew build -x test
- Create shadow JAR: ./gradlew shadowJar
- Run tests: ./gradlew test

Development notes:
- Use ktlint for formatting: ./gradlew ktlintCheck / ktlintFormat
- Copy build/libs/Sclat.jar into a Spigot server plugins/ directory to run in-game.
