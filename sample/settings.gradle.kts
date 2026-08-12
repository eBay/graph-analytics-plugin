@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("..")
}

plugins {
    id("com.ebay.graph-analytics")
    id("com.gradle.develocity") version("4.5.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graph-analytics-sample"

val isCI = System.getenv("CI") != null

develocity {
    server = "https://community.develocity.cloud"
    projectId = "ebay"
    buildScan {
        uploadInBackground = !isCI
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
        }
    }
}

buildCache {
    local {
        isEnabled = true
    }

    remote(develocity.buildCache) {
        isEnabled = true
        // Check access key presence to avoid build cache errors on PR builds when access key is not present
        val accessKey = System.getenv("DEVELOCITY_ACCESS_KEY")
        isPush = isCI && accessKey != null
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
