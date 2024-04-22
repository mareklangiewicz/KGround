@file:Suppress("unused")

package pl.mareklangiewicz.usubmit

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import pl.mareklangiewicz.bad.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import pl.mareklangiewicz.annotations.ExperimentalApi
import pl.mareklangiewicz.uctx.UCtx
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.w
import pl.mareklangiewicz.umath.lerpInv

fun interface USubmit : UCtx {
  suspend operator fun invoke(data: Any?): Any?
  companion object Key : CoroutineContext.Key<USubmit>
  override val key: CoroutineContext.Key<*> get() = Key
}
suspend inline fun <reified T: USubmit> implictx(): T =
  coroutineContext[USubmit] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }

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
 * Code below is only proposed way to communicate between "worker"/"submitter" and "manager"/"user"/"decision maker".
 * But basic general "contract" is always: Anything "Any?" can be potentially submitted and "Any?" can be returned.
 * But often (in specific project) both sides at runtime will agree what is accepted and what means what.
 * Maybe acceptable practice will be for client/worker code to first chk(usubmit is FamiliarManagerInterface)...
 * But I want typical client code to be developed independently of potential manager setup by the user.
 */
interface USubmitItem

/**
 * Let's call the entity that calls [USubmit.invoke] the worker, and the one that reacts: the manager.
 * The worker can provide a list of [UTask] as his current "abilities" as [USubmit.invoke] parameter,
 * so the manager can choose one and return it from [USubmit.invoke] and the worker will actually do the chosen task.
 * Manager always can pause the worker by suspending [USubmit.invoke] call for long time;
 * Manager always can fire the worker by throwing [kotlinx.coroutines.CancellationException] from [USubmit.invoke].
 * Also [UIssue] can be included by worker to show sth to the user (but normally [UTask] is returned back).
 */
data class UTask(val name: String) : USubmitItem

/**
 * The worker can provide [UIssue] to [USubmit.invoke] to inform the manager/user about some issue.
 * These issues are meant to be "recoverable", so worker should be able to continue
 * if the manager returns normally from [USubmit.invoke].
 * More critical worker issues/errors should be thrown as exceptions (as usual).
 * @param id identifies issue, so Manager+user can decide to sth like "Yes for all":
 * automatically react the same way on future issues with the same [id]
 * for next... 100 same issues, or next... 5 minutes, etc.
 * I consider it bad to allow user to "Yes for all" for all the same issues FOREVER.
 * The "Yes for all" should be easily monitored (how many times it's automatically answered),
 * and easily turned off by user (when he sees manager is accepting too much)
 * BTW: for behavior like "Yes to all" to work, not only issue with the same id have to be provided
 * again, but also same [UTask] have to be provided, representing some specific answer, f.e. "Yes".
 * TODO_someday: think about serializable ids (and serializable everything here)
 *   so we can have remote managers easily (with kotlinx.serialization)
 *   (cancellation/exceptions won't be easily serializable, but we can probably do it similarly to rsocket-kotlin)
 */
data class UIssue(val name: String, val type: UIssueType, val id: Any? = null) : USubmitItem
enum class UIssueType { Info, Warning, Error, Question }

/**
 * A hint from worker to manager not to wait to long for user and return the same [UTimeout] object back after duration.
 * But manager can ignore it, obviously, or wait a bit longer when busy etc. (zenity support timeout only in seconds).
 */
data class UTimeout(val duration: Duration = 10.seconds) : USubmitItem

/**
 * A way for worker to report current progress.
 * Usually manager will show it to the user continuously and update it,
 * when new [UProgress] (in the same coroutine/Job) is received.
 * Sometimes [max] (or even [min]) can also be updated during the Job,
 * for example when searching through big file tree and discovering new subtrees.
 * @param highlight Just a hint for manager to highlight current progress more. Usually with bold font.
 */
data class UProgress(
  val pos: Float? = null,
  val min: Float = 0f,
  val max: Float = 1f,
  val details: String? = null,
  val highlight: Boolean = false,
) : USubmitItem

val UProgress.fraction: Float? get() = pos?.let { lerpInv(min, max, it) }

@ExperimentalApi // even more experimental stuff than above conventions based on USubmitItem
data class USubmitItems(
  val issue: UIssue? = null,
  val progress: UProgress? = null,
  val timeout: UTimeout? = null,
  val tasks: List<UTask> = emptyList(),
) {
  fun mergeWith(that: USubmitItems, failOnDuplicates: Boolean = true): USubmitItems = USubmitItems(
      issue = oneOrNull(that.issue, issue, failOnDuplicates = failOnDuplicates),
      progress = oneOrNull(that.progress, progress, failOnDuplicates = failOnDuplicates),
      timeout = oneOrNull(that.timeout, timeout, failOnDuplicates = failOnDuplicates),
      tasks = run {
        if (failOnDuplicates) chk(that.tasks.all { it !in this.tasks })
        this.tasks + that.tasks
      }
  )

  private inline fun <reified T> oneOrNull(vararg objs: T?, failOnDuplicates: Boolean = true): T? {
    val nn = objs.filterNotNull()
    if (failOnDuplicates) nn.chkSize(max = 1)
    else if (nn.size > 1) ulog.w("Ignoring ${T::class} duplicates")
    return nn.firstOrNull()
  }
}

@ExperimentalApi
fun Any?.getAllUSubmitItems(
  failOnUnexpected: Boolean = true,
  failOnDuplicates: Boolean = true,
): USubmitItems = when (this) {
  is USubmitItems -> this
  is UIssue -> USubmitItems(this)
  is UProgress -> USubmitItems(progress = this)
  is UTimeout -> USubmitItems(timeout = this)
  is UTask -> USubmitItems(tasks = listOf(this))
  is Collection<*> -> fold(USubmitItems()) { acc, next -> acc.mergeWith(next.getAllUSubmitItems(), failOnDuplicates) }
  else ->
    if (failOnUnexpected) bad { "Unexpected usubmit data: $this" }
    else USubmitItems().also { ulog.w("Ignoring unexpected usubmit data: $this") }
}

@ExperimentalApi
suspend operator fun USubmit.invoke(vararg items: USubmitItem?) = invoke(items.toList())


@ExperimentalApi fun USubmitItems.chkItems(maxTasks: Int = 16) = apply {
  with (tasks) {
    chkSize(max = maxTasks) { "Too many tasks to select from: $size > $maxTasks" }
    count { it.isAccepting }.chkIn(max = 1)
    count { it.isDeclining }.chkIn(max = 1)
  }
}

val UTask.isAccepting: Boolean get() =
  name.lowercase() in setOf("accept", "yes", "yeah", "confirm", "ok", "fine", "enter", "go", "start", "begin", "select")

val UTask.isDeclining: Boolean get() =
  name.lowercase() in setOf("decline", "no", "nope", "cancel", "abort", "esc", "escape", "nah", "stop", "end")

val UTask.isCustom: Boolean get() = !isAccepting && !isDeclining

@OptIn(ExperimentalApi::class)
suspend fun USubmit.askIf(question: String, labelYes: String = "Yes", labelNo: String = "No"): Boolean {
  val taskYes = UTask(labelYes).reqThis { isAccepting }
  val taskNo = UTask(labelNo).reqThis { isDeclining }
  return this(UIssue(question, UIssueType.Question), taskYes, taskNo) == taskYes
  // BTW it's fine if we get another instance of task with matching label (we use "==" on data classes)
}

@OptIn(ExperimentalApi::class)
suspend fun USubmit.askForOneOf(question: String, vararg answers: String): String? {
  val tasks = answers.map(::UTask).toTypedArray()
  return (this(UIssue(question, UIssueType.Question), *tasks) as? UTask)?.name
}
