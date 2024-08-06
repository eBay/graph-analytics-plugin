package com.ebay.plugins.graph.analytics

import org.gradle.api.GradleException
import org.gradle.api.tasks.CacheableTask
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

/**
 * This task analyzes the graph and calculates network size information as well as the network
 * expansion factor for each vertex.
 */
@CacheableTask
internal abstract class NetworkExpansionAnalysisTask : BaseGraphInputOutputTask() {
    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        val networkAbove by lazy { ReachableNodesScoringAlgorithm(graph, ReachableNodesDirection.INCOMING) }
        val networkBelow by lazy { ReachableNodesScoringAlgorithm(graph, ReachableNodesDirection.OUTGOING) }

        graph.vertexSet().forEach { vertexInfo ->
            val networkAboveValue = networkAbove.getVertexScore(vertexInfo)
            val networkBelowValue = networkBelow.getVertexScore(vertexInfo)

            // This data comes from the [BasicGraphAnalysisTask] and is required to be present.  This task must
            // depend upon that task in order to ensure this data will exist.
            val inDegree = vertexInfo.attributes["inDegree"]?.value?.toIntOrNull()
                ?: throw GradleException("Unable to load 'inDegree' attribute for vertex ${vertexInfo.path}.  " +
                        "Does this task dependOn the BasicGraphAnalysisTask?")

            vertexInfo.attributes["networkAbove"] = DefaultAttribute(networkAboveValue, AttributeType.INT)
            vertexInfo.attributes["networkBelow"] = DefaultAttribute(networkBelowValue, AttributeType.INT)
            vertexInfo.attributes["expansionFactor"] = DefaultAttribute(networkBelowValue * inDegree, AttributeType.INT)
        }
    }

    companion object {
        const val TASK_NAME = "networkExpansionAnalysis"
    }
}