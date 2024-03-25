
package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.XClipSelection.Clipboard
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.ifInteractiveCodeEnabled
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.samples.tryInteractivelyAnything
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.ulog
import pl.mareklangiewicz.kommand.xclipOut
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ulog.i
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
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.jupyter.PlaygroundKt#play
 * So way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandjupyter:run task is set up to run the main fun here.
 */
fun main(args: Array<String>) = runBlocking {
    when {
        args.size == 2 && args[0] == "try-code" -> tryInteractivelySomethingRef(args[1])
        args.size == 2 && args[0] == "get-user-flag" -> ulog.i(getUserFlagFullStr(SYS, args[1]))
        args.size == 3 && args[0] == "set-user-flag" -> setUserFlag(SYS, args[1], args[2].toBoolean())
        else -> bad { "Incorrect args. See Main.kt:main" }
    }
}

@OptIn(DelicateApi::class)
private suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
    ulog.d("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
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
    ulog.d("tryInteractivelySomethingRef(\"$reference\")")
    val ref = if (reference == "xclip")
        xclipOut(Clipboard).ax(SYS).singleOrNull() ?: bad { "Clipboard has to have code reference in single line." }
    else reference
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

