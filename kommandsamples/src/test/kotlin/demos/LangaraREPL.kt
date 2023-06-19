@file:Suppress("TestFunctionName")

package pl.mareklangiewicz.kommand.demos

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.*

/** Changes in this fun should NOT be pushed to GIT. */
@Deprecated("Use notebooks + samples")
fun CliPlatform.LangaraREPL() {

    lsRegFiles("/home/marek").printlns()

}