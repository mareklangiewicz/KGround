package pl.mareklangiewicz.templatempp

import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier as Mod
import androidx.compose.ui.graphics.*
import androidx.compose.ui.window.*

fun main() {
    mainCli()
    mainComposeApp() // comment this out to pretend it's just cli app
}

fun mainCli() {
    println("mainCli begin")
    helloCommon()
    helloPlatform()
    helloSomeHtml()
    println("mainCli end")
}

fun mainComposeApp() = application {
    Window(onCloseRequest = ::exitApplication, title = "Template MPP App") {
        TemplateMppJvmTheme(
            darkTheme = true,
            // default isSystemInDarkTheme doesn't work for me yet
            // (there was some issue about linux support)
        ) {
            Surface(modifier = Mod.fillMaxSize()) {
                HelloComposable("JVM Desktop")
            }
        }
    }
}


@Composable
fun TemplateMppJvmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

// TODO: commonize stuff

private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)

private val Purple40 = Color(0xFF6650a4)
private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFF7D5260)


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

