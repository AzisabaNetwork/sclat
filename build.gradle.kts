// Gradle 9.0.0 can't run in Java8
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.runPaper)
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
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
    compileOnly(libs.spigotApi)
    compileOnly(libs.spigot)
    compileOnly(libs.bukkit)
    compileOnly(libs.noteblockapi)
    compileOnly(libs.lunachat)
    compileOnly(libs.protocolLib)
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    compileOnly(libs.paperApi)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
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
        downloadPlugins {
            url("https://github.com/ucchyocean/LunaChat/releases/download/v3.0.16/LunaChat.jar") // LunaChat 3.0.16
            url("https://github.com/koca2000/NoteBlockAPI/releases/download/1.6.3/NoteBlockAPI-1.6.3.jar") // NoteBlockAPI 1.6.3
            url("https://github.com/dmulloy2/ProtocolLib/releases/download/4.6.0/ProtocolLib.jar") // ProtocolLib 4.6.0
        }
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

    shadowJar {
        minimize()
        mergeServiceFiles()
        isEnableRelocation = true
        relocationPrefix = "libs.be4rjp.sclat"
        archiveFileName.set("Sclat.jar")
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
