package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which compares the input to the expected value using the equals operator.
 */
internal class EqualToGraphMatcher<T>(private val expected: T) : GraphMatcher<T> {
    override fun matches(value: T): DescribedMatch {
        val result = value == expected
        return DescribedMatch(
            actual = { value.quote() },
            description = value.summarizedDescription("equal to ${expected.quote()}"),
            matched = result,
        )
    }
}