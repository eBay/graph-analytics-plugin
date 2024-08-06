package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.validation.RootedEdge
import com.ebay.plugins.graph.analytics.validation.RootedVertex

/**
 * Matcher which extracts the edge's source vertex, delegating the match of the source
 * vertex to the specified delegate matcher.
 */
internal class EdgeSourceGraphMatcher(
	private val delegate: GraphMatcher<in RootedVertex>
): GraphMatcher<RootedEdge>  {
	override fun matches(value: RootedEdge): DescribedMatch {
		val delegateResult = delegate.matches(value.edgeSource())
		return DescribedMatch(
			actual = { value },
			description = value.summarizedDescription("edge source"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult)
		)
	}

	private fun RootedEdge.edgeSource(): RootedVertex {
		return RootedVertex(graph, root, graph.getEdgeSource(edge))
	}
}
