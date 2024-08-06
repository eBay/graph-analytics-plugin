# Sample Project

This is a sample project which acts as a technology demonstrator for the Graph Analytics
Plugin.

## Overview

The sample project contains one high level module which simulates an application project.
This module depends upon two library modules.  Each of the library modules exports
a "test support" module - effectively test fixtures for use by consumers of the library.
One of the libraries depends upon the other.

## Demonstration

For this demonstration, global project-wide settings are used to configure the plugin.
These are defined in the
[graph-analytics.gradle.kts](conventions/src/main/kotlin/convention/graph-analytics.gradle.kts)
convention plugin.

The test support libraries appear to Gradle as if they are ordinary libraries.  A custom
vertex attribute collector is add a vertex attribute indicating whether or not a specific
`convention.test-support` convention plugin has been applied to the project.  This attribute
is then consumed by a custom vertex analysis task which assigns a project module into
one of a small number of archetype categories ("api", "impl", "test-support", and "app").



