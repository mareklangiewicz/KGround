package pl.mareklangiewicz.templatempp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
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
        TemplateMppTheme(
            darkTheme = true,
            // default isSystemInDarkTheme doesn't work for me yet
            // (there was some issue about linux support)
        ) {
            HelloComposableFull("JVM Desktop")
        }
    }
}

