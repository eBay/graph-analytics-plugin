import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.allOf
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.greaterThan
import com.ebay.plugins.graph.analytics.validation.matchers.GraphMatchers.numericAttribute

plugins {
    `embedded-kotlin`
    id("convention.graph-analytics")
}

dependencies {
    implementation(projects.lib1Api)
}

graphAnalytics {
    validation {
        // Override the default rule for the "api-must-be-lightweight" rule for this module
        ruleOverrides.put("api-must-be-lightweight", allOf(
            numericAttribute("networkBelow", greaterThan(2))
        ))
    }
}
