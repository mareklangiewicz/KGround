package pl.mareklangiewicz.templatemppapp

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.Modifier as Mod
import pl.mareklangiewicz.hello.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemplateMppAndroTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Mod.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloComposable("Android")
                }
            }
        }
    }
}


// @Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TemplateMppAndroTheme {
        HelloComposable("Android")
    }
}