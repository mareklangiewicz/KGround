package pl.mareklangiewicz.kommand.samples

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.kommand.line
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

fun testSamplesObject(obj: Any, maxDepth: Int = 30) {
    if (maxDepth < 1) {
        println("maxDepth < 1. Ignoring obj...")
        return
    }
    val props = obj.getNamedPropsValues()
    for ((name, prop) in props) when (prop) {
        is Sample -> "On sample $name" o { testSample(prop) }
        null -> error("prop is null! name: $name")
        else -> "On $name" o { testSamplesObject(prop, maxDepth - 1) }
    }
}

fun testSample(sample: Sample) = "check kommand.line" o {
    val line = sample.kommand.line()
    if (sample.expectedLine == null) println("Expected line not provided.")
    else line eq sample.expectedLine
    println("Actual kommand.line is: $line")
}

// Copied and pasted from Kokpit (for now)
// TODO_someday: micro open source library for common reflection based browsing (multiplatform?)
@Suppress("UNCHECKED_CAST")
private fun <T : Any> T.getNamedPropsValues(): List<Pair<String, Any?>> {
    return (this::class as KClass<T>).declaredMemberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { it.getter.isAccessible = true; it.name to it(this) }
}
