package com.ebay.plugins.graph.analytics

import org.gradle.api.artifacts.Configuration

/**
 * Default implementation of the [ConfigurationClassifier].
 */
internal class ConfigurationClassifierDefault : ConfigurationClassifier {
    override fun classify(configuration: Configuration): ConfigurationClass {
        val configName = configuration.name
        return when {
            configName.startsWith("test") || configName.startsWith("androidTest") -> {
                ConfigurationClass.TEST
            }
            configName.startsWith("api") || configName.startsWith("implementation") -> {
                ConfigurationClass.PRODUCTION
            }
            else -> {
                ConfigurationClass.OTHER
            }
        }
    }
}