plugins {
    `embedded-kotlin`
    id("convention.graph-analytics")
}

dependencies {
    implementation(projects.lib1Impl)
    implementation(projects.lib2Impl)
}
