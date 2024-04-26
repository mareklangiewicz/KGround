package pl.mareklangiewicz.kground.io

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kground.plusIfNN
import pl.mareklangiewicz.uctx.uctx


// See: https://publicobject.com/2023/04/16/read-a-project-file-in-a-kotlin-multiplatform-test/
@NotPortableApi("TODO: test on different platforms. Browser?")
expect fun getEnv(name: String): String?


@NotPortableApi("Currently on JS it will use Dispatchers.Default")
expect fun getDefaultDispatcherIO(): CoroutineDispatcher

@NotPortableApi("Currently on JS it will fail.")
expect fun getDefaultFS(): UFileSys

/** The working directory with which the current process was started. */
fun UFileSys.getDefaultCWD() = UCWD(canonicalize(".".toPath()))


/** Setting some param explicitly to null means we don't add any (even default) to context. */
@OptIn(NotPortableApi::class)
suspend inline fun <R> uctxIO(
  context: CoroutineContext = EmptyCoroutineContext,
  name: String? = null,
  dispatcher: CoroutineDispatcher? = getDefaultDispatcherIO(),
  fs: UFileSys? = getDefaultFS(),
  cwd: UCWD? = fs?.getDefaultCWD(),
  noinline block: suspend CoroutineScope.() -> R,
) = uctx(context plusIfNN dispatcher plusIfNN fs plusIfNN cwd, name = name, block)
