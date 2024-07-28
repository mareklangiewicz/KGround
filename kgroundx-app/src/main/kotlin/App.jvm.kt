package pl.mareklangiewicz.kground

import kotlinx.coroutines.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.interactive.*

@OptIn(DelicateApi::class, NotPortableApi::class, ExperimentalApi::class)
fun main(args: Array<String>) = runBlocking { mainCodeExperiments(args) }

// TODO NOW: update kommandline (refactor kommandjupyter to jvm lib); release it (with improved mainCodeExperiments) and test changes here

