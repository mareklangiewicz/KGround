@file:Suppress("unused")

package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.kommand.*

data class Sample(
    val kommand: Kommand,
    val expectedLineRaw: String? = null,
) : Kommand by kommand

data class TypedSample<K: Kommand, In, Out, Err>(
    val typedKommand: TypedKommand<K, In, Out, Err>,
    val expectedLineRaw: String? = null,
)

data class ReducedSample<K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut>(
    val reducedKommand: ReducedKommand<K, In, Out, Err, TK, ReducedOut>,
    val expectedLineRaw: String? = null,
)

internal infix fun Kommand.s(expectedLineRaw: String?) = Sample(this, expectedLineRaw = expectedLineRaw)

internal infix fun <K: Kommand, In, Out, Err> TypedKommand<K, In, Out, Err>.ts(expectedLineRaw: String?) =
    TypedSample(this, expectedLineRaw = expectedLineRaw)

internal infix fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut>
        ReducedKommand<K, In, Out, Err, TK, ReducedOut>.rs(expectedLineRaw: String?) =
    ReducedSample(this, expectedLineRaw = expectedLineRaw)


data object Samples {
    val Core = pl.mareklangiewicz.kommand.core.CoreSamples
    val Find = pl.mareklangiewicz.kommand.find.FindSamples
    val Admin = pl.mareklangiewicz.kommand.admin.AdminSamples
    val Debian = pl.mareklangiewicz.kommand.debian.DebianSamples
    val Git = pl.mareklangiewicz.kommand.git.GitSamples
    val GitHub = pl.mareklangiewicz.kommand.github.GhSamples
}

