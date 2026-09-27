@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.usubmit.xd

import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.usubmit.USubmit
import pl.mareklangiewicz.usubmit.xd.XD.*

/**
 * Thrown by [CannedSupervisor] for a question it has no fitting answer for: no [Issue.id], no answer
 * under that id, or an answer that does not fit the question (like "maybe" to [AskIf]).
 * The message names both the id and the prompt, so the caller knows what answer to add.
 */
@ExperimentalApi
class UnansweredIssueErr(val issue: Issue, reason: String) :
  BadStateErr("$reason: id=${issue.id} prompt=\"${issue.name}\"")

/**
 * A supervisor that answers from a fixed table instead of asking a human, for agents and scripts
 * that must not pop up anything (see ZenitySupervisor in kommand-line for the interactive one).
 *
 * Answers are keyed by the stable [Issue.id] of each question -- never by its prompt text, which is
 * for humans and often interpolates paths or counts. A question it cannot answer is never guessed:
 * [UnansweredIssueErr] is thrown instead, so every decision taken is one somebody named explicitly.
 *
 * Answer strings per question type:
 * - [AskIf]: "yes"/"true" or the accept name (like "Start") accepts; "no"/"false" or the decline name declines.
 * - [AskForAction]: one of the offered action names.
 * - [AskForEntry]: the entry itself.
 *
 * Everything to show ([ToShow]) is logged to [log] (or the ULog in the coroutine context), not shown.
 */
@ExperimentalApi
class CannedSupervisor(val answers: Map<String, String>, val log: ULog? = null) : USubmit {

  override suspend fun invoke(data: Any?): Any? = when (data) {
    is ToShow -> show(data)
    is ToAsk -> ask(data)
    else -> bad { "Unsupported data type ${data?.let { it::class }}" }
  }

  private suspend fun show(data: ToShow) {
    val log = log ?: localULog()
    when (data) {
      is ShowInfo -> log.i(data.issue.name)
      is ShowWarning -> log.w(data.issue.name)
      is ShowError -> log.e(data.issue.name)
      is ShowProgress -> log.i(data)
      is ShowMany -> data.stuff.forEach { show(it) }
    }
  }

  private suspend fun ask(data: ToAsk): Answer {
    if (data is AskAndShow) { show(data.toShow); return ask(data.toAsk) }
    val issue = data.issue
    val id = issue.id ?: throw UnansweredIssueErr(issue, "Question without id can not be answered by CannedSupervisor")
    val answer = answers[id.toString()] ?: throw UnansweredIssueErr(issue, "No canned answer")
    return when (data) {
      is AskIf -> when {
        answer.lowercase() in listOf("yes", "true") || answer.equals(data.accept.name, ignoreCase = true) -> data.accept
        answer.lowercase() in listOf("no", "false") || answer.equals(data.decline.name, ignoreCase = true) -> data.decline
        else -> throw UnansweredIssueErr(issue, "Canned answer \"$answer\" is neither accept nor decline")
      }
      is AskForAction -> data.actions.firstOrNull { it.name == answer }?.let(::DoAction)
        ?: throw UnansweredIssueErr(issue, "Canned answer \"$answer\" is not one of ${data.actions.map { it.name }}")
      is AskForEntry -> UseEntry(Entry(answer, data.suggest.entry.hidden))
      is AskAndShow -> error("handled above")
    }
  }
}
