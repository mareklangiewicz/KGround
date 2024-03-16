package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.Adb.*
import pl.mareklangiewicz.kommand.Adb.Command.*
import pl.mareklangiewicz.kommand.Adb.Option.*
import pl.mareklangiewicz.kommand.Ide.Cmd.Diff
import pl.mareklangiewicz.kommand.Ide.Option.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.iproute2.*
import pl.mareklangiewicz.kommand.Vim.Option.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.All
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.core.LsOpt.SortType.*
import pl.mareklangiewicz.kommand.debian.*
import kotlin.test.*
import kotlin.test.Ignore
import kotlin.test.Test


@OptIn(DelicateApi::class)
// @Ignore // stuff specific to my laptop fails on CI
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
    @Test fun testLs2() = ls { -All; -Author; -LongFormat; -HumanReadable; +"/home/marek" }.chkInGVim()
    @Test fun testLs3() = ls { +"/home/marek" }.chkInGVim()
    @Test fun testLsHome() = ls("/home/marek").execb(SYS).logEach()
    @Test fun testLsHomeSubDirs() = lsSubDirs("/home/marek").execb(SYS).logEach()
    @Test fun testLsHomeSubDirsWithHidden() = lsSubDirs("/home/marek", withHidden = true).execb(SYS).logEach()
    @Test fun testLsHomeRegFiles() = lsRegFiles("/home/marek").execb(SYS).logEach()

    @Test fun testMkDir1() = mkdir("/tmp/testMkDir1/blaa/blee", withParents = true)
        .chkLineRawAndExec("mkdir -p /tmp/testMkDir1/blaa/blee")

    @Test fun testRm1() = rm { -RmOpt.Dir; +"/tmp/testMkDir1/blaa/blee" }
        .chkWithUser("rm -d /tmp/testMkDir1/blaa/blee")

    @Test fun testRm2() = rmTreeWithForce("/tmp/testMkDir1") {
        // double check if we are removing what we think we are:
        ls(it).execb(this) == listOf("blaa")
    }.execb(SYS).logEach()

    @Test fun testCat1() = cat { +"/etc/fstab" }.chkInGVim()
    @Test fun testCat2() = cat { +"/etc/fstab"; +"/etc/hosts" }.chkInGVim()

    @Test fun testSs1() = ssTulpn().chkWithUser() // ss -tulpn
    @Test fun testSs2() = ssTulpn().chkInGVim() // ss -tulpn

    @Test fun testManMan() = man { +"man" }.chkWithUser()
    @Test fun testManVim() = man { +"vim" }.chkWithUser()
    @Test fun testManOpenAll() = man { -ManOpt.All; +"open" }.chkWithUser()
    @Test fun testManOpen2() = man(2) { +"open" }.chkWithUser()
    @Test fun testManOpenSys() = man(ManSection.SysCall) { +"open" }.chkWithUser()
    @Test fun testManApropos() = man { -ManOpt.Apropos(); +"package" }.chkWithUser()
    @Test fun testManWhatIs() = man { -ManOpt.WhatIs; +"which" }.chkWithUser()

    @Test fun testAdbDevices() = adb(Devices) { -Option.All; -Usb }.chkWithUser("adb -a -d devices")
    @Test fun testAdbShell() = adb(Shell).chkWithUser("adb shell")

    @Test fun testVim() {
        val kommand = vim(".") { -Gui; -ServerName("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.chkWithUser("vim -g --servername DDDD .")
    }
    @Test fun testWhich() = which("vim", "ls", all = true).chkWithUser()

    @Ignore
    @Test fun testMkTemp() = println(mktemp().execb(SYS))
    @Test fun testBash() {
        val kommand1 = vim(".") { -Gui; -ServerName("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.chkWithUser("bash -c vim -g --servername DDDD .")
    }
    @Test fun testBashGetExports() = bashGetExportsMap().execb(SYS)
        .logEachEntry { println("exported env: ${it.key} == \"${it.value}\"") }
    @Test fun testIdeapOpen() = ide(Ide.Type.ideap, Ide.Cmd.Open) { +"/home/marek/.bashrc"; -Line(2); -Column(13) }.chkWithUser()
    @Test fun testIdeapDiff() = ide(Ide.Type.ideap, Ide.Cmd.Diff) { +"/home/marek/.bashrc"; +"/home/marek/.profile" }.chkWithUser()
}
