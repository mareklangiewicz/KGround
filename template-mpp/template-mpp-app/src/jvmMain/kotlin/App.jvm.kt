package pl.mareklangiewicz.templatempp

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
    Window(onCloseRequest = ::exitApplication, title = "Template MPP App") {
        HelloComposable("JVM Desktop")
    }
}
