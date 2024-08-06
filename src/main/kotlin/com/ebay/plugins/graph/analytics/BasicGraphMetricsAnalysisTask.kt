package com.ebay.plugins.graph.analytics

import org.gradle.api.tasks.CacheableTask
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

/**
 * This task applies the most basic graph metrics that require no scoring algorithm to the graph.
 */
@CacheableTask
abstract class BasicGraphMetricsAnalysisTask : BaseGraphInputOutputTask() {
    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        graph.vertexSet().forEach { vertexInfo ->
            vertexInfo.attributes["degree"] = DefaultAttribute(graph.degreeOf(vertexInfo), AttributeType.INT)
            vertexInfo.attributes["inDegree"] = DefaultAttribute(graph.inDegreeOf(vertexInfo), AttributeType.INT)
            vertexInfo.attributes["outDegree"] = DefaultAttribute(graph.outDegreeOf(vertexInfo), AttributeType.INT)
        }
    }

    companion object {
        const val TASK_NAME = "basicGraphMetricsAnalysis"
    }
}