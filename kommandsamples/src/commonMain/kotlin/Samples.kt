@file:Suppress("unused")

package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kommand.*


data class Sample(
  val kommand: Kommand,
  val expectedLineRaw: String? = null,
) : Kommand by kommand

data class TypedSample<K : Kommand, In, Out, Err>(
  val typedKommand: TypedKommand<K, In, Out, Err>,
  val expectedLineRaw: String? = null,
)

data class ReducedSample<ReducedOut>(
  val reducedKommand: ReducedKommand<ReducedOut>,
  val expectedLineRaw: String? = null,
) : ReducedKommand<ReducedOut> by reducedKommand

internal infix fun Kommand.s(expectedLineRaw: String?) = Sample(this, expectedLineRaw = expectedLineRaw)

internal infix fun <K : Kommand, In, Out, Err> TypedKommand<K, In, Out, Err>.ts(expectedLineRaw: String?) =
  TypedSample(this, expectedLineRaw = expectedLineRaw)

internal infix fun <ReducedOut> ReducedKommand<ReducedOut>.rs(expectedLineRaw: String?) =
  ReducedSample(this, expectedLineRaw = expectedLineRaw)


data object Samples {
  @ExampleApi val Demo = pl.mareklangiewicz.kommand.demo.MyDemoSamples
  val Core = pl.mareklangiewicz.kommand.core.CoreSamples
  val Find = pl.mareklangiewicz.kommand.find.FindSamples
  val Ssh = pl.mareklangiewicz.kommand.ssh.SshSamples
  val Admin = pl.mareklangiewicz.kommand.admin.AdminSamples
  val Debian = pl.mareklangiewicz.kommand.debian.DebianSamples
  val Git = pl.mareklangiewicz.kommand.git.GitSamples
  val GitHub = pl.mareklangiewicz.kommand.github.GhSamples
}

