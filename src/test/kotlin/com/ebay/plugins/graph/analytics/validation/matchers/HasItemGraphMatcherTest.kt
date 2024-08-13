package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.testng.annotations.Test

class HasItemGraphMatcherTest {
    @Test
    fun match() {
        val result = HasItemGraphMatcher(
            EqualToGraphMatcher("1")
        ).matches(listOf("1", "2", "3"))
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = HasItemGraphMatcher(
            EqualToGraphMatcher("4")
        ).matches(listOf("1", "2", "3"))
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}