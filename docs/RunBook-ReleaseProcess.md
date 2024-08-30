# Release Process

## Overview

- At the beginning of the development cycle, the `gradle.properties` file should be updated to
  the next anticipated version number, following semantic versioning conventions.  Generally,
  this should just increment the patch level.  For example, `0.0.2`.
- Development takes place...
- When development is completed and a release is desired, evaluate the version number to ensure
  that it is still accurate with respect to the expectations of the semantic versioning scheme.
  Adjust the version in `gradle.properties` accordingly.
- Publish the release with change notes.  This process is detailed below.

## Publishing a Release

- Navigate to the [Releases](https://github.com/eBay/graph-analytics-plugin/releases) page on
  GitHub.
- Click the "Draft a new release" button.
- Click the "Choose a tag" button and enter the version being published, prefixed with a `v`.
  For example, `v0.0.2`.  Click the "Create new tag: on publish" button.
- Ensure the `Target` is set to `main`.
- Ensure the `Previous tag` is set to the last release tag.
- Click `Generate release notes` to populate the release notes.
- Use the prefixed version number as the title of the release.
- Edit the release notes, as/if necessary.
- Ensure that `Set as the latest release` is checked.
- Click the "Publish release" button.

The act of publishing the release will trigger the build and publish the release to the Gradle
Plugin Repository.  This process can be monitored by navigating to the
[Actions](https://github.com/eBay/graph-analytics-plugin/actions) page.