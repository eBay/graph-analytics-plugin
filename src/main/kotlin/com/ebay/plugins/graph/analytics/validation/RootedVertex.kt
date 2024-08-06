package com.ebay.plugins.graph.analytics.validation

import com.ebay.plugins.graph.analytics.Attributed
import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.Attribute

/**
 * A tuple of a [VertexInfo] representing the root vertex being analyzed for volations and
 * an [VertexInfo] representing the vertex being immediately considered for match (e.g., a child
 * vertex).  This allows for more complex rules to be created which consider the overall scope.
 */
class RootedVertex(
    val graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
    val root: VertexInfo,
    val vertex: VertexInfo = root,
) : Attributed, Summarized {
    /**
     * Expose the attributes of the underlying vertex
     */
    override var attributes: MutableMap<String, Attribute> = vertex.attributes

    override fun toString(): String {
        return vertex.toString()
    }

    override fun getSummary(): String {
        return vertex.path
    }
}