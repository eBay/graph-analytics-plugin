package com.ebay.plugins.graph.analytics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
abstract class BaseGraphPersistenceTest {

    internal abstract val uut: GraphPersistence

    private val tempDir: File by lazy {
        Files.createTempDirectory(javaClass.simpleName).toFile()
    }

    @BeforeMethod
    fun setupTempDir() {
        tempDir.deleteRecursively()
        tempDir.mkdirs()
    }

    @AfterMethod
    fun cleanupTempDir() {
        tempDir.deleteRecursively()
    }

    @Test
    fun vertexAttributeSupport() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)

        val attributes = mutableMapOf<String, Attribute>()
        val supportedTypes = uut.supportedAttributeTypes
        if (supportedTypes.contains(AttributeType.NULL)) {
            attributes["null"] = DefaultAttribute(null, AttributeType.NULL)
        }
        if (supportedTypes.contains(AttributeType.BOOLEAN)) {
            attributes["boolean"] = DefaultAttribute(true, AttributeType.BOOLEAN)
        }
        if (supportedTypes.contains(AttributeType.INT)) {
            attributes["int"] = DefaultAttribute(1337, AttributeType.INT)
        }
        if (supportedTypes.contains(AttributeType.LONG)) {
            attributes["long"] = DefaultAttribute(1337L, AttributeType.LONG)
        }
        if (supportedTypes.contains(AttributeType.FLOAT)) {
            attributes["float"] = DefaultAttribute(1337.0f, AttributeType.FLOAT)
        }
        if (supportedTypes.contains(AttributeType.DOUBLE)) {
            attributes["double"] = DefaultAttribute(1337.0, AttributeType.DOUBLE)
        }
        if (supportedTypes.contains(AttributeType.STRING)) {
            attributes["string"] = DefaultAttribute("stringValue", AttributeType.STRING)
        }
        if (supportedTypes.contains(AttributeType.HTML)) {
            attributes["html"] = DefaultAttribute("<b>html</b>", AttributeType.HTML)
        }
        if (supportedTypes.contains(AttributeType.UNKNOWN)) {
            attributes["unknown"] = DefaultAttribute("mystery", AttributeType.UNKNOWN)
        }
        if (supportedTypes.contains(AttributeType.IDENTIFIER)) {
            attributes["identifier"] = DefaultAttribute("1:2:3:4:5:6", AttributeType.IDENTIFIER)
        }

        VertexInfo(path = ":1", attributes = attributes).also { graph.addVertex(it) }

        val persisted = File(tempDir, "graph.${uut.fileExtension}")
        uut.export(graph, persisted)
        println("persisted:\n${persisted.readText()}")

        val imported = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        uut.import(imported, persisted)

        // For debugging, dump to console
        val importedPersisted = File(tempDir, "imported.${uut.fileExtension}")
        uut.export(imported, importedPersisted)
        println("importedPersisted:\n${importedPersisted.readText()}")

        assertThat(graph.vertexSet().size, equalTo(1))
        assertThat(graph.vertexSet().size, equalTo(imported.vertexSet().size))

        graph.vertexSet().forEach { vertex ->
            // We have to work around edge reference equality issues and manually verify the edges
            val importedVertex = imported.vertexSet().find { it.path == vertex.path } ?: throw AssertionError("Vertex not found")

            assertThat("Path mismatch", vertex.path, equalTo(importedVertex.path))
            // ID attribute is automatically added
            importedVertex.attributes.remove("ID")
            assertThat("Attribute count mismatch", vertex.attributes.size, equalTo(importedVertex.attributes.size))
            vertex.attributes.forEach { (key, expected) ->
                val actual = importedVertex.attributes[key]
                assertThat("Key '$key' not found", actual, notNullValue())
                assertThat("Attribute '$key' type mismatch", expected.type, equalTo(actual?.type))
            }
        }
    }

    @Test
    fun edgeAttributeSupport() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = VertexInfo(path = ":1").also { graph.addVertex(it) }
        val vertex2 = VertexInfo(path = ":2").also { graph.addVertex(it) }
        graph.addEdge(vertex1, vertex2, EdgeInfo().apply {
            val supportedTypes = uut.supportedAttributeTypes
            if (supportedTypes.contains(AttributeType.NULL)) {
                attributes["null"] = DefaultAttribute(null, AttributeType.NULL)
            }
            if (supportedTypes.contains(AttributeType.BOOLEAN)) {
                attributes["boolean"] = DefaultAttribute(true, AttributeType.BOOLEAN)
            }
            if (supportedTypes.contains(AttributeType.INT)) {
                attributes["int"] = DefaultAttribute(1337, AttributeType.INT)
            }
            if (supportedTypes.contains(AttributeType.LONG)) {
                attributes["long"] = DefaultAttribute(1337L, AttributeType.LONG)
            }
            if (supportedTypes.contains(AttributeType.FLOAT)) {
                attributes["float"] = DefaultAttribute(1337.0f, AttributeType.FLOAT)
            }
            if (supportedTypes.contains(AttributeType.DOUBLE)) {
                attributes["double"] = DefaultAttribute(1337.0, AttributeType.DOUBLE)
            }
            if (supportedTypes.contains(AttributeType.STRING)) {
                attributes["string"] = DefaultAttribute("stringValue", AttributeType.STRING)
            }
            if (supportedTypes.contains(AttributeType.HTML)) {
                attributes["html"] = DefaultAttribute("<b>html</b>", AttributeType.HTML)
            }
            if (supportedTypes.contains(AttributeType.UNKNOWN)) {
                attributes["unknown"] = DefaultAttribute("mystery", AttributeType.UNKNOWN)
            }
            if (supportedTypes.contains(AttributeType.IDENTIFIER)) {
                attributes["identifier"] = DefaultAttribute("1:2:3:4:5:6", AttributeType.IDENTIFIER)
            }
        })

        val persisted = File(tempDir, "graph.${uut.fileExtension}")
        uut.export(graph, persisted)
        println("persisted:\n${persisted.readText()}")

        val imported = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        uut.import(imported, persisted)

        // For debugging, dump to console
        val importedPersisted = File(tempDir, "imported.${uut.fileExtension}")
        uut.export(imported, importedPersisted)
        println("importedPersisted:\n${importedPersisted.readText()}")

        graph.vertexSet().forEach { vertex ->
            assertThat(imported.containsVertex(vertex), equalTo(true))
        }

        assertThat(graph.edgeSet().size, equalTo(1))
        assertThat(graph.edgeSet().size, equalTo(imported.edgeSet().size))
        graph.edgeSet().forEach { edge ->
            // We have to work around edge reference equality issues and manually verify the edges
            val importedEdge = imported.edgeSet().iterator().next()
            assertThat("Source mismatch", graph.getEdgeSource(edge), equalTo(imported.getEdgeSource(importedEdge)))
            assertThat("Target mismatch", graph.getEdgeTarget(edge), equalTo(imported.getEdgeTarget(importedEdge)))
            assertThat("Attribute count mismatch", edge.attributes.size, equalTo(importedEdge.attributes.size))
            edge.attributes.forEach { (key, expected) ->
                val actual = importedEdge.attributes[key]
                assertThat("Key '$key' not found", actual, not(nullValue()))
                assertThat("Attribute '$key' type mismatch", expected.type, equalTo(actual?.type))
            }
        }
    }

    @Test
    fun exportImport() {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex1 = createVertexWithAttribute(path = ":1", attrId="v1").also { graph.addVertex(it) }
        val vertex2 = createVertexWithAttribute(path = ":2", attrId="v2").also { graph.addVertex(it) }
        val vertex3 = createVertexWithAttribute(path = ":3", attrId="v3").also { graph.addVertex(it) }
        graph.addEdge(vertex2, vertex1, createEdgeWithAttribute("2_1"))
        graph.addEdge(vertex3, vertex2, createEdgeWithAttribute("3_2"))
        graph.addEdge(vertex3, vertex1, createEdgeWithAttribute("3_1"))

        val persisted = File(tempDir, "graph.${uut.fileExtension}")
        uut.export(graph, persisted)
        println(persisted.readText())

        val imported = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        uut.import(imported, persisted)

        val importedPersisted = File(tempDir, "imported.${uut.fileExtension}")
        uut.export(imported, importedPersisted)
        println(importedPersisted.readText())

        graph.vertexSet().forEach { vertex ->
            assertThat(imported.containsVertex(vertex), equalTo(true))
        }

        assertThat(graph.edgeSet().size, equalTo(imported.edgeSet().size))
        graph.edgeSet().forEach { edge ->
            // We have to work around edge reference equality issues and manually verify the edges
            val found = imported.edgeSet().find { edge == it } != null
            assertThat(found, equalTo(true))
        }
    }

    @Test
    fun graftedImports() {
        val graph1 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex11 = VertexInfo(path = ":1_1").also { graph1.addVertex(it) }
        val vertex12 = VertexInfo(path = ":1_2").also { graph1.addVertex(it) }
        val vertex13 = VertexInfo(path = ":1_3").also { graph1.addVertex(it) }
        graph1.addEdge(vertex12, vertex11, createEdgeWithAttribute("12_11"))
        graph1.addEdge(vertex13, vertex12, createEdgeWithAttribute("13_12"))
        graph1.addEdge(vertex13, vertex11, createEdgeWithAttribute("13_11"))
        val persisted1 = File(tempDir, "graph1.${uut.fileExtension}")
        uut.export(graph1, persisted1)

        val graph2 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex21 = VertexInfo(path = ":2_1").also { graph2.addVertex(it) }
        val vertex22 = VertexInfo(path = ":2_2").also { graph2.addVertex(it) }
        val vertex23 = VertexInfo(path = ":2_3").also { graph2.addVertex(it) }
        graph2.addEdge(vertex22, vertex21, createEdgeWithAttribute("22_21"))
        graph2.addEdge(vertex23, vertex22, createEdgeWithAttribute("23_22"))
        graph2.addEdge(vertex23, vertex21, createEdgeWithAttribute("23_22"))
        val persisted2 = File(tempDir, "graph2.${uut.fileExtension}")
        uut.export(graph2, persisted2)

        val graph3 = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val vertex31 = VertexInfo(path = ":3_1").also { graph3.addVertex(it) }
        graph3.addVertex(vertex13)
        graph3.addVertex(vertex23)
        graph3.addEdge(vertex31, vertex13, createEdgeWithAttribute("31_13"))
        graph3.addEdge(vertex31, vertex23, createEdgeWithAttribute("31_23"))
        val persisted3 = File(tempDir, "graph3.${uut.fileExtension}")
        uut.export(graph3, persisted3)

        val imported = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        uut.import(imported, persisted1)
        uut.import(imported, persisted2)
        uut.import(imported, persisted3)

        buildSet {
            addAll(graph1.vertexSet())
            addAll(graph2.vertexSet())
            addAll(graph3.vertexSet())
        }.forEach { vertex ->
            assertThat(imported.containsVertex(vertex), equalTo(true))
        }

        buildSet {
            addAll(graph1.edgeSet())
            addAll(graph2.edgeSet())
            addAll(graph3.edgeSet())
        }.run {
            assertThat(size, equalTo(imported.edgeSet().size))
            forEach { edge ->
                // We have to work around edge reference equality issues and manually verify the edges
                val found = imported.edgeSet().find { edge == it } != null
                assertThat(found, equalTo(true))
            }
        }
    }

    private fun createVertexWithAttribute(path: String, attrId: String): VertexInfo {
        return VertexInfo(path = path, attributes = mutableMapOf(
            "attr_$attrId" to DefaultAttribute(attrId, AttributeType.STRING)
        ))
    }

    private fun createEdgeWithAttribute(id: String): EdgeInfo {
        return EdgeInfo().apply {
            attributes["attr_$id"] = DefaultAttribute(id, AttributeType.STRING)
        }
    }
}