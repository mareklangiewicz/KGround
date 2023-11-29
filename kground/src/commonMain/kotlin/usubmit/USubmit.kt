package pl.mareklangiewicz.kground.usubmit

import pl.mareklangiewicz.kground.*
import kotlin.jvm.*

fun interface USubmit {
    operator fun invoke(data: Any?): Any?
}

interface WithUSubmit { val usubmit: USubmit }

/**
 * Let's call the entity that calls [USubmit.invoke] the worker, and the one that reacts: the manager.
 * The worker can provide a list of [UTask] as his current "abilities" as [USubmit.invoke] parameter,
 * so the manager can choose one and return it from [USubmit.invoke] and the worker will actually do the chosen task.
 * It's normal for a manager to save tasks for later and show it to the user,
 * while letting the worker move on with his main job.
 * Then (if user selects some task in between submits), at the next submit, the manager should check again,
 * if the worker still has "ability" to do this task, and only if so:
 * manager can return the selected task from [USubmit.invoke], so worker can do it now.
 * Manager always can pause the worker by suspending submit call for long time;
 * Manager always can fire the worker by throwing [kotlinx.coroutines.CancellationException] from submit call.
 */
@JvmInline
value class UTask(val name: String)

/**
 * The worker can provide [UIssue] as [USubmit.invoke] parameter to inform the manager about some issue.
 * These issues are meant to be usually "recoverable", so worker should be able to continue if the manager
 * returns some value normally from [USubmit.invoke].
 * More critical worker issues/errors should be thrown as exceptions (as usual).
 */
@JvmInline
value class UIssue(val name: String)

class USubmitNotSupportedErr: USubmit {
    override fun invoke(data: Any?): Nothing = bad { "USubmit is not supported in this context." }
}