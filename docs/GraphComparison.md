# Graph Comparison

When considering the impact of changes to a project it can be useful to see what the
impact of the change is prior to merge, or what the change is over a longer period of
time.

The `graphComparison` task is designed to provide a textual report that highlights
the changes to the tracked metric values, using two GraphML analysis files as inputs.

## Execution

To inspect the project graph for a specific module, run the `graphComparison` task on the
project module you wish to inspect.

This task takes one or both of the following parameters:
- `--before <path>` The path to the GraphML analysis file of the starting state
- `--after <path>` The path to the GraphML analysis file of the end state

If only one of these parameters is provided the other will be assumed to be the current
analysis file for the project module.

For both parameters, absolute paths work.  If a relative path is specified it will be evaluated 
relative to the project module's directory where the task is being run.

For example, in the [sample project](../sample) we could
run:

```shell
../gradlew :app:graphAnalysis
cp app/build/graphAnalytics/analysis.graphml app/build/graphAnalytics/analysis-before.graphml
# ...some changes to the project graph could be made here...
../gradlew :app:graphComparison \
    --before build/graphAnalytics/analysis-before.graphml \
    --after build/graphAnalytics/analysis.graphml
# or...
../gradlew :app:graphComparison \
    --before build/graphAnalytics/analysis-before.graphml
```

This will generate a report file at `app/build/graphAnalytics/directComparison.txt`.  The location
of this file will also be printed to the console log whenever it is regenerated.

Note that in this silly example the analysis state before and after are the same file and therefore
there are no changes reported.

## Report Contents

The resulting report will provide the following:

- A textual rendering of the graph node and all of its attribute values
- Aggregate change counts for all numeric attributes
- A list of all project modules with changes in their metric values, along with details on the
  changes themselves.
