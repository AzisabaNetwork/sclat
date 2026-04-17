plugins {
    alias(libs.plugins.kotest)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "net.azisaba.sclat"
version = "1.0-SNAPSHOT"

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
    compileOnly(libs.paperApi)
    implementation(libs.yamlkt)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.spigot)
    testImplementation(libs.paperApi)
}

kotlin {
    jvmToolchain(11)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
