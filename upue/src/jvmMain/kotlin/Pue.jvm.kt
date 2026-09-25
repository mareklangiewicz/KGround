package pl.mareklangiewicz.upue

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

// TODO LATER: test it (this is just first fast attempt to implement something simple - it will probably be rewritten)
class ExecutorScheduler(private val ses: ScheduledExecutorService) : IScheduler {

    constructor(threads: Int = Runtime.getRuntime().availableProcessors() * 2) : this(Executors.newScheduledThreadPool(threads))

    override fun schedule(delay: Long, action: (Unit) -> Unit): Pushee<Cancel> {
        val sf = ses.schedule({ action(Unit) }, delay, TimeUnit.MILLISECONDS)
        return Pushee { sf.cancel(true) }
    }

    override val now = Pullee { System.currentTimeMillis() }
}

fun <A, V> Puee<A, V>.sync(): Puee<A, V> = Puee { a: A -> synchronized(this) { this(a) } }
// it synchronizes calls and assures the happens-before relationship between calls.


/**
 * This operator makes sure all items are pushed serially and that there is the happens-before relationship between pushes.
 */
fun <A, C> Pusher<A, C>.lsync(): Pusher<A, C> = lift { it.sync() }
