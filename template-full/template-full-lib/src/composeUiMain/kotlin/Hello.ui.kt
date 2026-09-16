package pl.mareklangiewicz.templatefull

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier as Mod
import androidx.compose.ui.unit.*

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
