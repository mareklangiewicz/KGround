package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.kommand.Kommand

data class Sample(
    val kommand: Kommand,
    val expectedLine: String? = null,
) : Kommand by kommand

internal infix fun Kommand.s(expectedLine: String) = Sample(this, expectedLine = expectedLine)


object Samples {
    val GitHub = pl.mareklangiewicz.kommand.github.GhSamples
}

