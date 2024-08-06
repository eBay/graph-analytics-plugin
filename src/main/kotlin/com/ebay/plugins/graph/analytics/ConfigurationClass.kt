package com.ebay.plugins.graph.analytics

/**
 * Gradle project configuration classifications.
 */
enum class ConfigurationClass {
    /**
     * The configuration is included in production code.
     */
    PRODUCTION,

    /**
     * The configuration is included for testing.
     */
    TEST,

    /**
     * The configuration is something else that we don't care about.
     */
    OTHER
}