@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.kommand

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.bad.chk
import pl.mareklangiewicz.bad.chkEmpty
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.bad.chkThrows
import pl.mareklangiewicz.bad.req
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.Vim.Option.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.debian.*
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.uspek.USpekContext
import pl.mareklangiewicz.uspek.USpekTree
import pl.mareklangiewicz.uspek.failed
import pl.mareklangiewicz.uspek.so
import pl.mareklangiewicz.uspek.suspek
import pl.mareklangiewicz.uspek.ucontext
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@OptIn(DelicateApi::class)
class KommandTests {

    init { "INIT ${this::class.simpleName}".teePP }

    val platform = getCurrentPlatformKind()

    @Test fun t() = runTestUSpekWithWorkaround {

        "On string with bash meta chars" so {
            val string = "abc|&;<def>(ghi) 1 2  3 \"\\jkl\t\nmno"
            "bash quote meta chars correctly" so {
                val quoted = bashQuoteMetaChars(string)
                quoted chkEq "abc\\|\\&\\;\\<def\\>\\(ghi\\)\\ 1\\ 2\\ \\ 3\\ \\\"\\\\jkl\\\t\\\nmno"
            }
        }

        if (platform == "JVM") "On JVM only" so { // TODO_someday: On Native? On NodeJs?

            "On real file system on tmp dir" so { // random dir name can't be in test name bc uspek would loop inf

                "On mktemp kommand" so {
                    var tmpFile = "/tmp/ERROR"
                    try {
                        tmpFile = mktemp(path = "/tmp", prefix = "tmpFile").ax()
                        "name is fine" so { tmpFile.chkThis { startsWith("/tmp/tmpFile") && endsWith(".tmp") } }
                        "file is there" so {
                            lsRegFiles("/tmp").ax().chkThis { any { "/tmp/$it" == tmpFile } }
                        }
                    }
                    finally { rmFileIfExists(tmpFile).ax() }
                }

                val dir = "testDirTmp" + Random.nextLong().absoluteValue
                val tmpDir = "/tmp/$dir"
                val tmpDirBla = "$tmpDir/bla"
                val tmpDirBlaBle = "$tmpDirBla/ble"

                "On mkdir with parents" so {
                    try {
                        mkdir(tmpDirBlaBle, withParents = true).chkLineRaw("mkdir -p $tmpDirBlaBle").ax()

                        "check created dirs with ls" so {
                            lsSubDirs("/tmp").chkLineRaw("ls --indicator-style=slash /tmp")
                                .ax().chkThis { contains(dir) }
                        }
                        "ls tmp dir is not file" so { lsRegFiles("/tmp").ax().chkThis { !contains(dir) } }

                        "On rm empty ble" so {
                            rmDirIfEmpty(tmpDirBlaBle).ax()

                            "bla does not contain ble" so { lsSubDirs(tmpDirBla).ax().chkEmpty() }
                        }

                        "On touchy blu file" so {
                            val blu = "blu.touchy"
                            val fullBlu = "$tmpDirBlaBle/$blu"
                            touch(fullBlu).ax()

                            "ls blu is there" so { lsRegFiles(tmpDirBlaBle).ax().chkThis { contains(blu) } }

                            // TODO_someday: test playing with touchy blu file content with some popular kommands

                            "On rm blu" so {
                                rm(fullBlu).ax()

                                "ls blu is NOT there" so { lsRegFiles(tmpDirBlaBle).ax().chkThis { !contains(blu) } }
                            }

                            "On rm wrong file name" so {
                                "using nice wrapper outputs File not found" so {
                                    rmFileIfExists("$fullBlu.wrong").ax().chkEq(listOf("File not found"))
                                }
                                "using plain rm throws BadExitStateErr".soThrows<BadExitStateErr> {
                                    rm("$fullBlu.wrong").ax()
                                }
                            }
                        }

                        "On rmTreeWithForce" so {
                            rmTreeWithForce(tmpDir) { cli, path -> path.startsWith("/tmp/testDirTmp") }.ax()

                            "tmp does not contain our dir" so { lsSubDirs("/tmp").ax().chkThis { !contains(dir) } }
                        }

                    }
                    finally {
                        // Clean up. Notice: The "On rmTreeWithForce" above is only for specific test branch,
                        // but here we always make sure we clean up in all uspek cases.
                        rmTreeWithForce(tmpDir) { cli, path -> path.startsWith("/tmp/testDirTmp") }.ax()
                    }
                }
            }
        }

    }
}


// FIXME: Use impl from new KGround instead
inline fun <T> T.chkThis(lazyMessage: () -> String = { "this is bad" }, thisIsFine: T.() -> Boolean): T =
    apply { chk(thisIsFine(), lazyMessage) }
inline fun <T> T.reqThis(lazyMessage: () -> String = { "this arg is bad" }, thisIsFine: T.() -> Boolean): T =
    apply { req(thisIsFine(), lazyMessage) }


// TODO_maybe: Add sth like this to USpekX?
suspend inline fun <reified T : Throwable> String.soThrows(
    crossinline expectation: (T) -> Boolean = { true },
    crossinline code: suspend () -> Unit
) = so { chkThrows<T>(expectation) { code() } }


@OptIn(DelicateApi::class)
@Ignore // TODO NOW: move&rewrite it all. some to samples, some above, etc. consider kotlin/native too
class KommandTestOld {


    @Test fun testVim() {
        val kommand = vim(".") { -Gui; -ServerName("DDDD") }
        assertEquals(listOf("-g", "--servername", "DDDD", "."), kommand.args)
        kommand.tryInteractivelyCheck("vim -g --servername DDDD .")
    }
    @Test fun testWhich() = which("vim", "ls", all = true).tryInteractivelyCheck()

    @Ignore
    @Test fun testMkTemp() = ulog.i(mktemp().axb(SYS))
    @Test fun testBash() {
        val kommand1 = vim(".") { -Gui; -ServerName("DDDD") }
        val kommand2 = bash(kommand1)
        assertEquals(listOf("-c", "vim -g --servername DDDD ."), kommand2.args)
        kommand2.tryInteractivelyCheck("bash -c vim -g --servername DDDD .")
    }
    @Ignore // Let's not print all env vars on github actions.
    @Test fun testBashGetExports() = bashGetExportsMap().axb(SYS)
        .logEachEntry { ulog.i("exported env: ${it.key} == \"${it.value}\"") }

    @Test fun testIdeOpen() = ideOpen("/home/marek/.bashrc", line = 15, column = 15).axb(SYS).unit

    @Test fun testIdeDiff() = ideDiff("/home/marek/.bashrc", "/home/marek/.profile").axb(SYS).unit
}


internal fun runTestUSpekWithWorkaround(
    context: CoroutineContext = USpekContext(),
    timeout: Duration = 10.seconds,
    code: suspend TestScope.() -> Unit,
) = runTest(context, timeout) {
    suspek { code() }
    coroutineContext.ucontext.branch.assertAllGood()
}

/**
 * Temporary workaround to make sure I notice failed tests in IntelliJ.
 * Without it, I get a green checkmark in IntelliJ even if some tests failed, and I have to check logs.
 * In the future I'll have custom mpp runner+logger, so this workaround will be removed.
 */
internal fun USpekTree.assertAllGood() {
    if (failed) throw end!!.cause!!
    branches.values.forEach { it.assertAllGood() }
}
