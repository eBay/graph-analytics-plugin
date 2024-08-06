package com.ebay.plugins.graph.analytics

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Base class which can be used by tasks which need to read the graph file as
 * an input (only).
 */
abstract class BaseGraphInputTask : BaseGraphPersistenceTask() {
    /**
     * The graph input to analyze.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val inputGraph: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        persistence.import(graph, inputGraph.asFile.get())
        processInputGraph(graph)
    }

    abstract fun processInputGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>)
}