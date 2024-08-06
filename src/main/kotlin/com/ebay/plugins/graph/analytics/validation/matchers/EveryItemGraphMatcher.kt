package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which matches only if all of the items in the input match the delegate matcher.
 */
internal class EveryItemGraphMatcher<T>(
    private val delegate: GraphMatcher<T>
) : GraphMatcher<Iterable<T>> {
    override fun matches(value: Iterable<T>): DescribedMatch {
        val matchResults = value.map { delegate.matches(it) }
        val result = matchResults.all { it.matched }
        return DescribedMatch(
            actual = value::toString,
            description = "every item",
            matched = result,
            subResults = matchResults,
        )
    }
}