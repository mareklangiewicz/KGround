package pl.mareklangiewicz.kommand.samples

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
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
    val objSimpleName = obj::class.simpleName ?: bad { "Unexpected samples obj without name" }
    if (depthLimit < 1) { println("depthLimit < 1. Ignoring obj $objSimpleName"); return }
    chk(objSimpleName.endsWith("Samples")) { "Unexpected obj name in samples: $objSimpleName" }
    obj::class.objectInstance.chkNN { "Unexpected obj in samples which is NOT singleton: $objSimpleName" }
    chk(obj::class.isData) { "Unexpected obj in samples which is NOT data object: $objSimpleName" }
    val props = obj.getNamedPropsValues()
    for ((name, prop) in props) when {
        prop is Sample -> "On sample $name" o { testSample(prop) }
        prop is TypedSample<*, *, *, *> -> "On typed sample $name" o { testTypedSample(prop) }
        prop is ReducedSample<*> -> "On reduced sample $name" o { testReducedSample(prop) }
        prop is ReducedScript<*> -> println("Ignoring reduced script $name")
        prop == null -> bad { "prop is null! name: $name" }
        else -> "On $name" o { testSamplesObject(prop, depthLimit - 1) }
    }
}

@OptIn(DelicateApi::class)
fun testSample(sample: Sample) = "check kommand lineRaw" o {
    val lineRaw = sample.kommand.lineRaw()
    if (sample.expectedLineRaw == null) println("Expected lineRaw not provided.")
    else lineRaw chkEq sample.expectedLineRaw
    println("Actual kommand lineRaw is: $lineRaw")
}

@OptIn(DelicateApi::class)
fun testTypedSample(sample: TypedSample<*, *, *, *>) = "check typed kommand lineRaw" o {
    val lineRaw = sample.typedKommand.kommand.lineRaw()
    if (sample.expectedLineRaw == null) println("Expected lineRaw not provided.")
    else lineRaw chkEq sample.expectedLineRaw
    println("Actual typed kommand lineRaw is: $lineRaw")
}

@OptIn(DelicateApi::class)
fun testReducedSample(sample: ReducedSample<*>) = "check reduced kommand lineRaw" o {
    val lineRaw = sample.reducedKommand.lineRawOrNull() ?: bad { "Unknown ReducedKommand implementation" }
    if (sample.expectedLineRaw == null) println("Expected lineRaw not provided.")
    else lineRaw chkEq sample.expectedLineRaw
    println("Actual reduced kommand lineRaw is: $lineRaw")
}

// Copied and pasted from Kokpit (for now)
// TODO_someday: micro open source library for common reflection based browsing (multiplatform?)
@Suppress("UNCHECKED_CAST")
private fun <T : Any> T.getNamedPropsValues(): List<Pair<String, Any?>> {
    return (this::class as KClass<T>).declaredMemberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { it.getter.isAccessible = true; it.name to it(this) }
}
