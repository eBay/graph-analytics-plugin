package com.ebay.plugins.graph.analytics

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.jgrapht.graph.DefaultDirectedGraph
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.File

/**
 * This suite of tests verifies the metrics are calculated properly for each of the projects
 * in a multi-project build.
 *
 * Due top metrics such as `inDegree` and `networkAbove`, the analyzed values for a given
 * project module may change depending upon which project is analyzed.  Thus, we check them
 * all.
 */
class PluginFunctionalTest : BasePluginFunctionalTest() {
    @BeforeTest
    fun setupGraph() {
        createProjectStructure(mapOf(
            "top" to DependenciesSpec(
                dependencies = listOf(":mid1:mid1Impl", ":mid2:mid2Impl"),
            ),
            "mid1" to DependenciesSpec(),
            "mid1/mid1Impl" to DependenciesSpec(
                dependencies = listOf(":mid1", ":leaf1"),
                testDependencies = listOf(":mid1:mid1TestSupport"),
            ),
            "mid1/mid1TestSupport" to DependenciesSpec(
                dependencies = listOf(":mid1"),
            ),
            "mid2" to DependenciesSpec(
                dependencies = listOf(":mid1")
            ),
            "mid2/mid2Impl" to DependenciesSpec(
                dependencies = listOf(":mid2", ":leaf2"),
                testDependencies = listOf(":mid1:mid1TestSupport", ":mid2:mid2TestSupport"),
            ),
            "mid2/mid2TestSupport" to DependenciesSpec(
                dependencies = listOf(":mid2"),
            ),
            "leaf1" to DependenciesSpec(),
            "leaf2" to DependenciesSpec(),
        ))
    }

    /**
     * Verify the analysis of the `:leaf1` project.
     */
    @Test
    fun checkLeaf1() {
        runAnalysis()
        assertGraph("leaf1/build/graphAnalytics/analysis.graphml") {
            assertProject(":leaf1") {
                assertThat(path, equalTo(":leaf1"))
                assertAttribute("degree", "0")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(1))
        }
    }

    /**
     * Verify the analysis of the `:leaf2` project.
     */
    @Test
    fun checkLeaf2() {
        runAnalysis()
        assertGraph("leaf2/build/graphAnalytics/analysis.graphml") {
            assertProject(":leaf2") {
                assertThat(path, equalTo(":leaf2"))
                assertAttribute("degree", "0")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(1))
        }
    }

    /**
     * Verify the analysis of the `:mid1` project.
     */
    @Test
    fun checkMid1() {
        runAnalysis()
        assertGraph("mid1/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "0")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(1))
        }
    }

    /**
     * Verify the analysis of the `:mid1:mid1Impl` project.
     */
    @Test
    fun checkMid1Impl() {
        runAnalysis()
        assertGraph("mid1/mid1Impl/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "2")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "2")
            }
            assertProject(":mid1:mid1TestSupport") {
                assertThat(path, equalTo(":mid1:mid1TestSupport"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "2")
            }
            assertProject(":leaf1") {
                assertThat(path, equalTo(":leaf1"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertProject(":mid1:mid1Impl") {
                assertThat(path, equalTo(":mid1:mid1Impl"))
                assertAttribute("degree", "3")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "3")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "4")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(4))
        }
    }

    /**
     * Verify the analysis of the `:mid1:mid1TestSupport` project.
     */
    @Test
    fun checkMid1TestSupport() {
        runAnalysis()
        assertGraph("mid1/mid1TestSupport/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertProject(":mid1:mid1TestSupport") {
                assertThat(path, equalTo(":mid1:mid1TestSupport"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(2))
        }
    }

    /**
     * Verify the analysis of the `:mid2` project.
     */
    @Test
    fun checkMid2() {
        runAnalysis()
        assertGraph("mid2/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertProject(":mid2") {
                assertThat(path, equalTo(":mid2"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(2))
        }
    }

    /**
     * Verify the analysis of the `:mid2:mid2Impl` project.
     */
    @Test
    fun checkMid2Impl() {
        runAnalysis()
        assertGraph("mid2/mid2Impl/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "2")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "5")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "2")
            }
            assertProject(":mid1:mid1TestSupport") {
                assertThat(path, equalTo(":mid1:mid1TestSupport"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "0.5")
                assertAttribute("expansionFactor", "2")
            }
            assertProject(":mid2") {
                assertThat(path, equalTo(":mid2"))
                assertAttribute("degree", "3")
                assertAttribute("inDegree", "2")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "1.5")
                assertAttribute("expansionFactor", "4")
            }
            assertProject(":mid2:mid2Impl") {
                assertThat(path, equalTo(":mid2:mid2Impl"))
                assertAttribute("degree", "4")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "4")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "6")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertProject(":mid2:mid2TestSupport") {
                assertThat(path, equalTo(":mid2:mid2TestSupport"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "3")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "3")
            }
            assertProject(":leaf2") {
                assertThat(path, equalTo(":leaf2"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertThat(vertexSet().size, equalTo(6))
        }
    }

    /**
     * Verify the analysis of the `:mid2:mid2Impl` project.
     */
    @Test
    fun checkMid2TestSupport() {
        runAnalysis()
        assertGraph("mid2/mid2TestSupport/build/graphAnalytics/analysis.graphml") {
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertProject(":mid2") {
                assertThat(path, equalTo(":mid2"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "1.0")
                assertAttribute("expansionFactor", "2")
            }
            assertProject(":mid2:mid2TestSupport") {
                assertThat(path, equalTo(":mid2:mid2TestSupport"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "3")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertThat(vertexSet().size, equalTo(3))
        }
    }

    @Test
    fun checkTop() {
        runAnalysis()

        assertGraph("top/build/graphAnalytics/analysis.graphml") {
            assertProject(":top") {
                assertThat(path, equalTo(":top"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "0")
                assertAttribute("outDegree", "2")
                assertAttribute("height", "4")
                assertAttribute("networkAbove", "1")
                assertAttribute("networkBelow", "9")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "0")
            }
            assertProject(":mid1") {
                assertThat(path, equalTo(":mid1"))
                assertAttribute("degree", "3")
                assertAttribute("inDegree", "3")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "7")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "3")
            }
            assertProject(":mid1:mid1Impl") {
                assertThat(path, equalTo(":mid1:mid1Impl"))
                assertAttribute("degree", "4")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "3")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "4")
                assertAttribute("betweennessCentrality", "2.5")
                assertAttribute("expansionFactor", "4")
            }
            assertProject(":mid1:mid1TestSupport") {
                assertThat(path, equalTo(":mid1:mid1TestSupport"))
                assertAttribute("degree", "3")
                assertAttribute("inDegree", "2")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "4")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "0.5")
                assertAttribute("expansionFactor", "4")
            }
            assertProject(":mid2") {
                assertThat(path, equalTo(":mid2"))
                assertAttribute("degree", "3")
                assertAttribute("inDegree", "2")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "2")
                assertAttribute("networkAbove", "4")
                assertAttribute("networkBelow", "2")
                assertAttribute("betweennessCentrality", "1.5")
                assertAttribute("expansionFactor", "4")
            }
            assertProject(":mid2:mid2Impl") {
                assertThat(path, equalTo(":mid2:mid2Impl"))
                assertAttribute("degree", "5")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "4")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "2")
                assertAttribute("networkBelow", "6")
                assertAttribute("betweennessCentrality", "3.5")
                assertAttribute("expansionFactor", "6")
            }
            assertProject(":mid2:mid2TestSupport") {
                assertThat(path, equalTo(":mid2:mid2TestSupport"))
                assertAttribute("degree", "2")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "1")
                assertAttribute("height", "3")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "3")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "3")
            }
            assertProject(":leaf1") {
                assertThat(path, equalTo(":leaf1"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertProject(":leaf2") {
                assertThat(path, equalTo(":leaf2"))
                assertAttribute("degree", "1")
                assertAttribute("inDegree", "1")
                assertAttribute("outDegree", "0")
                assertAttribute("height", "1")
                assertAttribute("networkAbove", "3")
                assertAttribute("networkBelow", "1")
                assertAttribute("betweennessCentrality", "0.0")
                assertAttribute("expansionFactor", "1")
            }
            assertThat(vertexSet().size, equalTo(9))
        }
    }

    @Test
    fun graphInspectionDefaultProject() {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(":top:graphInspection")
            .withPluginClasspath()
            .assertResult {
                assertTaskSuccess(":top:graphInspection")
                println(extractInspectionReport())
            }
    }

    @Test
    fun graphInspectionSpecifiedProject() {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(":top:graphInspection", "--project", ":mid2:mid2Impl")
            .withPluginClasspath()
            .assertResult {
                assertTaskSuccess(":top:graphInspection")
                println(extractInspectionReport())
        }
    }

    /**
     * For now, just exercise the task.
     */
    @Test
    fun graphValidation() {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("graphValidation")
            .withPluginClasspath()
            .assertResult {
                assertTaskSuccess(":top:graphValidation")
                assertTaskSuccess(":mid1:graphValidation")
                assertTaskSuccess(":mid1:mid1Impl:graphValidation")
                assertTaskSuccess(":mid1:mid1TestSupport:graphValidation")
                assertTaskSuccess(":mid2:graphValidation")
                assertTaskSuccess(":mid2:mid2Impl:graphValidation")
                assertTaskSuccess(":mid2:mid2TestSupport:graphValidation")
                assertTaskSuccess(":leaf1:graphValidation")
                assertTaskSuccess(":leaf2:graphValidation")
            }
    }

    /**
     * For now, just exercise the task.
     */
    @Test
    fun graphComparison() {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(
                ":top:graphComparison",
                "--before", "build/graphAnalytics/analysis.graphml",
                "--after", "build/graphAnalytics/analysis.graphml", // no delta. :)
            ).withPluginClasspath()
            .assertResult {
                assertTaskSuccess(":top:graphComparison")
                println(extractComparisonReport())
            }
    }

    private fun runAnalysis() {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("graphAnalysis")
            .withPluginClasspath()
            .build()
    }

    private fun GradleRunner.assertResult(assertionBlock: BuildResult.() -> Unit) {
        val result = build()
        kotlin.runCatching {
            assertionBlock.invoke(result)
        }.onFailure { e ->
            println("Assertion failure detected. Dumping debug data.")
            println(result.output)
            throw e
        }
    }

    private fun BuildResult.assertTaskSuccess(taskPath: String) {
        task(taskPath)?.let { task ->
            task.outcome?.let {
                assertThat(it, equalTo(TaskOutcome.SUCCESS))
            }
        } ?: throw AssertionError("Task $taskPath not found")
    }

    private fun BuildResult.extractInspectionReport(): String {
        val matcher = "Project inspection analysis comparison report available at: file://(.*)".toRegex()
        val matchResult = matcher.find(output)
        val filePath = matchResult?.groupValues?.get(1)
        assertThat("Unable to locate report location in output", filePath, not(nullValue()))
        return File(filePath!!).readText()
    }

    private fun BuildResult.extractComparisonReport(): String {
        val matcher = "Graph analysis comparison report available at: file://(.*)".toRegex()
        val matchResult = matcher.find(output)
        val filePath = matchResult?.groupValues?.get(1)
        assertThat("Unable to locate report location in output", filePath, not(nullValue()))
        return File(filePath!!).readText()
    }

    private fun assertGraph(
        path: String,
        assertionBlock: DefaultDirectedGraph<VertexInfo, EdgeInfo>.() -> Unit
    ) {
        val graph = DefaultDirectedGraph<VertexInfo, EdgeInfo>(EdgeInfo::class.java)
        val graphFile = projectDir.resolve(path)
        runCatching {
            persistence.import(graph, graphFile)
            assertionBlock.invoke(graph)
        }.onFailure { e ->
            println("Assertion failure detected. Dumping debug data.")
            val prodDepsFile = projectDir.resolve(path.replaceAfterLast(
                delimiter = File.separator,
                replacement = "intermediate${File.separator}productionDependencies.graphml"
            ))
            if (prodDepsFile.exists()) {
                println("Production Dependencies GraphML:\n${prodDepsFile.readText()}")
            }
            val testDepsFile = projectDir.resolve(path.replaceAfterLast(
                delimiter = File.separator,
                replacement = "intermediate${File.separator}testDependencies.graphml"
            ))
            if (testDepsFile.exists()) {
                println("Test Dependencies GraphML:\n${testDepsFile.readText()}")
            }
            println("Analysis GraphML:\n${graphFile.readText()}")
            throw(e)
        }
    }

    private fun DefaultDirectedGraph<VertexInfo, EdgeInfo>.assertProject(
        path: String,
        assertionBlock: VertexInfo.() -> Unit
    ) {
        val project = vertexSet().find { it.path == path }
        assertThat("Project $path was not found", project, not(nullValue()))
        assertionBlock.invoke(project!!)
    }

    private fun Attributed.assertAttribute(key: String, value: String?) {
        assertThat("$this $key", attributes[key]?.value, equalTo(value))
    }
}
