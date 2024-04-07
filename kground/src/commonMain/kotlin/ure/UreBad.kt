@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.ure.bad

import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.core.*

inline infix fun Ure.chkIR(ir: IR) = apply { toIR() chkEq ir }

inline infix fun Ure.chkIR(ir: String) = apply { toIR().str chkEq ir }

inline fun Ure.chkMatchEntire(
  input: CharSequence,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" does NOT match entire input" },
) = apply { matchEntireOrNull(input).chkNN(lazyMessage) }

inline fun Ure.chkNotMatchEntire(
  input: CharSequence,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" DOES match entire input" },
) = apply { matchEntireOrNull(input).chkNull(lazyMessage) }

inline fun Ure.reqMatchEntire(
  input: CharSequence,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" does NOT match entire input arg" },
) = apply { matchEntireOrNull(input).reqNN(lazyMessage) }

inline fun Ure.reqNotMatchEntire(
  input: CharSequence,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" DOES match entire input arg" },
) = apply { matchEntireOrNull(input).reqNull(lazyMessage) }

inline fun Ure.chkMatchAt(
  input: CharSequence,
  index: Int,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" does NOT match input at $index" },
) = apply { matchAtOrNull(input, index).chkNN(lazyMessage) }

inline fun Ure.chkNotMatchAt(
  input: CharSequence,
  index: Int,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" DOES match input at $index" },
) = apply { matchAtOrNull(input, index).chkNull(lazyMessage) }

inline fun Ure.reqMatchAt(
  input: CharSequence,
  index: Int,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" does NOT match input arg at $index" },
) = apply { matchAtOrNull(input, index).reqNN(lazyMessage) }

inline fun Ure.reqNotMatchAt(
  input: CharSequence,
  index: Int,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" DOES match input arg at $index" },
) = apply { matchAtOrNull(input, index).reqNull(lazyMessage) }

inline fun Ure.chkFindFirst(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input at all" },
) = apply { findFirstOrNull(input, startIndex).chkNN(lazyMessage) }

inline fun Ure.chkNotFindAny(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" WAS found in input" },
) = apply { findFirstOrNull(input, startIndex).chkNull(lazyMessage) }

inline fun Ure.reqFindFirst(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input arg at all" },
) = apply { findFirstOrNull(input, startIndex).reqNN(lazyMessage) }

inline fun Ure.reqNotFindAny(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "this ure ir \"${toIR()}\" WAS found in input arg" },
) = apply { findFirstOrNull(input, startIndex).reqNull(lazyMessage) }

inline fun Ure.chkFindSingle(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input at all" },
  lazyMessageIfMultiple: () -> String = { "this ure ir \"${toIR()}\" WAS found in input MORE than once" },
) = apply {
  val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
  findFirstOrNull(input, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Ure.reqFindSingle(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input arg at all" },
  lazyMessageIfMultiple: () -> String = { "this ure ir \"${toIR()}\" WAS found in input arg MORE than once" },
) = apply {
  val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
  findFirstOrNull(input, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun Ure.chkFindSingleWithOverlap(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input at all" },
  lazyMessageIfMultiple: () -> String = { "this ure ir \"${toIR()}\" WAS found in input MORE than once with overlap" },
) = apply {
  val first = findFirstOrNull(input, startIndex).chkNN(lazyMessageIfNone)
  findFirstOrNull(input, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun Ure.reqFindSingleWithOverlap(
  input: CharSequence,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "this ure ir \"${toIR()}\" was NOT found in input arg at all" },
  lazyMessageIfMultiple: () -> String = { "this ure ir \"${toIR()}\" WAS found in input arg MORE than once with overlap" },
) = apply {
  val first = findFirstOrNull(input, startIndex).reqNN(lazyMessageIfNone)
  findFirstOrNull(input, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkMatchEntire(
  ure: Ure,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" does NOT match this entire input" },
) = apply { matchEntireOrNull(ure).chkNN(lazyMessage) }

inline fun CharSequence.chkNotMatchEntire(
  ure: Ure,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" DOES match this entire input" },
) = apply { matchEntireOrNull(ure).chkNull(lazyMessage) }

inline fun CharSequence.reqMatchEntire(
  ure: Ure,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" does NOT match this entire input arg" },
) = apply { matchEntireOrNull(ure).reqNN(lazyMessage) }

inline fun CharSequence.reqNotMatchEntire(
  ure: Ure,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" DOES match this entire input arg" },
) = apply { matchEntireOrNull(ure).reqNull(lazyMessage) }

inline fun CharSequence.chkMatchAt(
  ure: Ure,
  index: Int,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" does NOT match this input at $index" },
) = apply { matchAtOrNull(ure, index).chkNN(lazyMessage) }

inline fun CharSequence.chkNotMatchAt(
  ure: Ure,
  index: Int,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" DOES match this input at $index" },
) = apply { matchAtOrNull(ure, index).chkNull(lazyMessage) }

inline fun CharSequence.reqMatchAt(
  ure: Ure,
  index: Int,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" does NOT match this input arg at $index" },
) = apply { matchAtOrNull(ure, index).reqNN(lazyMessage) }

inline fun CharSequence.reqNotMatchAt(
  ure: Ure,
  index: Int,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" DOES match this input arg at $index" },
) = apply { matchAtOrNull(ure, index).reqNull(lazyMessage) }

inline fun CharSequence.chkFindFirst(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input at all" },
) = apply { findFirstOrNull(ure, startIndex).chkNN(lazyMessage) }

inline fun CharSequence.chkNotFindAny(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input" },
) = apply { findFirstOrNull(ure, startIndex).chkNull(lazyMessage) }

inline fun CharSequence.reqFindFirst(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input arg at all" },
) = apply { findFirstOrNull(ure, startIndex).reqNN(lazyMessage) }

inline fun CharSequence.reqNotFindAny(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessage: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input arg" },
) = apply { findFirstOrNull(ure, startIndex).reqNull(lazyMessage) }

inline fun CharSequence.chkFindSingle(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input at all" },
  lazyMessageIfMultiple: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input MORE than once" },
) = apply {
  val first = findFirstOrNull(ure, startIndex).chkNN(lazyMessageIfNone)
  findFirstOrNull(ure, first.range.last + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingle(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input arg at all" },
  lazyMessageIfMultiple: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input arg MORE than once" },
) = apply {
  val first = findFirstOrNull(ure, startIndex).reqNN(lazyMessageIfNone)
  findFirstOrNull(ure, first.range.last + 1).reqNull(lazyMessageIfMultiple)
}

inline fun CharSequence.chkFindSingleWithOverlap(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input at all" },
  lazyMessageIfMultiple: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input MORE than once with overlap" },
) = apply {
  val first = findFirstOrNull(ure, startIndex).chkNN(lazyMessageIfNone)
  findFirstOrNull(ure, first.range.first + 1).chkNull(lazyMessageIfMultiple)
}

inline fun CharSequence.reqFindSingleWithOverlap(
  ure: Ure,
  startIndex: Int = 0,
  lazyMessageIfNone: () -> String = { "ure ir \"${ure.toIR()}\" was NOT found in this input arg at all" },
  lazyMessageIfMultiple: () -> String = { "ure ir \"${ure.toIR()}\" WAS found in this input arg MORE than once with overlap" },
) = apply {
  val first = findFirstOrNull(ure, startIndex).reqNN(lazyMessageIfNone)
  findFirstOrNull(ure, first.range.first + 1).reqNull(lazyMessageIfMultiple)
}



