package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.validation.RootedEdge
import com.ebay.plugins.graph.analytics.validation.RootedVertex

/**
 * Matches all outgoing edges (dependencies) of a vertex.
 */
internal class OutgoingEdgesGraphMatcher(
	private val delegate: GraphMatcher<in Iterable<RootedEdge>>
): GraphMatcher<RootedVertex>  {
	override fun matches(value: RootedVertex): DescribedMatch {
		val delegateResult = delegate.matches(value.outgoingEdges())
		return DescribedMatch(
			actual = { value },
			description = value.summarizedDescription("outgoing edges"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult),
		)
	}

	private fun RootedVertex.outgoingEdges(): Iterable<RootedEdge> {
		return graph.outgoingEdgesOf(vertex).map { edge ->
			RootedEdge(graph, root, edge)
		}
	}
}
