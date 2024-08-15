package com.ebay.plugins.graph.analytics

import org.testng.annotations.AfterClass
import java.io.File
import java.nio.file.Files

abstract class BasePluginFunctionalTest {
    protected val persistence: GraphPersistence = GraphPersistenceGraphMl()
    protected val projectDir: File by lazy {
        Files.createTempDirectory(javaClass.simpleName).toFile()
    }

    protected fun resetProjectDir() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()
    }

    @AfterClass
    fun cleanupTempDir() {
        projectDir.deleteRecursively()
    }

    protected fun createProjectStructure(projects: Map<String, DependenciesSpec>){
        resetProjectDir()
        copyResourceToFile("settings.gradle.kts", "settings.gradle.kts") { template ->
            val includeStatements = projects.keys.joinToString("\n") {
                val name = it.replace("/", ":")
                "include(\":$name\")"
            }
            template.replace("// INCLUDE PLACEHOLDER", includeStatements)
        }
        projects.forEach { (name, dependencies) ->
            copyResourceToFile("build.gradle.kts", "$name/build.gradle.kts") { template ->
                val prodDeps = dependencies.dependencies.joinToString("\n") {
                    "    implementation(project(\"${it}\"))"
                }
                val testDeps = dependencies.testDependencies.joinToString("\n") {
                    "    testImplementation(project(\"${it}\"))"
                }
                template
                    .replace("// PRODUCTION DEPENDENCIES PLACEHOLDER", prodDeps)
                    .replace("// TEST DEPENDENCIES PLACEHOLDER", testDeps)
            }
        }
    }

    private fun copyResourceToFile(
        resource: String,
        targetFile: String,
        transform: (String) -> String? = { it }
    ) {
        val content = javaClass.classLoader.getResourceAsStream(resource)?.use {
            transform.invoke(it.readAllBytes().decodeToString())
        } ?: throw IllegalArgumentException("Resource not found: $resource")

        val target = projectDir.resolve(targetFile)
        target.parentFile.mkdirs()
        target.writeText(content)
    }
}
