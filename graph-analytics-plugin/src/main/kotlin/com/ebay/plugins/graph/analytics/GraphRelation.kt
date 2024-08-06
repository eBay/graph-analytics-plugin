package com.ebay.plugins.graph.analytics

/**
 * Relationship between two vertices and and edge.
 */
internal data class GraphRelation(
    val from: String,
    val to: String,
    val edge: EdgeInfo
) : java.io.Serializable
