package com.ebay.plugins.graph.analytics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute
import org.testng.annotations.Test

class GraphEdgeInfoTest {
    @Test
    fun equality() {
        val graph1 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = VertexInfo(path = ":1").also { graph1.addVertex(it) }
        val vertex2 = VertexInfo(path = ":2").also { graph1.addVertex(it) }
        graph1.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val one = graph1.getEdge(vertex1, vertex2)

        val graph2 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        graph2.addVertex(vertex1)
        graph2.addVertex(vertex2)
        graph2.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val two = graph2.getEdge(vertex1, vertex2)

        assertThat(one, equalTo(two))
        assertThat(one.hashCode(), equalTo(two.hashCode()))
    }

    @Test
    fun nonAttributeEquality() {
        val graph1 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = VertexInfo(path = ":1").also { graph1.addVertex(it) }
        val vertex2 = VertexInfo(path = ":2").also { graph1.addVertex(it) }
        graph1.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val one = graph1.getEdge(vertex1, vertex2)

        val graph2 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        graph2.addVertex(vertex1)
        graph2.addVertex(vertex2)
        graph2.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr3" to DefaultAttribute("value3", AttributeType.STRING),
            "attr4" to DefaultAttribute("value4", AttributeType.STRING),
        )))
        val two = graph2.getEdge(vertex1, vertex2)

        assertThat(one, equalTo(two))
        assertThat(one.hashCode(), equalTo(two.hashCode()))
    }

    @Test
    fun nonSourceEquality() {
        val graph1 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = VertexInfo(path = ":1").also { graph1.addVertex(it) }
        val vertex2 = VertexInfo(path = ":2").also { graph1.addVertex(it) }
        graph1.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val one = graph1.getEdge(vertex1, vertex2)

        val graph2 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex3 = VertexInfo(path = ":3").also { graph2.addVertex(it) }
        graph2.addVertex(vertex2)
        graph2.addVertex(vertex3)
        graph2.addEdge(vertex3, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val two = graph2.getEdge(vertex3, vertex2)

        assertThat(one, not(equalTo(two)))
        assertThat(one.hashCode(), not(equalTo(two.hashCode())))
    }

    @Test
    fun nonTargetEquality() {
        val graph1 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = VertexInfo(path = ":1").also { graph1.addVertex(it) }
        val vertex2 = VertexInfo(path = ":2").also { graph1.addVertex(it) }
        graph1.addEdge(vertex1, vertex2, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val one = graph1.getEdge(vertex1, vertex2)

        val graph2 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex3 = VertexInfo(path = ":3").also { graph2.addVertex(it) }
        graph2.addVertex(vertex1)
        graph2.addVertex(vertex3)
        graph2.addEdge(vertex1, vertex3, EdgeInfo(attributes = mutableMapOf(
            "attr1" to DefaultAttribute("value1", AttributeType.STRING),
            "attr2" to DefaultAttribute("value2", AttributeType.STRING),
        )))
        val two = graph2.getEdge(vertex1, vertex3)

        assertThat(one, not(equalTo(two)))
        assertThat(one.hashCode(), not(equalTo(two.hashCode())))
    }
}