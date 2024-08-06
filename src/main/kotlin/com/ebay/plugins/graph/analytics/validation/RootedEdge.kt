package com.ebay.plugins.graph.analytics.validation

import com.ebay.plugins.graph.analytics.Attributed
import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.Attribute

/**
 * A tuple of a [VertexInfo] representing the root vertex being analyzed for volations and
 * an [EdgeInfo] representing the edge being immediately considered for match (e.g., a depenency
 * upon another vertex).  This allows for more complex rules to be created which consider the overall scope.
 */
class RootedEdge(
    val graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
    val root: VertexInfo,
    val edge: EdgeInfo,
): Attributed, Summarized {
    /**
     * Expose the attributes of the underlying edge
     */
    override var attributes: MutableMap<String, Attribute> = edge.attributes

    override fun toString(): String {
        return edge.toString()
    }

    override fun getSummary(): String {
        val source = graph.getEdgeSource(edge)
        val target = graph.getEdgeTarget(edge)
        return "${source.path} -> ${target.path}"
    }
}