package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which inverts the result of the delegate matcher.
 */
internal class NotGraphMatcher<T>(private val delegate: GraphMatcher<T>) : GraphMatcher<T> {
    override fun matches(value: T): DescribedMatch {
        val delegateResult = delegate.matches(value)
        return DescribedMatch(
            actual = { value },
            description = value.summarizedDescription("not"),
            matched = !delegateResult.matched,
            subResults = listOf(delegateResult),
            inversion = true
        )
    }
}