package pl.mareklangiewicz.kommand.demo

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.getSysUFileSys
import pl.mareklangiewicz.kground.io.pathToTmpNotes
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kommand.Adb
import pl.mareklangiewicz.kommand.ManOpt
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.vim.XVim
import pl.mareklangiewicz.kommand.adb
import pl.mareklangiewicz.kommand.admin.btop
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.shell.*
import pl.mareklangiewicz.kommand.core.cat
import pl.mareklangiewicz.kommand.find.findBoringCodeDirsAndReduceAsExcludedFoldersXml
import pl.mareklangiewicz.kommand.find.myKotlinPath
import pl.mareklangiewicz.kommand.getSysCLI
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.github.GhSamples
import pl.mareklangiewicz.kommand.ideDiff
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.iproute2.ssTulpn
import pl.mareklangiewicz.kommand.kommand
import pl.mareklangiewicz.kommand.konfig.getKeyValStr
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.kommand.konfig.logEachKeyVal
import pl.mareklangiewicz.kommand.man
import pl.mareklangiewicz.kommand.readFileHead
import pl.mareklangiewicz.kommand.reducedMap
import pl.mareklangiewicz.kommand.reducedOutToList
import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.term.TermKittyOpt.StartAsType
import pl.mareklangiewicz.kommand.term.inTermKitty
import pl.mareklangiewicz.kommand.vim.XVimOpt
import pl.mareklangiewicz.kommand.vim.gvim
import pl.mareklangiewicz.kommand.vim.gvimLines
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.kommand.zenity.zenityShowInfo
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.localULog
import pl.mareklangiewicz.usubmit.localUSubmit
import pl.mareklangiewicz.usubmit.xd.askForEntry
import pl.mareklangiewicz.usubmit.xd.showError

/**
 * A bunch of samples to show on my machine when presenting KommandLine.
 * So it might be not the best idea to actually ax all these kommands on other machines.
 * (SamplesTests just check generated kommand lines, without executing any kommands)
 */
@ExampleApi
@OptIn(DelicateApi::class, NotPortableApi::class)
data object MyDemoSamples {

  // TODO: refactor
  private val SYS = getSysCLI()
  private val FS = getSysUFileSys()
  private val pathToTmpNotes = FS.pathToTmpNotes
  private val pathToUserTmp = FS.pathToUserTmp!!

  val btop = btop() s "btop"

  val btopK = btop.inTermKitty(startAs = StartAsType.Maximized) s "kitty --detach --start-as maximized -- btop"

  val manAllMan = man { -ManOpt.All; +"man" } s "man -a man"

  val manAproposMan = man { -ManOpt.Apropos(); +"man" } s "man -k man"

  val manEntryPage = InteractiveScript {
    val page = getEntry("manual page for")
    man { +page }.inTermKitty().ax()
  }

  val manEntryAllPages = InteractiveScript {
    val pages = getEntry("manual pages from all sections for", "open")
    man { -ManOpt.All; +pages }.inTermKitty().ax()
  }

  val bashEchoXdgDesktop = bashEchoEnv("XDG_CURRENT_DESKTOP")

  val bashMapDesktopKind = bashEchoXdgDesktop.reducedOutToList().reducedMap {
    val elements = singleOrNull().orEmpty().split(":")
    val isDesktop = elements.isNotEmpty()
    val isUbuntu = "ubuntu" in elements
    val isGnome = "GNOME" in elements
    mapOf(
      "isDesktop" to isDesktop,
      "isUbuntu" to isUbuntu,
      "isGnome" to isGnome,
    )
  }

  val psAllGrepJava = bash("ps -e | grep java") s
    "bash -c ps -e | grep java"

  val psAllGrepJavaK = psAllGrepJava.inTermKitty(hold = true) s "kitty --detach --hold -- bash -c ps -e | grep java"

  val psAllGrepEntry = InteractiveScript {
    val process = getEntry("find process")
    bash("ps -e | grep $process").inTermKitty(hold = true).ax()
  }

  val catFstabAndHosts = cat { +"/etc/fstab"; +"/etc/hosts" } s
    "cat /etc/fstab /etc/hosts"

  val catFstabAndHostsK = catFstabAndHosts.inTermKitty(hold = true) s
    "kitty --detach --hold -- cat /etc/fstab /etc/hosts"

  val ssTulpn = ssTulpn() s "ss --tcp --udp --listening --processes --numeric"

  val adbDevices = adb(Adb.Command.Devices) s "adb devices"

  val adbShell = adb(Adb.Command.Shell) s "adb shell"

  val ideOpen = InteractiveScript {
    val path = getEntry("open file in IDE", suggested = "/home/marek/.bashrc").pth
    ideOpen(path).ax()
  }

  val ideDiff = InteractiveScript {
    val path1 = getEntry("first file to open in IDE Diff", suggested = "/home/marek/.vimrc").pth
    val path2 = getEntry("second file to open in IDE Diff", suggested = "/home/marek/.ideavimrc").pth
    ideDiff(path1, path2).ax()
  }

  val ideOpenXClip = InteractiveScript {
    kommand("xclip", "-o").ax(outFile = pathToTmpNotes)
    // bash("xclip -o > ${SYS.pathToTmpNotes}").ax() // equivalent to above
    ideOpen(pathToTmpNotes).ax()
  }

  val ideOpenBashExports = InteractiveScript {
    bashGetExportsToFile(pathToTmpNotes.strf).ax()
    ideOpen(pathToTmpNotes).ax()
  }

  val gvimShowBashExportsForLC = InteractiveScript {
    val exports = bashGetExportsMap().ax()
    val lines = exports.keys.filter { it.startsWith("LC") }.map { "exported env \'$it\' == \'${exports[it]}\'" }
    // writeFileWithDD(lines, pathToTmpNotes).ax()
    // gvim(pathToTmpNotes).ax()
    // better way instead of using tmp file:
    gvimLines(lines).ax()
  }

  val gvimServerDDDDOpenHomeDir = gvim("/home".pth) { -XVimOpt.ServerName("DDDD") } s "gvim --servername DDDD /home"


  @OptIn(DelicateApi::class)
  suspend fun showMyRepoMarkdownListInGVim() = InteractiveScript {
    val reposMdContent = GhSamples.myPublicRepoMarkdownList.ax()
    val tmpReposFileMd = "$pathToUserTmp/tmp.repos.md".pth // also to have syntax highlighting
    writeFileAndStartInGVim(reposMdContent, filePath = tmpReposFileMd).ax()
  }

  @OptIn(DelicateApi::class)
  suspend fun prepareMyExcludeFolderInKotlinMultiProject() = InteractiveScript {
    val out = findBoringCodeDirsAndReduceAsExcludedFoldersXml(myKotlinPath, withOnEachLog = true).ax()
    val boringFileXml = "$pathToUserTmp/tmp.boring.xml".pth // also to have syntax highlighting
    writeFileAndStartInGVim(out, filePath = boringFileXml).ax()
    // TODO_someday: use URE to inject it into /code/kotlin/kotlin.iml (and/or: /code/kotlin/.idea/kotlin.iml)
    SYS.lx(gvim("/home/marek/code/kotlin/kotlin.iml".pth))
    SYS.lx(gvim("/home/marek/code/kotlin/.idea/kotlin.iml".pth))
  }


  // Note: interactive code stuff have nicer support in Main.kt:main + Run Configurations (commited to repo)

  val interactiveCodeEnable = ReducedScript { setUserFlag(SYS, "code.interactive", true) }
  val interactiveCodeDisable = ReducedScript { setUserFlag(SYS, "code.interactive", false) }
  val interactiveCodeLog = ReducedScript { localULog().i(getUserFlagFullStr(SYS, "code.interactive")) }

  // Note: NOT InteractiveScript because I want to be able to switch interactive code even when it's NOT enabled.
  val interactiveCodeSwitch = ReducedScript {
    val enabled = zenityAskIf("Should interactive code be enabled?").ax()
    setUserFlag(SYS, "code.interactive", enabled)
    zenityShowInfo("user flag: code.interactive.enabled = $enabled").ax()
  }

  val myDemoTestsSwitch = InteractiveScript {
    val enabled = zenityAskIf("Should MyDemoTests be enabled?").ax()
    setUserFlag(SYS, "tests.MyDemoTests", enabled)
    zenityShowInfo("user flag: tests.MyDemoTests.enabled = $enabled").ax()
  }

  val showWholeUserConfig = InteractiveScript {
    val konfig = konfigInUserHomeConfigDir(SYS)
    zenityShowInfo(konfig.keys.map { konfig.getKeyValStr(it) }.joinToString("\n\n")).ax()
  }

  val playWithKonfigExamples = InteractiveScript {
    val log = localULog()
    val k = konfigInDir("/home/marek/tmp/konfig_examples".pth, checkForDangerousValues = false)
    log.i("before adding anything:")
    k.logEachKeyVal()
    k["tmpExampleInteger1"] = 111.strf
    k["tmpExampleInteger2"] = 222.strf
    k["tmpExampleString1"] = "some text 1"
    k["tmpExampleString2"] = "some text 2"
    log.i("after adding 4 keys:")
    k.logEachKeyVal()
    k["tmpExampleInteger2"] = null
    k["tmpExampleString2"] = null
    log.i("after nulling 2 keys:")
    k.logEachKeyVal()
    k["tmpExampleInteger1"] = null
    k["tmpExampleString1"] = null
    log.i("after nulling other 2 keys:")
    k.logEachKeyVal()
  }

  suspend fun readVimrcHead() = readFileHead("/home/marek/.vimrc".pth).ax()

  // Should fail; if called inside withLogBadStreams -> should log sth like: kl E STDERR: head: cannot open...
  suspend fun readNonExistentHead() = readFileHead("/home/marek/non-existent-file-46578563".pth).ax()
}


@OptIn(ExperimentalApi::class)
private suspend fun getEntry(question: String, suggested: String = "", errorMsg: String = "User didn't answer."): String {
  val submit = localUSubmit()
  return submit.askForEntry(question, suggested) ?: run { submit.showError(errorMsg); bad { errorMsg } }
}
