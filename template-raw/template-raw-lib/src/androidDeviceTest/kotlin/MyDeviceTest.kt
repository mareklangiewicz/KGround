package com.example.template_raw_lib

import android.os.*
import android.util.*
import androidx.test.ext.junit.runners.*
import androidx.test.platform.app.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.*

/**
 * The "tee" like util, to log any value using Log.i with custom "teeI" tag
 * @return The same provided value, so it can be chained/injected in expressions as debug tool.
 * BTW filter logcat by tag to quickly find these logs.
 */
private val <T> T.teeI get(): T = apply { Log.i("teeI", this.toString()) }

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
    "Manufacturer: ${Build.MANUFACTURER}".teeI
    "Model: ${Build.MODEL}".teeI
    "Brand: ${Build.BRAND}".teeI
    "Device: ${Build.DEVICE}".teeI
    "Product: ${Build.PRODUCT}".teeI
    "Hardware: ${Build.HARDWARE}".teeI
    "Board: ${Build.BOARD}".teeI
    "SDK Level: ${Build.VERSION.SDK_INT}".teeI
    "Release Version: ${Build.VERSION.RELEASE}".teeI
    "Development Codename: ${Build.VERSION.CODENAME}".teeI
    Build.SUPPORTED_ABIS.forEachIndexed { index, abi ->
      "ABI ${index + 1}: $abi".teeI
    }
  }
}
