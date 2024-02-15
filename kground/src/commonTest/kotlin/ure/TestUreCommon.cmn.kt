@file:OptIn(NotPortableApi::class, DelicateApi::class)

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.uspek.*


fun testUreCommonStuff() {

    "On ureIdent" o {
        val ure = ureIdent()
        ure.toIR().str chkEq "\\b[a-zA-Z]\\w*\\b"

        // TODO NOW: continue
    }

    // TODO continue
}
