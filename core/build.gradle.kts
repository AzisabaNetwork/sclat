plugins {
    id("org.jetbrains.kotlin.jvm")
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
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
