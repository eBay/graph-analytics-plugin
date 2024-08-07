# Graph Analysis

To analyze the project graph, run the `graphAnalysis` task on the project module you with
to analyze.

## Execution

For example, in the [sample project](../sample) we could run:
```shell
../gradlew :app:graphAnalysis
```

This will generate a GraphML file at `app/build/graphAnalytics/analysis.graphml`.  This file
can then be loaded into a visualization utility, as documented in
[Visualization](Visualization.md).
