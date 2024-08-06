package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class EveryItemGraphMatcherTest {
    @Test
    fun match() {
        val result = EveryItemGraphMatcher(
            EqualToGraphMatcher("1")
        ).matches(listOf("1", "1", "1"))
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = EveryItemGraphMatcher(
            EqualToGraphMatcher("1")
        ).matches(listOf("1", "2", "1"))
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}