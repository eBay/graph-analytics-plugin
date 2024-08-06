package com.ebay.plugins.graph.analytics

import org.gradle.api.tasks.CacheableTask
import org.jgrapht.alg.scoring.BetweennessCentrality
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

/**
 * This task applies the Betweenness Centrality analysis to the graph.
 */
@CacheableTask
abstract class BetweennessCentralityAnalysisTask : BaseGraphInputOutputTask() {
    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        val betweennessCentrality by lazy { BetweennessCentrality(graph) }

        graph.vertexSet().forEach { vertexInfo ->
            vertexInfo.attributes["betweennessCentrality"] = DefaultAttribute(betweennessCentrality.getVertexScore(vertexInfo), AttributeType.DOUBLE)
        }
    }

    companion object {
        const val TASK_NAME = "graphBetweennessCentralityAnalysis"
    }
}