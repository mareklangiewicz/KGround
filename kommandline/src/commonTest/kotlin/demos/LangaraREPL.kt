@file:Suppress("TestFunctionName")

package pl.mareklangiewicz.kommand.demos

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.*

fun Platform.LangaraREPL() {
    lsRegFiles("/home/marek").printlns()
}
