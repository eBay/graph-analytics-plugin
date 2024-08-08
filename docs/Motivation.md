# Motivation

When working with very large and complex Android applications that have evolved organically
over many years, it can be difficult to understand how the project modules relate to one another.
When the number of project modules increases beyond a certain point, it becomes an increasingly
overwhelming task.

Adding to this challenge is the need to provide automatic enforcement of key architectural
concerns, such as maintaining proper isolation between API and implementation details.
The specific application of this sort of project structure rules tend to be very project- or
team- specific.  Some solutions exist to help in this area but make assumptions that may not
be suitable for many projects.

As we surveyed the available tools, we found that none of them fully satisfied the
requirements that we wanted to solve for, including:
- Extensible and customizable graph analysis data layering model
- Flexibility in defining and enforcing project conventions
- Operation on a project module basis rather than on a per task basis
- Usefulness in providing insights into finding - and more importantly correcting - existing
  poorly chosen module relationships
- Use of a common, rich file format that can be used with existing open source visualization
  tools, such as [Gephi](https://gephi.org/)
- Compliance with modern Gradle plugin development best practices, such as configuration
  cache support and project isolation

The Graph Analytics Plugin was born out of the need to address these requirements.

## Inspiration

The following projects were sources of inspiration for the Graph Analytics Plugin.  We
are thankful for the work of these projects and the insights they provided:
- [Graph Assert Plugin](https://github.com/jraska/modules-graph-assert)
- [Graph Untangler Plugin](https://github.com/siggijons/graph-untangler-plugin)
- [Talaoit Plugin](https://github.com/cdsap/Talaiot)
