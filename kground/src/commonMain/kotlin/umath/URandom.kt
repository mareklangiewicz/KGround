package pl.mareklangiewicz.umath

import kotlin.random.*

infix fun Int.rnd(to: Int) = Random.nextInt(this, to + 1)
infix fun Double.rnd(to: Double) = Random.nextDouble(this, to)

fun Int.near(divisor: Int = 6) = this - this / divisor rnd this + this / divisor
fun Double.near(divisor: Double = 6.0) = this - this / divisor rnd this + this / divisor
fun Int.around(spread: Int = 6) = this + (-spread rnd spread)
fun Double.around(spread: Double = 6.0) = this + (-spread rnd spread)

