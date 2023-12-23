package pl.mareklangiewicz.templatempp

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier as Mod

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemplateMppAndroTheme {
                Surface(modifier = Mod.fillMaxSize()) {
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