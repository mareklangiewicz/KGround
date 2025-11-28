package pl.mareklangiewicz.templateraw

import androidx.compose.ui.window.*

fun main() {
  helloAllTogetherForFullCli("Full MPP App")

  mainComposeApp()
    // BTW: comment this out to pretend it's just cli app, but it's better to have separate small jvm module,
    // for pure cli app without compose if needed (see: template-full-jvm-cli-app)
}

fun mainComposeApp() = application {
  Window(onCloseRequest = ::exitApplication, title = "Template Full App") {
    TemplateRawTheme(
      darkTheme = true,
      // default isSystemInDarkTheme doesn't work for me yet
      // (there was some issue about linux support)
    ) {
      HelloComposableRaw("JVM Desktop")
    }
  }
}
