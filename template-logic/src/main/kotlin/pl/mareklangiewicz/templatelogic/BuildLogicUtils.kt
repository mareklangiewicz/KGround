package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

context(project: Project)
fun testContextParameters() {
    println("Successfully used context parameters in project: ${project.name}")
}

context(_project: Project)
val libDetails: LibDetails get() = _project.gradle.extLibDetails
