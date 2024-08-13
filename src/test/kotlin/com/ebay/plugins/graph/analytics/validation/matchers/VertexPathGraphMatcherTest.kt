package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import com.ebay.plugins.graph.analytics.validation.RootedVertex
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.path
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.jgrapht.graph.DefaultDirectedGraph
import org.mockito.kotlin.mock
import org.testng.annotations.Test
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.equalTo as graphEqualTo

class VertexPathGraphMatcherTest {
    private val vertex = VertexInfo(path = ":source")
    private val graph: DefaultDirectedGraph<VertexInfo, EdgeInfo> = mock()
    private val rootedVertex = RootedVertex(graph = graph, root = vertex)

    @Test
    fun success() {
        val uut = path(graphEqualTo(":source"))
        val actual = uut.matches(rootedVertex)
        println(actual.render())
        assertThat(actual.matched, equalTo(true))
    }

    @Test
    fun failure() {
        val uut = path(graphEqualTo(":notSource"))
        val actual = uut.matches(rootedVertex)
        println(actual.render())
        assertThat(actual.matched, equalTo(false))
    }
}