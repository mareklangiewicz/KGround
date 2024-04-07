package pl.mareklangiewicz.kommand.demo

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kommand.Adb
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ManOpt
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.Vim
import pl.mareklangiewicz.kommand.ZenityOpt.*
import pl.mareklangiewicz.kommand.adb
import pl.mareklangiewicz.kommand.admin.btop
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.bash
import pl.mareklangiewicz.kommand.bashGetExportsMap
import pl.mareklangiewicz.kommand.bashGetExportsToFile
import pl.mareklangiewicz.kommand.core.cat
import pl.mareklangiewicz.kommand.find.findBoringCodeDirsAndReduceAsExcludedFoldersXml
import pl.mareklangiewicz.kommand.find.myKotlinPath
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.github.GhSamples
import pl.mareklangiewicz.kommand.gvim
import pl.mareklangiewicz.kommand.ideDiff
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.iproute2.ssTulpn
import pl.mareklangiewicz.kommand.kommand
import pl.mareklangiewicz.kommand.konfig.getKeyValStr
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.kommand.konfig.logEachKeyVal
import pl.mareklangiewicz.kommand.man
import pl.mareklangiewicz.kommand.pathToTmpNotes
import pl.mareklangiewicz.kommand.readFileHead
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.term.termKitty
import pl.mareklangiewicz.kommand.ulog
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenity
import pl.mareklangiewicz.kommand.zenityAskForEntry
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.ulog.i

/**
 * A bunch of samples to show on my machine when presenting KommandLine.
 * So it might be not the best idea to actually ax all these kommands on other machines.
 * (SamplesTests just check generated kommand lines, without executing any kommands)
 */
@ExampleApi
@OptIn(DelicateApi::class)
data object MyDemoSamples {

  val btop = btop() s
    "btop"

  val btopK = termKitty(btop) s
    "kitty -1 --detach -- btop"

  val manAllMan = man { -ManOpt.All; +"man" } s "man -a man"

  val manAproposMan = man { -ManOpt.Apropos(); +"man" } s "man -k man"

  val manEntryPage = InteractiveScript {
    val page = getEntry("manual page for")
    termKitty(man { +page }).ax()
  }

  val manEntryAllPages = InteractiveScript {
    val pages = getEntry("manual pages from all sections for", "open")
    termKitty(man { -ManOpt.All; +pages }).ax()
  }

  val psAllGrepJava = bash("ps -e | grep java") s
    "bash -c ps -e | grep java"

  val psAllGrepJavaK = termKitty(psAllGrepJava, hold = true) s
    "kitty -1 --detach --hold -- bash -c ps -e | grep java"

  val psAllGrepEntry = InteractiveScript {
    val process = getEntry("find process")
    termKitty(bash("ps -e | grep $process"), hold = true).ax()
  }

  val catFstabAndHosts = cat { +"/etc/fstab"; +"/etc/hosts" } s
    "cat /etc/fstab /etc/hosts"

  val catFstabAndHostsK = termKitty(catFstabAndHosts, hold = true) s
    "kitty -1 --detach --hold -- cat /etc/fstab /etc/hosts"

  val ssTulpn = ssTulpn() s "ss --tcp --udp --listening --processes --numeric"

  val adbDevices = adb(Adb.Command.Devices) s "adb devices"

  val adbShell = adb(Adb.Command.Shell) s "adb shell"

  val ideOpen = InteractiveScript {
    val path = getEntry("open file in IDE", suggested = "/home/marek/.bashrc")
    ideOpen(path).ax()
  }

  val ideDiff = InteractiveScript {
    val path1 = getEntry("first file to open in IDE Diff", suggested = "/home/marek/.vimrc")
    val path2 = getEntry("second file to open in IDE Diff", suggested = "/home/marek/.ideavimrc")
    ideDiff(path1, path2).ax()
  }

  val ideOpenXClip = InteractiveScript {
    kommand("xclip", "-o").ax(outFile = SYS.pathToTmpNotes)
    // bash("xclip -o > ${SYS.pathToTmpNotes}").ax() // equivalent to above
    ideOpen(SYS.pathToTmpNotes).ax()
  }

  val ideOpenBashExports = InteractiveScript {
    bashGetExportsToFile(SYS.pathToTmpNotes).ax()
    ideOpen(SYS.pathToTmpNotes).ax()
  }

  val gvimShowBashExportsForLC = InteractiveScript {
    val exports = bashGetExportsMap().ax()
    val lines = exports.keys.filter { it.startsWith("LC") }.map { "exported env \'$it\' == \'${exports[it]}\'" }
    writeFileWithDD(lines, SYS.pathToTmpNotes).ax()
    gvim(SYS.pathToTmpNotes).ax()
  }

  val gvimServerDDDDOpenHomeDir = gvim("/home") { -Vim.Option.ServerName("DDDD") } s "vim -g --servername DDDD /home"


  @OptIn(DelicateApi::class)
  suspend fun showMyRepoMarkdownListInGVim() = InteractiveScript {
    val reposMdContent = GhSamples.myPublicRepoMarkdownList.ax()
    val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md" // also to have syntax highlighting
    writeFileAndStartInGVim(reposMdContent, filePath = tmpReposFileMd).ax()
  }

  @OptIn(DelicateApi::class)
  suspend fun prepareMyExcludeFolderInKotlinMultiProject() = InteractiveScript {
    val out = findBoringCodeDirsAndReduceAsExcludedFoldersXml(myKotlinPath, withOnEachLog = true).ax()
    val boringFileXml = SYS.pathToUserTmp + "/tmp.boring.xml" // also to have syntax highlighting
    writeFileAndStartInGVim(out, filePath = boringFileXml).ax()
    // TODO_someday: use URE to inject it into /code/kotlin/kotlin.iml (and/or: /code/kotlin/.idea/kotlin.iml)
    SYS.start(gvim("/home/marek/code/kotlin/kotlin.iml"))
    SYS.start(gvim("/home/marek/code/kotlin/.idea/kotlin.iml"))
  }


  // Note: interactive code stuff have nicer support in Main.kt:main + Run Configurations (commited to repo)

  val interactiveCodeEnable = ReducedScript { setUserFlag(SYS, "code.interactive", true) }
  val interactiveCodeDisable = ReducedScript { setUserFlag(SYS, "code.interactive", false) }
  val interactiveCodeLog = ReducedScript { ulog.i(getUserFlagFullStr(SYS, "code.interactive")) }

  // Note: NOT InteractiveScript because I want to be able to switch interactive code even when it's NOT enabled.
  val interactiveCodeSwitch = ReducedScript {
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
    ulog.i("before adding anything:")
    k.logEachKeyVal()
    k["tmpExampleInteger1"] = 111.toString()
    k["tmpExampleInteger2"] = 222.toString()
    k["tmpExampleString1"] = "some text 1"
    k["tmpExampleString2"] = "some text 2"
    ulog.i("after adding 4 keys:")
    k.logEachKeyVal()
    k["tmpExampleInteger2"] = null
    k["tmpExampleString2"] = null
    ulog.i("after nulling 2 keys:")
    k.logEachKeyVal()
    k["tmpExampleInteger1"] = null
    k["tmpExampleString1"] = null
    ulog.i("after nulling other 2 keys:")
    k.logEachKeyVal()
  }

  suspend fun readVimrcHead() = readFileHead("/home/marek/.vimrc").ax()

  // Should fail; if called inside withLogBadStreams -> should log sth like: kl E STDERR: head: cannot open...
  suspend fun readNonExistentHead() = readFileHead("/home/marek/non-existent-file-46578563").ax()
}


@OptIn(DelicateApi::class)
private suspend fun showInfo(info: String) = zenity(Type.Info) { -Text(info) }.ax()

@OptIn(DelicateApi::class)
private suspend fun showError(error: String) = zenity(Type.Error) { -Text(error) }.ax()

private suspend fun askIf(question: String) = zenityAskIf(question).ax()

private suspend fun askEntry(question: String, suggested: String? = null) =
  zenityAskForEntry(question, suggested = suggested).ax()?.takeIf { it.isNotBlank() }

private suspend fun getEntry(question: String, suggested: String? = null, errorMsg: String = "User didn't answer.") =
  askEntry(question, suggested) ?: run { showError(errorMsg); bad { errorMsg } }

