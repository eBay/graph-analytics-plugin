package com.ebay.plugins.graph.analytics.validation

import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatcher

/**
 * A rule which describes a violation of standards.
 */
data class GraphValidationRule(
    /**
     * The reason why this rule is important to the developer.  This will be used in the report to guide them
     * to a correct factoring.
     */
    val reason: String,

    /**
     * Determines whether or not the provided [RootedVertex] is in violation of this rule.
     */
    val matcher: GraphMatcher<RootedVertex>,
)