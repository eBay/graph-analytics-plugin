package com.ebay.plugins.graph.analytics

import com.ebay.plugins.graph.analytics.GraphUtils.merge
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Task to gather the dependencies graph of a project.
 */
@CacheableTask
internal abstract class GatherTask : BaseGraphPersistenceTask() {
    @get:Input
    internal abstract val explicitRelationships: SetProperty<GraphRelation>

    @get:Input
    internal abstract val selfInfoProp: Property<VertexInfo>

    @get:InputFiles
    @get:PathSensitive(value = PathSensitivity.NONE)
    internal abstract val contributedGraphs: ConfigurableFileCollection

    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val persistence = persistenceBuildService.get()
        graph.addVertex(selfInfoProp.get())

        contributedGraphs.forEach { file ->
            val subgraph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
            persistence.import(subgraph, file)
            graph.merge(subgraph)
        }
        val mapOfPathToVertexInfo = graph.vertexSet().associateBy { it.path }
        explicitRelationships.get().forEach { relation ->
            val fromVertex = mapOfPathToVertexInfo[relation.from]
                ?: throw GradleException("Relation from ${relation.from} not found.  This should not happen.")
            val toVertex = mapOfPathToVertexInfo[relation.to]
                ?: throw GradleException("Relation to ${relation.to} not found.  This should not happen.")
            graph.addEdge(fromVertex, toVertex, relation.edge)
        }
        persistence.export(graph, outputFile.asFile.get())
    }
}
