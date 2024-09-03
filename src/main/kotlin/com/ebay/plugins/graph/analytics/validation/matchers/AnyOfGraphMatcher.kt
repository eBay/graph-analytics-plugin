package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which matches only if any of the delegate rules match the input.
 */
internal class AnyOfGraphMatcher<T>(
    private val delegates: Iterable<GraphMatcher<in T>>
) : GraphMatcher<T> {
    override fun matches(value: T): DescribedMatch {
        // Evaluate all delegates to get a complete description
        val delegateResults = delegates.map { delegate ->
            delegate.matches(value)
        }
        val result = delegateResults.any { it.matched }
        return DescribedMatch(
            actual = value::toString,
            description = value.summarizedDescription("any of"),
            matched = result,
            subResults = delegateResults,
        )
    }
}