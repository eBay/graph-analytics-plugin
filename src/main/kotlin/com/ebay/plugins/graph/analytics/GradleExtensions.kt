package com.ebay.plugins.graph.analytics

import com.ebay.plugins.graph.analytics.validation.GraphValidationExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * Executes the provided block to configure the [GraphExtension] if the [GraphAnalyticsPlugin] is applied.
 */
fun Project.graph(block: GraphExtension.() -> Unit) {
    plugins.withType(GraphAnalyticsPlugin::class.java) {
        block.invoke(extensions.getByType(GraphExtension::class.java))
    }
}

/**
 * Executes the provided block to configure the [GraphValidationExtension].
 */
fun GraphExtension.validation(block: GraphValidationExtension.() -> Unit) {
    block.invoke(extensions.getByType(GraphValidationExtension::class.java))
}

/**
 * Helper function to configure the task to use the inputs of the given task.
 */
@Suppress("unused") // API method
fun TaskProvider<out BaseGraphInputOutputTask>.inputsFrom(
    project: Project,
    taskName: String,
    taskType: Class<out BaseGraphInputOutputTask>,
) {
    val inputTask = project.tasks.named(taskName, taskType)
    configure { task ->
        with(task) {
            dependsOn(inputTask)
            inputGraph.set(inputTask.get().outputGraph)
        }
    }
}