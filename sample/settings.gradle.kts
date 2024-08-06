@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("conventions")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics-sample"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":lib1-api")
include(":lib1-impl")
include(":lib1-test-support")
include(":lib2-api")
include(":lib2-impl")
include(":lib2-test-support")
