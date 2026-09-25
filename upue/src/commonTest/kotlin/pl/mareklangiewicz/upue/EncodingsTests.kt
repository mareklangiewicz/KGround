package pl.mareklangiewicz.upue

import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EncodingsTests {

    @Test fun abc16uspek() = uspek {

        "On 1234 string" o { testStringOnAFewAbc16Encoders("1234") }

        "On SOME_CODE string" o { testStringOnAFewAbc16Encoders(SOME_CODE) }

        "On random string" o {
            val rnd = Random(667)
            val string = buildString {
                repeat(10_000) { append(rnd.nextInt().toChar()) }
            }.encodeToByteArray().decodeToString() // trick to replace any invalid char sequences (utf craziness)
//            println(string.take(20)) // funny it's mostly chinese chars
            testStringOnAFewAbc16Encoders(string)
        }
    }

    private fun testStringOnAFewAbc16Encoders(string: String) = "On Abc16Encoders" o {
        val encoders = listOf(
                "default" to Abc16Encoder(),
                "upper" to Abc16Encoder(uppercase = true),
                "obfuscated 334" to Abc16Encoder(obfuscation = 334),
                "obfuscated 334 upper" to Abc16Encoder(uppercase = true, obfuscation = 334),
                "obfuscated -7" to Abc16Encoder(obfuscation = -7),
        )

        for ((name, encoder) in encoders) {
            "On string encoded with $name" o {
                val encoded = encoder.enc(string)
//                println(encoded.take(80))

                "decoding back gives original string" o {
                    val decoded = encoder.dec(encoded)
//                    println(string.take(20))
//                    println(decoded.take(20))
                    assertEquals(string, decoded, "Wrong result after decoding")
                }

                val slen = string.length
                val slenExpMax = slen * 8
                val elen = encoded.length
                "encoded length $elen is between $slen and $slenExpMax" o {
                    assertContains(slen..slenExpMax, elen, "$slen !in $slen..$slenExpMax")
                }

                val arange = if ("upper" in name) 'A'..'P' else 'a'..'p'
                "all chars are in $arange" o {
                    for (ch in encoded)
                        assertContains(arange, ch, "Strange char: $ch found.")
                }
            }
        }
    }
}

private val SOME_CODE = """
    companion object Default : Random(), Serializable {
        private val defaultRandom: Random = defaultPlatformRandom()

        private object Serialized : Serializable {
            private const val serialVersionUID = 0L

            private fun readResolve(): Any = Random
        }

        private fun writeReplace(): Any = Serialized

        override fun nextBits(bitCount: Int): Int = defaultRandom.nextBits(bitCount)
        override fun nextInt(): Int = defaultRandom.nextInt()
        override fun nextInt(until: Int): Int = defaultRandom.nextInt(until)
        override fun nextInt(from: Int, until: Int): Int = defaultRandom.nextInt(from, until)

        override fun nextLong(): Long = defaultRandom.nextLong()
        override fun nextLong(until: Long): Long = defaultRandom.nextLong(until)
        override fun nextLong(from: Long, until: Long): Long = defaultRandom.nextLong(from, until)

        override fun nextBoolean(): Boolean = defaultRandom.nextBoolean()

        override fun nextDouble(): Double = defaultRandom.nextDouble()
        override fun nextDouble(until: Double): Double = defaultRandom.nextDouble(until)
        override fun nextDouble(from: Double, until: Double): Double = defaultRandom.nextDouble(from, until)

        override fun nextFloat(): Float = defaultRandom.nextFloat()

        override fun nextBytes(array: ByteArray): ByteArray = defaultRandom.nextBytes(array)
        override fun nextBytes(size: Int): ByteArray = defaultRandom.nextBytes(size)
        override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray =
            defaultRandom.nextBytes(array, fromIndex, toIndex)
    }
""".trimIndent()