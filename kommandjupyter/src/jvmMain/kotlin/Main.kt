
package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.XClipSelection.Clipboard
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.ifInteractiveCodeEnabled
import pl.mareklangiewicz.kommand.isUserFlagEnabled
import pl.mareklangiewicz.kommand.samples.tryInteractivelyAnything
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.xclipOut
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.ure.MAX
import pl.mareklangiewicz.ure.ch
import pl.mareklangiewicz.ure.chWord
import pl.mareklangiewicz.ure.chWordFirst
import pl.mareklangiewicz.ure.chWordOrDot
import pl.mareklangiewicz.ure.matchEntireOrThrow
import pl.mareklangiewicz.ure.namedValues
import pl.mareklangiewicz.ure.ure
import pl.mareklangiewicz.ure.ureIdent
import pl.mareklangiewicz.ure.withName
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend

/**
 * TODO NOW: update description
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
        null -> println("Provide something")
        "something" -> when {
            args.size == 1 -> bad { "something requires something reference or \"xclip\" keyword" }
            args.size > 2 -> bad { "only one something reference allowed" }
            else -> tryInteractivelySomethingRef(args[1])
        }
        "code.interactive" -> when {
            args.size != 2 -> bad { "Error. format is: code.interactive enable/disable/print" }
            args[1] == "print" -> println(
                "code.interactive is " + if(isUserFlagEnabled(SYS, "code.interactive")) "enabled" else "not enabled"
            )
            args[1] == "enable" -> setUserFlag(SYS, "code.interactive", true)
            args[1] == "disable" -> setUserFlag(SYS, "code.interactive", false)
        }
    }
}

@OptIn(DelicateApi::class)
private suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
    println("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
    val call = prepareCallFor(className, memberName)
    // Note: prepareCallFor fails early if member not found,
    // before we start to interact with the user,
    // but the code is never called without confirmation.
    ifInteractiveCodeEnabled {
        zenityAskIf("Call $className#$memberName ?").ax(SYS) || return
        val member: Any? = call()
        // Note: call() will either already "do the thing" (when the member is just a fun to call)
        //  or it will only get the property (like ReducedScript/Sample etc.) which will be tried (or not) later.
        member.tryInteractivelyAnything()
    }
}

private fun prepareCallFor(className: String, memberName: String): suspend () -> Any? {
    val kClass: KClass<*> = Class.forName(className).kotlin
    val objectOrNull: Any? = kClass.objectInstance
    val kMember: KCallable<*>? = kClass.members.firstOrNull { it.name == memberName }
    when {
        kMember == null -> {
            val jMethod: Method = kClass.java.getDeclaredMethod(memberName)
            return { jMethod.invoke((objectOrNull)) }
        }
        kMember.isSuspend -> return { kMember.callSuspend(objectOrNull) }
        else -> return { kMember.call(objectOrNull) }
    }
}

/**
 * @param reference Either "xclip", or reference in format like from IntelliJ:CopyReference action.
 *   For example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop"
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
private suspend fun tryInteractivelySomethingRef(reference: String = "xclip") {
    println("tryInteractivelySomethingRef(\"$reference\")")
    val ref = if (reference == "xclip") xclipOut(Clipboard).ax(SYS).single() else reference
    val ure = ure {
        +ure("className") {
            +chWordFirst
            1..MAX of chWordOrDot
            +chWord
        }
        +ch('#')
        +ureIdent().withName("methodName")
    }
    val result = ure.matchEntireOrThrow(ref)
    val className by result.namedValues
    val methodName by result.namedValues
    tryInteractivelyClassMember(className!!, methodName!!)
}

