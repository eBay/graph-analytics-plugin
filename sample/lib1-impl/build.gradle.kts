plugins {
    `embedded-kotlin`
    id("convention.graph-analytics")
}

dependencies {
    api(projects.lib1Api)

    testImplementation(projects.lib1TestSupport)
}
