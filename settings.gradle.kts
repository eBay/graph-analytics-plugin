@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version("3.19.2")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics-plugin"

develocity {
    buildScan {
        server.set("https://gradle.corp.ebay.com")
    }
}
