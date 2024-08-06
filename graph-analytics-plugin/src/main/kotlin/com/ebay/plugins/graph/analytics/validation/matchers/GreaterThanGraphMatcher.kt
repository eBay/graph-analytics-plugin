package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which compares the input to the expected value using the greater than operator.
 */
internal class GreaterThanGraphMatcher<T : Number>(private val expected: T) : GraphMatcher<T?> {
    override fun matches(value: T?): DescribedMatch {
        val result = value != null && value.toDouble() > expected.toDouble()
        return DescribedMatch(
            actual = { value.quote() },
            description = value.summarizedDescription("greater than ${expected.quote()}"),
            matched = result,
        )
    }
}