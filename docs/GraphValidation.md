# Graph Validation

Graph validation is a powerful feature which allows for the enforcement of rules, based on
the graph data itself, upon the project as a whole.  This can be used to enforce architectural
structures within the project (e.g., architectural layer restrictions) or to enforce specific
best practices, as defined by the project team.

Graph validation is defined in terms of
[GraphValidationRule](../src/main/kotlin/com/ebay/plugins/graph/analytics/validation/GraphValidationRule.kt)
implementations.  Each rule is defined by specifying a set of conditions which must be met
which indicate a violation of the rule.  The conditions are specified in a manner similar to
hamcrest matchers, allowing for complex and custom conditions to be specified.

For an example, please refer to the sample project's
[rule definitions](../sample/buildSrc/src/main/kotlin/convention/graph-analytics.gradle.kts).

Rule implementations are registered with the `validation` extension on the `graphAnalytics`
extension.

## Execution

To validate the project graph for all project modules, run the `graphInspection` task from the
root project directory.  For example, in the [sample project](../sample) we could
run:

```shell
$ ./gradlew graphValidation
```

This will run the graph validation process for all project modules.  Since there are no
violations in the sample project, the process should complete successfully.  After completion,
manual inspection of the generated report files in each module's `build/graphAnalytics`
directory can provide some insight into how the plugin works.

#### `sample/app/build/graphAnalytics/analysis.graphml`

This file contains the complete project graph from the perspective of the `:app` module.
Only project modules which are reachable from the `:app` module are included in this graph.

This analysis file can be loaded into Gephi for visualization.

#### `sample/app/build/graphAnalytics/validationReport.txt`

This file contains a summary of the validation process for the `:app` module.  Since there
are no violations in the sample project, this file should be mostly empty.  If violations
are present, details about the violations - also printed to the console - will be found
here.

