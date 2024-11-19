package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

actual fun <T> runBlockingOrErr(block: suspend CoroutineScope.() -> T): T = runBlocking(block = block)
