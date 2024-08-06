package com.ebay.plugins.graph.analytics

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

/**
 * Gradle extension used to configure the [GraphAnalyticsPlugin].
 */
abstract class GraphExtension : ExtensionAware {
    /**
     * Specify the classifier implementation used to categorize Gradle configuration classes
     * into [ConfigurationClass] categories.
     */
    abstract val configurationClassifier: Property<ConfigurationClassifier>

    /**
     * Provide a custom project (vertex) attribute collector.
     */
    abstract val vertexAttributeCollectors: ListProperty<VertexAttributeCollector>

    /**
     * Provide additional graph analysis tasks.  These task will be automatically configured
     * by the [GraphAnalyticsPlugin] with the following:
     * - [GraphPersistenceBuildService]
     * - [BaseGraphPersistenceTask.graphFormat]
     * - [BaseGraphPersistenceTask.graphVersion]
     * - [BaseGraphInputOutputTask.inputGraph] (if not explicitly specified)
     * - [BaseGraphInputOutputTask.outputGraph] (if not explicitly specified)
     * The results of these tasks will be merged into the final analysis.
     */
    abstract val analysisTasks: ListProperty<TaskProvider<out BaseGraphInputOutputTask>>

    /**
     * Provide addition tasks which should be configured to receive the fully constituted
     * graph data as an input.
     */
    abstract val consumerTasks: ListProperty<TaskProvider<out BaseGraphInputTask>>
}
