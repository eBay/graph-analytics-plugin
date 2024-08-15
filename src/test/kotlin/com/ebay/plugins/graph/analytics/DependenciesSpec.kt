package com.ebay.plugins.graph.analytics

/**
 * Model for dependencies of a project module.
 */
data class DependenciesSpec(
    val dependencies: List<String> = emptyList(),
    val testDependencies: List<String> = emptyList(),
)
