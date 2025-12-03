package pl.mareklangiewicz.templateraw

import android.util.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

actual fun helloPlatform() = "Hello Andro World! (kotlin: ${KotlinVersion.CURRENT})".also { Log.i("hello", it) }

fun ComponentActivity.setMyHelloContent(
) {
  setContent {
    // TemplateRawAndroTheme { HelloComposableRaw("Android") }
    HelloComposableRaw("Android")
    // Text("hello bla")
  }
}

// }
// // @Preview(showBackground = true)
// // @Composable
// // fun DefaultPreview() {
// //   // TemplateRawAndroTheme { HelloComposableRaw("Android") }
// // }
