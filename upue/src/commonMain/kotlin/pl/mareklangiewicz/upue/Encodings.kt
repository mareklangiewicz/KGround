package pl.mareklangiewicz.upue

/**
 * IEncDec defines symmetric encoding/decoding.
 * Functions enc and dec must be inverse bijections, so for any d: d == dec(enc(d))
 */
interface IEncDec<Decoded: Any, Encoded: Any> {
    fun enc(d: Decoded): Encoded
    fun dec(e: Encoded): Decoded
}

class RevEncDec<D: Any, E: Any>(val src: IEncDec<E, D>) : IEncDec<D, E> {
    override fun enc(d: D): E = src.dec(d)
    override fun dec(e: E): D = src.enc(e)
}

fun <D: Any, E: Any> IEncDec<D, E>.rev() = RevEncDec(this)

open class MutMapEncoded<Key, Decoded: Any, Encoded: Any>(
        val src: IMutMap<Key, Decoded>,
        val encoder: IEncDec<Decoded, Encoded>,
): IMutMap<Key, Encoded> {
    override fun get(key: Key): Encoded? = src[key]?.let(encoder::enc)
    override fun set(key: Key, item: Encoded?) { src[key] = item?.let(encoder::dec) }
    override val keys get() = src.keys
}

class MutMapEncodedIf<Key, Coded: Any>(
        src: IMutMap<Key, Coded>,
        encoder: IEncDec<Coded, Coded>,
        val predicate: (key: Key) -> Boolean,
): MutMapEncoded<Key, Coded, Coded>(src, encoder) {
    override fun get(key: Key) = if (predicate(key)) super.get(key) else src[key]
    override fun set(key: Key, item: Coded?) { src[key] = if (predicate(key)) item?.let(encoder::dec) else item }
    override val keys get() = src.keys
}

fun <Key, Decoded: Any, Encoded: Any> IMutMap<Key, Decoded>.asEncoded(encoder: IEncDec<Decoded, Encoded>) =
        MutMapEncoded(this, encoder)

fun <Key, Coded: Any> IMutMap<Key, Coded>.asEncodedIf(encoder: IEncDec<Coded, Coded>, predicate: (key: Key) -> Boolean) =
        MutMapEncodedIf(this, encoder, predicate)

/** Encodes/decodes values under keys ending with ".abc16" with Abc16Encoder */
fun IMutMap<String, String>.asEncodedIfAbc16(obfuscation: Int? = null) =
        asEncodedIf(Abc16Encoder(obfuscation = obfuscation)) { it.endsWith(".abc16") }

/**
 * Example UByte obfuscation as IOneToOne class.
 * Warning: This is not thought to be "secure" in any way,
 * it's mostly to experiment with some very minimalistic implementation.
 */
class BasicUByteObfuscation(private val pepper: Int): IEncDec<UByte, UByte> {
    override fun dec(e: UByte): UByte =
            e.xor((pepper % 19).toUByte()).inv().xor(17u).rotateRight(pepper - 3)

    override fun enc(d: UByte): UByte =
            d.rotateLeft(pepper - 3).xor(17u).inv().xor((pepper % 19).toUByte())
}

private class Abc16UByteEncoder(
    private val acode: Int, // usually 'a'.code or 'A'.code
    private val uobf: BasicUByteObfuscation?
): IEncDec<UByte, Pair<Char, Char>> {

    override fun dec(e: Pair<Char, Char>) =
        ((e.first.code-acode shl 4) + (e.second.code-acode and 15)).toUByte().let { uobf?.dec(it) ?: it }

    override fun enc(d: UByte): Pair<Char, Char> =
        (uobf?.enc(d) ?: d).toInt().let { Char(acode + (it ushr 4)) to Char(acode + (it and 15)) }

}

/**
 * Minimalistic encoder with every character encoded as two "digits" from (a..p) "alphabet".
 * (or sometimes more if a particular character is more than one byte in Utf8)
 * Friendly to file names (even on M$Windows when in lowercase), urls, shells etc...
 * @param uppercase uses (A..P) instead of (a..p) "alphabet".
 * @param obfuscation when not null, it additionally performs basic obfuscation on every UByte before encoding.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class Abc16Encoder(uppercase: Boolean = false, obfuscation: Int? = null): IEncDec<String, String> {

    private val uencoder = Abc16UByteEncoder(
        acode = (if (uppercase) 'A' else 'a').code,
        uobf = obfuscation?.let { BasicUByteObfuscation(it) }
    )

    override fun enc(d: String) = buildString {
        d.encodeToByteArray(throwOnInvalidSequence = true).asUByteArray().forEach {
            val chars = uencoder.enc(it)
            append(chars.first)
            append(chars.second)
        }
    }

    override fun dec(e: String) = UByteArray(e.length / 2).apply {
        for (i in indices) this[i] = (uencoder.dec(e[i*2] to e[i*2 + 1]))
    }.asByteArray().decodeToString(throwOnInvalidSequence = true)
}
