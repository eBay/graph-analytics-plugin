package com.ebay.plugins.graph.analytics

import org.gradle.api.tasks.CacheableTask
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

/**
 * Analysis task which calculates the vertex height for all vertices in the graph.
 */
@CacheableTask
abstract class VertexHeightAnalysisTask : BaseGraphInputOutputTask() {
    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        val height by lazy { VertexHeightScoringAlgorithm(graph) }

        graph.vertexSet().forEach { vertexInfo ->
            vertexInfo.attributes["height"] = DefaultAttribute(height.getVertexScore(vertexInfo), AttributeType.INT)
        }
    }

    companion object {
        const val TASK_NAME = "graphVertexHeightAnalysis"
    }
}