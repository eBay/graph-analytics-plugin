package com.ebay.plugins.graph.analytics

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * Base class used for tasks which need to read and write the graph file.
 */
abstract class BaseGraphPersistenceTask : DefaultTask() {
    // Pass the graph format in so that version changes invalidate the cache
    @get:Input
    internal abstract val graphFormat: Property<String>

    // Pass the graph version in so that version changes invalidate the cache
    @get:Input
    internal abstract val graphVersion: Property<Int>

    @get:Internal
    internal abstract val persistenceBuildService: Property<GraphPersistenceBuildService>
}