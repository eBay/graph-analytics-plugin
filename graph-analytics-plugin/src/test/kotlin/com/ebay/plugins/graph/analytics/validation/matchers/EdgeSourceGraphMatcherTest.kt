package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import com.ebay.plugins.graph.analytics.validation.RootedEdge
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.jgrapht.graph.DefaultDirectedGraph
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.equalTo as graphEqualTo

class EdgeSourceGraphMatcherTest {
    private lateinit var root: VertexInfo
    private lateinit var source: VertexInfo
    private lateinit var target: VertexInfo
    private lateinit var graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>
    private lateinit var edge: EdgeInfo
    private lateinit var rootedEdge: RootedEdge

    @BeforeEach
    fun setupUut() {
        root = VertexInfo(path = ":root")
        source = VertexInfo(path = ":source")
        target = VertexInfo(path = ":target")
        edge = EdgeInfo()
        graph = mock {
            on { getEdgeSource(edge) }.thenReturn(source)
            on { getEdgeTarget(edge) }.thenReturn(target)
        }
        rootedEdge = RootedEdge(graph = graph, root = root, edge = edge)
    }

    @Test
    fun match() {
        val result = EdgeSourceGraphMatcher(
            VertexPathGraphMatcher(graphEqualTo(":source"))
        ).matches(rootedEdge)
        println(result.render())
        assertThat(result.matched, equalTo(true))
    }

    @Test
    fun mismatch() {
        val result = EdgeSourceGraphMatcher(
            VertexPathGraphMatcher(graphEqualTo(":other"))
        ).matches(rootedEdge)
        println(result.render())
        assertThat(result.matched, equalTo(false))
    }
}