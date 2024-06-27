package pl.mareklangiewicz.templatefull

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TemplateFullAndroTheme { HelloComposableFull("Android") }
    }
  }
}


// @Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TemplateFullAndroTheme { HelloComposableFull("Android") }
}
