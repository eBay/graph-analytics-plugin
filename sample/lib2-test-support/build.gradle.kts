plugins {
    `embedded-kotlin`
    id("convention.graph-analytics")
    id("convention.test-support")
}

dependencies {
    implementation(projects.lib2Api)
    implementation(projects.lib1TestSupport)
}
