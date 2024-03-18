package pl.mareklangiewicz.kommand.demo

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kommand.admin.btop
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.term.termKitty

/**
 * A bunch of samples to show on my machine when presenting KommandLine.
 * So it might be not the best idea to actually exec all these kommands on other machines.
 * (SamplesTests just check generated kommand lines, without executing any kommands)
 */
@ExampleApi
@OptIn(DelicateApi::class)
data object MyDemoSamples {
    val btop = btop() s "btop"
    val btopKitty = termKitty(btop()) s "kitty -1 --detach -- btop"
    // TODO NOW: move some demo stuff from MarekLangiewicz class in jvmTest
}