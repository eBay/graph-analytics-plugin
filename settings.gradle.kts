@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("graph-analytics-plugin")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
includeBuild("sample")
