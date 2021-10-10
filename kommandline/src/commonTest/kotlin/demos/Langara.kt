package pl.mareklangiewicz.kommand.demos

import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.Platform
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.bash
import pl.mareklangiewicz.kommand.bashGetExportsToFile
import pl.mareklangiewicz.kommand.gnome.gnometerm
import pl.mareklangiewicz.kommand.ideap
import pl.mareklangiewicz.kommand.ifInteractive
import pl.mareklangiewicz.kommand.kommand
import pl.mareklangiewicz.kommand.man
import pl.mareklangiewicz.kommand.output
import pl.mareklangiewicz.kommand.zenityAskForEntry
import pl.mareklangiewicz.kommand.zenityAskIf
import kotlin.test.Test

const val tmpFile = "/home/marek/tmp/tmp.notes"

class Langara {

    @Test fun demo_htop() = idemo { runInTerm(kommand("htop")) }

    @Test fun demo_ps() = idemo { run(bash("ps -e | grep " + askEntry("find process"))) }

    @Test fun demo_man() = idemo { runInTerm(man { +askEntry("manual page for") }) }

    @Test fun demo_ideap() = idemo {
        run(ideap { +askEntry("open file in ideap", suggested = "/home/marek/.bashrc") })
    }

    @Test fun demo_bash_export() = idemo {
        bashGetExportsToFile(tmpFile)
        run(ideap { +tmpFile })
    }

    @Test fun demo_xclip() = idemo {
        run(bash("xclip -o > $tmpFile")) // FIXME_later: do it with kotlin instead of bash script
        run(ideap { +tmpFile })
    }
}

private fun idemo(platform: Platform = SYS, block: Platform.() -> Unit) = ifInteractive { platform.block() }
private fun Platform.askIf(question: String) = zenityAskIf(question)
private fun Platform.askEntry(question: String, suggested: String? = null) = zenityAskForEntry(question, suggested = suggested)
private fun Platform.run(kommand: Kommand) = start(kommand).await().output()
private fun Platform.runInTerm(kommand: Kommand) = run(gnometerm(kommand))
