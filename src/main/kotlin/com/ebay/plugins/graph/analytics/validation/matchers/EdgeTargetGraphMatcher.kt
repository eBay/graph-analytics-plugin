package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.validation.RootedEdge
import com.ebay.plugins.graph.analytics.validation.RootedVertex

/**
 * Matcher which extracts the edge's target vertex, delegating the match of the target
 * vertex to the specified delegate matcher.
 */
internal class EdgeTargetGraphMatcher(
	private val delegate: GraphMatcher<in RootedVertex>
): GraphMatcher<RootedEdge> {
	override fun matches(value: RootedEdge): DescribedMatch {
		val delegateResult = delegate.matches(value.edgeTarget())
		return DescribedMatch(
			actual = value::toString,
			description = value.summarizedDescription("edge target"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult),
		)
	}

	private fun RootedEdge.edgeTarget(): RootedVertex {
		return RootedVertex(graph, root, graph.getEdgeTarget(edge))
	}
}
