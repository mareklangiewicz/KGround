package pl.mareklangiewicz.kgroundx.maintenance

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.bad.chkNN
import pl.mareklangiewicz.usubmit.UIssueType
import pl.mareklangiewicz.usubmit.UProgress
import pl.mareklangiewicz.usubmit.USubmit
import pl.mareklangiewicz.usubmit.UTask
import pl.mareklangiewicz.usubmit.chkItems
import pl.mareklangiewicz.usubmit.getAllUSubmitItems
import pl.mareklangiewicz.usubmit.isAccepting
import pl.mareklangiewicz.usubmit.isCustom
import pl.mareklangiewicz.usubmit.isDeclining
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*

@Deprecated("Use ZenitySupervisor from kommandline")
class MyZenityManager(val promptPrefix: String? = null): USubmit {

  private val mutex = Mutex()

  @OptIn(ExperimentalApi::class, DelicateApi::class)
  override suspend fun invoke(data: Any?): UTask? = mutex.withLock {
    val coroutineName = coroutineContext[CoroutineName]?.name
    val items = data.getAllUSubmitItems().chkItems()
    val taskOk = items.tasks.singleOrNull { it.isAccepting }
    val taskCancel = items.tasks.singleOrNull { it.isDeclining }
    val prompt = joinLinesNN(promptPrefix, items.issue?.name, items.progress?.str)
    val title = coroutineName ?: items.issue?.type?.name
    val timeoutSec = items.timeout?.duration?.inWholeSeconds?.toInt()?.let { it + 1 }
    return when {
      items.tasks.any { it.isCustom } -> {
        // We have some custom tasks so will be using zenity list type (via zenityAskForOneOf).
        // So all provided tasks will be on the list and zenity ok/cancel buttons will not represent any task.
        // Cancel will always return null (no matter if there is any .isDeclining task on the list)
        // Ok will return selected task (or null if no task is selected)
        val answer: String? = zenityAskForOneOf(
          *items.tasks.map { it.name }.toTypedArray(),
          prompt = prompt,
          title = title,
          withTimeoutSec = timeoutSec,
        ).ax()
        items.tasks.firstOrNull { it.name == answer }
      }
      items.issue?.type == UIssueType.Question -> {
        // No custom tasks, so just accept/decline (ok/cancel) (yes/no) kind of question.
        taskOk.chkNN { "No accepting answer available" }
        taskCancel.chkNN { "No declining answer available" }
        val ok = zenityAskIf(
          question = prompt,
          title = title,
          labelOk = taskOk.name,
          labelCancel = taskCancel.name,
          withTimeoutSec = timeoutSec,
        ).ax()
        if (ok) taskOk else taskCancel
      }
      else -> {
        // Not question (and no custom tasks), so just informative Info/Warning/Error/progress
        val ok: Boolean = zenityShowText(
          type = when (items.issue?.type) {
            UIssueType.Warning -> Type.Warning
            UIssueType.Error -> Type.Error
            else -> Type.Info // so null/no issue (for example just the progress) is also shown as zenity info
          },
          text = prompt,
          withTimeoutSec = timeoutSec,
        ).ax()
        if (ok) taskOk else taskCancel // either or both can be null which is fine in this case.
      }
    }
  }
}

private val UProgress.str get() = listOfNotNull("progress", pos?.let { "$pos of $min..$max" }, details).joinToString(" ")

// TODO_later: more public similar DSL (and use it more)? What similar do I have in kground already?
private fun joinLinesNN(vararg lines: String?) = lines.filterNotNull().joinToString("\n")
