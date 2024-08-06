package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which matches only if any one of the items in the input match the delegate matcher.
 */
internal class HasItemGraphMatcher<T>(
    private val delegate: GraphMatcher<T>
) : GraphMatcher<Iterable<T>> {
    override fun matches(value: Iterable<T>): DescribedMatch {
        val matchResults = value.map { delegate.matches(it) }
        val result = matchResults.any { it.matched }
        return DescribedMatch(
            actual = value::toString,
            description = "any item",
            matched = result,
            subResults = matchResults,
        )
    }
}