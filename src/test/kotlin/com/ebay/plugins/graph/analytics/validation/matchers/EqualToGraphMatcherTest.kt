package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.testng.annotations.Test

class EqualToGraphMatcherTest {
    @Test
    fun matchString() {
        val result = EqualToGraphMatcher("string").matches("string")
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun matchNumber() {
        val result = EqualToGraphMatcher(1).matches(1)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = EqualToGraphMatcher(1).matches(2)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}