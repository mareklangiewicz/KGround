package pl.mareklangiewicz.kommand.samples

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class SamplesTests {

    @TestFactory
    fun uspekSamplesTestFactory() = uspekTestFactory {
        testSamplesObject(Samples)
    }
}

fun testSamplesObject(obj: Any, depthLimit: Int = 30) {
    if (depthLimit < 1) {
        println("depthLimit < 1. Ignoring obj...")
        return
    }
    val props = obj.getNamedPropsValues()
    for ((name, prop) in props) when (prop) {
        is Sample -> "On sample $name" o { testSample(prop) }
        null -> error("prop is null! name: $name")
        else -> "On $name" o { testSamplesObject(prop, depthLimit - 1) }
    }
}

@OptIn(DelicateKommandApi::class)
fun testSample(sample: Sample) = "check kommand.lineRaw" o {
    val lineRaw = sample.kommand.lineRaw()
    if (sample.expectedLineRaw == null) println("Expected lineRaw not provided.")
    else lineRaw eq sample.expectedLineRaw
    println("Actual kommand.lineRaw is: $lineRaw")
}

// Copied and pasted from Kokpit (for now)
// TODO_someday: micro open source library for common reflection based browsing (multiplatform?)
@Suppress("UNCHECKED_CAST")
private fun <T : Any> T.getNamedPropsValues(): List<Pair<String, Any?>> {
    return (this::class as KClass<T>).declaredMemberProperties
        .filter { it.visibility == KVisibility.PUBLIC && it.name != "execs" }
        .map { it.getter.isAccessible = true; it.name to it(this) }
}
