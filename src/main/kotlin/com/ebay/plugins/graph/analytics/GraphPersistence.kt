package com.ebay.plugins.graph.analytics

import org.jgrapht.Graph
import org.jgrapht.nio.AttributeType
import java.io.File

/**
 * Interface ued to hide the persistence layer from the rest of the plugin code so that it can
 * be easily swapped out.
 */
interface GraphPersistence {
    /**
     * The file extension to use when persisting a graph of this type.  e.g. `.xml`
     */
    val fileExtension: String

    /**
     * The serialized format revision. Used to invalidate attempts to read versions from the
     * cache if the format changes. i.e., when changing the marshalled format, increment this
     * number.
     */
    val version: Int

    /**
     * The set of attribute types tha the format supports.
     */
    val supportedAttributeTypes: Set<AttributeType>

    /**
     * Import a graph from a file.
     */
    fun import(graph: Graph<VertexInfo, EdgeInfo>, file: File)

    /**
     * Export a graph to a file.
     */
    fun export(graph: Graph<VertexInfo, EdgeInfo>, file: File)
}