package com.ebay.plugins.graph.analytics.validation

import com.ebay.plugins.graph.analytics.BaseGraphPersistenceTask
import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexInfo
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatcher
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * Gradle task used to perform the project analysis to determine if there are any violations
 * of the configured rules.
 */
@CacheableTask
abstract class GraphValidationTask : BaseGraphPersistenceTask() {
    /**
     * The project path of the project being validated.
     */
    @get:Input
    abstract val projectPathProp: Property<String>

    /**
     * A collection of project analysis graphs to validate against.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputGraphs: ConfigurableFileCollection

    /**
     * List of Rule IDs to ignore.
     */
    @get:Input
    abstract val ignoredRulesProp: ListProperty<String>

    /**
     * The [rules] field is `@Internal` since `GraphValidationRule` is not a serializable type.  In order to
     * force the cache to be invalidated when the rules change, we accept string representation of the defined
     * rules.
     */
    @get:Input
    abstract val definedRules: ListProperty<String>

    /**
     * The [ruleOverrides] field is `@Internal` since `GraphMatcher` is not a serializable type.  In order to
     * force the cache to be invalidated when the overridden rules change, we accept a string representation
     * of the defined overrides.
     */
    @get:Input
    abstract val definedRuleOverrides: ListProperty<String>

    /**
     * Textual report output file.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * The absolute path to the root project.  This is used to remove this prefix from the input graph paths
     * when rendering the report.
     */
    @get:Internal
    abstract val rootProjectPath: DirectoryProperty

    /**
     * The rules to evaluate when validating each project graph.
     */
    @get:Internal
    abstract val rules: MapProperty<String, GraphValidationRule>

    /**
     * Graph matcher which should be used in place of the rules' defined matchers.  This allows for
     * threshold-based rules to be overridden on a per-project basis.
     */
    @get:Internal
    abstract val ruleOverrides: MapProperty<String, GraphMatcher<RootedVertex>>

    /**
     * Processes each of the analysis input graphs, validating them against the configured rules.
     */
    @TaskAction
    fun validateGraphs() {
        val rootDir = rootProjectPath.get().asFile
        val persistence = persistenceBuildService.get()
        val validationResults = inputGraphs.files.map { inputGraph ->
            val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
            persistence.import(graph, inputGraph)
            val graphId = inputGraph.toRelativeString(rootDir)
            validateGraph(graphId, graph)
        }

        val totalReport = validationResults.joinToString(separator = "\n") { validationResult ->
            buildReportString(validationResult)
        }
        outputFile.asFile.get().writeText(totalReport)

        val errors = validationResults.filterIsInstance<GraphValidation>()
            .map { validation ->
                validation.violations.size + validation.ignoredButValid.size
            }
            .filter { it > 0 }
        val errorCount = if (errors.isEmpty()) {
            0
        } else {
            errors.reduce { acc, i -> acc + i }
        }

        if (errorCount > 0) {
            logger.error(totalReport)
            throw GradleException("$errorCount graph validation error(s) detected.  " +
                    "Please see the task's console output for more details.")
        }
    }

    /**
     * Validates the provided graph against the configured rules, returning a validation result summary.
     */
    private fun validateGraph(graphId: String, graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>): GraphValidationResult {
        val vertexInfo = graph.vertexSet().find { it.path == projectPathProp.get() }
            ?: return GraphVertexNotFound(graphId = graphId)

        val rootedVertex = RootedVertex(graph, vertexInfo)

        val rulesWithOverridesApplied = rules.get().mapValues { (id, rule) ->
            ruleOverrides.get()[id]?.let { override ->
                GraphValidationRule(
                    "This rule is a project module- specific override.  Look for its definition in the\n" +
                            "project module's `build.gradle.kts` file",
                    override
                )
            } ?: rule
        }

        val violations = rulesWithOverridesApplied.filter { (_, rule) ->
            rule.matcher.matches(rootedVertex).matched
        }
        val allIgnoredRules = ignoredRulesProp.get()
        val ignoredViolations = violations.filter { (id, _) -> id in allIgnoredRules }
        val unIgnoredViolations = violations.filterNot { (id, _) -> id in allIgnoredRules }
        val ignoresWithoutViolations = allIgnoredRules.filterNot { ignoredViolations.containsKey(it) }

        return GraphValidation(
            graphId = graphId,
            rootedVertex = rootedVertex,
            violations = unIgnoredViolations.toMap(),
            ignoredViolations = ignoredViolations,
            ignoredButValid = ignoresWithoutViolations,
        )
    }

    /**
     * Build a report string for the provided validation result.
     */
    private fun buildReportString(validationResult: GraphValidationResult) = buildString {
        appendLine("=== Validation using graph analysis: ${validationResult.graphId}")
        appendLine()
        val summary = when(validationResult) {
            is GraphVertexNotFound -> "Project path ${projectPathProp.get()} not found in graph.  Validation skipped."
            is GraphValidation -> buildValidationReport(validationResult)
        }
        appendLine(summary)
    }

    /**
     * Builds a report string for an individual validation result.
     */
    private fun buildValidationReport(validation: GraphValidation) = buildString {
        if (validation.violations.isNotEmpty()) {
            appendLine("ERROR: ${validation.violations.size} rule(s) violations found:")
            validation.violations.forEach { (id, rule) ->
                appendLine("Rule: $id")
                appendLine("    Description:")
                appendLine(rule.reason.prependIndent("        "))
                appendLine("    Details:")
                val match = rule.matcher.matches(validation.rootedVertex)
                appendLine(match.render(onlyMatches = true, indent = "        "))
            }
        }
        if (validation.ignoredButValid.isNotEmpty()) {
            appendLine("ERROR: Ignored rule(s) which did not have any violations:")
            validation.ignoredButValid.forEach { appendLine("    $it") }
        }
        if (validation.ignoredViolations.isNotEmpty()) {
            appendLine("INFO ${validation.ignoredViolations.size} ignored rule violation(s) found:")
            appendLine(validation.ignoredViolations.keys.joinToString(separator = "\n", prefix = "    "))
        }
        if (validation.violations.isEmpty()
            && validation.ignoredButValid.isEmpty()
            && validation.ignoredViolations.isEmpty()) {
            appendLine("SUCCESS: No graph issues found.")
        }
    }
}