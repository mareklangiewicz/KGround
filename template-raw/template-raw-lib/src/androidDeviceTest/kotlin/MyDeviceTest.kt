package com.example.template_raw_lib

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "DeviceTestInfo"

@RunWith(AndroidJUnit4::class)
class MyDeviceTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pl.mareklangiewicz.templateraw.test", appContext.packageName)
    }

    @Test
    fun logDetailedDeviceInfo() {
        // Example of logging detailed device information for easy filtering in Logcat.
        Log.i(TAG, "Manufacturer: ${Build.MANUFACTURER}")
        Log.i(TAG, "Model: ${Build.MODEL}")
        Log.i(TAG, "Brand: ${Build.BRAND}")
        Log.i(TAG, "Device: ${Build.DEVICE}")
        Log.i(TAG, "Product: ${Build.PRODUCT}")
        Log.i(TAG, "Hardware: ${Build.HARDWARE}")
        Log.i(TAG, "Board: ${Build.BOARD}")
        Log.i(TAG, "SDK Level: ${Build.VERSION.SDK_INT}")
        Log.i(TAG, "Release Version: ${Build.VERSION.RELEASE}")
        Log.i(TAG, "Development Codename: ${Build.VERSION.CODENAME}")
        Build.SUPPORTED_ABIS.forEachIndexed { index, abi ->
            Log.i(TAG, "ABI ${index + 1}: $abi")
        }
    }
}
