package com.example.template_raw_lib

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyDeviceTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pl.mareklangiewicz.templateraw.test", appContext.packageName)
    }

    @Test
    fun checkDeviceInfo() {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val sdkInt = Build.VERSION.SDK_INT

        println("Device Info: Manufacturer=$manufacturer, Model=$model, SDK=$sdkInt")

        // This will cause the test to be SKIPPED if the manufacturer is not "Googlle"
        // and the message will be visible in the test results.
        assumeTrue(
            "SKIPPED: Unexpected device manufacturer: '$manufacturer'. Expected 'Googlle'.",
            manufacturer == "Googlle"
        )

        // The rest of the test will only execute if the assumption passes.
        assertTrue("Manufacturer should not be empty", manufacturer.isNotEmpty())
        assertTrue("Model should not be empty", model.isNotEmpty())
        assertTrue("SDK version should be a positive number", sdkInt > 0)
    }
}
