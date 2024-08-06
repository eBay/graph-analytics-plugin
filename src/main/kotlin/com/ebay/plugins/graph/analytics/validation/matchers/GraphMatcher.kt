package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * The base interface for all graph matchers.
 */
interface GraphMatcher<T> {
    /**
     * Evaluates the matcher against the specified value.
     *
     * @return the result of the match
     */
    fun matches(value: T): DescribedMatch
}