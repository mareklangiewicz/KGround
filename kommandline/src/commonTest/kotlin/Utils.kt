package pl.mareklangiewicz.kommand

import kotlin.coroutines.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.uctx.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.uspek.*

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
