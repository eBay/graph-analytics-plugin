import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `embedded-kotlin`
    id("java-gradle-plugin")
}

group = "com.ebay"

gradlePlugin {
    plugins {
        create("com.ebay.graph-analytics") {
            id = "com.ebay.graph-analytics"
            implementationClass = "com.ebay.plugins.graph.analytics.GraphAnalyticsPlugin"
        }
    }
}

dependencies {
    api(libs.jgrapht.core)
    api(libs.jgrapht.io) {
        // Not needed since it is only used for DOT, GML, JSON, and CSV support.
        exclude(group = "org.antlr", module = "antlr4-runtime")
    }

    testImplementation(libs.test.hamcrest)
    testImplementation(libs.test.mockito.kotlin)
    testImplementation(enforcedPlatform(libs.test.junit5.bom))
    testImplementation(libs.test.junit5.jupiter)
    testRuntimeOnly(libs.test.junit5.platformLauncher)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

project.tasks.withType(KotlinJvmCompile::class.java) {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn"))
    }
}

project.tasks.withType(Test::class.java) {
    useJUnitPlatform()
}