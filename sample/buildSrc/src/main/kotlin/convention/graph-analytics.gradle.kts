package convention

import com.ebay.plugins.graph.analytics.BaseGraphInputOutputTask
import com.ebay.plugins.graph.analytics.EdgeInfo
import com.ebay.plugins.graph.analytics.VertexAttributeCollector
import com.ebay.plugins.graph.analytics.VertexInfo
import com.ebay.plugins.graph.analytics.validation
import com.ebay.plugins.graph.analytics.validation.GraphValidationRule
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.allOf
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.edgeTarget
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.equalTo
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.greaterThan
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.hasOutgoingEdge
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.not
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.numericAttribute
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.stringAttribute
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.nio.AttributeType
import org.jgrapht.nio.DefaultAttribute

plugins {
    id("com.ebay.graph-analytics")
}

/**
 * Custom [VertexAttributeCollector] implementation, demonstrating how information can be
 * gathered from the project at configuration-time for use in subsequent analysis.  Note that
 * this work needs to be very lightweight as it executes during the configuration phase.
 */
class CustomVertexAttributeCollector : VertexAttributeCollector {
    override fun collectConfigurationTimeAttributes(vertexInfo: VertexInfo) {
        val isTestSupport = project.plugins.findPlugin("convention.test-support") != null
        vertexInfo.attributes["test-support"] = DefaultAttribute(isTestSupport, AttributeType.BOOLEAN)
    }
}

/**
 * Custom analysis tasks which categorizes the purpose of the modules in a project-specific
 * manner.
 *
 * This demonstrates the use of a custom [VertexAttributeCollector] (to identify test support
 * modules) as well as project path name parsing, resulting in a custom graph attribute being
 * applied to all nodes in the graph.
 */
abstract class VertexClassAnalysisTask : BaseGraphInputOutputTask() {
    override fun processGraph(graph: DefaultDirectedGraph<VertexInfo, EdgeInfo>) {
        graph.vertexSet().forEach { vertexInfo ->
            val vertexClass = when {
                vertexInfo.attributes["test-support"]?.value == "true" -> "test-support"
                vertexInfo.path.endsWith("-api") -> "api"
                vertexInfo.path.endsWith("-impl") -> "impl"
                vertexInfo.path.endsWith("app") -> "application"
                else -> "other"
            }
            vertexInfo.attributes["vertexClass"] = DefaultAttribute(vertexClass, AttributeType.STRING)
        }
    }
}

graphAnalytics {
    vertexAttributeCollectors.add(CustomVertexAttributeCollector())
    analysisTasks.add(project.tasks.register("vertexClassAnalysis", VertexClassAnalysisTask::class))

    validation {
        // Perform validation relative to the :app project's graph.  This gives us a complete
        // picture.  With the example rules defined below this is not actually required since
        // each module validates against metrics that are based only upn dependencies and do
        // not need to take into account dependents.
        validatedProjects.set(listOf(":app"))

        rules.put("no-api-to-impl", GraphValidationRule(
            reason = "API modules must be expressed in terms of public API only",
            matcher = allOf(
                stringAttribute("vertexClass", equalTo("api")),
                hasOutgoingEdge(edgeTarget(allOf(
                    stringAttribute("vertexClass", not(equalTo("api")),
                ))))
            )
        ))

        // Example of a rule on API modules to limit the size of their total transitive
        // dependency graph.  The `lib2-api` module violates this and defines a module-specific
        // override.
        rules.put("api-must-be-lightweight", GraphValidationRule(
            reason = "API modules should not have a lot of dependencies in order to protect the \n" +
                    "build performance of the consuming projects",
            matcher = allOf(
                stringAttribute("vertexClass", equalTo("api")),
                numericAttribute("networkBelow", greaterThan(1))
            )
        ))
    }
}