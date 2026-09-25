@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.upue

/**
 * Mini collections library
 *
 * Created by Marek Langiewicz on 18.02.16.
 *
 * Collection names are abbreviations - it's an intentional strange style - mostly to differ
 * from standard Java and Kotlin collection names. (also to communicate that this lib is hacky / experimental)
 */

// TODO NOW: use inline/value classes to wrap standard collections without overhead

// TODO: rename whole thing as "MutLst" and publish as separate mini multiplatform library.
// (separate from UPue)


// TODO_maybe: read more about mixed site variance:
// http://www.cs.cornell.edu/~ross/publications/mixedsite/mixedsite-tate-fool13.pdf

interface IGet<in K, out V> { operator fun get(key: K): V }

interface ISet<in K, in V> { operator fun set(key: K, item: V) }

interface IAdd<in T> { fun add(item: T) }

interface IContains { operator fun contains(item: Any?): Boolean }

interface ILen { val len: Int }

interface IClr { fun clr() }


/** minimalist stack. pushes and pulls elements from the same side */
interface ISak<T> : IPush<T>, IPull<T?>, IPeek<T?> // I don't require the "len" property in all stacks intentionally.

/** minimalist queue. pushes elements to one side and pulls from the other */
interface IQue<T> : IPush<T>, IPull<T?>, IPeek<T?> // I don't require the "len" property in all queues intentionally.

interface IDeq<T> {
    val head: ISak<T>
    val tail: ISak<T>
}

fun <T> IDeq<T>.rev(): IDeq<T> = object : IDeq<T> {
    override val head = this@rev.tail
    override val tail = this@rev.head
}

fun <T> IDeq<T>.asQue(): IQue<T> = object : IQue<T> {
    override val push = tail.push
    override val pull = head.pull
}


interface ICol<out T> : Iterable<T>, ILen, IContains {
    override fun contains(item: Any?) = indexOf(item) >= 0
}

/** additive collection means user can add items to it */
interface IAddCol<T> : ICol<T>, IAdd<T>

// IMPORTANT: methods behavior for indices: idx < 0 || idx >= size   is UNDEFINED here!
// Subclasses may define, for example, negative indices count backwards from the end,
// but we do NOT promise anything like that here in IArr.
// Any contract enchantments should be documented in particular implementation.
interface IArr<out T> : ICol<T>, IGet<Int, T> {

    override operator fun iterator(): Iterator<T> = Itor(this)

    class Itor<out T>(private val arr: IArr<T>, private var idx: Int = 0) : Iterator<T> {
        override fun hasNext(): Boolean = idx < arr.len
        override fun next(): T = arr[idx++]
    }
}

interface IAddArr<T> : IArr<T>, IAddCol<T>
interface IMutArr<T> : IArr<T>, ISet<Int, T>

object MutArrOf0: IMutArr<Nothing> {
    override fun get(key: Int): Nothing = throw IndexOutOfBoundsException("EmptyArr has no element at idx: $key")
    override fun set(key: Int, item: Nothing) = throw IndexOutOfBoundsException("EmptyArr can't mutate element at idx: $key")
    override val len: Int get() = 0
}

class MutArrOf1<T>(private var item: T): IMutArr<T> {
    override val len get() = 1
    override fun get(key: Int) = item.also { key.chk(0, 0) }
    override fun set(key: Int, item: T) { key.chk(0, 0); this.item = item }
}

class MutArrOf2<T>(private var first: T, private var second: T): IMutArr<T> {
    override val len get() = 2
    override fun get(key: Int) = if (key.chk(0, 1) == 0) first else second
    override fun set(key: Int, item: T) { if(key.chk(0, 1) == 0) this.first = item else second = item }
}

class ArrOf1Get<out T>(private val get: () -> T): IArr<T> {
    override val len get() = 1
    override fun get(key: Int) = key.chk(0, 0).run { get() }
}

class ArrOfSame<T>(override var len: Int, var value: T): IArr<T> {
    override fun get(key: Int) = key.chk(0, len-1).run { value }
}

inline fun <T> IArr<T>?.orEmpty(): IArr<T> = this ?: MutArrOf0

fun <T> mutArrOf() = MutArrOf0
fun <T> mutArrOf(item: T) = MutArrOf1(item)
fun <T> mutArrOf(first: T, second: T) = MutArrOf2(first, second)
fun <T> mutArrOf(vararg elem: T) = elem.asMutArr()
fun <T> Pair<T, T>.asMutArr() = MutArrOf2(first, second)

fun <T> arrOf() = MutArrOf0 as IArr<T>
fun <T> arrOf(item: T) = MutArrOf1(item) as IArr<T>
fun <T> arrOf(first: T, second: T) = MutArrOf2(first, second) as IArr<T>
fun <T> arrOf(vararg elem: T) = elem.asArr()
fun <T> Pair<T, T>.asArr() = MutArrOf2(first, second) as IArr<T>

/**
 * Something like Python list slicing (start idx is included, stop idx is excluded)
 * Negative start and stop indices are also interpreted like in Python
 * IMPORTANT: this implementation assumes that underlying array does not change its size
 * TODO SOMEDAY: third argument: step (also like in Python)
 * TODO LATER: test it!
 */
open class ArrCut<out T, ArrT: IArr<T>>(val src: ArrT, astart: Int, astop: Int) : IArr<T> {

    val stop : Int = astop.pos(src.len).chk(0, src.len)
    val start: Int = astart.pos(src.len).chk(0, stop)
    override val len: Int = stop - start

    override fun get(key: Int) = src[start + key.pos(len).chk(0, len-1)]
    override operator fun iterator(): Iterator<T> = IArr.Itor(this)
}

class MutArrCut<T>(asrc: IMutArr<T>, astart: Int, astop: Int) : IMutArr<T>, ArrCut<T, IMutArr<T>>(asrc, astart, astop) {
    override fun set(key: Int, item: T) { src[start + key.pos(len).chk(0, len-1)] = item }
}

operator fun <T> IArr<T>.get(start: Int, stop: Int) = ArrCut(this, start, stop) // TODO LATER: test it
operator fun <T> IMutArr<T>.get(start: Int, stop: Int) = MutArrCut(this, start, stop) // TODO LATER: test it


open class ArrSum<out T, ArrT: IArr<T>>(val first: ArrT, val second: ArrT): IArr<T> {
    override val len get() = first.len + second.len
    override fun get(key: Int) = if (key < first.len) first[key] else second[key - first.len]
}

class MutArrSum<T>(afirst: IMutArr<T>, asecond: IMutArr<T>): IMutArr<T>, ArrSum<T, IMutArr<T>>(afirst, asecond) {
    override fun set(key: Int, item: T) = if (key < first.len) first[key] =  item else second[key - first.len] = item
}

operator fun <T> IArr<T>.plus(other: IArr<T>) = ArrSum(this, other)



interface IMutLst<T> : IMutArr<T>, IAddArr<T>, IDeq<T>, IClr {
    fun ins(idx: Int, item: T)
    fun del(idx: Int): T
    fun mov(src: Int, dst: Int) {
        if(dst == src) return
        val item = del(src)
        ins(if(dst > src) dst-1 else dst, item)
    }
    override fun add(item: T) = ins(len, item)
    override fun clr() { for(i in len-1 downTo 0) del(i) }
        // almost always this implementation should be overridden
}



fun <T> Collection<T>.asCol() = object : ICol<T> {
    override val len: Int get() = this@asCol.size
    override fun iterator() = this@asCol.iterator()
    override fun contains(item: Any?) = item in this@asCol
}

fun <T> List<T>.asArr() = object : IArr<T> {
    override val len: Int get() = this@asArr.size
    override fun iterator() = this@asArr.iterator()
    override fun get(key: Int) = this@asArr[key]
    override fun contains(item: Any?) = item in this@asArr
}

fun <T> Array<T>.asArr() = asMutArr() as IArr<T>
fun <T> Array<T>.asMutArr() = object : IMutArr<T> {
    override val len: Int get() = this@asMutArr.size
    override fun iterator() = this@asMutArr.iterator()
    override fun get(key: Int) = this@asMutArr[key]
    override fun set(key: Int, item: T) { this@asMutArr[key] = item }
    override fun contains(item: Any?): Boolean = item in this@asMutArr
        // current standard Array.contains impl is the same as ICol.contains, but better to always use original impl.
}

fun <T> MutableList<T>.asAddArr() = asMutLst() as IAddArr<T>
fun <T> MutableList<T>.asMutArr() = asMutLst() as IMutArr<T>
fun <T> MutableList<T>.asMutLst() = object : IMutLst<T> {
    override fun get(key: Int) = this@asMutLst[key]
    override fun set(key: Int, item: T) { this@asMutLst[key] = item }

    override fun ins(idx: Int, item: T) = this@asMutLst.add(idx, item)
    override fun del(idx: Int): T = this@asMutLst.removeAt(idx)

    override val len: Int get() = this@asMutLst.size
    override fun iterator() = this@asMutLst.iterator()
    override fun contains(item: Any?): Boolean = item in this@asMutLst
    override fun clr() = this@asMutLst.clear()

    override val head = LstHead(this)
    override val tail = LstTail(this)
}

fun Int.pos(size: Int) = if(this < 0) size + this else this
fun Int.chk(min: Int, max: Int) = if(this < min || this > max) throw IndexOutOfBoundsException() else this



/** null value represents no value under particular key */
interface IMap<K, out V> : IGet<K, V?> {
    val keys: ICol<K>
}

/** null value represents no value under particular key */
interface IMutMap<K, V> : IMap<K, V>, ISet<K, V?>, IClr {
    override fun clr() { for (k in keys) this[k] = null }
    fun setAll(asrc: IMap<K, V>) = asrc.keys.forEach { this[it] = asrc[it] }
}

fun <K, V> Map<K, V>.asMap() = object : IMap<K, V> {
    override fun get(key: K): V? = this@asMap[key]
    override val keys: ICol<K> = this@asMap.keys.asCol()
}

fun <K, V> MutableMap<K, V>.asMutMap() = object : IMutMap<K, V> {
    override fun clr() = clear()
    override fun get(key: K): V? = this@asMutMap[key]
    override val keys: ICol<K> = this@asMutMap.keys.asCol()
    override fun set(key: K, item: V?) { if (item == null) remove(key) else this@asMutMap[key] = item }
}

/**
 * It's a "view" of part of src map that sees only keys with specific prefix/suffix.
 * Keys of the "view" are always automatically stripped of cutPrefix and cutSuffix.
 */
open class StrMapCut<out V, MapT: IMap<String, V>>(
    val src: MapT,
    val cutPrefix: String = "",
    val cutSuffix: String = ""
): IMap<String, V> {
    protected fun wrap(key: String) = cutPrefix + key + cutSuffix
    override fun get(key: String): V? = src[wrap(key)]
    override val keys: ICol<String>
        get() = src.keys.filter { it.startsWith(cutPrefix) && it.endsWith(cutSuffix) }.asCol()
}

fun <V> IMap<String, V>.cut(cutPrefix: String = "", cutSuffix: String = "") =
    StrMapCut(this, cutPrefix, cutSuffix)

/**
 * It's a mutable "view" of part of src map that sees only keys with specific prefix/suffix.
 * Keys of the "view" are always automatically stripped of cutPrefix and cutSuffix.
 * Consequently, setting a value, sets it to src under a key with added prefix/suffix.
 */
class StrMutMapCut<V, MapT: IMutMap<String, V>>(
    asrc: MapT,
    cutPrefix: String = "",
    cutSuffix: String = ""
): StrMapCut<V, MapT>(asrc, cutPrefix, cutSuffix), IMutMap<String, V> {
    override fun set(key: String, item: V?) { src[wrap(key)] = item }
}

fun <V> IMutMap<String, V>.cutMut(cutPrefix: String = "", cutSuffix: String = "") =
    StrMutMapCut(this, cutPrefix, cutSuffix)

