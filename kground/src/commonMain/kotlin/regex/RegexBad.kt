@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.regex.bad


import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.regex.*



inline fun Regex.chkMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex: \"$this\" does not match entire input" },
) = apply { matchEntireOrNull(input).chkNN(lazyMessage) }

inline fun Regex.reqMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex: \"$this\" does not match entire input arg" },
) = apply { matchEntireOrNull(input).reqNN(lazyMessage) }

inline fun Regex.chkMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex: \"$this\" does not match input at: $index" },
) = apply { matchAtOrNull(input, index).chkNN(lazyMessage) }

inline fun Regex.reqMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex: \"$this\" does not match input arg at: $index" },
) = apply { matchAtOrNull(input, index).reqNN(lazyMessage) }

inline fun Regex.chkFindFirst(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex: \"$this\" was not found in input at all" },
) = apply { findFirstOrNull(input, startIndex).chkNN(lazyMessage) }

inline fun Regex.reqFindFirst(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex: \"$this\" was not found in input arg at all" },
) = apply { findFirstOrNull(input, startIndex).reqNN(lazyMessage) }

inline fun Regex.chkFindSingle(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex: \"$this\" was not found in input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex: \"$this\" was found in input more than once" },
) = apply {
    val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Regex.reqFindSingle(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex: \"$this\" was not found in input arg at all" },
    lazyMessageIfMultiple: () -> String = { "this regex: \"$this\" was found in input arg more than once" },
) = apply {
    val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun Regex.chkFindSingleWithOverlap(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex: \"$this\" was not found in input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex: \"$this\" was found in input more than once with overlap" },
) = apply {
    val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Regex.reqFindSingleWithOverlap(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex: \"$this\" was not found in input arg at all" },
    lazyMessageIfMultiple: () -> String = { "this regex: \"$this\" was found in input arg more than once with overlap" },
) = apply {
    val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex: \"$re\" does not match this entire input" },
) = apply { matchEntireOrNull(re).chkNN(lazyMessage) }

inline fun CharSequence.reqMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex: \"$re\" does not match this entire input arg" },
) = apply { matchEntireOrNull(re).reqNN(lazyMessage) }

inline fun CharSequence.chkMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex: \"$re\" does not match this input at: $index" },
) = apply { matchAtOrNull(re, index).chkNN(lazyMessage) }

inline fun CharSequence.reqMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex: \"$re\" does not match this input arg at: $index" },
) = apply { matchAtOrNull(re, index).reqNN(lazyMessage) }

inline fun CharSequence.chkFindFirst(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex: \"$re\" was not found in this input at all" },
) = apply { findFirstOrNull(re, startIndex).chkNN(lazyMessage) }

inline fun CharSequence.reqFindFirst(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex: \"$re\" was not found in this input arg at all" },
) = apply { findFirstOrNull(re, startIndex).reqNN(lazyMessage) }

inline fun CharSequence.chkFindSingle(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex: \"$re\" was not found in this input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex: \"$this\" was found in input more than once" },
) = apply {
    val first = findFirstOrNull(re, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingle(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex: \"$re\" was not found in this input arg at all" },
    lazyMessageIfMultiple: () -> String = { "regex: \"$re\" was found in this input arg more than once" },
) = apply {
    val first = findFirstOrNull(re, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkFindSingleWithOverlap(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex: \"$re\" was not found in this input at all" },
    lazyMessageIfMultiple: () -> String = { "regex: \"$re\" was found in this input more than once with overlap" },
) = apply {
    val first = findFirstOrNull(re, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingleWithOverlap(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex: \"$re\" was not found in this input arg at all" },
    lazyMessageIfMultiple: () -> String = { "regex: \"$re\" was found in this input arg more than once with overlap" },
) = apply {
    val first = findFirstOrNull(re, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}

