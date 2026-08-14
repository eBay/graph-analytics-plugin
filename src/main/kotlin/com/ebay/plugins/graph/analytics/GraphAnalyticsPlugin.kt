package com.ebay.plugins.graph.analytics

import com.ebay.plugins.graph.analytics.validation.GraphValidationExtension
import com.ebay.plugins.graph.analytics.validation.GraphValidationTask
import com.ebay.plugins.graph.analytics.validation.RootedVertex
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatcher
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Category
import org.gradle.api.capabilities.Capability
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.GradleVersion
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.DefaultAttribute

/**
 * Plugin implementation which defines tasks and configurations artifacts which are used to
 * generate project dependency graph analytic data.
 */
@Suppress("unused", "UnstableApiUsage")
internal class GraphAnalyticsPlugin : Plugin<Any> {
    override fun apply(target: Any) {
        when (target) {
            is Project -> applyProject(target)
            is Settings -> applySettings(target)
            else -> throw GradleException("GraphAnalyticsPlugin can only be applied to a Project")
        }
    }

    private fun applySettings(settings: Settings) {
        // Verify Gradle version compatibility
        if (GradleVersion.current() < GradleVersion.version(GRADLE_VERSION_MINIMUM)) {
            throw GradleException("GraphAnalyticsPlugin requires Gradle $GRADLE_VERSION_MINIMUM " +
                    "or later (was: ${settings.gradle.gradleVersion})"
            )
        }

        // Apply the plugin to all projects
        settings.gradle.beforeProject { project ->
            project.plugins.apply(GraphAnalyticsPlugin::class.java)
        }
    }

    private fun applyProject(project: Project) {
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

        val paths = GraphAnalyticsPaths(project.layout.buildDirectory, graphPersistenceBuildServiceProvider)
        val prodDependenciesFile = paths.intermediateGraph("productionDependencies")
        val testDependenciesFile = paths.intermediateGraph("testDependencies")
        val consolidatedFile = paths.intermediateGraph("consolidatedDependencies")
        val analysisFile = paths.reportGraph("analysis")
        val directComparisonFile =  paths.report("directComparison.txt")
        val projectReportFile = paths.report("projectReport.txt")
        val validationReportFile = paths.report("validationReport.txt")

        // Production dependencies:
        val prodDependencyScope = project.createDependencyScopeConfig(GATHER_PROD_DEPENDENCIES_SCOPE_CONFIGURATION)
        val prodDependencies = project.createResolvableConfig(
            GATHER_PROD_DEPENDENCIES_RESOLVE_CONFIGURATION,
            extendsFromConfig = prodDependencyScope,
        )
        val gatherProdDependenciesTaskProvider = project.tasks.register(GATHER_PROD_DEPENDENCIES_RESOLVE_TASK, GatherTask::class.java)
        gatherProdDependenciesTaskProvider.configure { task ->
                task.apply {
                    contributedGraphs.setFrom(prodDependencies)
                    selfInfoProp.set(selfInfo)
                    outputFile.set(prodDependenciesFile)
                }
            }
        project.createConsumableConfig(
            GATHER_PROD_DEPENDENCIES_EXPORT_CONFIGURATION,
            GRAPH_ANALYTICS_KIND_PROD_DEPENDENCIES,
            extendsFromConfig = prodDependencyScope,
        ).configure { it.outgoing.artifact(gatherProdDependenciesTaskProvider) }

        // Test dependencies:
        val testDependencyScope = project.createDependencyScopeConfig(GATHER_TEST_DEPENDENCIES_SCOPE_CONFIGURATION)
        val testDependencies = project.createResolvableConfig(
            GATHER_TEST_DEPENDENCIES_RESOLVE_CONFIGURATION,
            extendsFromConfig = testDependencyScope,
        )
        val gatherTestDependenciesTaskProvider = project.tasks.register(GATHER_TEST_DEPENDENCIES_RESOLVE_TASK, GatherTask::class.java)
        gatherTestDependenciesTaskProvider.configure { task ->
                task.apply {
                    contributedGraphs.setFrom(testDependencies)
                    selfInfoProp.set(selfInfo)
                    outputFile.set(testDependenciesFile)
                }
            }
        project.createConsumableConfig(
            GATHER_TEST_DEPENDENCIES_EXPORT_CONFIGURATION,
            GRAPH_ANALYTICS_KIND_TEST_DEPENDENCIES,
            extendsFromConfig = testDependencyScope,
        ).configure { it.outgoing.artifact(gatherTestDependenciesTaskProvider) }

        // Consolidated project graph
        val consolidatedDependencyScope = project.createDependencyScopeConfig(CONSOLIDATION_DEPENDENCIES_SCOPE_CONFIGURATION)
        val consolidatedDependencies = project.createResolvableConfig(
            CONSOLIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION,
            extendsFromConfig = consolidatedDependencyScope,
        )
        val consolidationTaskProvider = project.tasks.register(CONSOLIDATION_DEPENDENCIES_RESOLVE_TASK, ConsolidationTask::class.java)
        consolidationTaskProvider.configure { task ->
            task.apply {
                dependsOn(gatherProdDependenciesTaskProvider)
                dependsOn(gatherTestDependenciesTaskProvider)
                graphFiles.from(consolidatedDependencies)
                graphFiles.from(testDependenciesFile)
                graphFiles.from(prodDependenciesFile)
                outputFile.set(consolidatedFile)
            }
        }
        project.createConsumableConfig(
            CONSOLIDATION_DEPENDENCIES_EXPORT_CONFIGURATION,
            GRAPH_ANALYTICS_KIND_CONSOLIDATED_DEPENDENCIES,
            extendsFromConfig = consolidatedDependencyScope,
        ).configure { it.outgoing.artifact(consolidationTaskProvider) }

        // Project graph analysis task
        val analysisTaskProvider = project.tasks.register(ANALYSIS_TASK, ConsolidationTask::class.java)
        analysisTaskProvider.configure { task ->
            task.apply {
                group = TASK_GROUP
                description = "Creates a project graph with embedded analysis"
                outputFile.set(analysisFile)
            }
        }
        project.createConsumableConfig(ANALYSIS_EXPORT_CONFIGURATION, GRAPH_ANALYTICS_KIND_ANALYSIS)
            .configure { it.outgoing.artifact(analysisTaskProvider) }
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
                dependsOn(analysisTaskProvider) // In case one of the arguments is not provided
                defaultAnalysisFile.set(analysisFile)
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
        val validationDependencyScope = project.createDependencyScopeConfig(VALIDATION_DEPENDENCIES_SCOPE_CONFIGURATION)
        val validationDependencies = project.createResolvableConfig(
            VALIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION,
            extendsFromConfig = validationDependencyScope,
        )
        validationDependencyScope.configure { validationConfig ->
            validationConfig.dependencies.addAllLater(validationExtension.validatedProjects.map { projectPaths ->
                projectPaths.filter { it != project.path }.map { projectPath ->
                    projectDependencyRequiringCapability(
                        project = project,
                        dependencyProjectPath = projectPath,
                        kind = GRAPH_ANALYTICS_KIND_ANALYSIS,
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
                // A project cannot variant-resolve itself ("No variants exist"), so the local
                // analysis output is used when this project is in the validation set.
                inputGraphs.setFrom(validationExtension.validatedProjects.map { projectPaths ->
                    if (projectPaths.contains(project.path)) {
                        project.files(analysisTaskProvider, validationDependencies)
                    } else {
                        project.files(validationDependencies)
                    }
                })
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
                val (dependenciesConfig, taskProvider) = when(configClass) {
                    ConfigurationClass.PRODUCTION -> {
                        Pair(prodDependencyScope, gatherProdDependenciesTaskProvider)
                    }
                    ConfigurationClass.TEST -> {
                        Pair(testDependencyScope, gatherTestDependenciesTaskProvider)
                    }
                    ConfigurationClass.OTHER -> {
                        // We only care about prod and test dependencies
                        return@configureEach
                    }
                }

                config.allDependencies.configureEach { dependency ->
                    if (dependency !is ProjectDependency) {
                        return@configureEach
                    }
                    val depProjectPath = dependency.path
                    addDependency(
                        project = project,
                        configurationName = dependenciesConfig.name,
                        configurationTask = taskProvider,
                        configurationClass = configClass,
                        dependencyProjectPath = depProjectPath,
                        edgeLabel = config.name
                    )

                    if (configClass == ConfigurationClass.PRODUCTION) {
                        // For production dependencies we want to link to the consolidated
                        // project graphs of the dependency projects into our own graph
                        project.dependencies.add(
                            consolidatedDependencyScope.name,
                            projectDependencyRequiringCapability(
                                project = project,
                                dependencyProjectPath = depProjectPath,
                                kind = GRAPH_ANALYTICS_KIND_CONSOLIDATED_DEPENDENCIES,
                            )
                        )
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
        configurationName: String,
        configurationTask: TaskProvider<GatherTask>,
        configurationClass: ConfigurationClass,
        dependencyProjectPath: String,
        edgeLabel: String,
    ) {
        // We always depend on the production dependencies configuration.  i.e., `testImplementation(foo)` wouldn't
        // depend upon `foo`'s `testImplementation`, it would depend upon `foo`'s `implementation`.
        project.dependencies.add(
            configurationName,
            projectDependencyRequiringCapability(
                project = project,
                dependencyProjectPath = dependencyProjectPath,
                kind = GRAPH_ANALYTICS_KIND_PROD_DEPENDENCIES,
            )
        )

        configurationTask.configure {
            val relation = GraphRelation(
                from = project.path,
                to = dependencyProjectPath,
                edge = EdgeInfo().apply {
                    attributes["configuration"] = DefaultAttribute.createAttribute(edgeLabel)
                    attributes["class"] = DefaultAttribute.createAttribute(configurationClass.name)
                }
            )
            it.explicitRelationships.add(relation)
        }
    }

    private fun projectDependencyRequiringCapability(
        project: Project,
        dependencyProjectPath: String,
        kind: String,
    ): ModuleDependency {
        val dep = project.dependencies.project(mapOf("path" to dependencyProjectPath)) as ModuleDependency
        dep.capabilities { caps ->
            caps.requireCapability(graphAnalyticsCapability(kind, dependencyProjectPath))
        }
        return dep
    }

    private fun Project.createDependencyScopeConfig(
        name: String,
    ): NamedDomainObjectProvider<DependencyScopeConfiguration> {
        return configurations.dependencyScope(name) { config ->
            config.isTransitive = false
        }
    }

    private fun Project.createResolvableConfig(
        name: String,
        extendsFromConfig: NamedDomainObjectProvider<out Configuration>,
    ): NamedDomainObjectProvider<ResolvableConfiguration> {
        return configurations.resolvable(name) { config ->
            config.isTransitive = false
            config.extendsFrom(extendsFromConfig.get())
        }
    }

    private fun Project.createConsumableConfig(
        name: String,
        kind: String,
        extendsFromConfig: NamedDomainObjectProvider<out Configuration>? = null,
    ): NamedDomainObjectProvider<ConsumableConfiguration> {
        return configurations.consumable(name) { config ->
            with(config) {
                isTransitive = false
                attributes.attribute(
                    Category.CATEGORY_ATTRIBUTE,
                    objects.named(Category::class.java, GRAPH_ANALYTICS_CATEGORY),
                )
                // Declaring any outgoing capability replaces the implicit project GAV.
                outgoing.capability(implicitProjectCapability())
                outgoing.capability(graphAnalyticsCapability(kind, path))

            }
            extendsFromConfig?.let { config.extendsFrom(it.get()) }
        }
    }

    private fun Project.implicitProjectCapability(): Capability {
        val owner = this
        return object : Capability {
            override fun getGroup(): String = owner.group.toString()
            override fun getName(): String = owner.name
            override fun getVersion(): String = owner.version.toString()
        }
    }

    companion object {
        private const val GRADLE_VERSION_MINIMUM = "8.11"

        const val EXTENSION_NAME = "graphAnalytics"
        const val TASK_GROUP = "graph analytics"
        const val GRAPH_PERSISTENCE_BUILD_SERVICE = "graphPersistence"

        const val ANALYSIS_TASK = "graphAnalysis"
        const val DIRECT_COMPARISON_TASK = "graphComparison"
        const val PROJECT_INSPECTION_TASK = "graphInspection"

        /**
         * Marker category so these configurations are treated as variants. Consumers must not
         * request this attribute; selection is by capability constraint only.
         */
        private const val GRAPH_ANALYTICS_CATEGORY = "graph-analytics"

        private const val GATHER_PROD_DEPENDENCIES_SCOPE_CONFIGURATION = "graphAnalytics_dependencies_prodDependencies"
        private const val GATHER_PROD_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_prodDependencies"
        private const val GATHER_PROD_DEPENDENCIES_RESOLVE_TASK = "graphProductionDependencies"
        private const val GATHER_PROD_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_prodDependencies"

        private const val GATHER_TEST_DEPENDENCIES_SCOPE_CONFIGURATION = "graphAnalytics_dependencies_testDependencies"
        private const val GATHER_TEST_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_testDependencies"
        private const val GATHER_TEST_DEPENDENCIES_RESOLVE_TASK = "graphTestDependencies"
        private const val GATHER_TEST_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_testDependencies"

        private const val CONSOLIDATION_DEPENDENCIES_SCOPE_CONFIGURATION = "graphAnalytics_dependencies_consolidatedDependencies"
        private const val CONSOLIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_consolidatedDependencies"
        private const val CONSOLIDATION_DEPENDENCIES_RESOLVE_TASK = "graphConsolidatedDependencies"
        private const val CONSOLIDATION_DEPENDENCIES_EXPORT_CONFIGURATION = "graphAnalytics_consolidatedDependencies"

        private const val ANALYSIS_EXPORT_CONFIGURATION = "graphAnalytics_analysis"
        private const val VALIDATION_DEPENDENCIES_SCOPE_CONFIGURATION = "graphAnalytics_dependencies_validationDependencies"
        private const val VALIDATION_DEPENDENCIES_RESOLVE_CONFIGURATION = "graphAnalytics_resolvable_validationDependencies"

        /**
         * Capability kinds that distinguish graph-analytics consumable variants.
         * These encode the former export configuration identity.
         */
        const val GRAPH_ANALYTICS_KIND_PROD_DEPENDENCIES = "prod-dependencies"
        const val GRAPH_ANALYTICS_KIND_TEST_DEPENDENCIES = "test-dependencies"
        const val GRAPH_ANALYTICS_KIND_CONSOLIDATED_DEPENDENCIES = "consolidated-dependencies"
        const val GRAPH_ANALYTICS_KIND_ANALYSIS = "analysis"

        /**
         * Capability coordinate used to select a graph-analytics variant of a project.
         *
         * Format: `com.ebay.plugins:graph-analytics-{kind}-{pathId}:1.0`
         *
         * [projectPath] is encoded in the capability name (not a variant attribute). Gradle
         * treats a capability GAV as unique in a resolution graph; a path attribute would
         * not make two modules providing the same GAV legal in one configuration.
         */
        fun graphAnalyticsCapability(kind: String, projectPath: String): String {
            val pathId = projectPath.trimStart(':').replace(':', '.').ifEmpty { "root" }
            return "com.ebay.plugins:graph-analytics-$kind-$pathId:1.0"
        }
    }
}
