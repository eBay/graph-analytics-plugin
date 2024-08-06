package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.VertexInfo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.jgrapht.nio.DefaultAttribute
import org.junit.jupiter.api.Test

class AttributeNumberGraphMatcherTest {
    @Test
    fun matchInt() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatchIntValue() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchIntType() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute("2")
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun matchLong() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1L)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1L),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatchLongValue() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1L)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2L),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchLongType() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute("2")
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun matchFloat() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1.0F)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1.0F)
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatchFloatValue() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(2.0F)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1.0F),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchFloatType() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute("2")
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2.0F),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun matchDouble() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(1.0)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1.0)
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatchDoubleValue() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute(2.0)
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(1.0),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun mismatchDoubleType() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf(
            "key" to DefaultAttribute.createAttribute("2")
        ))
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2.0),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }

    @Test
    fun notFound() {
        val attributed = VertexInfo(path = "key", attributes = mutableMapOf())
        val result = AttributeNumberGraphMatcher(
            "key",
            EqualToGraphMatcher(2.0),
        ).matches(attributed)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}