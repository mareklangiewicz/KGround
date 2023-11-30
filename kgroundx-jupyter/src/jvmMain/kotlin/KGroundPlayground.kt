package pl.mareklangiewicz.kgroundx.jupyter.playground

import kotlinx.coroutines.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*


/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file is to experiment here, with better IDE support, and then C&P working code snippets into notebooks.
 * Do not commit changes in this file. The kgroundx-jupyter:run task is set up to run the main fun here.
 */

fun main() {
    runBlocking {
        playground()
    }
}

@OptIn(DelicateKommandApi::class)
suspend fun playground() {
    println("Let's play with kground and kommand integration...")
    ls { -LsOpt.LongFormat; -LsOpt.All }.x {
        println("out line: $it")
    }
    // TODO: better example stuff that can be done with KGround
    //  (but only some small non invasive code)
}

