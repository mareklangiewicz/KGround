package pl.mareklangiewicz.umath

import kotlin.random.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.udata.*

infix fun Int.rnd(to: Int) = Random.nextInt(this, to.reqIn(max = Int.MAX_VALUE-1) + 1)
infix fun Long.rnd(to: Long) = Random.nextLong(this, to.reqIn(max = Long.MAX_VALUE-1) + 1)

infix fun Double.rnd(to: Double): Double = Random.nextDouble(this, to)
infix fun Float.rnd(to: Float): Float = (dbl rnd to.dbl).flt

fun Int.near(divisor: Int = 6) = this - this / divisor rnd this + this / divisor
fun Long.near(divisor: Long = 6L) = this - this / divisor rnd this + this / divisor
fun Double.near(divisor: Double = 6.0) = this - this / divisor rnd this + this / divisor
fun Float.near(divisor: Float = 6.0f) = this - this / divisor rnd this + this / divisor

fun Int.around(spread: Int = 6) = this + (-spread rnd spread)
fun Long.around(spread: Long = 6) = this + (-spread rnd spread)
fun Double.around(spread: Double = 6.0) = this + (-spread rnd spread)
fun Float.around(spread: Float = 6.0f) = this + (-spread rnd spread)


// Convenience properties to use mostly in string interpolations, to generate nice unique(ish) strings.

val rndBigInt get() = 100_000 rnd (Int.MAX_VALUE - 1000)
val rndBigLong get() = 100_000L rnd (Long.MAX_VALUE - 10_000L)
val rndBigIntStr get() = rndBigInt.strf
val rndBigLongStr get() = rndBigLong.strf
