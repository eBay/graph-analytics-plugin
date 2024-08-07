# Graph Inspection

When dealing with large projects it can sometimes be non-obvious why a project module's
metrics are what they are.  The `graphInspection` task is designed to create a textual
report that can be used to help gain better understanding on the origin of the costs.

## Execution

To inspect the project graph for a specific module, run the `graphInspection` task on the
project module you wish to inspect. The inspection task may be run in one of two ways,
described below.

The first approach inspects a project module from the perspective of the graph of another
project module.  Depending upon the project structure, this may provide a more comprehensive
view of the module's relationships.  This approach would typically be used to analyze a
project module from the perspective of an application project module.

For example, in the [sample project](../sample) we could run:
```shell
./gradlew :app:graphInspection --project :lib2-impl
```

This will generate a report file at `app/build/graphAnalytics/projectReport.txt`.
The location of this file will also be printed to the console log whenever it is regenerated.

Alternately, the inspection task can be run against a specific project module.  In this mode
of operation, only the project graph known to that project module will be available.  This
will - for instance - exclude information about modules which depend upon the project module.

For example, in the [sample project](../sample) we could run:

```shell
./gradlew :lib2-impl:graphInspection
```

This will generate a report file at `lib2-impl/build/graphAnalytics/projectReport.txt`.
The location of this file will also be printed to the console log whenever it is regenerated.

## Report Contents

The resulting report will provide the following:

- A textual rendering of the graph node and all of its attribute values
- If the project module is involved in one or more dependency cycles, these cycles will be
  enumerated.  This can be useful to find sub-graphs which form a natural cluster of dependencies
  that, if broken, could help to isolate the individual libraries.
- For each numeric attribute defined, the top 10 dependencies ordered by the attribute value
  (descending) will be listed.
- A full, depth-first traversal of the project module's dependencies, with each dependency
  rendered with all defined graph attributes.