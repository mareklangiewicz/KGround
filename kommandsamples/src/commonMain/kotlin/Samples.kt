@file:Suppress("unused")

package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.kommand.*

data class Sample(
    val kommand: Kommand,
    val expectedLine: String? = null,
) : Kommand by kommand

internal infix fun Kommand.s(expectedLine: String) = Sample(this, expectedLine = expectedLine)


object Samples {
    val Core = pl.mareklangiewicz.kommand.core.CoreSamples
    val Find = pl.mareklangiewicz.kommand.find.FindSamples
    val Admin = pl.mareklangiewicz.kommand.admin.AdminSamples
    val Debian = pl.mareklangiewicz.kommand.debian.DebianSamples
    val Git = pl.mareklangiewicz.kommand.git.GitSamples
    val GitHub = pl.mareklangiewicz.kommand.github.GhSamples
}

