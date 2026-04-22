// Gradle 9.0.0 can't run in Java8
plugins {
    id("io.kotest") version "6.1.11"
    id("com.gradleup.shadow") version "8.3.10"
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

group = "be4rjp"
description = "Sclat"

allprojects {
    apply {
        plugin("io.kotest")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
    }

    version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"

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
        compileOnly("com.github.koca2000:NoteBlockAPI:1.6.3")
        compileOnly("com.github.ucchyocean:LunaChat:3.0.16")
        compileOnly("net.dmulloy2:ProtocolLib:5.1.0")
        compileOnly("com.github.Be4rJP:DADADAChecker:1.0.0")
        compileOnly("com.github.Be4rJP:BlockStudio:-SNAPSHOT")

        implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0-rc01")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
        implementation("org.incendo:cloud-paper:2.0.0-SNAPSHOT") // found newer 2.0.0-beta.10
        implementation("org.jspecify:jspecify:1.0.0")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("fr.mrmicky:fastboard:2.1.5")
        implementation("com.akuleshov7:ktoml-core:0.7.1")

        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("io.kotest:kotest-runner-junit5:6.1.11")
        testImplementation("io.kotest:kotest-assertions-core:6.1.11")
        testImplementation("io.kotest:kotest-property:6.1.11")
        testImplementation("io.mockk:mockk:1.14.9")
        testImplementation("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
        testImplementation("com.destroystokyo.paper:paper-api:1.14.4-R0.1-SNAPSHOT")
        testImplementation("ch.qos.logback:logback-classic:1.4.11")
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
