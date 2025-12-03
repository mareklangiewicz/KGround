package pl.mareklangiewicz.templateraw.androapp

import android.os.*
import androidx.activity.*
import pl.mareklangiewicz.templateraw.setMyHelloContent

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setMyHelloContent()
  }
}
