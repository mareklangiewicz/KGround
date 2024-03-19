package pl.mareklangiewicz.kommand.demo

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kommand.CLI
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.admin.btop
import pl.mareklangiewicz.kommand.bash
import pl.mareklangiewicz.kommand.exec
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.term.termKitty
import pl.mareklangiewicz.kommand.zenityAskForEntry

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

    val ps1 = termKitty(bash("ps -e | grep java", pause = true)) s null

    val ps2 = ReducedScript { cli, dir ->
        val process = zenityAskForEntry("find process").exec(cli)
        when (process) {
            null -> println("Cancelled")
            "" -> println("Empty process name is incorrect")
            else -> termKitty(bash("ps -e | grep $process", pause = true)).exec(cli)
        }
    }

    // TODO NOW: move some demo stuff from MarekLangiewicz class in jvmTest
}

private val tmpNotesFile = CLI.SYS.pathToUserTmp + "/tmp.notes"
