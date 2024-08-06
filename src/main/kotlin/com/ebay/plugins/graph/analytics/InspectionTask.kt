package com.ebay.plugins.graph.analytics

import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.options.Option
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import java.time.Duration
import java.time.Instant

/**
 * Task to create a project-specific inspection report for use in helping to investigate/expose potentially hidden
 * costs associated with the project definition.
 */
@CacheableTask
internal abstract class InspectionTask : BaseGraphInputTask() {
    @get:Input
    internal abstract val selfInfoProp: Property<VertexInfo>

    @get:Input
    @set:Option(option = "project", description = "Project to report upon")
    @get:Optional // By default we use the project that the task was run against
    internal abstract var projectPath: String?

    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    override fun processInputGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        val targetProject = projectPath ?: selfInfoProp.get().path

        val selfInfo = graph.vertexSet().find { it.path == targetProject }
            ?: throw GradleException("Project path not found in graph: $targetProject")

        val report = buildString {
            appendLine("Project inspection report for: $targetProject ${selfInfo.attributes}")
            appendLine()

            dependencyCyclesReport(graph, selfInfo)
            topNodesReport(graph, selfInfo)

            append("Dependency tree:\n")
            addDependency(graph = graph, indent = "", vertex = selfInfo)
            append("* Indicates a vertex that has already been rendered\n")
        }

        outputFile.asFile.get().writeText(report)
        logger.lifecycle("Project inspection analysis comparison report available at: file://${outputFile.asFile.get()}")
    }

    /**
     * Builds up a list of cycles by performing a depth-first traversal of the dependency edges.
     */
    private fun detectCycles(
        graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        projectNode: VertexInfo,
        currentNode: VertexInfo,
        stopTime: Instant,
        detectedCycles: MutableList<List<VertexInfo>> = mutableListOf(),
        traversedPath: List<VertexInfo> = listOf(currentNode)
    ): List<List<VertexInfo>> {
        // Truncate the results if the graph is excessive:
        if (Instant.now().isAfter(stopTime)) {
            return emptyList()
        }

        graph.outgoingEdgesOf(currentNode).map { edge ->
            graph.getEdgeTarget(edge)
        }.sortedBy {
            it.path
        }.forEach { node ->
            val updatedTraversedPath = traversedPath + node
            if (node == projectNode) {
                detectedCycles.add(updatedTraversedPath)
            } else if (!traversedPath.contains(node)) {
                detectCycles(graph, projectNode, node, stopTime, detectedCycles, updatedTraversedPath)
            }
        }
        return if (traversedPath.size == 1) {
            detectedCycles.toList()
        } else {
            // Work avoidance
            emptyList()
        }
    }

    /**
     * Renders a report detailing any project dependency cycles that may be found for the target project.
     */
    private fun StringBuilder.dependencyCyclesReport(
        graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        selfInfo: VertexInfo,
    ) {
        val stopTime = Instant.now().plus(MAX_TRAVERSAL_DURATION)
        val detectedCycles = detectCycles(graph, selfInfo, selfInfo, stopTime = stopTime)
        if (Instant.now().isAfter(stopTime)) {
            appendLine("WARNING: Project graph traversal has exceeded the maximum duration of $MAX_TRAVERSAL_DURATION.")
            appendLine("         Cycle detection will be incomplete.  This can happen when the module being inspected")
            appendLine("         has a very large dependency graph.  Consider breaking up the module into a set of smaller,")
            appendLine("         more cohesive modules.")
        }
        if (detectedCycles.isEmpty()) {
            appendLine("No project dependency cycles involving ${selfInfo.path} were detected.")
        } else {
            appendLine("${detectedCycles.size} project dependency cycle(s) involving ${selfInfo.path} were detected:")
            detectedCycles.sortedByDescending { it.size }.forEachIndexed { index, cycle ->
                val cycleString = cycle.joinToString(separator = " --> ") { it.path }
                appendLine("${index + 1}: $cycleString")
            }

            val pathToCount = mutableMapOf<String, Int>()
            detectedCycles.flatten().forEach { node ->
                pathToCount.compute(node.path) { _, existing -> (existing ?: 0) + 1 }
            }
            pathToCount.remove(selfInfo.path)
            appendLine()
            appendLine("Projects involved in cycles, by frequency of occurrence:")
            pathToCount.keys.sortedByDescending { pathToCount[it] }.forEach { path ->
                appendLine("\t${pathToCount[path]}: $path")
            }
        }
        appendLine()
        appendLine()
    }

    /**
     * Generates a list of all dependencies, direct and transitive, of the target project.
     */
    private fun computeAllDependencies(
        graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        currentNode: VertexInfo,
        processedVertices: MutableSet<VertexInfo> = mutableSetOf()
    ): Set<VertexInfo> {
        graph.outgoingEdgesOf(currentNode).forEach { edge ->
            val node = graph.getEdgeTarget(edge)
            if (!processedVertices.contains(node)) {
                processedVertices.add(currentNode)
                computeAllDependencies(graph, node, processedVertices)
            }
        }
        return processedVertices
    }

    /**
     * Renders a series of reports detailing each numeric attribute/metric.  Each individual report is
     * rendered by [topNodesReportForMetric].
     */
    private fun StringBuilder.topNodesReport(
        graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        projectNode: VertexInfo,
    ) {
        val allDependencies = computeAllDependencies(graph, projectNode)

        val allNumericAttrNames = allDependencies.flatMap { vertexInfo ->
            vertexInfo.attributes.entries
        }.filter { (_, attr) ->
            when (attr.type) {
                AttributeType.INT -> true
                AttributeType.LONG -> true
                AttributeType.DOUBLE -> true
                AttributeType.FLOAT -> true
                else -> false
            }
        }.map { (key, _) -> key }.toSortedSet()

        allNumericAttrNames.forEach { attrName ->
            topNodesReportForMetric(allDependencies, attrName)
        }
    }

    /**
     * Renders a report detailing a specific numeric attribute/metric, listing the top N nodes by metric value.
     */
    private fun StringBuilder.topNodesReportForMetric(
        allDependencies: Set<VertexInfo>,
        attrName: String
    ) {
        val metricValues = allDependencies.mapNotNull { node ->
            node.attributes[attrName]?.value?.toDoubleOrNull()?.let {
                Pair(node, it)
            }
        }.sortedByDescending { (_, value) ->
            value
        }.takeLast(TOP_N).map { it.first }

        if (metricValues.isNotEmpty()) {
            appendLine("Top $TOP_N dependencies by '$attrName':")
            metricValues.forEachIndexed { index, node ->
                appendLine("\t${index + 1}: ${node.attributes[attrName]} -- ${node.path} ${node.attributes}")
            }
            appendLine()
            appendLine()
        }
    }

    private fun StringBuilder.addDependency(
        graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>,
        indent: String,
        vertex: VertexInfo,
        processedVertices: MutableSet<VertexInfo> = mutableSetOf()
    ) {
        val nextIndent = if (indent.isEmpty()) {
            "    --> "
        } else {
            "    $indent"
        }
        append(indent)
        append(vertex.path)
        if (processedVertices.add(vertex)) {
            append(" (")
            var firstAttribute = true
            vertex.attributes.forEach { (name, value) ->
                if (!firstAttribute) append(", ")
                append(name)
                append("=")
                append(value)
                firstAttribute = false
            }
            append(")\n")

            graph.outgoingEdgesOf(vertex).forEach { edge ->
                val target = graph.getEdgeTarget(edge)
                // Only process outbound edges
                if (target != vertex) {
                    addDependency(graph, nextIndent, target, processedVertices)
                }
            }
        } else {
            append(" *\n")
        }
    }

    companion object {
        private const val TOP_N = 10
        private val MAX_TRAVERSAL_DURATION = Duration.ofMinutes(5)
    }
}
