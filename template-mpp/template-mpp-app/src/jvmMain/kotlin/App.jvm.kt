package pl.mareklangiewicz.hello

import androidx.compose.ui.window.*

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Template MPP App") {
        HelloComposable("JVM Desktop")
    }
}
