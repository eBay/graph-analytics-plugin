package com.ebay.plugins.graph.analytics

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

@CacheableTask
abstract class BaseGraphInputOutputTask : BaseGraphPersistenceTask() {
    /**
     * The graph input to analyze.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val inputGraph: RegularFileProperty

    /**
     * The graph output file containing the graph including the applied analysis data.
     */
    @get:OutputFile
    internal abstract val outputGraph: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        persistence.import(graph, inputGraph.asFile.get())
        processGraph(graph)
        persistence.export(graph, outputGraph.asFile.get())
    }

    abstract fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>)
}