package pl.mareklangiewicz.templateraw

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MyComposeUITest {

    @Test
    fun testHelloComposableRawInCommon() = runComposeUiTest {
        setContent {
            HelloComposableRaw("Compose UI kinda common")
        }

        // Check that the greeting text exists and is displayed.
        // Using a substring match to ignore the dynamic rotation value.
        onNodeWithText("Hello Compose UI", substring = true).assertExists()
        // onNodeWithText("Hello Common!", substring = true).assertIsEnabled()
    }
}
