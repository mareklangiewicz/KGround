package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kground.*

actual fun <T> runBlockingOrErr(block: suspend CoroutineScope.() -> T): T = runBlocking(block = block)


fun Flow<*>.logEachBlocking() = runBlocking { logEach() }
