
package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.XClipSelection.Clipboard
import pl.mareklangiewicz.kommand.xclipOut
import pl.mareklangiewicz.kommand.exec
import pl.mareklangiewicz.kommand.ifInteractiveCodeEnabled
import pl.mareklangiewicz.kommand.samples.tryInteractivelyAnything
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.ure.MAX
import pl.mareklangiewicz.ure.ch
import pl.mareklangiewicz.ure.chWordOrDot
import pl.mareklangiewicz.ure.matchEntireOrThrow
import pl.mareklangiewicz.ure.namedValues
import pl.mareklangiewicz.ure.ure
import pl.mareklangiewicz.ure.ureIdent
import pl.mareklangiewicz.ure.ureText
import pl.mareklangiewicz.ure.withName
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file allows invoking code from kommandjupyter/src/jvmMain/kotlin/gitignored/Playground.kt:play(),
 * so that way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandjupyter:run task is set up to run the main fun here.
 * Add "--args play" to intellij run configuration (or to command line),
 * to actually invoke Playground.kt:play() when calling gradle "run" task.
 * UPDATE: Let's also implement another case: running sample given in command line or from xclip
 */
fun main(args: Array<String>) = runBlocking {
    when(args.firstOrNull()) {
        null -> println("Provide single arg: 'play', to invoke the Playground.kt:play()")
        "play" -> when {
            args.size > 1 -> bad { "play doesn't take additional parameters" }
            else -> tryInteractivelyClassMember("pl.mareklangiewicz.kommand.jupyter.PlaygroundKt", "play")
        }
        "sample" -> when {
            args.size == 1 -> bad { "sample requires sample reference or \"xclip\" keyword" }
            args.size > 2 -> bad { "only one sample reference allowed" }
            else -> tryInteractivelySampleRef(args[1])
        }
    }
}

@OptIn(DelicateApi::class)
private suspend fun tryInteractivelyClassMember(className: String, memberName: String) {

    println("tryInteractivelyClassMember(\"$className\", \"$memberName\")")

    val jClass: Class<*> = Class.forName(className)
    val kClass: KClass<*> = jClass.kotlin
    val objectOrNull: Any? = kClass.objectInstance

    val jMethod: Method = jClass.getDeclaredMethod(memberName)
    val getter = { jMethod.invoke(objectOrNull) }

    // Notice: more "kotliny" impl would fail with sthName of generated property getters
    // as represented in jvm (and as copied with intellij action:CopyReference)
    // (like: MyDemoSamples.getBtop for property MyDemoSamples.btop)
    // val kMember: KCallable<*> = kClass.members.first { it.name == memberName }
    // val getter = { kMember.call(objectOrNull) }

    ifInteractiveCodeEnabled {
        zenityAskIf("Try $className#$memberName ?").exec(SYS) || return
        val member: Any? = getter()
        member.tryInteractivelyAnything()
    }
}

/**
 * @param reference Either "xclip", or reference in format like from IntelliJ:CopyReference action.
 *   For example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop"
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
private suspend fun tryInteractivelySampleRef(reference: String = "xclip") {

    println("tryInteractivelySample(\"$reference\")")

    val ref = if (reference == "xclip") xclipOut(Clipboard).exec(SYS).single() else reference
    val ure = ure {
        +ure("className") {
            +ureText("pl.mareklangiewicz.kommand.")
            1..MAX of chWordOrDot
            +ureText("Samples")
        }
        +ch('#')
        +ureIdent().withName("methodName")
    }
    val result = ure.matchEntireOrThrow(ref)
    val className by result.namedValues
    val methodName by result.namedValues
    tryInteractivelyClassMember(className!!, methodName!!)
}

