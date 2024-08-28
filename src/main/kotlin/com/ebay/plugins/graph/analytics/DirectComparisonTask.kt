package com.ebay.plugins.graph.analytics

import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import org.jgrapht.graph.DefaultDirectedGraph
import javax.inject.Inject

/**
 * Task used to compare two graph analysis files and create a delta report.
 */
@DisableCachingByDefault(because = "The task argument inputs do not consider file contents")
internal abstract class DirectComparisonTask : BaseGraphPersistenceTask() {
    @get:Inject
    internal abstract val projectLayout: ProjectLayout

    @get:InputFile
    internal abstract val defaultAnalysisFile: RegularFileProperty

    /**
     * Relative path to the project analysis graph file.
     */
    @get:Input
    @get:Optional
    @set:Option(
        option = "before",
        description = "Path to the base graph file, relative to the project the task is run within"
    )
    internal abstract var beforeFilePath: String?

    /**
     * Relative path to the changed project analysis graph file.
     */
    @get:Input
    @get:Optional
    @set:Option(
        option = "after",
        description = "Path to the graph file containing the changes, relative to the project the task is run within"
    )
    internal abstract var afterFilePath: String?

    /**
     * The output location of this project's report.
     */
    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val beforeFilePathLocal = beforeFilePath
        val afterFilePathLocal = afterFilePath
        if (beforeFilePathLocal == null && afterFilePath == null) {
            throw GradleException("One or both of --before and --after must be provided")
        }

        val beforeFile: RegularFile = if (beforeFilePathLocal == null) {
            defaultAnalysisFile.get()
        } else {
            projectLayout.projectDirectory.file(beforeFilePathLocal)
        }
        val afterFile: RegularFile = if (afterFilePathLocal == null) {
            defaultAnalysisFile.get()
        } else {
            projectLayout.projectDirectory.file(afterFilePathLocal)
        }

        val beforeGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        persistence.import(beforeGraph, beforeFile.asFile)

        val afterGraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        persistence.import(afterGraph, afterFile.asFile)

        val report = GraphComparisonHelper().compare(beforeGraph, afterGraph)

        outputFile.get().asFile.writeText(report)
        logger.lifecycle("Graph analysis comparison report available at: file://${outputFile.asFile.get()}")
    }
}
