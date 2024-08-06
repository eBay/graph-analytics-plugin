package com.ebay.plugins.graph.analytics

import com.ebay.plugins.graph.analytics.validation.GraphValidationExtension
import com.ebay.plugins.graph.analytics.validation.GraphValidationTask
import com.ebay.plugins.graph.analytics.validation.RootedVertex
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatcher
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.DefaultAttribute

/**
 * Plugin implementation which defines tasks and configurations artifacts which are used to
 * generate project dependency graph analytic data.
 */
@Suppress("unused", "UnstableApiUsage")
internal class GraphAnalyticsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val graphPersistenceBuildServiceProvider = project.gradle.sharedServices.registerIfAbsent(
            GRAPH_PERSISTENCE_BUILD_SERVICE,
            GraphPersistenceBuildService::class.java,
        ) {}
        project.tasks.withType(BaseGraphPersistenceTask::class.java).configureEach { task ->
            task.graphFormat.set(graphPersistenceBuildServiceProvider.map { it.delegate.fileExtension })
            task.graphVersion.set(graphPersistenceBuildServiceProvider.map { it.delegate.version })
            task.persistenceBuildService.set(graphPersistenceBuildServiceProvider)
            task.usesService(graphPersistenceBuildServiceProvider)
        }

        val graphExtension = project.extensions.create(EXTENSION_NAME, GraphExtension::class.java)
        with(graphExtension.analysisTasks) {
            add(project.tasks.register(BasicGraphMetricsAnalysisTask.TASK_NAME, BasicGraphMetricsAnalysisTask::class.java))
            add(project.tasks.register(BetweennessCentralityAnalysisTask.TASK_NAME, BetweennessCentralityAnalysisTask::class.java))
            add(project.tasks.register(VertexHeightAnalysisTask.TASK_NAME, VertexHeightAnalysisTask::class.java))

            add(project.tasks.register(NetworkExpansionAnalysisTask.TASK_NAME, NetworkExpansionAnalysisTask::class.java).also {
                it.inputsFrom(project, BasicGraphMetricsAnalysisTask.TASK_NAME, BasicGraphMetricsAnalysisTask::class.java)
            })
        }
        val validationExtension = graphExtension.extensions.create("validation", GraphValidationExtension::class.java).apply {
            validatedProjects.convention(listOf(project.path))
        }

        val selfInfo = createVertexInfo(project, graphExtension)

        val paths = GraphAnalyticsPaths(project, graphPersistenceBuildServiceProvider)
        val prodDependenciesFile = paths.intermediateGraph("productionDependencies")
        val testDependenciesFile = paths.intermediateGraph("testDependencies")
        val consolidatedFile = paths.intermediateGraph("consolidatedDependencies")
        val analysisFile = paths.reportGraph("analysis")
        val directComparisonFile =  paths.report("directComparison.txt")
        val projectReportFile = paths.report("projectReport.txt")
        val validationReportFile = paths.report("validationReport.txt")

        // Production dependencies:
        val prodDependencies = project.createResolvableConfig(GATHER_PROD_DEPENDENCIES_RESOLVE_CONFIGURATION)
        val gatherProdDependenciesTaskProvider = project.tasks.register(GATHER_PROD_DEPENDENCIES_RESOLVE_TASK, GatherTask::class.java)
        gatherProdDependenciesTaskProvider.configure { task ->
                task.apply {
                    contributedGraphs.setFrom(prodDependencies)
                    selfInfoProp.set(selfInfo)
                    outputFile.set(prodDependenciesFile)
                }
            }
        project.artifacts.add(GATHER_PROD_DEPENDENCIES_RESOLVE_CONFIGURATION, gatherProdDependenciesTaskProvider)
        project.createConsumableConfig(GATHER_PROD_DEPENDENCIES_EXPORT_CONFIGURATION).apply {
            extendsFrom(prodDependencies)
        }

        // Test dependencies:
        val testDependencies = project.createResolvableConfig(GATHER_TEST_DEPENDENCIES_RESOLVE_CONFIGURATION)
        val gatherTestDependenciesTaskProvider = project.tasks.register(GATHER_TEST_DEPENDENCIES_RESOLVE_TASK, GatherTask::class.java)
        gatherTestDependenciesTaskProvider.configure { task ->
                task.apply {
                    contributedGraphs.setFrom(testDependencies)
                    selfInfoProp.set(selfInfo)
                    outputFile.set(testDependenciesFile)
                }
            }
        project.artifacts.add(GATHER_TEST_DEPENDENCIES_RESOLVE_CONFIGURATION, gatherTestDependenciesTaskProvider)
        project.createConsumableConfig(GATHER_TEST_DEPENDENCIES_EXPORT_CONFIGURATION).apply {
            extendsFrom(testDependencies)
        }

        // Consolidated project graph
        val consolidatedDependencies = project.createResolvableConfig(CONSOLIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION)
        val consolidationTaskProvider = project.tasks.register(CONSOLIDATION_DEPENDENCIES_RESOLVE_TASK, ConsolidationTask::class.java)
        consolidationTaskProvider.configure { task ->
            task.apply {
                dependsOn(gatherProdDependenciesTaskProvider)
                dependsOn(gatherTestDependenciesTaskProvider)
                graphFiles.from(testDependenciesFile)
                graphFiles.from(prodDependenciesFile)
                outputFile.set(consolidatedFile)
            }
        }
        project.artifacts.add(CONSOLIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION, consolidationTaskProvider)
        project.createConsumableConfig(CONSOLIDATION_DEPENDENCIES_EXPORT_CONFIGURATION).apply {
            extendsFrom(consolidatedDependencies)
        }

        // Project graph analysis task
        val analysisTaskProvider = project.tasks.register(ANALYSIS_TASK, ConsolidationTask::class.java)
        analysisTaskProvider.configure { task ->
            task.apply {
                group = TASK_GROUP
                description = "Creates a project graph with embedded analysis"
                outputFile.set(analysisFile)
            }
        }
        project.createConsumableConfig(ANALYSIS_EXPORT_CONFIGURATION)
        project.artifacts.add(ANALYSIS_EXPORT_CONFIGURATION, analysisTaskProvider)
        project.afterEvaluate {
            graphExtension.analysisTasks.get().forEach { taskProvider ->
                taskProvider.configure { task ->
                    with(task) {
                        if (!task.inputGraph.isPresent) {
                            dependsOn(consolidationTaskProvider)
                            inputGraph.set(consolidatedFile)
                        }
                        if (!outputGraph.isPresent) {
                            outputGraph.set(paths.intermediateGraph(name))
                        }
                    }
                }
                analysisTaskProvider.configure { task ->
                    with(task) {
                        dependsOn(taskProvider)
                        graphFiles.from(taskProvider.map { it.outputGraph })
                    }
                }
            }
            graphExtension.consumerTasks.get().forEach { taskProvider ->
                taskProvider.configure { task ->
                    with(task) {
                        dependsOn(analysisTaskProvider)
                        inputGraph.set(analysisFile)
                    }
                }
            }
        }

        // Direct comparison task
        project.tasks.register(DIRECT_COMPARISON_TASK, DirectComparisonTask::class.java) { task ->
            task.apply {
                group = TASK_GROUP
                description = "Directly compare two existing graph files"
                outputFile.set(directComparisonFile)
            }
        }

        // Project inspection task
        project.tasks.register(PROJECT_INSPECTION_TASK, InspectionTask::class.java) { task ->
            task.apply {
                group = TASK_GROUP
                description = "Extract project-specific information from the graph analysis"
                dependsOn(analysisTaskProvider)
                inputGraph.set(analysisFile)
                outputFile.set(projectReportFile)
                selfInfoProp.set(selfInfo)
            }
        }

        // Verification project graph (can include other project graphs to provide a holistic picture)
        val validationDependencies = project.createResolvableConfig(VALIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION).apply {
            dependencies.addAllLater(validationExtension.validatedProjects.map { projectPaths ->
                projectPaths.map { projectPath ->
                    project.dependencies.project(
                        mapOf(
                            "path" to projectPath,
                            "configuration" to ANALYSIS_EXPORT_CONFIGURATION,
                        )
                    )
                }
            })
        }
        project.tasks.register("graphValidation", GraphValidationTask::class.java) { task ->
            with(task) {
                group = TASK_GROUP
                description = "Validate the graph correctness against configured rules"
                projectPathProp.set(project.path)
                rootProjectPath.set(project.isolated.rootProject.projectDirectory)
                inputGraphs.setFrom(validationDependencies)
                outputFile.set(validationReportFile)
                definedRules.set(validationExtension.rules.map { rulesMap ->
                    rulesMap.values.map { rule -> rule.matcher.describe() }
                })
                definedRuleOverrides.set(validationExtension.ruleOverrides.map { overridesMap ->
                    overridesMap.values.map { rule -> rule.describe() }
                })
                rules.set(validationExtension.rules)
                ruleOverrides.set(validationExtension.ruleOverrides)
                ignoredRulesProp.set(validationExtension.ignore)
            }
        }

        project.afterEvaluate {
            val classifier = graphExtension.configurationClassifier.convention(ConfigurationClassifierDefault()).get()
            project.configurations.configureEach { config ->
                val configClass = classifier.classify(config)
                if (configClass == ConfigurationClass.OTHER) return@configureEach

                val (dependenciesConfig, taskProvider) = when(configClass) {
                    ConfigurationClass.PRODUCTION -> {
                        Pair(prodDependencies, gatherProdDependenciesTaskProvider)
                    }
                    ConfigurationClass.TEST -> {
                        Pair(testDependencies, gatherTestDependenciesTaskProvider)
                    }
                    else -> {
                        // Should never happen
                        throw(GradleException("Unsupported config class: $configClass"))
                    }
                }

                val projectDependencies = config.allDependencies.filterIsInstance<ProjectDependency>()
                projectDependencies.map { it.dependencyProject }.forEach { depProject ->
                    addDependency(
                        project = project,
                        configuration =  dependenciesConfig,
                        configurationTask = taskProvider,
                        configurationClass = configClass,
                        dependencyProject = depProject,
                        edgeLabel = config.name
                    )

                    if (configClass == ConfigurationClass.PRODUCTION) {
                        // For production dependencies we want to link to the consolidated
                        // project graphs of the dependency projects into our own graph
                        consolidationTaskProvider.configure {
                            it.dependsOn(depProject.tasks.named(CONSOLIDATION_DEPENDENCIES_RESOLVE_TASK))
                            it.graphFiles.from(paths.intermediateGraph("consolidatedDependencies", depProject))
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a string description of the graph matcher so that any change to its configuration will invalidate
     * cached results.
     */
    private fun GraphMatcher<RootedVertex>.describe(): String {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val root = VertexInfo(path = ":dummy")
        graph.addVertex(root)
        return matches(RootedVertex(graph = graph, root = root)).render(onlyMatches = false)
    }

    private fun createVertexInfo(project: Project, graphExtension: GraphExtension): Provider<VertexInfo> {
        return project.provider {
            val collectors = graphExtension.vertexAttributeCollectors.get()
            VertexInfo(path = project.path).also {
                collectors.forEach { collector ->
                    collector.collectConfigurationTimeAttributes(it)
                }
            }
        }
    }

    private fun addDependency(
        project: Project,
        configuration: Configuration,
        configurationTask: TaskProvider<GatherTask>,
        configurationClass: ConfigurationClass,
        dependencyProject: Project,
        edgeLabel: String,
    ) {
        // We always depend on the production dependencies configuration.  i.e., `testImplementation(foo)` wouldn't
        // depend upon `foo`'s `testImplementation`, it would depend upon `foo`'s `implementation`.
        val newDep = project.dependencies.project(
            mapOf(
                "path" to dependencyProject.path,
                "configuration" to GATHER_PROD_DEPENDENCIES_EXPORT_CONFIGURATION,
            )
        )
        configuration.dependencies.add(newDep)

        configurationTask.configure {
            val relation = GraphRelation(
                from = project.path,
                to = dependencyProject.path,
                edge = EdgeInfo().apply {
                    attributes["configuration"] = DefaultAttribute.createAttribute(edgeLabel)
                    attributes["class"] = DefaultAttribute.createAttribute(configurationClass.name)
                }
            )
            it.explicitRelationships.add(relation)
        }
    }

    private fun Project.createResolvableConfig(name: String): Configuration {
        return configurations.create(name).apply {
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
    }

    private fun Project.createConsumableConfig(name: String): Configuration {
        return configurations.create(name).apply {
            isCanBeConsumed = true
            isCanBeResolved = false
            isTransitive = false
        }
    }

    companion object {
        const val EXTENSION_NAME = "graphAnalytics"
        const val TASK_GROUP = "graph analytics"
        const val GRAPH_PERSISTENCE_BUILD_SERVICE = "graphPersistence"

        const val ANALYSIS_TASK = "graphAnalysis"
        const val DIRECT_COMPARISON_TASK = "graphComparison"
        const val PROJECT_INSPECTION_TASK = "graphInspection"

        private const val GATHER_PROD_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_prodDependencies"
        private const val GATHER_PROD_DEPENDENCIES_RESOLVE_TASK = "graphProductionDependencies"
        private const val GATHER_PROD_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_prodDependencies"

        private const val GATHER_TEST_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_testDependencies"
        private const val GATHER_TEST_DEPENDENCIES_RESOLVE_TASK = "graphTestDependencies"
        private const val GATHER_TEST_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_testDependencies"

        private const val CONSOLIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_consolidatedDependencies"
        private const val CONSOLIDATION_DEPENDENCIES_RESOLVE_TASK = "graphConsolidatedDependencies"
        private const val CONSOLIDATION_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_consolidatedDependencies"

        private const val VALIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_validationDependencies"

        private const val ANALYSIS_EXPORT_CONFIGURATION = "graphAnalytics_analysis"
    }
}
