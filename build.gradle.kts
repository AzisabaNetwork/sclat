// Gradle 9.0.0 can't run in Java8
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotest)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jmh)
}

group = "be4rjp"
version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"
description = "Sclat"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    exclusiveContent {
        forRepository {
            mavenLocal()
        }
        filter {
            includeGroup("org.spigotmc")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://repo.codemc.io/repository/maven-public/")
        }
        filter {
            includeGroup("org.bstats")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://repo.onarandombox.com/content/groups/public/")
        }
        filter {
            includeGroup("com.onarandombox.multiversecore")
        }
    }
}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.noteblockapi)
    compileOnly(libs.lunachat)
    compileOnly(libs.protocolLib)
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    compileOnly(libs.paperApi)
//    compileOnly(libs.multiverse.core)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)
    implementation(libs.yamlkt)
    implementation(libs.fastboard)
    implementation(libs.bundles.ktoml)
    implementation(libs.bundles.adventure)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
    testImplementation(libs.spigot)
    // Needed for serializer tests
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    jmhImplementation(libs.kotlin.stdlib)
    jmhImplementation(libs.bundles.adventure)
    jmhImplementation(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.generator.annprocess)
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

    test {
        useJUnitPlatform()
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
