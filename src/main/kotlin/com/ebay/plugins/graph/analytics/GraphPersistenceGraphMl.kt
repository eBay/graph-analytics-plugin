package com.ebay.plugins.graph.analytics

import org.gradle.api.GradleException
import org.jgrapht.Graph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.graphml.GraphMLExporter
import org.jgrapht.nio.graphml.GraphMLImporter
import java.io.File

/**
 * Persistence implementation which writes GraphML XML files, as per:
 * http://graphml.graphdrawing.org/
 */
internal class GraphPersistenceGraphMl : GraphPersistence {
    override val fileExtension = "graphml"
    override val version = 1
    override val supportedAttributeTypes = setOf(
        AttributeType.BOOLEAN,
        AttributeType.INT,
        AttributeType.LONG,
        AttributeType.FLOAT,
        AttributeType.DOUBLE,
        AttributeType.STRING,
    )

    override fun import(graph: Graph<VertexInfo, EdgeInfo>, file: File) {
        val importer = GraphMLImporter<VertexInfo, EdgeInfo>().apply {
            addGraphAttributeConsumer { str, attr ->
                if (str == GRAPH_VERSION_ATTR && attr.value != version.toString()) {
                    // Should never happen since we pass the version in as an input to each task
                    throw GradleException("Attempting to load unsupported version")
                }
            }
            addVertexAttributeConsumer { pair, str ->
                // pair.first can be null if the same vertex is added multiple times
                if (pair.first == null) return@addVertexAttributeConsumer

                if (pair.second == "ID") {
                    pair.first.path = str.value
                } else {
                    pair.first.attributes[pair.second] = str
                }
            }
            addEdgeAttributeConsumer { pair, str ->
                // pair.first can be null if the same edge is added multiple times
                if (pair.first == null) return@addEdgeAttributeConsumer

                pair.first.attributes[pair.second] = str
            }
            vertexFactory = java.util.function.Function { str ->
                VertexInfo(path = str)
            }
        }

        importer.importGraph(graph, file)
    }

    override fun export(graph: Graph<VertexInfo, EdgeInfo>, file: File) {
        val vertexKeyMap = mutableMapOf<String, AttributeType>()
        graph.vertexSet().map { it.attributes.entries }.forEach { attrMap ->
            attrMap.forEach { (key, attr) ->
                vertexKeyMap[key] = attr.type
            }
        }
        val edgeKeyMap = mutableMapOf<String, AttributeType>()
        graph.edgeSet().map { it.attributes.entries }.forEach { attrMap ->
            attrMap.forEach { (key, attr) ->
                edgeKeyMap[key] = attr.type
            }
        }
        val exporter = GraphMLExporter<VertexInfo, EdgeInfo>().apply {
            setVertexIdProvider { it.path }
            vertexKeyMap.forEach { (key, type) ->
                registerAttribute(key, GraphMLExporter.AttributeCategory.NODE, type)
            }
            edgeKeyMap.forEach { (key, type) ->
                registerAttribute(key, GraphMLExporter.AttributeCategory.EDGE, type)
            }
            setGraphAttributeProvider {
                mapOf(GRAPH_VERSION_ATTR to DefaultAttribute(version, AttributeType.INT))
            }
            setVertexIdProvider { vertexInfo ->
                vertexInfo.path
            }
            setVertexAttributeProvider { vertexInfo ->
                vertexInfo.attributes
            }
            setEdgeAttributeProvider { edgeInfo ->
                edgeInfo.attributes
            }
        }
        exporter.exportGraph(graph, file)
    }

    companion object {
        const val GRAPH_VERSION_ATTR = "version"
    }
}