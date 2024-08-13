package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.testng.annotations.Test

class AnyOfGraphMatcherTest {
    @Test
    fun match() {
        val result = AnyOfGraphMatcher(
            listOf(
                EqualToGraphMatcher("test string"),
                EqualToGraphMatcher("other string"),
            )
        ).matches("test string")
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = AnyOfGraphMatcher(
            listOf(
                EqualToGraphMatcher("other string"),
                EqualToGraphMatcher("yet another string"),
            )
        ).matches("test string")
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}