package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.validation.RootedVertex

/**
 * Matcher which extracts the vertex's path, delegating the match of the path to the
 * supplied matcher.
 */
internal class VertexPathGraphMatcher(
	private val delegate: GraphMatcher<String>
): GraphMatcher<RootedVertex>  {
	override fun matches(value: RootedVertex): DescribedMatch {
		val delegateResult = delegate.matches(value.vertex.path)
		return DescribedMatch(
			actual = { value.vertex.path },
			description = value.summarizedDescription("path"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult),
		)
	}
}
