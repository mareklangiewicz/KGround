package pl.mareklangiewicz.templatefull.androapp

import android.os.*
import androidx.activity.*
import pl.mareklangiewicz.templatefull.setMyHelloContent

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setMyHelloContent()
  }
}
