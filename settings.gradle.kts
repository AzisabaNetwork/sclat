rootProject.name = "Sclat"

plugins {
    id("com.gradle.develocity") version ("4.3.2")
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { true }
    }
}
