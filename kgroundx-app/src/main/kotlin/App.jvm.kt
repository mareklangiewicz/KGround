package pl.mareklangiewicz.kground

import kotlinx.coroutines.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.BadStateErr
import pl.mareklangiewicz.interactive.*

@OptIn(DelicateApi::class, NotPortableApi::class, ExperimentalApi::class)
fun main(args: Array<String>) {
  try {
    mainCodeExperiments(args)
      // FIXME_later: better messages inside (probably use some args parsing library)
      // (after merging KommandLine with KGround)
  }
  catch (e: BadStateErr) {
    if (e.message?.startsWith("Incorrect args") == true) {
      println(e.message)
      println("For example:")
      println("kgroundx get-user-flag code.interactive")
      println("kgroundx set-user-flag code.interactive true")
      println("kgroundx set-user-flag code.interactive false")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToAbcdK")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToKGround")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectAllMyProjects")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyOtherExamples#updateGradlewInMyProjects")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.experiments.MyExperiments#collectGabrysCards")
    }
    else throw e
  }
}
