@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("..")
}

plugins {
    id("com.ebay.graph-analytics")
    id("com.gradle.develocity") version("4.0.2")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics-sample"

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":lib1-api")
include(":lib1-impl")
include(":lib1-test-support")
include(":lib2-api")
include(":lib2-impl")
include(":lib2-test-support")
