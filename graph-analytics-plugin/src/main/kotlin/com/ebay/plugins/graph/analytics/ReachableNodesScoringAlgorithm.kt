package com.ebay.plugins.graph.analytics

import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Determines the size of the subgraph visible by a vertex..
 */
class ReachableNodesScoringAlgorithm<V, E>(
    private val graph: Graph<V, E>,
    edgeDirection: ReachableNodesDirection,
) : VertexScoringAlgorithm<V, Int> {
    private val scores = mutableMapOf<V, Int>()

    private val edgesOf: (V) -> Set<E> = when(edgeDirection) {
        ReachableNodesDirection.INCOMING ->  graph::incomingEdgesOf
        ReachableNodesDirection.OUTGOING ->  graph::outgoingEdgesOf
    }

    private val subjectOf: (E) -> V = when(edgeDirection) {
        ReachableNodesDirection.INCOMING ->  graph::getEdgeSource
        ReachableNodesDirection.OUTGOING ->  graph::getEdgeTarget
    }

    override fun getScores(): Map<V, Int> {
        return scores.toMap()
    }

    override fun getVertexScore(v: V): Int {
        require(graph.containsVertex(v)) { "Cannot return score of unknown vertex" }

        val alreadyComputed = scores[v]
        if (alreadyComputed != null) return alreadyComputed

        return compute(v).also {
            scores[v] = it
        }
    }

    private fun compute(vertex: V): Int {
        val processedVertices: MutableSet<V> = mutableSetOf(vertex)
        val remainingVertices: Queue<V> = LinkedBlockingQueue<V>().apply { add(vertex) }
        var count = 0
        do {
            count++
            val v = remainingVertices.remove()
            edgesOf.invoke(v).forEach { edge ->
                val target = subjectOf.invoke(edge)
                if (processedVertices.add(target)) {
                    remainingVertices.add(target)
                }
            }
        } while (remainingVertices.isNotEmpty())

        return count
    }
}