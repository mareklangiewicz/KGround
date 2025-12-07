package pl.mareklangiewicz.templateraw

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CommonUITest {

    @Test
    fun testHelloComposableRawInCommon() = runComposeUiTest {
        setContent {
            HelloComposableRaw("Common UI")
        }

        // Check that the greeting text exists and is displayed.
        // Using a substring match to ignore the dynamic rotation value.
        onNodeWithText("Hello Common UI!", substring = true).assertExists()
        // onNodeWithText("Hello Common!", substring = true).assertIsEnabled()
    }
}
