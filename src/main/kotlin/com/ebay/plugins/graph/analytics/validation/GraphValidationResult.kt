package com.ebay.plugins.graph.analytics.validation

/**
 * The result of validating an individual project graph.
 */
internal sealed interface GraphValidationResult {
    val graphId: String
}

internal data class GraphVertexNotFound(
    override val graphId: String
): GraphValidationResult

internal data class GraphValidation(
    override val graphId: String,
    val rootedVertex: RootedVertex,
    val violations: Map<String, GraphValidationRule> = emptyMap(),
    val ignoredViolations: Map<String, GraphValidationRule> = emptyMap(),
    val ignoredButValid: List<String> = emptyList(),
) : GraphValidationResult