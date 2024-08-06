package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class LessThanGraphMatcherTest {
    @Test
    fun matchNumber() {
        val result = LessThanGraphMatcher(2).matches(1)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = LessThanGraphMatcher(1).matches(2)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchNull() {
        val result = LessThanGraphMatcher(1).matches(null)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}
