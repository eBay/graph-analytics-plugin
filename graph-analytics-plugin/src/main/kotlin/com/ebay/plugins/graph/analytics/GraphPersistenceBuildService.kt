package com.ebay.plugins.graph.analytics

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.jgrapht.Graph
import java.io.File

/**
 * A service used to share the graph persistence implementation between tasks.
 */
abstract class GraphPersistenceBuildService : BuildService<BuildServiceParameters.None> {
    internal val delegate: GraphPersistence = GraphPersistenceGraphMl()

    fun import(graph: Graph<VertexInfo, EdgeInfo>, file: File) {
        delegate.import(graph, file)
    }

    fun export(graph: Graph<VertexInfo, EdgeInfo>, file: File) {
        delegate.export(graph, file)
    }
}

