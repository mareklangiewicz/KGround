package pl.mareklangiewicz.kommand.samples

import org.junit.jupiter.api.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.ureflect.*
import pl.mareklangiewicz.uspek.*


// TODO_someday: all my tests should be suspendable, and log should be injected to context and received by implictx
private var log: ULog = UHackySharedFlowLog()

class SamplesLinesTests {

  @TestFactory
  fun uspekSamplesLines() = uspekTestFactory {
    testSamplesObject(Samples)
  }
}

@OptIn(DelicateApi::class, NotPortableApi::class)
fun testSamplesObject(obj: Any, depthLimit: Int = 30) {
  val objSimpleName = obj::class.simpleName ?: bad { "Unexpected samples obj without name" }
  if (depthLimit < 1) {
    log.d("depthLimit < 1. Ignoring obj $objSimpleName"); return
  }
  chk(objSimpleName.endsWith("Samples")) { "Unexpected obj name in samples: $objSimpleName" }
  obj::class.objectInstance.chkNN { "Unexpected obj in samples which is NOT singleton: $objSimpleName" }
  chk(obj::class.isData) { "Unexpected obj in samples which is NOT data object: $objSimpleName" }
  val props = obj.getReflectNamedPropsValues()
  for ((name, prop) in props) when {
    prop is Sample -> "On sample $name" o { testSample(prop) }
    prop is TypedSample<*, *, *, *> -> "On typed sample $name" o { testTypedSample(prop) }
    prop is ReducedSample<*> -> "On reduced sample $name" o { testReducedSample(prop) }
    prop is ReducedScript<*> -> "Ignoring reduced script $name" o {}
    prop is Kommand -> "Ignoring kommand $name" o {} // usually better to wrap it: some_kommand s exp_line/null
    prop == null -> bad { "prop is null! name: $name" }
    else -> "On $name" o { testSamplesObject(prop, depthLimit - 1) }
  }
  obj.getReflectSomeMemberFunctions().forEach { log.d("Ignoring fun ${it.name}") }
}

@OptIn(DelicateApi::class)
fun testSample(sample: Sample) = "check kommand lineRaw" o {
  val lineRaw = sample.kommand.lineRaw()
  if (sample.expectedLineRaw == null) log.d("Expected lineRaw not provided.")
  else lineRaw chkEq sample.expectedLineRaw
  log.d("Actual kommand lineRaw is: $lineRaw")
}

@OptIn(DelicateApi::class)
fun testTypedSample(sample: TypedSample<*, *, *, *>) = "check typed kommand lineRaw" o {
  val lineRaw = sample.typedKommand.kommand.lineRaw()
  if (sample.expectedLineRaw == null) log.d("Expected lineRaw not provided.")
  else lineRaw chkEq sample.expectedLineRaw
  log.d("Actual typed kommand lineRaw is: $lineRaw")
}

@OptIn(DelicateApi::class)
fun testReducedSample(sample: ReducedSample<*>) = "check reduced kommand lineRaw" o {
  val lineRaw = sample.reducedKommand.lineRawOrNull() ?: bad { "Unknown ReducedKommand implementation" }
  if (sample.expectedLineRaw == null) log.d("Expected lineRaw not provided.")
  else lineRaw chkEq sample.expectedLineRaw
  log.d("Actual reduced kommand lineRaw is: $lineRaw")
}
