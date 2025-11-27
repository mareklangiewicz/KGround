package pl.mareklangiewicz.templateraw

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TemplateRawAndroTheme { HelloComposableRaw("Android") }
    }
  }
}


// @Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TemplateRawAndroTheme { HelloComposableRaw("Android") }
}
