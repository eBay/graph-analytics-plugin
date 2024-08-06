package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * Matcher which compares the input to the expected value using the less than operator.
 */
internal class LessThanGraphMatcher<T : Number>(private val expected: T) : GraphMatcher<T?> {
    override fun matches(value: T?): DescribedMatch {
        val result = value != null && value.toDouble() < expected.toDouble()
        return DescribedMatch(
            actual = { value.quote() },
            description = value.summarizedDescription("less than ${expected.quote()}"),
            matched = result,
        )
    }
}