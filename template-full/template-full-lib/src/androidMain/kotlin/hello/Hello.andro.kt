package pl.mareklangiewicz.templatefull

import android.util.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

actual fun helloPlatform() = "Hello Andro World! (kotlin: ${KotlinVersion.CURRENT})".also { Log.i("hello", it) }

// The android app's UI lives here, not in :template-full-andro-app: since AGP 9 an android
// application module cannot also apply the KMP plugin, so the shared composable is reachable
// only from this lib. The app module just calls this.
fun ComponentActivity.setMyHelloContent() {
  setContent {
    HelloComposableFull("Android")
  }
}
