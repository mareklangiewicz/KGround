package pl.mareklangiewicz.kommand

import kotlin.test.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.bad.chkThrows
import pl.mareklangiewicz.uspek.so
import okio.Path
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.udata.strf
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.chkEmpty
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.bad.chkThis
import pl.mareklangiewicz.bad.chkThrows
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kgroundx.maintenance.ZenitySupervisor
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.shell.bashQuoteMetaChars
import pl.mareklangiewicz.kommand.konfig.IKonfig
import pl.mareklangiewicz.kommand.konfig.konfigInDir
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.uspek.USpekContext
import pl.mareklangiewicz.uspek.USpekTree
import pl.mareklangiewicz.uspek.failed
import pl.mareklangiewicz.uspek.so
import pl.mareklangiewicz.uspek.suspek
import pl.mareklangiewicz.uspek.ucontext

class UtilsTest {
  @Test fun findSingleTest() {
    val input = "xabcxabcy"
    val result = Regex("abc").findSingle(input, 2)
    assertEquals(5..7, result.range)
  }
}


// TODO_maybe: Add sth like this to USpekX?
suspend inline fun <reified T : Throwable> String.soThrows(
  crossinline expectation: (T) -> Boolean = { true },
  crossinline code: suspend () -> Unit,
) = so { chkThrows<T>(expectation) { code() } }

@OptIn(NotPortableApi::class)
internal fun runTestUSpekWithWorkarounds(
  context: CoroutineContext = USpekContext(),
  timeout: Duration = 10.seconds,
  code: suspend TestScope.() -> Unit,
) = runTest(context, timeout) {
  val log = UHackySharedFlowLog { level, data -> "T ${level.symbol} ${data.str(maxLength = 512)}" }
  val submit = ZenitySupervisor("FIXME later")
  // FIXME later: this should NOT be used; later: provide special USubmit for tests
  val cli = getSysCLI()
  uctx(log + submit + cli) {
    suspek {
      code()
    }
  }
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
