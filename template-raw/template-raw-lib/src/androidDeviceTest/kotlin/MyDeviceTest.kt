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

    companion object {
        private const val DEVICE_INFO_TAG = "DeviceTestInfo"
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pl.mareklangiewicz.templateraw.test", appContext.packageName)
    }

    @Test
    fun logDetailedDeviceInfo() {
        // Example of logging detailed device information for easy filtering in Logcat.
        Log.i(DEVICE_INFO_TAG, "--- Detailed Device Info ---")
        Log.i(DEVICE_INFO_TAG, "Manufacturer: ${Build.MANUFACTURER}")
        Log.i(DEVICE_INFO_TAG, "Model: ${Build.MODEL}")
        Log.i(DEVICE_INFO_TAG, "Brand: ${Build.BRAND}")
        Log.i(DEVICE_INFO_TAG, "Device: ${Build.DEVICE}")
        Log.i(DEVICE_INFO_TAG, "Product: ${Build.PRODUCT}")
        Log.i(DEVICE_INFO_TAG, "Hardware: ${Build.HARDWARE}")
        Log.i(DEVICE_INFO_TAG, "Board: ${Build.BOARD}")

        Log.i(DEVICE_INFO_TAG, "--- Android Version ---")
        Log.i(DEVICE_INFO_TAG, "SDK Level: ${Build.VERSION.SDK_INT}")
        Log.i(DEVICE_INFO_TAG, "Release Version: ${Build.VERSION.RELEASE}")
        Log.i(DEVICE_INFO_TAG, "Development Codename: ${Build.VERSION.CODENAME}")

        Log.i(DEVICE_INFO_TAG, "--- Supported ABIs ---")
        Build.SUPPORTED_ABIS.forEachIndexed { index, abi ->
            Log.i(DEVICE_INFO_TAG, "ABI ${index + 1}: $abi")
        }
        Log.i(DEVICE_INFO_TAG, "--- End of Detailed Device Info ---")


        // Example of a check that logs a warning with the same tag.
        // Using a fake name to ensure it triggers on your device.
        val expectedManufacturer = "Googlle"
        if (Build.MANUFACTURER != expectedManufacturer) {
            Log.w(
                DEVICE_INFO_TAG,
                "Unexpected device manufacturer: '${Build.MANUFACTURER}'. Expected '$expectedManufacturer'."
            )
        }

        // A simple assertion to ensure the test itself always passes.
        assertTrue("This test is for logging and should always pass", true)
    }
}
