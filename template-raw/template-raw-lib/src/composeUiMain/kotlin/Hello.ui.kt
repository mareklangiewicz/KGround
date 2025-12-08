package pl.mareklangiewicz.templateraw

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier as Mod
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*

/** Wrapped with full screen surface as background (so it obeys the theme color) */
@Composable fun HelloComposableRaw(name: String) =
  Surface(Mod.fillMaxSize()) { HelloComposable(name) }

@Composable fun HelloComposable(name: String) {
  Column(Mod.padding(16.dp)) {
    var rotation by remember { mutableFloatStateOf(80f) }
    Text(text = "Hello $name! rotation:$rotation")
    RotatedBox(rotation)
    Button(onClick = { rotation += 5f }) {
      Text("Rotate more")
    }
    Text(helloEveryOneWithSomeHtml())
  }
}

@Composable
fun RotatedBox(degrees: Float = 10f) {
  Box(
    Mod
      .padding(8.dp)
      .border(1.dp, Color.Red)
      .padding(32.dp)
      .rotate(degrees)
      .border(1.dp, Color.Blue)
      .size(200.dp, 200.dp)
      .padding(8.dp),
  ) {
    Text("hello rotated text")
  }
}
