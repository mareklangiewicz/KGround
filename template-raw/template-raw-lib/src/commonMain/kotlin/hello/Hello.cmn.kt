package pl.mareklangiewicz.templateraw

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier as Mod
import androidx.compose.ui.unit.*
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
        h1 { +"Some H1 in Template Full App" }
        p { +"Some paragraph" }
        p { +"Some other paragraph" }
        p { +helloCommon() }
        p { +helloPlatform() }
      }
    }
  }
    .also { println(it) }

fun helloAllTogetherForFullCli(hint: String) {
  println("helloAllTogetherForFullCli begin ($hint)")
  helloCommon()
  helloPlatform()
  helloSomeHtml()
  println("helloAllTogetherForFullCli end ($hint)")
}


/** Wrapped with full screen surface as background (so it obeys the theme color) */
@Composable fun HelloComposableFull(name: String) =
  Surface(Mod.fillMaxSize()) { HelloComposable(name) }

@Composable fun HelloComposable(name: String) {
  Column(Mod.padding(16.dp)) {
    var rotation by remember { mutableStateOf(80f) }
    Text(text = "Hello $name! rotation:$rotation")
    RotatedBox(rotation)
    Button(onClick = { rotation += 5f }) {
      Text("Rotate more")
    }
    Text(helloEveryOneWithSomeHtml())
  }
}
