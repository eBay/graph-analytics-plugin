package com.ebay.plugins.graph.analytics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute
import org.testng.annotations.Test

class GraphVertexInfoTest {
    @Test
    fun equality() {
        val one = VertexInfo(path = ":some:path", attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        ))
        val two = VertexInfo(path = ":some:path").apply {
            attributes["attr1"] = DefaultAttribute("value1", AttributeType.STRING)
            attributes["attr2"] = DefaultAttribute("value2", AttributeType.STRING)
        }
        assertThat(one, equalTo(two))
        assertThat(one.hashCode(), equalTo(two.hashCode()))
    }

    @Test
    fun equalityInMapKey() {
        val map = LinkedHashMap<VertexInfo, String>()
        val one = VertexInfo(path = ":some:path", attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        ))
        map[one] = "one"

        val two = VertexInfo(path = ":some:path").apply {
            attributes["attr1"] = DefaultAttribute("value1", AttributeType.STRING)
            attributes["attr2"] = DefaultAttribute("value2", AttributeType.STRING)
        }
        assertThat(map.keys.contains(one), equalTo(true))
        assertThat(map.keys.contains(two), equalTo(true))
    }

    // NOTE: See comment on [GraphVertexInfo.hashCode]
    @Test
    fun nonAttributeEquality() {
        val one = VertexInfo(path = ":some:path", attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        ))
        val two = VertexInfo(path = ":some:path", attributes = mutableMapOf(
            "attr3" to DefaultAttribute("value3", AttributeType.STRING),
            "attr4" to DefaultAttribute("value4", AttributeType.STRING),
        ))
        assertThat(one, equalTo(two))
        assertThat(one.hashCode(), equalTo(two.hashCode()))
    }
}