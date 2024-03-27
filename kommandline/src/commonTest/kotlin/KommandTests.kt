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
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.uspek.USpekContext
import pl.mareklangiewicz.uspek.USpekTree
import pl.mareklangiewicz.uspek.failed
import pl.mareklangiewicz.uspek.so
import pl.mareklangiewicz.uspek.suspek
import pl.mareklangiewicz.uspek.ucontext
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.random.Random
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

            "On real file system on tmp dir" so {

                "On mktemp kommand" so {
                    var tmpFile = "/tmp/fake"
                    try {
                        tmpFile = mktemp(path = "/tmp", prefix = "tmpFile").ax()
                        "name is fine" so { tmpFile.chkThis { startsWith("/tmp/tmpFile") && endsWith(".tmp") } }
                        "file is there" so {
                            lsRegFiles("/tmp").ax().chkThis { any { "/tmp/$it" == tmpFile } }
                        }
                    }
                    finally { rmFileIfExists(tmpFile).ax() }
                }

                // Note: random dir name can't be in test name bc uspek would loop infinitely finding new "branches"
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

                            "On blu file content" so {
                                "it is empty" so { readFileWithCat(fullBlu).ax().chkEmpty() }
                                "On write poem" so {
                                    val poem = listOf("NOTHING IS FAIR IN THIS WORLD OF MADNESS!")
                                    writeFileWithDD(poem, fullBlu).ax()
                                    "poem is there" so { readFileWithCat(fullBlu).ax() chkEq poem }
                                    "On write empty list of lines" so {
                                        writeFileWithDD(emptyList<String>(), fullBlu).ax()
                                        "it is empty again" so { readFileWithCat(fullBlu).ax().chkEmpty() }
                                    }
                                }
                            }

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
