package pl.mareklangiewicz.kground.io.usys

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import okio.FileSystem
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kground.io.DefaultDispatcherForIO
import pl.mareklangiewicz.kground.io.DefaultOkioFileSystemOrErr
import pl.mareklangiewicz.uctx.UCtx


open class USys(
  val fs: FileSystem = DefaultOkioFileSystemOrErr,
  val dispatcher: CoroutineDispatcher = DefaultDispatcherForIO,
) : UCtx {
  companion object Key : CoroutineContext.Key<USys>
  override val key: CoroutineContext.Key<*> get() = Key

  // TODO: move some sys/platform related flags from kommandline/CLI to here
}
suspend inline fun <reified T: USys> implictx(): T =
  coroutineContext[USys] as? T ?: bad { "No ${T::class.simpleName} provided in coroutine context." }
