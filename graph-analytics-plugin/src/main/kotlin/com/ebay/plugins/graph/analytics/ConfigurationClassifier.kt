package com.ebay.plugins.graph.analytics

import org.gradle.api.artifacts.Configuration

/**
 * Used to determine what [ConfigurationClassifier] a [Configuration] should considered to
 * be in.
 */
interface ConfigurationClassifier {
    fun classify(configuration: Configuration): ConfigurationClass
}