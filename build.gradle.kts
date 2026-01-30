// Gradle 9.0.0 can't run in Java8
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
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
    compileOnly(libs.spigot)
    compileOnly(libs.noteblockapi)
    compileOnly(libs.lunachat)
//    compileOnly(libs.protocolLib)
    compileOnly(files("libs/ProtocolLib.jar"))
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    compileOnly(libs.paperApi)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)
}

// Project Settings
val targetJavaVersion: Int = 11
val defaultEncoding: String = "UTF-8"

kotlin {
    jvmToolchain(targetJavaVersion)
}

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
    build {
        dependsOn(shadowJar)
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
        indentWithSpaces(4)
        toggleOffOn()
        removeUnusedImports()
        endWithNewline()
        eclipse()
    }
    kotlin {
        ktlint()
        target("src/main/java/**/*.kt", "src/main/kotlin/**/*.kt")
        endWithNewline()
        trimTrailingWhitespace()
        indentWithSpaces(4)
    }
}
