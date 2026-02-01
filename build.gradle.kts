// Gradle 9.0.0 can't run in Java8
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    compileOnly(files("libs/ProtocolLib.jar"))
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    compileOnly(libs.paperApi)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)
    implementation(libs.yamlkt)
}

// Project Settings
val targetJavaVersion: Int = 11
val defaultEncoding: String = "UTF-8"

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn(shadowJar)
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
