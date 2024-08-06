package com.ebay.plugins.graph.analytics

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Task used to compare two graph analysis files and create a delta report.
 */
@CacheableTask
internal abstract class ComparisonTask : BaseGraphPersistenceTask() {
    /**
     * The project analysis graph file.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val baseFile: RegularFileProperty

    /**
     * The changed project analysis graph file.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val changedFile: RegularFileProperty

    /**
     * The output location of this project's report.
     */
    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val baseGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        persistence.import(baseGraph, baseFile.asFile.get())

        val changedGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        persistence.import(changedGraph, changedFile.asFile.get())

        val report = GraphComparisonHelper().compare(baseGraph, changedGraph)

        outputFile.get().asFile.writeText(report)
        logger.lifecycle("Graph analysis comparison report available at: file://${outputFile.asFile.get()}")
    }
}
