plugins {
    `embedded-kotlin`
    id("convention.graph-analytics")
}

dependencies {
    api(projects.lib2Api)

    testImplementation(projects.lib1TestSupport)
    testImplementation(projects.lib2TestSupport)
}
