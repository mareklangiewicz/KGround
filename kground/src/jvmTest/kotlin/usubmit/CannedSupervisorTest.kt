@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.usubmit.xd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.usubmit.xd.XD.*

class CannedSupervisorTest {

  private val logged = mutableListOf<Pair<ULogLevel, Any?>>()
  private val log = ULog { level, data -> logged += level to data }

  private fun canned(vararg answers: Pair<String, String>) = CannedSupervisor(answers.toMap(), log)

  @Test fun askIfIsAnsweredByIdNotByPromptText() = runBlocking {
    val submit = canned("deploy" to "yes", "cleanup" to "no")
    assertTrue(submit.askIf("Deploy /some/dynamic/path?", questionId = "deploy"))
    assertEquals(false, submit.askIf("Clean up now?", questionId = "cleanup"))
  }

  @Test fun askIfAcceptsTheAnswerNamesToo() = runBlocking {
    val submit = canned("go" to "start", "stop" to "Abort")
    assertTrue(submit.askIf("Go?", Start, Stop, questionId = "go"))
    assertEquals(false, submit.askIf("Keep going?", Continue, Abort, questionId = "stop"))
  }

  @Test fun anUnanticipatedQuestionFailsInsteadOfBeingGuessed() = runBlocking {
    val submit = canned("other" to "yes")
    val err = assertFailsWith<UnansweredIssueErr> { submit.askIf("Delete everything?", questionId = "delete") }
    assertEquals("delete", err.issue.id)
    assertTrue("Delete everything?" in err.message!!, err.message)
  }

  @Test fun aQuestionWithNoIdFailsEvenIfItsPromptIsAKey() = runBlocking {
    val submit = canned("Delete everything?" to "yes")
    assertFailsWith<UnansweredIssueErr> { submit.askIf("Delete everything?") }
    Unit
  }

  @Test fun anAnswerThatDoesNotFitTheQuestionFails() = runBlocking {
    assertFailsWith<UnansweredIssueErr> { canned("q" to "maybe").askIf("Q?", questionId = "q") }
    assertFailsWith<UnansweredIssueErr> { canned("q" to "c").askForAction("Q?", "a", "b", questionId = "q") }
    Unit
  }

  @Test fun actionsAndEntriesAreAnsweredToo() = runBlocking {
    val submit = canned("pick" to "b", "name" to "kthreelhu")
    assertEquals("b", submit.askForAction("Pick one", "a", "b", questionId = "pick"))
    assertEquals("kthreelhu", submit.askForEntry("Project?", questionId = "name"))
  }

  @Test fun showsGoToTheLogNotToAHuman() = runBlocking {
    val submit = canned()
    submit.showInfo("just so you know")
    submit.showWarning("careful")
    submit.showError("broken")
    assertEquals(listOf(ULogLevel.INFO, ULogLevel.WARN, ULogLevel.ERROR), logged.map { it.first })
    assertTrue(logged.all { it.second.toString().isNotEmpty() })
  }
}
