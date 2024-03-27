@file:OptIn(ExperimentalApi::class)
@file:Suppress("unused")

package pl.mareklangiewicz.kommand.jupyter

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.ulog.i

@ExampleApi
object MainExamples {

    @OptIn(DelicateApi::class)
    suspend fun examplesToRefactor() {
        // ls { -LsOpt.LongFormat; -LsOpt.All }.ax {
        //     ulog.i("out line: $it")
        // }
        // EchoSamples.echoTwoParagraphsWithEscapes.kommand.startInTermIfUserConfirms(SYS)
        // MyDemoSamples.btopKitty.ax()
        // MyDemoSamples.ps1.ax()
        // gitStatus().ax().logEach()
        // searchCommand("pip").ax()?.logEach()
        // DpkgSamples.searchZenity.ax()?.logEach()
        // SshSamples.sshPimInTermGnome.ax()
        // SshSamples.sshPimLsInTermKitty.ax()
        // SshSamples.sshPimLsLAH.ax(errToOut = true).logEach()
    }
}



