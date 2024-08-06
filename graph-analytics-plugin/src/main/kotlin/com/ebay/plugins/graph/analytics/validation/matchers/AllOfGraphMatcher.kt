package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which matches only if all delegate rules match the input.
 */
internal class AllOfGraphMatcher<T>(
    private val delegates: Iterable<GraphMatcher<in T>>
) : GraphMatcher<T> {
    override fun matches(value: T): DescribedMatch {
        val delegateResults = delegates.map { delegate ->
            delegate.matches(value)
        }
        val result = delegateResults.all { it.matched }
        return DescribedMatch(
            actual = value::toString,
            description = value.summarizedDescription("all of"),
            matched = result,
            subResults = delegateResults,
        )
    }
}