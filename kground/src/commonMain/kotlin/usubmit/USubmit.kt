@file:Suppress("unused")

package pl.mareklangiewicz.usubmit

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import pl.mareklangiewicz.bad.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.uctx.UCtx
import pl.mareklangiewicz.umath.lerpInv

fun interface USubmit : UCtx {
  suspend operator fun invoke(data: Any?): Any?
  companion object Key : CoroutineContext.Key<USubmit>
  override val key: CoroutineContext.Key<*> get() = Key
}
suspend inline fun <reified T: USubmit> implictxOrNull(): T? = coroutineContext[USubmit] as? T

suspend inline fun <reified T: USubmit> implictx(): T =
  implictxOrNull() ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

@Deprecated("")
interface WithUSubmit {
  val usubmit: USubmit
}

@Deprecated("")
class USubmitNotSupportedErr : USubmit {
  override suspend fun invoke(data: Any?): Nothing = bad { "USubmit is not supported in this context." }
}

/**
 * This and code below is just one proposed "partial static typing" convention. It will be evolving/changed a lot.
 * The idea is to distill some quite useful, small, simple conventions for what data to put through USubmit and back.
 * But I still want to keep stable basic code above using Any?: "USubmit.invoke(data: Any?): Any?".
 * It's a "hole" in type system for good reasons. I don't want to force any conventions/types globally.
 * Code below is only proposed way to communicate between "worker"/"submitter" and "supervisor"/"user".
 * But basic general "contract" is always: Anything "Any?" can be potentially submitted and "Any?" can be returned.
 * But often (in specific project) both sides at runtime will agree what is accepted and what means what.
 * Maybe acceptable practice will be for client/worker code to first chk(usubmit is FamiliarSupervisorInterface)...
 * But I want typical client code to be developed independently of potential supervisor setup by the user.
 */
@Deprecated("Use XD Model")
interface USubmitItem

/**
 * Let's call the entity that calls [USubmit.invoke] the worker, and the one that reacts: the supervisor.
 * The worker can provide a list of [UTask] as his current "abilities" as [USubmit.invoke] parameter,
 * so the supervisor can choose one and return it from [USubmit.invoke] and the worker will actually do the chosen task.
 * Supervisor always can pause the worker by suspending [USubmit.invoke] call for long time;
 * Supervisor always can fire the worker by throwing [kotlinx.coroutines.CancellationException] from [USubmit.invoke].
 * Also [UIssue] can be included by worker to show sth to the user (but normally [UTask] is returned back).
 */
@Deprecated("Use XD Model")
data class UTask(val name: String) : USubmitItem

/**
 * The worker can provide [UIssue] to [USubmit.invoke] to inform the supervisor/user about some issue.
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
 * again, but also same [UTask] have to be provided, representing some specific answer, f.e. "Yes".
 * TODO_someday: think about serializable ids (and serializable everything here)
 *   so we can have remote supervisor easily (with kotlinx.serialization)
 *   (cancellation/exceptions won't be easily serializable, but we can probably do it similarly to rsocket-kotlin)
 */
@Deprecated("Use XD Model")
data class UIssue(val name: String, val type: UIssueType, val id: Any? = null) : USubmitItem

@Deprecated("Use XD Model")
enum class UIssueType { Info, Warning, Error, Question }


/**
 * When sent to submitter it means we WANT some entry (so field entry here is SUGGESTED)
 * When returned it means actual entry (usually) entered by the user, or null if user cancelled/escaped/etc.
 * @param hidden only means it should be hidden to the user when he enters it (passed both ways).
 * So usually hidden means this entry represents password. Warning: entry/password is not encrypted here!
 */
@ExperimentalApi // Even more experimental stuff. Not sure if I really want to complicate these "conventions" that much.
@Deprecated("Use XD Model")
data class UEntry(val entry: String? = null, val hidden: Boolean = false) : USubmitItem

/**
 * A hint from worker to supervisor not to wait too long for user and return the same [UTimeout] object back after duration.
 * But supervisor can ignore it, obviously, or wait a bit longer when busy etc. (zenity support timeout only in seconds).
 */
@Deprecated("Use XD Model")
data class UTimeout(val duration: Duration = 10.seconds) : USubmitItem

/**
 * A way for worker to report current progress.
 * Usually supervisor will show it to the user continuously and update it,
 * when new [UProgress] (in the same coroutine/Job) is received.
 * Sometimes [max] (or even [min]) can also be updated during the Job,
 * for example when searching through big file tree and discovering new subtrees.
 * @param highlight Just a hint for supervisor to highlight current progress more. Usually with bold font.
 */
@Deprecated("Use XD Model")
data class UProgress(
  val pos: Float? = null,
  val min: Float = 0f,
  val max: Float = 1f,
  val details: String? = null,
  val highlight: Boolean = false,
) : USubmitItem

val UProgress.fraction: Float? get() = pos?.let { lerpInv(min, max, it) }

@Deprecated("Use XD Model")
@ExperimentalApi // even more experimental stuff than above conventions based on USubmitItem
data class USubmitItems(
  val issue: UIssue? = null,
  val progress: UProgress? = null,
  val timeout: UTimeout? = null,
  val entry: UEntry? = null,
  val tasks: List<UTask> = emptyList(),
) {
  fun mergeWith(that: USubmitItems, failOnDuplicates: Boolean = true): USubmitItems = USubmitItems(
      issue = oneOrNull(that.issue, issue, failOnDuplicates = failOnDuplicates),
      progress = oneOrNull(that.progress, progress, failOnDuplicates = failOnDuplicates),
      timeout = oneOrNull(that.timeout, timeout, failOnDuplicates = failOnDuplicates),
      entry = oneOrNull(that.entry, entry, failOnDuplicates = failOnDuplicates),
      tasks = run {
        if (failOnDuplicates) chk(that.tasks.all { it !in this.tasks })
        this.tasks + that.tasks
      }
  )

  private inline fun <reified T> oneOrNull(vararg objs: T?, failOnDuplicates: Boolean = true): T? {
    val nn = objs.filterNotNull()
    if (failOnDuplicates) nn.chkSize(max = 1)
    else if (nn.size > 1) println("Ignoring ${T::class} duplicates")
    return nn.firstOrNull()
  }
}

@Deprecated("Use XD Model")
@ExperimentalApi
fun Any?.getAllUSubmitItems(
  failOnUnexpected: Boolean = true,
  failOnDuplicates: Boolean = true,
): USubmitItems = when (this) {
  is USubmitItems -> this
  is UIssue -> USubmitItems(this)
  is UProgress -> USubmitItems(progress = this)
  is UTimeout -> USubmitItems(timeout = this)
  is UEntry -> USubmitItems(entry = this)
  is UTask -> USubmitItems(tasks = listOf(this))
  is Collection<*> -> fold(USubmitItems()) { acc, next -> acc.mergeWith(next.getAllUSubmitItems(), failOnDuplicates) }
  else ->
    if (failOnUnexpected) bad { "Unexpected usubmit data: $this" }
    else USubmitItems().also { println("Ignoring unexpected usubmit data: $this") }
}

@Deprecated("Use XD Model")
@ExperimentalApi
suspend operator fun USubmit.invoke(vararg items: USubmitItem?) = invoke(items.toList())


@Deprecated("Use XD Model")
@ExperimentalApi fun USubmitItems.chkItems(maxTasks: Int = 16) = apply {
  with (tasks) {
    chkSize(max = maxTasks) { "Too many tasks to select from: $size > $maxTasks" }
    count { it.isAccepting }.chkIn(max = 1)
    count { it.isDeclining }.chkIn(max = 1)
  }
}

@Deprecated("Use XD Model")
val UTask.isAccepting: Boolean get() =
  name.lowercase() in setOf("accept", "yes", "yeah", "confirm", "ok", "fine", "enter", "go", "start", "begin", "select")

@Deprecated("Use XD Model")
val UTask.isDeclining: Boolean get() =
  name.lowercase() in setOf("decline", "no", "nope", "cancel", "abort", "esc", "escape", "nah", "stop", "end")

@Deprecated("Use XD Model")
val UTask.isCustom: Boolean get() = !isAccepting && !isDeclining

@Deprecated("Use XD Model")
@OptIn(ExperimentalApi::class)
suspend fun USubmit.askIf(question: String, labelYes: String = "Yes", labelNo: String = "No"): Boolean {
  val taskYes = UTask(labelYes).reqThis { isAccepting }
  val taskNo = UTask(labelNo).reqThis { isDeclining }
  return this(UIssue(question, UIssueType.Question), taskYes, taskNo) == taskYes
  // BTW it's fine if we get another instance of task with matching label (we use "==" on data classes)
}

@Deprecated("Use XD Model")
@OptIn(ExperimentalApi::class)
suspend fun USubmit.askForOneOf(question: String, vararg answers: String): String? {
  val tasks = answers.map(::UTask).toTypedArray()
  return (this(UIssue(question, UIssueType.Question), *tasks) as? UTask)?.name
}

@Deprecated("Use XD Model")
@OptIn(ExperimentalApi::class)
suspend fun USubmit.askForEntry(question: String, suggested: String? = null, hidden: Boolean = false): String? {
  return (this(UIssue(question, UIssueType.Question), UEntry(suggested, hidden)) as? UEntry)?.entry
}
