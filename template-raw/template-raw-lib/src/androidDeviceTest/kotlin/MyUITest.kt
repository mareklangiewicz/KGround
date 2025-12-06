package com.example.template_raw_lib

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.runner.RunWith
import pl.mareklangiewicz.templateraw.HelloComposableRaw

@RunWith(AndroidJUnit4::class)
class MyUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHelloComposableRaw() {
        composeTestRule.setContent {
            HelloComposableRaw("Android")
        }

        // Check that the greeting text exists and is displayed.
        // Using a substring match to ignore the dynamic rotation value.
        composeTestRule.onNodeWithText("Hello Android!", substring = true).assertExists()
        composeTestRule.onNodeWithText("Hello Android!", substring = true).assertIsDisplayed()
    }
}
