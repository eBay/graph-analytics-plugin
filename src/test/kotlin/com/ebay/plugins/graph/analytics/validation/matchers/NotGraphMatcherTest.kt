package com.ebay.plugins.graph.analytics.validation.matchers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.equalTo as graphEqualTo

class NotGraphMatcherTest {
    @Test
    fun success() {
        val actual = NotGraphMatcher(graphEqualTo("1")).matches("0")
        println(actual.render())
        assertThat(actual.matched, equalTo(true))
    }

    @Test
    fun failure() {
        val actual = NotGraphMatcher(graphEqualTo("1")).matches("1")
        println(actual.render())
        assertThat(actual.matched, equalTo(false))
    }
}