// Gradle 9.0.0 can't run in Java8
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.runPaper)
    alias(libs.plugins.spotless)
}

group = "be4rjp"
version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"
description = "Sclat"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://raw.githubusercontent.com/Rayzr522/maven-repo/master/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.comphenix.net/content/groups/public/")
}

dependencies {
    api(libs.spigotApi)
    api(libs.spigot)
    api(libs.bukkit)
    api(libs.noteblockapi)
    api(libs.lunachat)
    api(files("libs/ProtocolLib.jar"))
    api(libs.dadadachecker)
    api(libs.blockstudio)
    compileOnly(libs.paperApi)
}

// Project Settings
val targetJavaVersion = 8
val defaultEncoding: String = "UTF-8"

// Java Version Setup
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks {
    runServer {
        minecraftVersion("1.14.4")
    }

    compileJava {
        options.encoding = defaultEncoding

        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }

    javadoc {
        options.encoding = defaultEncoding
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.jar)
        }
    }

    repositories {
        maven {
            name = "azisaba-repo"
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            url =
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    uri("https://repo.azisaba.net/repository/maven-snapshots/")
                } else {
                    uri("https://repo.azisaba.net/repository/maven-releases/")
                }
        }
    }
}

spotless {
    format("misc") {
        target(".gitignore")
        trimTrailingWhitespace()
        indentWithSpaces(4)
        endWithNewline()
    }
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        toggleOffOn()
        removeUnusedImports()
        endWithNewline()
        eclipse()
    }
}
