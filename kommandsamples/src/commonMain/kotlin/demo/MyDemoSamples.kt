package pl.mareklangiewicz.kommand.demo

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.InteractiveScript
import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.ReducedKommand
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.ZenityOpt.*
import pl.mareklangiewicz.kommand.admin.btop
import pl.mareklangiewicz.kommand.bash
import pl.mareklangiewicz.kommand.bashGetExportsToFile
import pl.mareklangiewicz.kommand.core.LsOpt
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType
import pl.mareklangiewicz.kommand.core.ls
import pl.mareklangiewicz.kommand.exec
import pl.mareklangiewicz.kommand.gvim
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.konfig.getKeyValStr
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.kommand.konfig.logEachKeyVal
import pl.mareklangiewicz.kommand.man
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.term.termKitty
import pl.mareklangiewicz.kommand.zenity
import pl.mareklangiewicz.kommand.zenityAskForEntry
import pl.mareklangiewicz.kommand.zenityAskIf

/**
 * A bunch of samples to show on my machine when presenting KommandLine.
 * So it might be not the best idea to actually exec all these kommands on other machines.
 * (SamplesTests just check generated kommand lines, without executing any kommands)
 */
@ExampleApi
@OptIn(DelicateApi::class)
data object MyDemoSamples {

    val btop = btop() s
            "btop"

    val btopKitty = termKitty(btop()) s
            "kitty -1 --detach -- btop"

    val ps1 = termKitty(bash("ps -e | grep java", pause = true)) s
            "kitty -1 --detach -- bash -c ps -e | grep java ; echo END.ENTER; read"

    val ps2 = InteractiveScript {
        val process = getEntry("find process")
        termKitty(bash("ps -e | grep $process", pause = true)).x()
    }

    private val lsALotNicely = ls("/home/marek", "/usr", wHidden = true, wColor = ColorType.ALWAYS) {
            -LsOpt.Author
            -LsOpt.LongFormat
            -LsOpt.HumanReadable
            -LsOpt.Sort(LsOpt.SortType.TIME)
        }

    // Notice: it should add colors because "ls" is called with terminal as stdout
    val lsALotNicelyInTerm = termKitty(lsALotNicely, hold = true) s
            "kitty -1 --detach --hold -- ls -A --color=always --author -l -h --sort=time /home/marek /usr"

    // Notice: it will NOT add colors because "ls" is called with file as stdout
    val lsALotNicelyInGVim = InteractiveScript {
        lsALotNicely.exec(SYS, outFile = tmpNotesFile)
        gvim(tmpNotesFile).x()
    }

    val man1 = InteractiveScript {
        val page = getEntry("manual page for")
        termKitty(man { +page }).x()
    }
    val ideOpen1 = InteractiveScript {
        val path = getEntry("open file in IDE", suggested = "/home/marek/.bashrc")
        ideOpen(path).x()
    }

    val ideOpenBashExports = InteractiveScript {
        bashGetExportsToFile(tmpNotesFile).x()
        ideOpen(tmpNotesFile).x()
    }

    val ideOpenXClip = InteractiveScript {
        bash("xclip -o > $tmpNotesFile").x() // FIXME_later: do it with kotlin instead of bash script
        ideOpen(tmpNotesFile).x()
    }

    // Note: not InteractiveScript because I want to be able to enable interactive code when it's disabled.
    val iCodeSwitch = ReducedScript {
        val enabled = askIf("Should interactive code be enabled?")
        setUserFlag(SYS, "code.interactive", enabled)
        showInfo("user flag: code.interactive.enabled = $enabled")
    }

    val myDemoTestsSwitch = InteractiveScript {
        val enabled = askIf("Should MyDemoTests be enabled?")
        setUserFlag(SYS, "tests.MyDemoTests", enabled)
        showInfo("user flag: tests.MyDemoTests.enabled = $enabled")
    }

    val showWholeUserConfig = InteractiveScript {
        val konfig = konfigInUserHomeConfigDir(SYS)
        showInfo(konfig.keys.map { konfig.getKeyValStr(it) }.joinToString("\n\n"))
    }

    val playWithKonfigExamples = InteractiveScript {
        val k = konfigInDir("/home/marek/tmp/konfig_examples", checkForDangerousValues = false)
        println("before adding anything:")
        k.logEachKeyVal()
        k["tmpExampleInteger1"] = 111.toString()
        k["tmpExampleInteger2"] = 222.toString()
        k["tmpExampleString1"] = "some text 1"
        k["tmpExampleString2"] = "some text 2"
        println("after adding 4 keys:")
        k.logEachKeyVal()
        k["tmpExampleInteger2"] = null
        k["tmpExampleString2"] = null
        println("after nulling 2 keys:")
        k.logEachKeyVal()
        k["tmpExampleInteger1"] = null
        k["tmpExampleString1"] = null
        println("after nulling other 2 keys:")
        k.logEachKeyVal()
    }
}



private suspend fun Kommand.x() = exec(SYS)
private suspend fun <T> ReducedKommand<T>.x() = exec(SYS)
private suspend fun <T> ReducedScript<T>.x() = exec(SYS)

@OptIn(DelicateApi::class)
private suspend fun showInfo(info: String) = zenity(Type.Info) { -Text(info) }.x()

@OptIn(DelicateApi::class)
private suspend fun showError(error: String) = zenity(Type.Error) { -Text(error) }.x()

private suspend fun askIf(question: String) = zenityAskIf(question).x()

private suspend fun askEntry(question: String, suggested: String? = null) =
    zenityAskForEntry(question, suggested = suggested).x()?.takeIf { it.isNotBlank() }

private suspend fun getEntry(question: String, suggested: String? = null, errorMsg: String = "User didn't answer.") =
    askEntry(question, suggested) ?: run { showError(errorMsg); bad { errorMsg } }

private val tmpNotesFile = SYS.pathToUserTmp + "/tmp.notes"

