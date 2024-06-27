package pl.mareklangiewicz.templatefull

import androidx.compose.ui.window.*

fun main() {
  mainCli()
  mainComposeApp() // comment this out to pretend it's just cli app
}

fun mainCli() {
  println("mainCli begin")
  helloCommon()
  helloPlatform()
  helloSomeHtml()
  println("mainCli end")
}

fun mainComposeApp() = application {
  Window(onCloseRequest = ::exitApplication, title = "Template Full App") {
    TemplateFullTheme(
      darkTheme = true,
      // default isSystemInDarkTheme doesn't work for me yet
      // (there was some issue about linux support)
    ) {
      HelloComposableFull("JVM Desktop")
    }
  }
}

