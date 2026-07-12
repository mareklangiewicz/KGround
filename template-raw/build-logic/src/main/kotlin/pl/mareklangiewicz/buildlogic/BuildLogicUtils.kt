package pl.mareklangiewicz.buildlogic

import org.gradle.api.Project

context(project: Project)
fun testContextParameters() {
    println("Successfully used context parameters in project: ${project.name}")
}
