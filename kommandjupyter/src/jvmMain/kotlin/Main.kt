
package pl.mareklangiewicz.kommand.jupyter

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file allows invoking code from kommandjupyter/src/jvmMain/kotlin/gitignored/Playground.kt:play(),
 * so that way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandjupyter:run task is set up to run the main fun here.
 * Add "--args play" to intellij run configuration (or to command line),
 * to actually invoke Playground.kt:play() when calling gradle "run" task.
 */
fun main(args: Array<String>) {
    // Require single "play" arg to avoid running experimental playground code accidentally.
    if (args.singleOrNull() != "play")
        println("Provide single arg: 'play', to invoke the Playground.kt:play()")
    else
        Class.forName("pl.mareklangiewicz.kommand.jupyter.PlaygroundKt")
            .getDeclaredMethod("play")
            .invoke(null)
}

