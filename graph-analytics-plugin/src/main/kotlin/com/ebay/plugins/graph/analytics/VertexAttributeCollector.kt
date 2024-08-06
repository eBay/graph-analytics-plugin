package com.ebay.plugins.graph.analytics

/**
 * Collector which can generate vertex information at configuration time.  This work must be
 * very lightweight but gives an opportunity to collect information from or about installed
 * plugins, etc.
 *
 * The collected information is applied and aggregated prior to the analysis phase.
 */
interface VertexAttributeCollector {
    fun collectConfigurationTimeAttributes(vertexInfo: VertexInfo)
}