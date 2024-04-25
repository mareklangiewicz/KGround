package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import pl.mareklangiewicz.bad.*

actual fun <T> runBlockingOrErr(block: suspend CoroutineScope.() -> T): T = bad { "No runBlocking on JS" }

