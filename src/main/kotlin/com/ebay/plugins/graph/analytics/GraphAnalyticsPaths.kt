package com.ebay.plugins.graph.analytics

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Helper class to provide paths for the graph analytics plugin.
 */
class GraphAnalyticsPaths(
    private val defaultBuildDirectory: DirectoryProperty,
    private val graphPersistenceBuildServiceProvider: Provider<GraphPersistenceBuildService>,
) {
    private val ext by lazy {
        graphPersistenceBuildServiceProvider.get().delegate.fileExtension
    }

    fun intermediateGraph(id: String, buildDirectory: DirectoryProperty = defaultBuildDirectory): Provider<RegularFile> {
        return intermediate("$id.$ext", buildDirectory)
    }

    private fun intermediate(filename: String, buildDirectory: DirectoryProperty = defaultBuildDirectory): Provider<RegularFile> {
        return buildDirectory.file("$PLUGIN_BUILD_DIR/intermediate/$filename")
    }

    fun reportGraph(id: String, buildDirectory: DirectoryProperty = defaultBuildDirectory): Provider<RegularFile> {
        return report("$id.$ext", buildDirectory)
    }

    fun report(filename: String, buildDirectory: DirectoryProperty = defaultBuildDirectory): Provider<RegularFile> {
        return buildDirectory.file("$PLUGIN_BUILD_DIR/$filename")
    }

    companion object {
        private const val PLUGIN_BUILD_DIR = "graphAnalytics"
    }
}