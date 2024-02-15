@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.regex.bad


import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.regex.*



inline fun Regex.chkMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex \"$this\" does NOT match entire input" },
) = apply { matchEntireOrNull(input).chkNN(lazyMessage) }

inline fun Regex.chkNotMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex \"$this\" DOES match entire input" },
) = apply { matchEntireOrNull(input).chkNull(lazyMessage) }

inline fun Regex.reqMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex \"$this\" does NOT match entire input arg" },
) = apply { matchEntireOrNull(input).reqNN(lazyMessage) }

inline fun Regex.reqNotMatchEntire(
    input: CharSequence,
    lazyMessage: () -> String = { "this regex \"$this\" DOES match entire input arg" },
) = apply { matchEntireOrNull(input).reqNull(lazyMessage) }

inline fun Regex.chkMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex \"$this\" does NOT match input at $index" },
) = apply { matchAtOrNull(input, index).chkNN(lazyMessage) }

inline fun Regex.chkNotMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex \"$this\" DOES match input at $index" },
) = apply { matchAtOrNull(input, index).chkNull(lazyMessage) }

inline fun Regex.reqMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex \"$this\" does NOT match input arg at $index" },
) = apply { matchAtOrNull(input, index).reqNN(lazyMessage) }

inline fun Regex.reqNotMatchAt(
    input: CharSequence,
    index: Int,
    lazyMessage: () -> String = { "this regex \"$this\" DOES match input arg at $index" },
) = apply { matchAtOrNull(input, index).reqNull(lazyMessage) }

inline fun Regex.chkFindFirst(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex \"$this\" was NOT found in input at all" },
) = apply { findFirstOrNull(input, startIndex).chkNN(lazyMessage) }

inline fun Regex.chkNotFindAny(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex \"$this\" WAS found in input" },
) = apply { findFirstOrNull(input, startIndex).chkNull(lazyMessage) }

inline fun Regex.reqFindFirst(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex \"$this\" was NOT found in input arg at all" },
) = apply { findFirstOrNull(input, startIndex).reqNN(lazyMessage) }

inline fun Regex.reqNotFindAny(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "this regex \"$this\" WAS found in input arg" },
) = apply { findFirstOrNull(input, startIndex).reqNull(lazyMessage) }

inline fun Regex.chkFindSingle(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex \"$this\" was NOT found in input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex \"$this\" WAS found in input MORE than once" },
) = apply {
    val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Regex.reqFindSingle(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex \"$this\" was NOT found in input arg at all" },
    lazyMessageIfMultiple: () -> String = { "this regex \"$this\" WAS found in input arg MORE than once" },
) = apply {
    val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun Regex.chkFindSingleWithOverlap(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex \"$this\" was NOT found in input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex \"$this\" WAS found in input MORE than once with overlap" },
) = apply {
    val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Regex.reqFindSingleWithOverlap(
    input: CharSequence,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "this regex \"$this\" was NOT found in input arg at all" },
    lazyMessageIfMultiple: () -> String = { "this regex \"$this\" WAS found in input arg MORE than once with overlap" },
) = apply {
    val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(input, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex \"$re\" does NOT match this entire input" },
) = apply { matchEntireOrNull(re).chkNN(lazyMessage) }

inline fun CharSequence.chkNotMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex \"$re\" DOES match this entire input" },
) = apply { matchEntireOrNull(re).chkNull(lazyMessage) }

inline fun CharSequence.reqMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex: \"$re\" does not match this entire input arg" },
) = apply { matchEntireOrNull(re).reqNN(lazyMessage) }

inline fun CharSequence.reqNotMatchEntire(
    re: Regex,
    lazyMessage: () -> String = { "regex \"$re\" DOES match this entire input arg" },
) = apply { matchEntireOrNull(re).reqNull(lazyMessage) }

inline fun CharSequence.chkMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex \"$re\" does NOT match this input at $index" },
) = apply { matchAtOrNull(re, index).chkNN(lazyMessage) }

inline fun CharSequence.chkNotMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex \"$re\" DOES match this input at $index" },
) = apply { matchAtOrNull(re, index).chkNull(lazyMessage) }

inline fun CharSequence.reqMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex \"$re\" does NOT match this input arg at $index" },
) = apply { matchAtOrNull(re, index).reqNN(lazyMessage) }

inline fun CharSequence.reqNotMatchAt(
    re: Regex,
    index: Int,
    lazyMessage: () -> String = { "regex \"$re\" DOES match this input arg at $index" },
) = apply { matchAtOrNull(re, index).reqNull(lazyMessage) }

inline fun CharSequence.chkFindFirst(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex \"$re\" was NOT found in this input at all" },
) = apply { findFirstOrNull(re, startIndex).chkNN(lazyMessage) }

inline fun CharSequence.chkNotFindAny(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex \"$re\" WAS found in this input" },
) = apply { findFirstOrNull(re, startIndex).chkNull(lazyMessage) }

inline fun CharSequence.reqFindFirst(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex \"$re\" was NOT found in this input arg at all" },
) = apply { findFirstOrNull(re, startIndex).reqNN(lazyMessage) }

inline fun CharSequence.reqNotFindAny(
    re: Regex,
    startIndex: Int = 0,
    lazyMessage: () -> String = { "regex \"$re\" WAS found in this input arg" },
) = apply { findFirstOrNull(re, startIndex).reqNull(lazyMessage) }

inline fun CharSequence.chkFindSingle(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex \"$re\" was NOT found in this input at all" },
    lazyMessageIfMultiple: () -> String = { "this regex \"$this\" WAS found in input MORE than once" },
) = apply {
    val first = findFirstOrNull(re, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingle(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex \"$re\" was NOT found in this input arg at all" },
    lazyMessageIfMultiple: () -> String = { "regex \"$re\" WAS found in this input arg MORE than once" },
) = apply {
    val first = findFirstOrNull(re, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkFindSingleWithOverlap(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex \"$re\" was NOT found in this input at all" },
    lazyMessageIfMultiple: () -> String = { "regex \"$re\" WAS found in this input MORE than once with overlap" },
) = apply {
    val first = findFirstOrNull(re, startIndex).chkNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingleWithOverlap(
    re: Regex,
    startIndex: Int = 0,
    lazyMessageIfNone: () -> String = { "regex \"$re\" was NOT found in this input arg at all" },
    lazyMessageIfMultiple: () -> String = { "regex \"$re\" WAS found in this input arg MORE than once with overlap" },
) = apply {
    val first = findFirstOrNull(re, startIndex).reqNN(lazyMessageIfNone)
    findFirstOrNull(re, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}

