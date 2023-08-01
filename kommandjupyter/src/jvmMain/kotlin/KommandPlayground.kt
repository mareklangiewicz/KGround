package pl.mareklangiewicz.kommand.playground

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.core.*


/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file is to experiment here, with better IDE support, and then C&P working code snippets into notebooks.
 * Do not commit changes in this file. The kommandjupyter:run task is set up to run the main fun here.
 */

fun main() = playground()

fun playground() {
    println("Let's play with kommand integration...")
    ls().x().logEach()
}