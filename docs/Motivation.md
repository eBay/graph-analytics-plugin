# Motivation

At eBay, we have a very large and complex Android application that has evolved organically
over many years.  Historically, the application was build from a small number of monolithic
modules.

As we have worked to transition this application to a more modular architecture, we have run
into the need to better understand how the project modules relate to one another, surfacing
opportunities for better factoring and unlocking better build performance.

Very early on in this process, we realized that understanding the relationships between
hundreds of project modules quickly became an overwhelming task.  Adding to that challenge
was the need to provide automatic enforcement of key architectural concerns, such as
maintaining proper isolation between API and implementation details.

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
