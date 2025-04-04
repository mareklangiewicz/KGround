@file:Suppress("unused")

package pl.mareklangiewicz.kommand.zenity

import kotlinx.coroutines.flow.toList
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.chkThis
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.zenity.ZenityOpt.*
import pl.mareklangiewicz.udata.MutLO
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class) fun zenityShowError(
  error: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  withWrapping: Boolean = false,
  withTimeoutSec: Int? = null,
): ReducedKommand<Boolean> = zenityShowText(Type.Error, error, title, labelOk, withWrapping, withTimeoutSec)

@OptIn(DelicateApi::class) fun zenityShowWarning(
  warning: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  withWrapping: Boolean = false,
  withTimeoutSec: Int? = null,
): ReducedKommand<Boolean> = zenityShowText(Type.Warning, warning, title, labelOk, withWrapping, withTimeoutSec)

@OptIn(DelicateApi::class) fun zenityShowInfo(
  info: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  withWrapping: Boolean = false,
  withTimeoutSec: Int? = null,
): ReducedKommand<Boolean> = zenityShowText(Type.Info, info, title, labelOk, withWrapping, withTimeoutSec)

@DelicateApi
fun zenityShowText(
  type: Type, // only Info or Warning or Error
  text: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  withWrapping: Boolean = false,
  withTimeoutSec: Int? = null,
): ReducedKommand<Boolean> = zenity(type.chkThis { this in setOf(Type.Info, Type.Warning, Type.Error)}) {
  -Text(text)
  title?.let { -Title(it) }
  labelOk?.let { -OkLabel(it) }
  if (!withWrapping) -NoWrap
  withTimeoutSec?.let { -Timeout(it) }
}.reducedToIfAccepted()

@OptIn(DelicateApi::class)
fun zenityAskIf(
  question: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "Yes" (probably localized)
  labelCancel: String? = null, // default should be sth like "No" (probably localized)
  withWrapping: Boolean = false,
  withTimeoutSec: Int? = null,
): ReducedKommand<Boolean> = zenity(Type.Question) {
  -Text(question)
  title?.let { -Title(it) }
  labelOk?.let { -OkLabel(it) }
  labelCancel?.let { -CancelLabel(it) }
  if (!withWrapping) -NoWrap
  withTimeoutSec?.let { -Timeout(it) }
}.reducedToIfAccepted()

@OptIn(DelicateApi::class)
fun zenityAskForOneOf(
  vararg answers: String,
  prompt: String? = null,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  labelCancel: String? = null, // default should be sth like "Cancel" (probably localized)
  labelColumn: String = "Answer",
  withTimeoutSec: Int? = null,
): ReducedKommand<String?> = zenity(Type.List) {
  prompt?.let { -Text(it) }
  title?.let { -Title(it) }
  labelOk?.let { -OkLabel(it) }
  labelCancel?.let { -CancelLabel(it) }
  -Column(labelColumn)
  withTimeoutSec?.let { -Timeout(it) }
  for (a in answers) +a
}.reducedToSingleAnswer()

@OptIn(DelicateApi::class)
fun zenityAskForPassword(
  prompt: String = "Enter password",
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  labelCancel: String? = null, // default should be sth like "Cancel" (probably localized)
  withTimeoutSec: Int? = null,
) = zenityAskForEntry(prompt, title, labelOk, labelCancel, withTimeoutSec, withHiddenEntry = true)

@OptIn(DelicateApi::class)
fun zenityAskForEntry(
  prompt: String,
  title: String? = null,
  labelOk: String? = null, // default should be sth like "OK" (probably localized)
  labelCancel: String? = null, // default should be sth like "Cancel" (probably localized)
  withTimeoutSec: Int? = null,
  withSuggestedEntry: String? = null,
  withHiddenEntry: Boolean = false,
): ReducedKommand<String?> =
  zenity(Type.Entry) {
    -Text(prompt)
    title?.let { -Title(it) }
    labelOk?.let { -OkLabel(it) }
    labelCancel?.let { -CancelLabel(it) }
    withTimeoutSec?.let { -Timeout(it) }
    withSuggestedEntry?.let { -EntryText(it) }
    if (withHiddenEntry) -HideText
  }.reducedToSingleAnswer()

/** @return null means user did not answer at all (pressed esc or timeout);
 * it's different from empty answer, which means user answered ok with some empty selection/entry/whateva */
@DelicateApi
fun Zenity.reducedToSingleAnswer(): ReducedKommand<String?> = reducedManually {
  val answer = stdout.toList().chkStdOut({ size < 2 }).firstOrNull() ?: ""
  val exit = awaitAndChkExit(firstCollectErr = true) { this in setOf(0, 1, 5) }
  // 1 is user cancelled, 5 is timeout
  answer.takeIf { exit == 0 }
}

/**
 * @return true means user pressed sth like "OK"/"Yes"/... button.
 * otherwise false (timeout, pressing cancel/no/closing with "x", ...)
 */
@DelicateApi
fun Zenity.reducedToIfAccepted(): ReducedKommand<Boolean> = reducedToSingleAnswer().reducedMap { this != null }

@DelicateApi
fun zenity(type: Type, init: Zenity.() -> Unit = {}) = Zenity().apply { -type; init() }

/*
* https://help.gnome.org/users/zenity/stable/index.html.en
* https://linux.die.net/man/1/zenity
*/
@OptIn(DelicateApi::class)
data class Zenity(
  override val opts: MutableList<ZenityOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
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

  data object Help : ZenityOpt, KOptLN() // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : ZenityOpt, KOptLN() // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object About : ZenityOpt, KOptLN()
  data class Title(val t: String) : ZenityOpt, KOptLN(t)
  /** icon path or one of keywords: info, warning, question, error */
  data class Icon(val icon: String) : ZenityOpt, KOptL("window-icon", icon)
  data class Timeout(val seconds: Int) : ZenityOpt, KOptLN(seconds.strf)
  data class Text(val t: String) : ZenityOpt, KOptLN(t)
  data class OkLabel(val label: String) : ZenityOpt, KOptLN(label)
  data class CancelLabel(val label: String) : ZenityOpt, KOptLN(label)
  data class Day(val d: Int) : ZenityOpt, KOptLN(d.strf)
  data class Month(val m: Int) : ZenityOpt, KOptLN(m.strf)
  data class Year(val y: Int) : ZenityOpt, KOptLN(y.strf)
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
  data class HideColumn(val c: Int) : ZenityOpt, KOptLN(c.strf)
  data object Listen : ZenityOpt, KOptLN()
  data class Percentage(val p: Int) : ZenityOpt, KOptLN(p.strf)
  data object AutoClose : ZenityOpt, KOptLN()
  data object AutoKill : ZenityOpt, KOptLN()
  data object Pulsate : ZenityOpt, KOptLN()
  data class InitValue(val v: Int) : ZenityOpt, KOptL("value", v.strf)
  data class MinValue(val v: Int) : ZenityOpt, KOptLN(v.strf)
  data class MaxValue(val v: Int) : ZenityOpt, KOptLN(v.strf)
  data class Step(val v: Int) : ZenityOpt, KOptLN(v.strf)
  data object PrintPartial : ZenityOpt, KOptLN()
}
