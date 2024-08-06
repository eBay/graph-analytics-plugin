package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class MatchesPatternGraphMatcherTest {
    @Test
    fun success() {
        val actual = MatchesPatternGraphMatcher(":sour.*").matches(":source")
        println(actual.render())
        assertThat(actual.matched, equalTo(true))
    }

    @Test
    fun failure() {
        val actual = MatchesPatternGraphMatcher(":foo.*").matches(":source")
        println(actual.render())
        assertThat(actual.matched, equalTo(false))
    }
}