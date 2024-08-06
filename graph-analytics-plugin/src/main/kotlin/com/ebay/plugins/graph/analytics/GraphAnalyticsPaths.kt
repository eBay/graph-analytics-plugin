package com.ebay.plugins.graph.analytics

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Helper class to provide paths for the graph analytics plugin.
 */
class GraphAnalyticsPaths(
    private val defaultProject: Project,
    private val graphPersistenceBuildServiceProvider: Provider<GraphPersistenceBuildService>,
) {
    private val ext by lazy {
        graphPersistenceBuildServiceProvider.get().delegate.fileExtension
    }

    fun intermediateGraph(id: String, project: Project = defaultProject): Provider<RegularFile> {
        return intermediate("$id.$ext", project)
    }

    private fun intermediate(filename: String, project: Project = defaultProject): Provider<RegularFile> {
        return project.layout.buildDirectory.file("$PLUGIN_BUILD_DIR/intermediate/$filename")
    }

    fun reportGraph(id: String, project: Project = defaultProject): Provider<RegularFile> {
        return report("$id.$ext", project)
    }

    fun report(filename: String, project: Project = defaultProject): Provider<RegularFile> {
        return project.layout.buildDirectory.file("$PLUGIN_BUILD_DIR/$filename")
    }

    companion object {
        private const val PLUGIN_BUILD_DIR = "graphAnalytics"
    }
}