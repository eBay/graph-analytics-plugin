package com.ebay.plugins.graph.analytics

import com.ebay.plugins.graph.analytics.GraphUtils.merge
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Task used to consolidate the production dependencies and the test dependencies into a single, holistic
 * project graph (sans analysis).
 */
@CacheableTask
internal abstract class ConsolidationTask : BaseGraphPersistenceTask() {
    /**
     * Dependencies of the project should publish their own dependency graph which is then included
     * into this project's graph.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val graphFiles: ConfigurableFileCollection

    /**
     * The output location of this project's dependency graph.
     */
    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        graphFiles.forEach { file ->
            val subgraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
            persistence.import(subgraph, file)
            graph.merge(subgraph)
        }
        persistence.export(graph, outputFile.asFile.get())
    }
}
