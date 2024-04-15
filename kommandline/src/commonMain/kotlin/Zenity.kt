@file:Suppress("unused")

package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.toList
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.ZenityOpt.*

@OptIn(DelicateApi::class)
fun zenityAskIf(question: String, title: String? = null) = zenity(Type.Question) {
  -Text(question)
  -NoWrap
  title?.let { -Title(it) }
}.reducedExit { it == 0 }

@OptIn(DelicateApi::class)
fun zenityAskForPassword(question: String = "Enter password", title: String? = null) =
  zenity(Type.Entry) {
    -HideText
    -Text(question)
    title?.let { -Title(it) }
  }.reducedToSingleAnswer()

@OptIn(DelicateApi::class)
fun zenityAskForEntry(question: String, title: String? = null, suggested: String? = null) =
  zenity(Type.Entry) {
    -Text(question)
    title?.let { -Title(it) }
    suggested?.let { -EntryText(it) }
  }.reducedToSingleAnswer()

/** @return null means user did not answer at all (pressed esc); it's different from empty answer */
private fun Zenity.reducedToSingleAnswer(): ReducedKommand<String?> = reducedManually {
  val answer = stdout.toList().chkStdOut({ size < 2 }).firstOrNull()
  val exit = awaitAndChkExit(firstCollectErr = true) { this in 0..1 }
  answer?.takeIf { exit == 0 } // 1 means user canceled
}

@DelicateApi
fun zenity(type: Type, init: Zenity.() -> Unit = {}) = Zenity().apply { -type; init() }

/*
* https://help.gnome.org/users/zenity/stable/index.html.en
* https://linux.die.net/man/1/zenity
*/
@OptIn(DelicateApi::class)
data class Zenity(
  override val opts: MutableList<ZenityOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<ZenityOpt> {
  override val name get() = "zenity"
}

@DelicateApi
interface ZenityOpt : KOptTypical {

  sealed class Type : ZenityOpt, KOptLN() {
    data object Calendar : Type()
    data object Entry : Type()
    data object Error : Type()
    data object FileSelection : Type()
    data object Info : Type()
    data object List : Type()
    data object Notification : Type()
    data object Progress : Type()
    data object Question : Type()
    data object TextInfo : Type()
    data object Warning : Type()
    data object Scale : Type()
  }

  data object Help : ZenityOpt, KOptLN()
  data object Version : ZenityOpt, KOptLN()
  data object About : ZenityOpt, KOptLN()
  data class Title(val t: String) : ZenityOpt, KOptLN(t)
  /** icon path or one of keywords: info, warning, question, error */
  data class Icon(val icon: String) : ZenityOpt, KOptL("window-icon", icon)
  data class Timeout(val seconds: Int) : ZenityOpt, KOptLN(seconds.toString())
  data class Text(val t: String) : ZenityOpt, KOptLN(t)
  data class OkLabel(val label: String) : ZenityOpt, KOptLN(label)
  data class CancelLabel(val label: String) : ZenityOpt, KOptLN(label)
  data class Day(val d: Int) : ZenityOpt, KOptLN(d.toString())
  data class Month(val m: Int) : ZenityOpt, KOptLN(m.toString())
  data class Year(val y: Int) : ZenityOpt, KOptLN(y.toString())
  data class DateFormat(val format: String) : ZenityOpt, KOptLN(format)
  data class EntryText(val t: String) : ZenityOpt, KOptLN(t)
  data object HideText : ZenityOpt, KOptLN()
  data object NoWrap : ZenityOpt, KOptLN()
  data class FileName(val fn: String) : ZenityOpt, KOptL("filename", fn)
  data object Multiple : ZenityOpt, KOptLN()
  data object Directory : ZenityOpt, KOptLN()
  data object Save : ZenityOpt, KOptLN()
  data class Separator(val s: String) : ZenityOpt, KOptLN(s)
  data object ConfirmOverwrite : ZenityOpt, KOptLN()
  data class Column(val header: String) : ZenityOpt, KOptLN(header)
  data object CheckList : ZenityOpt, KOptL("checklist")
  data object RadioList : ZenityOpt, KOptL("radiolist")
  data object Editable : ZenityOpt, KOptLN()
  data class PrintColumn(val c: String) : ZenityOpt, KOptLN(c)
  data class HideColumn(val c: Int) : ZenityOpt, KOptLN(c.toString())
  data object Listen : ZenityOpt, KOptLN()
  data class Percentage(val p: Int) : ZenityOpt, KOptLN(p.toString())
  data object AutoClose : ZenityOpt, KOptLN()
  data object AutoKill : ZenityOpt, KOptLN()
  data object Pulsate : ZenityOpt, KOptLN()
  data class InitValue(val v: Int) : ZenityOpt, KOptL("value", v.toString())
  data class MinValue(val v: Int) : ZenityOpt, KOptLN(v.toString())
  data class MaxValue(val v: Int) : ZenityOpt, KOptLN(v.toString())
  data class Step(val v: Int) : ZenityOpt, KOptLN(v.toString())
  data object PrintPartial : ZenityOpt, KOptLN()
}


