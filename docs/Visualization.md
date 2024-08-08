# Visualization

For manual exploration of the project graph, we recommend the use of [Gephi](https://gephi.org/).
Gephi can directly load the GraphML files generated by the Graph Analytics Plugin and provide
a rich set of tools for exploring and visualizing the project graph.

## Generating the GraphML File

To use Gephi, you must first generate a GraphML file from the project graph.

The Graph Analysis Plugin provides a Gradle task (`graphAnalysis`), available on every
project module, to generate this file.  Selecting a project module at the top of the
project (e.g., the application module that contains the other modules as dependencies)
is recommended.

For example, in the [sample project](../sample) we could run:
```shell
$ ./gradlew :app:graphAnalysis
```

Upon successful completion, the resulting GraphML file will be located at:
```
app/build/graphAnalytics/analysis.graphml
```

This file may then be loaded into Gephi for visualization.

## Recommended Settings

The following settings are recommended as a decent starting point for visualization
of the project graph data:
1. In the `Graph` window, adjust visibility settings:
    - Enable `Show Node Labels` (Outlined 'T' in the bottom left)
    - Adjust the label font size down
2. Configure the `Layout`:
    - Use `Force Atlas` layout with the following parameter changes:
        - `Repulsion strength`: 500.0
        - `Attraction distribution`: checked
        - `Adjust by sizes`: checked
    - `Run` the `Force Atlas` layout until it stabilizes, then hit `Stop`
3. Use the `LabelAdjust` layout engine.  Hit `Run` and then `Stop` after it stabilizes
4. Adjust appearance settings to highlight problematic dependencies:
    - `Nodes` appearance.  Adjust and hit `Apply` on each:
        - `Color` tab, use `betweennessCentrality` ranking and select a color palette
        - `Size` tab, use `networkBelow` ranking
            - Min size: 1
            - Max size: 500
        - `Label Color` tab, use `outDegree` and select a color palette
        - `Label Size` tab, leave at default
    - `Edges` appearance:
        - `Color` tab, use `class` partitioning and select a color palette
        - `Label Color` tab, leave at default
        - `Label Size` tab, leave at default 