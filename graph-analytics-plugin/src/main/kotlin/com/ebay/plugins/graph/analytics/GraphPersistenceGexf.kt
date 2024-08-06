package com.ebay.plugins.graph.analytics

import org.gradle.api.GradleException
import org.jgrapht.Graph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.gexf.GEXFAttributeType
import org.jgrapht.nio.gexf.GEXFExporter
import org.jgrapht.nio.gexf.SimpleGEXFImporter
import java.io.File

/**
 * Persistence implementation that writes GEXF XML files, as per:
 * https://gexf.net/
 */
internal class GraphPersistenceGexf : GraphPersistence {
    override val fileExtension = "gexf"
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
        val importer = SimpleGEXFImporter<VertexInfo, EdgeInfo>().apply {
            addGraphAttributeConsumer { str, attr ->
                if (str == GRAPH_VERSION_ATTR && attr.value != version.toString()) {
                    // Should never happen since we pass the version in as an input to each task
                    throw GradleException("Attempting to load unsupported version")
                }
            }
            addVertexAttributeConsumer { pair, attribute ->
                // pair.first can be null if the same vertex is added multiple times
                if (pair.first == null) return@addVertexAttributeConsumer

                if (!VERTEX_RESERVED_ATTRIBUTES.contains(pair.second.lowercase())) {
                    pair.first.attributes[pair.second] = attribute
                }
            }
            addEdgeAttributeConsumer { pair, attribute ->
                // pair.first can be null if the same edge is added multiple times
                if (pair.first == null) return@addEdgeAttributeConsumer

                if (!EDGE_RESERVED_ATTRIBUTES.contains(pair.second.lowercase())) {
                    pair.first.attributes[pair.second] = attribute
                }
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
        val exporter = GEXFExporter<VertexInfo, EdgeInfo>().apply {
            creator = "eBay Project Cost Plugin"
            setVertexIdProvider { it.path }
            vertexKeyMap.forEach { (key, type) ->
                registerAttribute(key, GEXFExporter.AttributeCategory.NODE, toGexfType(type))
            }
            edgeKeyMap.forEach { (key, type) ->
                registerAttribute(key, GEXFExporter.AttributeCategory.EDGE, toGexfType(type))
            }
            setGraphAttributeProvider {
                mapOf(GRAPH_VERSION_ATTR to DefaultAttribute(version, AttributeType.INT))
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

    private fun toGexfType(attrType: AttributeType): GEXFAttributeType {
        return when(attrType) {
            AttributeType.BOOLEAN -> { GEXFAttributeType.BOOLEAN }
            AttributeType.INT -> { GEXFAttributeType.INTEGER }
            AttributeType.LONG -> { GEXFAttributeType.LONG }
            AttributeType.FLOAT -> { GEXFAttributeType.FLOAT }
            AttributeType.DOUBLE -> { GEXFAttributeType.DOUBLE }
            AttributeType.STRING -> { GEXFAttributeType.STRING }
            else -> throw UnsupportedOperationException("Unsupported attribute type: $attrType")
        }
    }

    companion object {
        private const val GRAPH_VERSION_ATTR = "version"

        // From: org.jgrapht.nio.gexf.GEXFExporter.VERTEX_RESERVED_ATTRIBUTES
        private val VERTEX_RESERVED_ATTRIBUTES = setOf("id", "label")

        // From: org.jgrapht.nio.gexf.GEXFExporter.EDGE_RESERVED_ATTRIBUTES
        private val EDGE_RESERVED_ATTRIBUTES = setOf("id", "type", "label", "source", "target", "weight")
    }
}