@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../..")
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics-sample-conventions"