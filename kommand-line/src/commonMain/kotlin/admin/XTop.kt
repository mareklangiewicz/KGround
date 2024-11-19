@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.kommand

fun top() = kommand("top")

fun htop() = kommand("htop")

fun btop() = kommand("btop")


