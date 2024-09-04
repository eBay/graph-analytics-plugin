![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/com.ebay.graph-analytics)

# Graph Analytics Plugin

## About This Project

This [Gradle](https://gradle.org/) plugin was designed to be used in multi-module
projects to analyze the inter-module dependency graph and provide insights into the
structure of the project.  This information can then be used to identify areas of
improvement and measure the resulting impact of changes being made.

Each project module becomes a vertex in the resulting graph.  The dependencies between the
project modules are expressed as edges between these vertices.

The plugin provides the following features:
- Generation of a GraphML file representing the project dependency graph
- Analysis of individual project modules, providing deeper insights into the costs being
  incurred by the module
- Extensibility, allowing for custom metrics to be added to the analysis
- Validation of the graph, allowing enforcement of graph metrics on a per-project basis using
  a flexible and extensible rule system 

## Background

To better understand why this plugin exists and how it works, the following documents
may be referenced:
- [Motivation](docs/Motivation.md): Why the plugin was created and the problems it solves
- [Design Overview](docs/Design.md): High level overview of how the plugin functions

## Requirements

The plugin is designed to work with Gradle 8.8 or later.

## Usage

To use, add the plugin your project's settings.gradle.kts file, as follows.  This will ensure
that the plugin is applied to all project modules:
```kotlin
// settings.gradle.kts
plugins {
    id("com.ebay.graph-analytics") version("<current version goes here>")
}
```

The following tasks are provided by the plugin on each project module:
| Task Name         | Description |
|-------------------|-------------|
| `graphAnalysis`   | Runs the graph analysis and generates the GraphML file for the module |
| `graphComparison` | Compares two graph analysis GraphML files and highlights the changes |
| `graphInspection` | Creates a report providing details into the project graph of an individual project module |
| `graphValidation` | Performs a graph analysis and assert the graph validation rules have been adhered to |

For more details on each of these tasks, reference the following:
- [Graph Analysis](docs/GraphAnalysis.md)
- [Graph Inspection](docs/GraphInspection.md)
- [Graph Comparison](docs/GraphComparison.md)
- [Graph Validation](docs/GraphValidation.md)

## Metrics Provided

Although the plugin is designed to be extensible, it comes with a number of built-in metrics:

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

## Configuration

The plugin will provide utility in its default configuration.  However, the addition of
project-specific metrics and rules can greatly extend its capabilities!

For more information, please refer to the [Configuration](docs/Configuration.md) document.

## Integrations

### Metrics for Develocity

Out-of-the-box, this plugin provides project graph information based upon the declared
structure of the project.  When working to improve build performance, the next questions
that naturally follow fall along the following lines:
- How much build time does each project module contribute?
- How many frequently are project modules built?

To answer these questions, the
[Metrics for Develocity](https://github.com/eBay/metrics-for-develocity-plugin)
plugin can be used to gather this information from a
[Develocity](https://gradle.com/gradle-enterprise-solutions/)
server instance, layering this data into the project graph analysis performed by
this plugin.

For more information, see the
[Project Cost Graph Analytics Integration](https://github.com/eBay/metrics-for-develocity-plugin/tree/main/src/main/kotlin/com/ebay/plugins/metrics/develocity/projectcost#project-cost-graph-analytics-integration)
document.

## Contributing

Contributions are welcome!  Please refer to the [CONTRIBUTING](CONTRIBUTING.md) document for
guidelines on how to contribute.

## Run Books

The following documents describe various processes needed for operating and/or maintaining
the plugin:
- [Run Book: Release Process](docs/RunBook-ReleaseProcess.md)

## License

This project is dual-licensed under the Eclipse Public License 2.0 and the Apache License
Version 2.0.

See [LICENSES](LICENSES.md) for more information.
