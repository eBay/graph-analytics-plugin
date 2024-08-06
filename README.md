# Graph Analytics Plugin

## About This Project

This Gradle Plugin was designed to be used in multi-module projects to analyze the inter-module
dependency graph and provide insights into the structure of the project.  This information
can then be used to identify areas of improvement and measure the resulting impact of
changes being made.

Each project module becomes a vertex in the resulting graph.  The dependencies between the
project modules are expressed as edges between these vertices.

The plugin provides the following features:
- Generation of a GraphML file representing the project dependency graph
- Analysis of individual project modules, providing deeper insights into the costs being
  incurred by the module
- Extensibility, allowing for custom metrics to be added to the analysis
- Validation of the graph, allowing enforcement of graph metrics on a per-project basis using
  a flexible and extensible rule system 


## Usage

To use, add the plugin to all modules in your project:
```kotlin
// build.gradle.kts
plugins {
    id("com.ebay.graph-analytics") version("0.0.0")
}
```

Then, run the `graphAnalysis` task in the module you wish to analyze.  See `Manual Analysis`
for more information.

## Configuration

The plugin is highly configurable and can be customized to suit the needs of your project.

The plugin will add a `graphAnalytics` extension to the project.  This extension can be used to
configure the following:

### Configuration Classifier

Each Gradle configuration is bucketed into a `PRODUCTION`, `TEST`, or `OTHER` category.  The
`PRODUCTION` and `TEST` categories are used to build independent directed graphs which are
later combined into a single graph.  Configurations falling into the `OTHER` bucket are
ignored altogether.

It is the job of the
[ConfigurationClassifier](graph-analytics-plugin/src/main/kotlin/com/ebay/plugins/graph/analytics/ConfigurationClassifier.kt)
to determine what bucket each Gradle configuration should be assigned to.

In most scenarios,  the default classifier should be sufficient.  However, if you have custom
configurations or find that the default classifier is not working as expected, you can provide
your own behavior to override the default.

### Vertex Attribute Collectors

Some vertex attributes may be derived from information defined by the project module and only
be available at configuration-time.  For example, detecting whether or not a specific plugin
was applied to the project.  Configuration-time data gathering such as this is performed by
implementing custom
[VertexAttributeCollector](graph-analytics-plugin/src/main/kotlin/com/ebay/plugins/graph/analytics/VertexAttributeCollector.kt)s.

For an example, please refer to the sample project's
[CustomVertexAttributeCollector](sample/conventions/src/main/kotlin/convention/graph-analytics.gradle.kts)

### Vertex Analysis Tasks

Vertex analysis tasks are used to compute metrics on each project module in the graph.  These
metrics then become part of the GraphML data file and are used in the manual analysis process
as well as in graph validation.

To add a custom vertex analysis task, the task must extend from the
[BaseGraphInputOutputTask](graph-analytics-plugin/src/main/kotlin/com/ebay/plugins/graph/analytics/BaseGraphInputOutputTask.kt)
class and be added to the `graphAnalytics` extension.  For an example, please refer to the
sample project's
[VertexClassAnalysisTask](sample/conventions/src/main/kotlin/convention/graph-analytics.gradle.kts)
task.

### Graph Data Consumer Tasks

Some tasks need to consume the graph data after it has been fully generated.  To facilitate this,
tasks which extend
[BaseGraphInputTask](graph-analytics-plugin/src/main/kotlin/com/ebay/plugins/graph/analytics/BaseGraphInputTask.kt)
may be registered as consumers.

### Graph Validation

Graph validation is a powerful feature which allows for the enforcement of rules, based on
the graph data itself, upon the project as a whole.  This can be used to enforce architectural
structures within the project (e.g., architectural layer restrictions) or to enforce specific
best practices, as defined by the project team.

Graph validation is defined in terms of
[GraphValidationRule](graph-analytics-plugin/src/main/kotlin/com/ebay/plugins/graph/analytics/validation/GraphValidationRule.kt)
implementations.  Each rule is defined by specifying a set of conditions which must be met
which indicate a violation of the rule.  The conditions are specified in a manner similar to
hamcrest matchers, allowing for complex and custom conditions to be specified.

For an example, please refer to the
sample project's 
[rule definitions](sample/conventions/src/main/kotlin/convention/graph-analytics.gradle.kts).

Rule implementations are registered with the `validation` extension on the `graphAnalytics`
extension.

## Design Overview

Each project defines dependencies which apply to the production code as well as (optionally)
dependencies which apply only to tests for the project in question.  For these dependencies,
only those which are references to other projects (e.g., `implementation(projects.featureModule)`)
are considered.

In order to avoid creating Gradle project dependency cycles, the production dependencies are
collected separately from test dependencies.  These separate graphs are then consolidated into
a single project graph containing a holistic view of the project graph.  Note that each individual
graph is acyclic _until_ they are combined, at which point the graph may potentially become cyclic.

Once the consolidated project graph is created it is then run through an analysis phase.
The analysis performs computations on each vertex in the graph and adds attributes to record
the results.  The following metrics are added:

| Metric ID               | Description                                                                                                                                                                      |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `degree`                | The total number of incoming (dependents) and outgoing (dependencies) a project has                                                                                              |
| `inDegree`              | The number of dependents which depend upon the project                                                                                                                           |
| `outDegree`             | The number of dependencies which the project declares                                                                                                                            |
| `height`                | The size of the longest path in the project's tree of dependencies                                                                                                               |
| `networkAbove`          | The total number of dependant projects which depend upon the project.  This is useful to understand the number of projects impacted by a change to this project.                 |
| `networkBelow`          | The total number of dependency projects which this project depends upon.  This is useful to understand the number of projects which would affect this project when changed.      |
| `betweennessCentrality` | Calculates the [Betweenness Centrality](https://en.wikipedia.org/wiki/Betweenness_centrality) value for the project                                                              |
| `expansionFactor`       | A synthetic metric that is the product of the `inDegree` and `networkBelow`.  This attempts to capture the relative impact a project has in expanding the overall project graph. |

## Manual Analysis

For manual analysis, we use [Gephi](https://gephi.org/).

Analysis process with some decent starting settings:
1. Generate a project graph artifact:
   `./gradlew :myModule:graphAnalysis`
2. Run Gephi and load the resulting GraphML file, located at `myModule/build/graphAnalytics/analysis.graphml`
3. In the `Graph` window, adjust visibility settings:
   - Enable `Show Node Labels` (Outlined 'T' in the bottom left)
   - Adjust the label font size down
4. Configure the `Layout`:
   - Use `Force Atlas` layout with the following parameter changes:
     - `Repulsion strength`: 500.0
     - `Attraction distribution`: checked
     - `Adjust by sizes`: checked
   - `Run` the `Force Atlas` layout until it stabilizes, then hit `Stop`
5. Use the `LabelAdjust` layout engine.  Hit `Run` and then `Stop` after it stabilizes
6. Adjust appearance settings to highlight problematic dependencies:
   - `Nodes` appearance.  Adjust and hit `Apply` on each:
     - `Color` tab, use `betweennessCentrality` ranking and select a color palette
     - `Size` tab, use `height` ranking
       - Min size: 1
       - Max size: 50
     - `Label Color` tab, use `degreeOut` and select a color palette
     - `Label Size` tab, leave at default
   - `Edges` appearance:
     - `Color` tab, use `class` partitioning and select a color palette
     - `Label Color` tab, leave at default 
     - `Label Size` tab, leave at default 

## Investigating a Specific Project

When looking at the graph data it may become apparent that a project module that was assumed to be
simple in nature in reality has a deeper transitive dependency sub-graph.  To investigate the
reason for this, a project inspection task has been created.  This task traverses the reachable
outgoing edges (dependencies) and reports the results in a tree structure with node details available
for each dependency.

The report can be generated in one of two ways:
- By inspecting a project that includes the dependency, the project module to report on can be supplied as
  an optional argument.  For example: `./gradlew :myModule:graphInspection --project :myModule`
- By running the inspection task against a specific project.  For example:
  `./gradlew :myModule:graphInspection`

NOTE: Because the analysis graph may be cyclic in nature, this report may expose the fact that there
are sub-graphs which form a natural cluster of dependencies.  This is a situation which should be investigated
to see if the cycle can be broken in order to break up the cluster and isolate the individual libraries.

Upon completion, the comparison report will be available at the following location (URL will be
displayed in the console log).  For example:

`myModule/build/graphAnalytics/projectReport.txt`

## Comparing Existing Graph Files

Sometimes, it may be useful to directly compare two pre-existing graph files to create a report in the same
format in the simulation mode, documented above.  To solve for this use case, a `graphComparison` task was
created.

To compare two graphs, run the graph comparison task in the target project module.  The graphs are
supplied as paths relative to the project module the task is run in.  For example:

```
./gradlew :myModule:graphComparison \
    --base /path/to/initial-analysis.graphml \
    --changed build/graphAnalytics/analysis.graphml
```

Upon completion, the comparison report will be available at the following location (URL will be
displayed in the console log):

`myModule/build/graphAnalytics/directComparison.txt`


## Related Projects

The following are some related solutions which may be of interest:
- [Graph Assert Plugin](https://github.com/jraska/modules-graph-assert)
- [Graph Untangler Plugin](https://github.com/siggijons/graph-untangler-plugin)
- [Talaoit Plugin](https://github.com/cdsap/Talaiot)
