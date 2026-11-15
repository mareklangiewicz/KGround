package pl.mareklangiewicz.templatefull

import android.util.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// Keep greeting construction usable in host tests; Android logging belongs at the UI boundary.
actual fun helloPlatform() = "Hello Andro World! (kotlin: ${KotlinVersion.CURRENT})"

// The android app's UI lives here, not in :template-full-andro-app: since AGP 9 an android
// application module cannot also apply the KMP plugin, so the shared composable is reachable
// only from this lib. The app module just calls this.
fun ComponentActivity.setMyHelloContent() {
  Log.i("hello", helloPlatform())
  setContent {
    HelloComposableFull("Android")
  }
}
