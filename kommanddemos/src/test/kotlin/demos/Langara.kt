package pl.mareklangiewicz.kommand.demos

import org.junit.jupiter.api.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.Platform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.*
import pl.mareklangiewicz.kommand.konfig.*


class Langara {

    companion object {
        const val tmpNotesFile = "/home/marek/tmp/tmp.notes"
    }

    @Test fun demo_repl() = idemo { LangaraREPL() }

    @Test fun demo_htop() = idemo { gnometerm(kommand("htop"))() }

    @Test fun demo_ps() = idemo { gnometerm(bash("ps -e | grep " + askEntry("find process"), pause = true))() }

    @Test fun demo_man() = idemo { gnometerm(man { +askEntry("manual page for") })() }

    @Test fun demo_ideap() = idemo {
        ideap { +askEntry("open file in ideap", suggested = "/home/marek/.bashrc") }()
    }

    @Test fun demo_bash_export() = idemo {
        bashGetExportsToFile(tmpNotesFile)
        ideap { +tmpNotesFile }()
    }

    @Test fun demo_xclip() = idemo {
        bash("xclip -o > $tmpNotesFile")() // FIXME_later: do it with kotlin instead of bash script
        ideap { +tmpNotesFile }()
    }

    @Test fun demo_set_konfig_examples() = idemo {
        val k = konfig("/home/marek/tmp/konfig_examples")
        println("before adding anything:")
        k.printAll()
        k["tmpExampleInteger1"] = 111.toString()
        k["tmpExampleInteger2"] = 222.toString()
        k["tmpExampleString1"] = "some text 1"
        k["tmpExampleString2"] = "some text 2"
        println("after adding 4 keys:")
        k.printAll()
        k["tmpExampleInteger2"] = null
        k["tmpExampleString2"] = null
        println("after nulling 2 keys:")
        k.printAll()
        k["tmpExampleInteger1"] = null
        k["tmpExampleString1"] = null
        println("after nulling other 2 keys:")
        k.printAll()
    }

    @Test fun interactive_code_switch() = SYS.run {
        val enabled = askIf("Should interactive code be enabled?")
        konfig().run {
            this["interactive_code"] = enabled.toString()
            print("interactive_code")
        }
    }

    @Test fun print_all_konfig() = SYS.konfig().printAll()

    @Test fun experiment() = idemo {
        val ideaEnabled = bash("ps -e")().any { it.contains("idea.sh") }
        if (ideaEnabled) ideap { +tmpNotesFile }() else vim(tmpNotesFile)()
    }
}

private fun idemo(platform: Platform = SYS, block: Platform.() -> Unit) = ifInteractive { platform.block() }
private fun Platform.askIf(question: String) = zenityAskIf(question)
private fun Platform.askEntry(question: String, suggested: String? = null) = zenityAskForEntry(question, suggested = suggested)
