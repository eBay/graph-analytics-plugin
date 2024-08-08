package com.ebay.plugins.graph.analytics

import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Helper used to create a comparison report between two graph instances.
 */
internal class GraphComparisonHelper {
    fun compare(
        beforeGraph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        afterGraph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
    ): String {
        val changes = mutableListOf<ComparisonChange>()
        beforeGraph.vertexSet().forEach { baseVertex ->
            afterGraph.vertexSet().find { it.path == baseVertex.path }?.let { changedVertex ->
                baseVertex.attributes.forEach { (key, baseAttr) ->
                    changedVertex.attributes[key]?.let { changedAttr ->
                        changes.add(ComparisonChange(
                            project = baseVertex.path,
                            attributeName = key,
                            attributeType = baseAttr.type,
                            originalValue = baseAttr.value,
                            changedValue = changedAttr.value,
                        ))
                    }
                }
            }
        }

        val report = buildString {
            append("Graph nodes: ${beforeGraph.vertexSet().size} before, ${afterGraph.vertexSet().size} after\n")
            append("Graph edges: ${beforeGraph.edgeSet().size} before, ${afterGraph.edgeSet().size} after\n")
            append("\n")

            val deltas = changes.filter { it.comparisonResult != 0 }
            val deltasByProject = deltas.groupBy { it.project }.toSortedMap()
            append("${deltasByProject.keys.size} project(s) reporting a total of ${deltas.size} change(s)\n")
            append("\n")

            append("Aggregate metric changes:\n")
            val allAttributes = changes.map { it.attributeName }.toSortedSet()
            allAttributes.forEach { attributeName ->
                val compared = deltas.filter { it.attributeName == attributeName }.map { it.comparisonResult }
                val increased = compared.count { it < 0 }
                val decreased = compared.count { it > 0 }
                val same = compared.count { it == 0 }
                append("\t${attributeName}: $decreased decreased, $same stayed the same, $increased increased\n")

                val attrChanges = changes.filter { it.attributeName == attributeName }
                val originals = attrChanges.mapNotNull { it.originalAsNumber()?.toDouble() }
                val changed = attrChanges.mapNotNull { it.changedAsNumber()?.toDouble() }
                if (originals.isNotEmpty() && changed.isNotEmpty()) {
                    val sumBefore = originals.sum().toInt()
                    val sumAfter = changed.sum().toInt()
                    val sumDelta = sumAfter - sumBefore
                    if (sumDelta != 0) {
                        append("\t\tsum: $sumBefore -> $sumAfter (delta: $sumDelta / ${percentage(sumBefore, sumDelta)})\n")
                    }
                    val minBefore = originals.min().toInt()
                    val minAfter = changed.min().toInt()
                    val minDelta = minAfter - minBefore
                    if (minDelta != 0) {
                        append("\t\tmin: $minBefore -> $minAfter (delta: $minDelta / ${percentage(minBefore, minDelta)})\n")
                    }
                    val maxBefore = originals.max().toInt()
                    val maxAfter = changed.max().toInt()
                    val maxDelta = maxAfter - maxBefore
                    if (maxDelta != 0) {
                        append("\t\tmax: $maxBefore -> $maxAfter (delta: $maxDelta / ${percentage(maxBefore, maxDelta)})\n")
                    }
                    val avgBefore = originals.average().toInt()
                    val avgAfter = changed.average().toInt()
                    val avgDelta = avgAfter - avgBefore
                    if (avgDelta != 0) {
                        append("\t\taverage: $avgBefore -> $avgAfter (delta: $avgDelta / ${percentage(avgBefore, avgDelta)})\n")
                    }
                }
            }
            append("\n")

            append("Deltas by project:\n")
            deltasByProject.forEach { (project, deltas) ->
                append("$project:\n")
                deltas.forEach { change ->
                    append("\t$change\n")
                }
            }
        }
        return report
    }

    private fun percentage(original: Number, delta: Number): String {
        val percentage = ((delta.toDouble() / original.toDouble()) * 100)
        return String.format("%1\$.2f%%", percentage)
    }
}
