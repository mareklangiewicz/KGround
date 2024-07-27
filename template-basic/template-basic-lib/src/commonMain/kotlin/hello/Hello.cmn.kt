package pl.mareklangiewicz.templatebasic

import kotlinx.html.*
import kotlinx.html.stream.*


fun helloCommon(): String = "Hello Pure Common World!".also { println(it) }

expect fun helloPlatform(): String

// It's ok that it repeats helloCommon and helloPlatform twice (also in helloSomeHtml)
fun helloEveryOneWithSomeHtml() = helloCommon() + "\n\n" + helloPlatform() + "\n\n" + helloSomeHtml()

fun helloSomeHtml(): String =
  buildString {
    appendHTML().html {
      body {
        h1 { +"Some H1 in Template Basic App" }
        p { +"Some paragraph" }
        p { +"Some other paragraph" }
        p { +helloCommon() }
        p { +helloPlatform() }
      }
    }
  }
    .also { println(it) }


fun helloAllTogetherForBasicCli(hint: String) {
  println("helloAllTogetherForBasicCli begin ($hint)")
  helloCommon()
  helloPlatform()
  helloSomeHtml()
  println("helloAllTogetherForBasicCli end ($hint)")
}
