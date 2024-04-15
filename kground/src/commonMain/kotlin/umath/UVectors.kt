@file:Suppress("unused")

package pl.mareklangiewicz.umath

import pl.mareklangiewicz.udata.*


fun lerp(min: Int, max: Int, fraction: Float = 0.5f) = lerp(min.flt, max.flt, fraction).int
fun lerp(min: Float, max: Float, fraction: Float = 0.5f) = min * (1 - fraction) + max * fraction
fun lerp(min: Double, max: Double, fraction: Double = 0.5) = min * (1 - fraction) + max * fraction

fun lerpInv(min: Int, max: Int, position: Int) = (position - min) / (max - min)
fun lerpInv(min: Float, max: Float, position: Float) = (position - min) / (max - min)
fun lerpInv(min: Double, max: Double, position: Double) = (position - min) / (max - min)


data class XY(val x: Double = 0.0, val y: Double = 0.0) {
  override fun toString() = "(${x.str()},${y.str()})"
}

data class XYZ(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
  override fun toString() = "(${x.str()},${y.str()},${z.str()})"
  val xy get() = x xy y
}

infix fun Double.xy(that: Double) = XY(this, that)
infix fun XY.yz(that: Double) = XYZ(x, y, that)

operator fun XY.plus(that: XY) = x + that.x xy y + that.y
operator fun XY.minus(that: XY) = x - that.x xy y - that.y
operator fun XY.times(that: Double) = x * that xy y * that
operator fun XY.div(that: Double) = x / that xy y / that

operator fun XYZ.plus(that: XYZ) = x + that.x xy y + that.y yz z + that.z
operator fun XYZ.minus(that: XYZ) = x - that.x xy y - that.y yz z - that.z
operator fun XYZ.times(that: Double) = x * that xy y * that yz z * that
operator fun XYZ.times(that: XYZ) = x * that.x xy y * that.y yz z * that.z
operator fun XYZ.div(that: Double) = x / that xy y / that yz z / that
operator fun XYZ.div(that: XYZ) = x / that.z xy y / that.y yz z / that.z

fun lerp(p1: XY, p2: XY, fraction: Double = 0.5) = lerp(p1.x, p2.x, fraction) xy lerp(p1.y, p2.y, fraction)
fun lerp(p1: XYZ, p2: XYZ, fraction: Double = 0.5) = lerp(p1.xy, p2.xy, fraction) yz lerp(p1.z, p2.z, fraction)

infix fun XY.avg(that: XY) = lerp(this, that)
infix fun XYZ.avg(that: XYZ) = lerp(this, that)


infix fun XY.rnd(to: XY) = (x rnd to.x) xy (y rnd to.y)
infix fun XYZ.rnd(to: XYZ) = (x rnd to.x) xy (y rnd to.y) yz (z rnd to.z)

fun XY.near(divisor: Double = 6.0) = this - this / divisor rnd this + this / divisor
fun XYZ.near(divisor: Double = 6.0) = this - this / divisor rnd this + this / divisor

fun XY.around(spread: Double = 6.0) = this + ((-spread xy -spread) rnd (spread xy spread))
fun XYZ.around(spread: Double = 6.0) = this + ((-spread xy -spread yz -spread) rnd (spread xy spread yz spread))

