package pl.mareklangiewicz.kommand.demos

import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.coreutils.lsRegFiles
import pl.mareklangiewicz.kommand.printlns

fun main() {
    SYS.lsRegFiles("/home/marek").printlns()
}
