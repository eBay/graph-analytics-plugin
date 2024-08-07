# Configuration

The plugin is highly configurable and can be customized to suit the needs of your project.

The plugin will add a `graphAnalytics` extension to the project.  This extension can be used to
configure the following:

### Configuration Classifier

Each Gradle configuration is bucketed into a `PRODUCTION`, `TEST`, or `OTHER` category.  The
`PRODUCTION` and `TEST` categories are used to build independent directed graphs which are
later combined into a single graph.  Configurations falling into the `OTHER` bucket are
ignored altogether.

It is the job of the
[ConfigurationClassifier](../src/main/kotlin/com/ebay/plugins/graph/analytics/ConfigurationClassifier.kt)
to determine what bucket each Gradle configuration should be assigned to.

In most scenarios,  the default classifier should be sufficient.  However, if you have custom
configurations or find that the default classifier is not working as expected, you can provide
your own behavior to override the default.

Example:
```
graphAnalytics {
    configurationClassifier.set(MyCustomConfigurationClassifier())
}
```

### Vertex Attribute Collectors

Some vertex attributes may be derived from information defined by the project module and only
be available at configuration-time.  For example, detecting whether or not a specific plugin
was applied to the project.  Configuration-time data gathering such as this is performed by
implementing custom
[VertexAttributeCollector](../src/main/kotlin/com/ebay/plugins/graph/analytics/VertexAttributeCollector.kt)s.

For an example, please refer to the sample project's
[CustomVertexAttributeCollector](../sample/buildSrc/src/main/kotlin/convention/graph-analytics.gradle.kts)

### Vertex Analysis Tasks

Vertex analysis tasks are used to compute metrics on each project module in the graph.  These
metrics then become part of the GraphML data file and are used in the manual analysis process
as well as in graph validation.

To add a custom vertex analysis task, the task must extend from the
[BaseGraphInputOutputTask](../src/main/kotlin/com/ebay/plugins/graph/analytics/BaseGraphInputOutputTask.kt)
class and be added to the `graphAnalytics` extension.  For an example, please refer to the
sample project's
[VertexClassAnalysisTask](../sample/buildSrc/src/main/kotlin/convention/graph-analytics.gradle.kts)
task.

### Graph Data Consumer Tasks

Some tasks need to consume the graph data after it has been fully generated.  To facilitate this,
tasks which extend
[BaseGraphInputTask](../src/main/kotlin/com/ebay/plugins/graph/analytics/BaseGraphInputTask.kt)
may be registered as consumers.
