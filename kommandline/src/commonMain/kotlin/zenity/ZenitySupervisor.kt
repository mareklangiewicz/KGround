@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.kgroundx.maintenance

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*
import pl.mareklangiewicz.udata.unt
import pl.mareklangiewicz.usubmit.xd.XD.*
import pl.mareklangiewicz.usubmit.*

// TODO_later: probably refactor,
//   But keep usubmit communication/protocol/invariants simple, not specific to zenity or any style of UI.
//   (think also of other UI styles like neovim+fzf.vim)
class ZenitySupervisor(val promptPrefix: String? = null): USubmit {

  private val mutex = Mutex()

  override suspend fun invoke(data: Any?): Any? = mutex.withLock {
    data as? ToSubmit ?: bad { "Unsupported data type ${data!!::class}" }
    val issue = data.issue
    val timeoutSec = data.timeout?.duration?.inWholeSeconds?.toInt()?.let { it + 1 }
    val name = issue?.name
    val title = coroutineContext[CoroutineName]?.name ?: name
    val prompt = joinLinesNN(promptPrefix, name)

    return when (data) {

      is ToShow -> when (data) {
        is ShowError -> zenityShowError(prompt, title, withTimeoutSec = timeoutSec).ax().unt
        is ShowWarning -> zenityShowWarning(prompt, title, withTimeoutSec = timeoutSec).ax().unt
        is ShowInfo -> zenityShowInfo(prompt, title, withTimeoutSec = timeoutSec).ax().unt
        is ShowProgress -> println(data.str)
          // FIXME: some non-suspending notification?? progress itself should never actually suspend!
        is ShowMany -> data.stuff.forEach { invoke(it) }
          // FIXME: show more at once?
          //   collect errors, warnings, infos, and especially progress, and show one nice big dialog?
      }

      is ToAsk -> {
        when (data) {
          is AskForEntry -> zenityAskForEntry(
            prompt = prompt,
            title = title,
            withTimeoutSec = timeoutSec,
            withSuggestedEntry = data.suggest.entry.entry,
            withHiddenEntry = data.suggest.entry.hidden,
          ).ax()?.let { UseEntry(Entry(it, data.suggest.entry.hidden)) }
          is AskIf -> {
            val ok = zenityAskIf(prompt, title, data.accept.name, data.decline.name, withTimeoutSec = timeoutSec).ax()
            if (ok) data.accept else data.decline
          }
          is AskForAction -> {
            zenityAskForOneOf(*data.actions.map { it.name }.toTypedArray(), prompt = prompt, title = title)
              .ax()?.let { DoAction(Action(it)) }
          }
          is AskAndShow -> { invoke(data.toShow); invoke(data.toAsk) }
            // FIXME: show all to show and ask question at once? (toShow will often be ShowMany)
            //   collect question, errors, warnings, infos, and especially progress, and show one nice big dialog?
        }
      }
    }
  }
}

private val ShowProgress.str get() = listOfNotNull("progress", pos?.let { "$pos of $min..$max" }, details).joinToString(" ")

// TODO_later: more public similar DSL (and use it more)? What similar do I have in kground already?
private fun joinLinesNN(vararg lines: String?) = lines.filterNotNull().joinToString("\n")
