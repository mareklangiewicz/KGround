@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.usubmit.xd

import kotlin.jvm.JvmInline
import kotlin.time.Duration
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.umath.lerpInv
import kotlin.time.Duration.Companion.seconds
import pl.mareklangiewicz.usubmit.USubmit
import pl.mareklangiewicz.usubmit.xd.XD.*


/**
 * XD Model ;) (eXperimental Data Model)
 *
 * This code below is just one proposed "partial static typing" convention. It will be evolving/changed a lot.
 * The idea is to distill some quite useful, small, simple conventions for what data to put through USubmit and back.
 * But I still want to keep stable basic USubmit contract using Any?: "USubmit.invoke(data: Any?): Any?".
 * It's a "hole" in type system for good reasons. I don't want to force any conventions/types globally.
 * Code below is only proposed way to communicate between "worker"/"submitter" and "supervisor"/"user".
 * But basic general "contract" is always: Anything "Any?" can be potentially submitted and "Any?" can be returned.
 * But often (in specific project) both sides at runtime will agree what is accepted and what means what.
 * Maybe acceptable practice will be for client/worker code to first chk(usubmit is FamiliarSupervisorInterface)...
 * But I want typical client code to be developed independently of potential supervisor setup by the user.
 */
@ExperimentalApi sealed interface XD {

  /**
   * Let's call the entity that calls [USubmit.invoke] the worker, and the one that reacts: the supervisor.
   * The worker can provide a list of [Action] as his current "abilities" as [USubmit.invoke] parameter: [AskForAction],
   * so the supervisor can choose one and return [DoAction] from [USubmit.invoke] and the worker will actually do it.
   * Supervisor always can pause the worker by suspending [USubmit.invoke] call for long time;
   * Supervisor always can fire the worker by throwing [kotlinx.coroutines.CancellationException] from [USubmit.invoke].
   */
  data class Action(val name: String) : XD

  /**
   * @param hidden only means it should be hidden to the user when he enters it (passed both ways).
   * So usually hidden means this entry represents password. Warning: entry/password is not encrypted here!
   */
  data class Entry(val entry: String = "", val hidden: Boolean = false) : XD

  /**
   * The worker provides [UIssue] to [USubmit.invoke] in some [ToAsk] to ask the supervisor/user about some issue.
   * These issues are meant to be "recoverable", so worker should be able to continue
   * if the supervisor returns normally from [USubmit.invoke].
   * More critical worker issues/errors should be thrown as exceptions (as usual).
   * @param id identifies issue, so Supervisor+user can decide to sth like "Yes for all":
   * automatically react the same way on future issues with the same [id]
   * for next... 100 same issues, or next... 5 minutes, etc.
   * I consider it bad to allow user to "Yes for all" for all the same issues FOREVER.
   * The "Yes for all" should be easily monitored (how many times it's automatically answered),
   * and easily turned off by user (when he sees supervisor is accepting too much)
   * BTW: for behavior like "Yes to all" to work, not only issue with the same id have to be provided
   * again, but also same [Answer] have to be possible, representing some specific answer, f.e. "Yes".
   * TODO_someday: think about serializable ids (and serializable everything here)
   *   so we can have remote supervisor easily (with kotlinx.serialization)
   *   (cancellation/exceptions won't be easily serializable, but we can probably do it similarly to rsocket-kotlin)
   */
  data class Issue(val name: String, val id: Any? = null) : XD

  /** In XD model the [USubmit.invoke] data is always of type [ToSubmit] */
  sealed interface ToSubmit : XD { val issue : Issue?; val timeout : SuggestTimeout? }
  sealed interface ToSuggest : XD
  sealed interface ToShow : ToSubmit
  sealed interface ToAsk : ToSubmit { override val issue : Issue }

  /**
   * A hint from worker to supervisor not to wait too long for user.
   * But supervisor can ignore it, obviously, or wait a bit longer when busy etc. (zenity support timeout in seconds).
   */
  @JvmInline value class SuggestTimeout(val duration: Duration = 10.seconds) : ToSuggest

  @JvmInline value class SuggestEntry(val entry: Entry = Entry()) : ToSuggest

  /** In XD model the [USubmit.invoke] return value is always of type [ToReturn] */
  sealed interface ToReturn : XD

  sealed interface Answer : ToReturn

  @JvmInline value class Accept(val name: String = "Yes") : Answer
  @JvmInline value class Decline(val name: String = "No") : Answer

  @JvmInline value class DoAction(val action: Action) : Answer
  @JvmInline value class UseEntry(val entry: Entry) : Answer

  data class ShowInfo(override val issue: Issue, override val timeout: SuggestTimeout? = null) : ToShow
  data class ShowWarning(override val issue: Issue, override val timeout: SuggestTimeout? = null) : ToShow
  data class ShowError(override val issue: Issue, override val timeout: SuggestTimeout? = null) : ToShow

  /**
   * A way for worker to report current progress. It itself should never suspend the work.
   * Usually supervisor will show it to the user continuously and update it,
   * when new [ShowProgress] (in the same coroutine/Job) is received.
   * Sometimes [max] (or even [min]) can also be updated during the Job,
   * for example when searching through big file tree and discovering new subtrees.
   * @param highlight Just a hint for supervisor to highlight current progress more. Usually with bold font.
   */
  data class ShowProgress(
    val pos: Float? = null,
    val min: Float = 0f,
    val max: Float = 1f,
    val details: String? = null,
    val highlight: Boolean = false,
    override val timeout: SuggestTimeout? = null,
  ) : ToShow { override val issue: Issue? get() = null }

  data class AskIf(
    override val issue: Issue,
    val accept: Accept = Yes,
    val decline: Decline = No,
    override val timeout: SuggestTimeout? = null,
  ) : ToAsk

  data class AskForAction(
    override val issue: Issue,
    val actions: List<Action>,
    override val timeout: SuggestTimeout? = null,
  ) : ToAsk

  data class AskForEntry(
    override val issue: Issue,
    val suggest: SuggestEntry = SuggestEntry(),
    override val timeout: SuggestTimeout? = null,
  ) : ToAsk

  data class AskAndShow(val toAsk : ToAsk, val toShow: ToShow) : ToAsk {
    override val issue get() = toAsk.issue
    override val timeout get() = toAsk.timeout
  }

  @JvmInline value class ShowMany(val stuff: List<ToShow>) : ToShow {
    override val issue: Issue? get() = null
    override val timeout: SuggestTimeout? get() = null
  }


}

val ShowProgress.fraction: Float? get() = pos?.let { lerpInv(min, max, it) }

val Yes = Accept()
val No = Decline()
val Ok = Accept("Ok")
val Cancel = Decline("Cancel")
val Start = Accept("Start")
val Stop = Decline("Stop")
val Continue = Accept("Continue")
val Abort = Decline("Abort")
val Confirm = Accept("Confirm")
val Refuse = Decline("Refuse")
val Accept = Accept("Accept")
val Deny = Decline("Deny")

@ExperimentalApi
suspend fun USubmit.askIf(
  question: String,
  accept: Accept = Yes,
  decline: Decline = No,
  timeout: Duration? = null,
  questionId: Any? = null,
): Boolean = this(AskIf(Issue(question, questionId), accept, decline, timeout?.let(::SuggestTimeout))) == accept

@ExperimentalApi
suspend fun USubmit.askForAction(
  question: String,
  vararg actions: String,
  timeout: Duration? = null,
  questionId: Any? = null,
): String? = (this(AskForAction(
  Issue(question, questionId),
  actions.map { Action(it) },
  timeout?.let(::SuggestTimeout),
)) as? DoAction)?.action?.name

@ExperimentalApi
suspend fun USubmit.askForEntry(
  question: String,
  suggest: String = "",
  hidden: Boolean = false,
  timeout: Duration? = null,
  questionId: Any? = null,
): String? = (this(AskForEntry(
  issue = Issue(question, questionId),
  suggest = SuggestEntry(Entry(suggest, hidden)),
  timeout = timeout?.let(::SuggestTimeout),
)) as? UseEntry)?.entry?.entry
