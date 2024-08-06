package com.ebay.plugins.graph.analytics.validation

import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatcher
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty

/**
 * Gradle extension for configuring [GraphValidationRule]s.
 */
abstract class GraphValidationExtension {
    /**
     * List of project paths to use as analysis inputs.  Default value: The project path of the project being validated.
     *
     * This is useful when the project verification needs to take into account a holistic graph
     * (e.g., from an application module's perspective) and not just a graph containing information
     * from itself and its dependencies.
     *
     * Each project added to this list will be individually validated against the rules.  The task will
     * fail if any project fails validation.
     *
     * If a project graph does not contain the project being validated, the task will ignore the verification
     * rules for that project and the task will succeed.
     */
    abstract val validatedProjects: ListProperty<String>

    /**
     * Map of Rule ID to rule definition.  These rules would typically be applied by a convention plugin and
     * describe the overall project requirements.
     */
    abstract val rules: MapProperty<String, GraphValidationRule>

    /**
     * Map of Rule ID to a custom rule definition for the project module.  This can be used to - for example -
     * modify a threshold for a specific project.
     */
    abstract val ruleOverrides: MapProperty<String, GraphMatcher<RootedVertex>>

    /**
     * List of Rule IDs to ignore.
     */
    abstract val ignore: ListProperty<String>
}