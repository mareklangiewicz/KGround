package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*

// temporary hack
@Deprecated("Use suspend fun Kommand.ax(...)")
actual fun Kommand.axb(
  cli: CLI,
  vararg useNamedArgs: Unit,
  dir: String?,
  inContent: String?,
  inLineS: Flow<String>?,
  inFile: String?,
  outFile: String?,
): List<String> = TODO("Remove this functionality")

// also temporary hack
@Deprecated("Use suspend fun ReducedKommand.ax(...)")
actual fun <ReducedOut> ReducedScript<ReducedOut>.axb(cli: CLI, dir: String?): ReducedOut =
  TODO("Remove this functionality")
