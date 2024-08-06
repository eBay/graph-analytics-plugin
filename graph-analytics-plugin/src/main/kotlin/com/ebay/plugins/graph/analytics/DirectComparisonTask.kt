package com.ebay.plugins.graph.analytics

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jgrapht.graph.DefaultDirectedGraph
import javax.inject.Inject

/**
 * Task used to compare two graph analysis files and create a delta report.
 */
@CacheableTask
internal abstract class DirectComparisonTask : BaseGraphPersistenceTask() {
    @get:Inject
    internal abstract val projectLayout: ProjectLayout

    /**
     * Relative path to the project analysis graph file.
     */
    @get:Input
    @set:Option(option = "base", description = "Path to the base graph file, relative to the project the task is run within")
    internal abstract var baseFilePath: String

    /**
     * Relative path to the changed project analysis graph file.
     */
    @get:Input
    @set:Option(option = "changed", description = "Path to the graph file containing the changes, relative to the project the task is run within")
    internal abstract var changedFilePath: String

    /**
     * The output location of this project's report.
     */
    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val baseFile = projectLayout.projectDirectory.file(baseFilePath)
        val changedFile = projectLayout.projectDirectory.file(changedFilePath)

        val baseGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        persistence.import(baseGraph, baseFile.asFile)

        val changedGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        persistence.import(changedGraph, changedFile.asFile)

        val report = GraphComparisonHelper().compare(baseGraph, changedGraph)

        outputFile.get().asFile.writeText(report)
        logger.lifecycle("Graph analysis comparison report available at: file://${outputFile.asFile.get()}")
    }
}
