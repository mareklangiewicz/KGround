package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.Adb.*
import pl.mareklangiewicz.kommand.Adb.Command.*
import pl.mareklangiewicz.kommand.Adb.Option.*
import pl.mareklangiewicz.kommand.Ide.Cmd.diff
import pl.mareklangiewicz.kommand.Ide.Option.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.iproute2.*
import pl.mareklangiewicz.kommand.iproute2.Ss.Option.*
import pl.mareklangiewicz.kommand.iproute2.Ss.Option.tcp
import pl.mareklangiewicz.kommand.Vim.Option.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.All
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.core.LsOpt.SortType.*
import pl.mareklangiewicz.kommand.core.Rm.Option.*
import kotlin.test.*
import kotlin.test.Ignore
import kotlin.test.Test


@OptIn(DelicateKommandApi::class)
@Ignore // stuff specific to my laptop fails on CI
class KommandTest {
    @Test fun testBashQuoteMetaChars() {
        val str = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
        val out = bashQuoteMetaChars(str)
        println(str)
        println(out)
        assertEquals("abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno", out)
    }
    @Test fun testLs1() = ls { -Color(ALWAYS); -All; -Author; -LongFormat; -Sort(TIME); +".."; +"/usr" }
        .chkWithUser("ls --color=always -a --author -l --sort=time .. /usr")
    @Test fun testLs2() = ls { -All; -Author; -LongFormat; -HumanReadable; +"/home/marek" }.chkInIdeap()
    @Test fun testLs3() = ls { +"/home/marek" }.chkInIdeap()
    @Test fun testLsHome() = ls("/home/marek").execb(SYS).logEach()
    @Test fun testLsHomeSubDirs() = lsSubDirs("/home/marek").execb(SYS).logEach()
    @Test fun testLsHomeSubDirsWithHidden() = lsSubDirs("/home/marek", withHidden = true).execb(SYS).logEach()
    @Test fun testLsHomeRegFiles() = lsRegFiles("/home/marek").execb(SYS).logEach()

    @Test fun testMkDir1() = mkdir("/tmp/testMkDir1/blaa/blee", withParents = true)
        .typedKommand.kommand.chkWithUser("mkdir -p /tmp/testMkDir1/blaa/blee")

    @Test fun testRm1() = rm { -dir; +"/tmp/testMkDir1/blaa/blee" }
        .chkWithUser("rm --dir /tmp/testMkDir1/blaa/blee")

    @Test fun testCat1() = cat { +"/etc/fstab" }.chkInIdeap()
    @Test fun testCat2() = cat { +"/etc/fstab"; +"/etc/hosts" }.chkInIdeap()

    @Test fun testSs1() = ss { -tcp; -udp; -listening; -processes; -numeric }.chkWithUser() // ss -tulpn
    @Test fun testSs2() = ss { -tcp; -udp; -listening; -processes; -numeric }.chkInIdeap() // ss -tulpn

    @Test fun testManMan() = man { +"man" }.chkWithUser()
    @Test fun testManVim() = man { +"vim" }.chkWithUser()
    @Test fun testManOpenAll() = man { -ManOpt.All; +"open" }.chkWithUser()
    @Test fun testManOpen2() = man(2) { +"open" }.chkWithUser()
    @Test fun testManOpenSys() = man(ManSection.SysCall) { +"open" }.chkWithUser()
    @Test fun testManApropos() = man { -ManOpt.Apropos(); +"package" }.chkWithUser()
    @Test fun testManWhatIs() = man { -ManOpt.WhatIs; +"which" }.chkWithUser()

    @Test fun testAdbDevices() = adb(devices) { -Option.all; -usb }.chkWithUser("adb -a -d devices")
    @Test fun testAdbShell() = adb(shell).chkWithUser("adb shell")

    @Test fun testVim() {
        val kommand = vim(".") { -gui; -servername("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.chkWithUser("vim -g --servername DDDD .")
    }
    @Test fun testMkTemp() = mktemp().chkWithUser()
    @Test fun testWhich() = which { +"vim" }.chkWithUser()

    @Ignore // jitpack
    @Test fun testCreateTempFile() = println(SYS.mktempExec())
    @Test fun testBash() {
        val kommand1 = vim(".") { -gui; -servername("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.chkWithUser("bash -c vim\\ -g\\ --servername\\ DDDD\\ .")
    }
    @Test fun testBashGetExports() = bashGetExportsMap().execb(SYS)
        .logEachEntry { println("exported env: ${it.key} == \"${it.value}\"") }
    @Test fun testIdeap() = ideap { +"/home/marek/.bashrc"; -ln(2); -col(13) }.chkWithUser()
    @Test fun testIdeapDiff() = ideap(diff) { +"/home/marek/.bashrc"; +"/home/marek/.profile" }.chkWithUser()
}

// FIXME: use impl from new kground
private fun <K, V> Map<K, V>.logEachEntry(logln: (Map.Entry<K, V>) -> Unit = ::println) = iterator().forEach(logln)
