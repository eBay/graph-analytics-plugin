package com.ebay.plugins.graph.analytics

import org.jgrapht.Graph

/**
 * Internal helper to merge two graphs together, preserving attributes.
 */
internal object GraphUtils {
     fun Graph<VertexInfo, EdgeInfo>.merge(other: Graph<VertexInfo, EdgeInfo>) {
         other.vertexSet().forEach { vertex ->
             mergeVertex(vertex)
         }
         other.edgeSet().forEach { otherEdge ->
             val source = mergeVertex(other.getEdgeSource(otherEdge))
             val target = mergeVertex(other.getEdgeTarget(otherEdge))
             val removedEdge = removeEdge(source, target)
             addEdge(source, target).apply {
                 removedEdge?.let { merge(it) }
                 merge(otherEdge)
             }
         }
     }

    private fun Graph<VertexInfo, EdgeInfo>.mergeVertex(
        vertex: VertexInfo
    ): VertexInfo {
        return if (addVertex(vertex)) {
            vertex
        } else {
            vertexSet().find { it.hashCode() == vertex.hashCode() }!!.merge(vertex)
        }
    }

    private fun VertexInfo.merge(other: VertexInfo) = apply {
        require(path == other.path)
        attributes.putAll(other.attributes)
    }

    private fun EdgeInfo.merge(other: EdgeInfo) = apply {
        attributes.putAll(other.attributes)
    }
}