package pl.mareklangiewicz.templatempp

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TemplateMppAndroTheme { HelloComposableFull("Android") }
    }
  }
}


// @Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TemplateMppAndroTheme { HelloComposableFull("Android") }
}
