package com.example.template_raw_lib

import android.os.Build
import android.os.Bundle
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

        // This will still be printed to logcat
        println("Device Info: Manufacturer=$manufacturer, Model=$model, SDK=$sdkInt")

        // This will report the data to the test runner on the host
        val deviceInfo = Bundle()
        deviceInfo.putString("manufacturer", manufacturer)
        deviceInfo.putString("model", model)
        deviceInfo.putInt("sdkInt", sdkInt)
        InstrumentationRegistry.getInstrumentation().sendStatus(0, deviceInfo)

        assertTrue("Manufacturer should not be empty", manufacturer.isNotEmpty())
        assertTrue("Model should not be empty", model.isNotEmpty())
        assertTrue("SDK version should be a positive number", sdkInt > 0)
    }
}
