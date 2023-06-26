@file:Suppress("unused")

package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.kommand.*

data class Sample(
    val kommand: Kommand,
    val expectedLine: String? = null,
) : Kommand by kommand

internal infix fun Kommand.s(expectedLine: String) = Sample(this, expectedLine = expectedLine)


object Samples {
    val CoreUtils = pl.mareklangiewicz.kommand.coreutils.CoreUtilsSamples
    val DebianUtils = pl.mareklangiewicz.kommand.debianutils.DebianUtilsSamples
    val Git = pl.mareklangiewicz.kommand.git.GitSamples
    val GitHub = pl.mareklangiewicz.kommand.github.GhSamples
}

