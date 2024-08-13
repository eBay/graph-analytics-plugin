package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.testng.annotations.Test

class GreaterThanGraphMatcherTest {
    @Test
    fun matchNumber() {
        val result = GreaterThanGraphMatcher(1).matches(2)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = GreaterThanGraphMatcher(2).matches(1)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchNull() {
        val result = GreaterThanGraphMatcher(1).matches(null)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}
