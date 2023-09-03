@file:Suppress("TestFunctionName")

package pl.mareklangiewicz.kommand.demos

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*

/** Changes in this fun should NOT be pushed to GIT. */
@Deprecated("Use notebooks + samples")
fun CliPlatform.LangaraREPL() = withPrintingBadStreams {
    lsRegFiles("/home/marek").execb(this).logEach()
}