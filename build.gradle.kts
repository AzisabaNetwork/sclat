// Gradle 9.0.0 can't run in Java8
plugins {
    alias(libs.plugins.ktlint)
    id("io.kotest") version "6.1.11"
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "be4rjp"
version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"
description = "Sclat"

allprojects {
    apply {
        plugin("io.kotest")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
    }

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
    }

    dependencies {
        compileOnly("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
        compileOnly("com.destroystokyo.paper:paper-api:1.14.4-R0.1-SNAPSHOT")
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("io.kotest:kotest-runner-junit5:6.1.11")
        testImplementation("io.kotest:kotest-assertions-core:6.1.11")
        testImplementation("io.kotest:kotest-property:6.1.11")
        testImplementation("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
        testImplementation("com.destroystokyo.paper:paper-api:1.14.4-R0.1-SNAPSHOT")
    }

    val targetJavaVersion: Int = 11

    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}

dependencies {
    compileOnly(libs.noteblockapi)
    compileOnly(libs.lunachat)
    compileOnly(libs.protocolLib)
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)
    implementation(libs.yamlkt)
    implementation(libs.fastboard)
    implementation(libs.bundles.ktoml)
    implementation(project("core"))
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
