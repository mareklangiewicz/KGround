package pl.mareklangiewicz.upue

/**
 * Wraps any JS "array-like" object in IArr
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Indexed_collections#working_with_array-like_objects
 */
open class JsArr<T>(protected val jsArrLikeObj: dynamic): IArr<T> {
    init { len } // just to fail-fast if jsObj doesn't even look array-like
    override fun get(idx: Int): T = jsArrLikeObj[idx] as T
    final override val len: Int get() = jsArrLikeObj.length as Int
}

class JsMutArr<T>(jsArrLikeObj: dynamic): JsArr<T>(jsArrLikeObj), IMutArr<T> {
    override fun set(idx: Int, item: T) { jsArrLikeObj[idx] = item }
}
