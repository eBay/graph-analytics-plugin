# Design Overview

## Analysis

Prior to gathering dependency data, the plugin calls "vertex attribute collectors" to gather
configuration-time information about each project module.  This information is stored in
the graph as attributes on the vertices.

Each project defines dependencies which apply to the production code as well as (optionally)
dependencies which apply only to tests for the project in question.  For these dependencies,
only those which are references to other projects (e.g., `implementation(projects.featureModule)`)
are considered.

A "configuration classifier" is used to determine whether a particular configuration represents
a production configuration or a test configuration.

In order to avoid creating Gradle project dependency cycles, the production dependencies are
collected separately from test dependencies.  These separate graphs are then consolidated into
a single project graph containing a holistic view of the project graph.  Note that each individual
graph is acyclic _until_ they are combined, at which point the graph may potentially become cyclic.

Once the consolidated project graph is created it is then run through an analysis phase.
The analysis performs computations on each vertex in the graph and adds attributes to record
the results.  Multiple analysis tasks may be run during this phase to gather or compute different
types of data.

## Validation

Validation consumes the generated GraphML file as an input and performs checks on the graph
for each project module.

In order to create a type-safe, extensible validation system, the plugin uses a "matcher"
system similar to the one used in [Hamcrest](https://hamcrest.org/JavaHamcrest) Matchers.
A small library of matchers is provided with the plugin to enable common checks and to
provide the basis for extending the checks in a project-specific manner.
