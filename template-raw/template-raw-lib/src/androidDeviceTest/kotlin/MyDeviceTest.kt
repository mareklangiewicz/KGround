package com.example.template_raw_lib

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        // This will be printed to logcat's default stream
        println("Device Info: Manufacturer=$manufacturer, Model=$model, SDK=$sdkInt")

        if (manufacturer != "Googlle") {
            // This will appear as a warning in logcat, with a custom tag for easy filtering.
            Log.w("MyDeviceTest", "Unexpected device manufacturer: '$manufacturer'. Expected 'Google'.")
        }

        assertTrue("Manufacturer should not be empty", manufacturer.isNotEmpty())
        assertTrue("Model should not be empty", model.isNotEmpty())
        assertTrue("SDK version should be a positive number", sdkInt > 0)
    }
}
