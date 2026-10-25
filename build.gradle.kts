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
    compileOnly(libs.spigot)
    compileOnly(libs.noteblockapi)
    compileOnly(libs.lunachat)
    compileOnly(libs.protocolLib)
    compileOnly(libs.dadadachecker)
    compileOnly(libs.blockstudio)
    compileOnly(libs.paperApi)
    implementation(libs.cloudPaper)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)
    implementation(libs.yamlkt)
    implementation(libs.fastboard)
    implementation(libs.bundles.ktoml)
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

    // 実サーバー(Spigot 1.14.4 + 依存プラグイン)を起動し、ボット2体で試合を1つ通す。
    //   ./gradlew scenarioTest                 -> Normal
    //   ./gradlew scenarioTest -PworkMode=TDM  -> モード指定
    // CI では回さない(Spigot の BuildTools と node/pnpm が要る)。詳細は e2e/README.md。
    register<Exec>("scenarioTest") {
        group = "verification"
        description = "Runs a full match end-to-end against a real Spigot server (e2e/run.sh)"
        dependsOn(shadowJar)
        workingDir = rootDir
        environment("SCLAT_SKIP_BUILD", "1")
        val modeArgs = (findProperty("workMode") as String?)?.let { listOf("--work-mode", it) } ?: emptyList()
        commandLine(listOf("bash", "e2e/run.sh") + modeArgs)
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
