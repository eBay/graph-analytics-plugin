package com.ebay.plugins.graph.analytics

import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Vertex height scoring algorithm.
 */
class VertexHeightScoringAlgorithm(
    private val graph: Graph<VertexInfo, EdgeInfo>,
) : VertexScoringAlgorithm<VertexInfo, Int> {
    private val scores = mutableMapOf<VertexInfo, Int>()

    override fun getScores(): Map<VertexInfo, Int> {
        return scores.toMap()
    }

    override fun getVertexScore(v: VertexInfo): Int {
        require(graph.containsVertex(v)) { "Cannot return score of unknown vertex" }

        val alreadyComputed = scores[v]
        if (alreadyComputed != null) return alreadyComputed

        return compute(v).also {
            scores[v] = it
        }
    }

    private fun compute(vertex: VertexInfo): Int {
        val processedVertices: MutableSet<VertexInfo> = mutableSetOf()
        val remainingVertices: Queue<VertexInfo> = LinkedBlockingQueue<VertexInfo>().apply { add(vertex) }
        var height = 0
        while(remainingVertices.isNotEmpty()) {
            height++
            val toProcess = remainingVertices.toMutableSet()
            remainingVertices.clear()

            toProcess.forEach { v ->
                processedVertices.add(v)
                graph.edgesOf(v).forEach { edge ->
                    val target = graph.getEdgeTarget(edge)
                    // Only process outbound edges
                    if (target != v) {
                        if (!processedVertices.contains(target)) {
                            remainingVertices.add(target)
                        }
                    }
                }
            }
        }
        return height
    }
}