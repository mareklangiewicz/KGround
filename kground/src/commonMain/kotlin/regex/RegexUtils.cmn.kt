package pl.mareklangiewicz.regex

import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ure.UReplacement


/** More explicit name for stdlib [Regex.matchEntire] */
fun Regex.matchEntireOrNull(input: CharSequence): MatchResult? = matchEntire(input)

fun Regex.matchEntireOrThrow(input: CharSequence): MatchResult =
  matchEntireOrNull(input).reqNN { "this regex: \"$this\" does not match entire input" }

/** More explicit name for stdlib [Regex.matchAt] */
fun Regex.matchAtOrNull(input: CharSequence, index: Int): MatchResult? = matchAt(input, index)

fun Regex.matchAtOrThrow(input: CharSequence, index: Int): MatchResult =
  matchAtOrNull(input, index).reqNN { "this regex: \"$this\" does not match input at index: $index" }

/** Similar to stdlib [Regex.find], but if startIndex > input length, it just returns null instead of throwing */
fun Regex.findFirstOrNull(input: CharSequence, startIndex: Int = 0): MatchResult? =
  if (startIndex > input.length) null else find(input, startIndex)

/** @throws BadArgErr if not found */
fun Regex.findFirst(input: CharSequence, startIndex: Int = 0): MatchResult =
  findFirstOrNull(input, startIndex).reqNN { "this regex: \"$this\" is nowhere in input" }

/**
 * @throws BadArgErr if not found or found more than one
 * Does NOT check for overlapping matches.
 */
fun Regex.findSingle(input: CharSequence, startIndex: Int = 0): MatchResult =
  findFirst(input, startIndex) // can already throw when not found
    .also {
      val second = findFirstOrNull(input, it.range.last + 1)
      second.reqNull { "this regex: \"$this\" has been found second time at idx: ${second!!.range.first}" }
    }

/** @throws BadArgErr if not found or found more than one. Even if the second one overlaps with the first one. */
fun Regex.findSingleWithOverlap(input: CharSequence, startIndex: Int = 0): MatchResult =
  findFirst(input, startIndex) // can already throw if not found
    .also {
      val second = findFirstOrNull(input, it.range.first + 1)
      second.reqNull { "this regex: \"$this\" has been found second time at idx: ${second!!.range.first}" }
    }

/**
 * Note: Even with this overlapping version, the first match starting at particular position wins,
 * (no additional results starting from the same place). It's like the whole thing is always "atomic".
 * Actual atomic/possessive/reluctant constructs matter INSIDE every single matching, not across MatchResults.
 */
fun Regex.findAllWithOverlap(input: CharSequence, startIndex: Int = 0): Sequence<MatchResult> {
  req(startIndex in 0..input.length) { "startIdx: $startIndex is not in bounds: 0..${input.length}" }
  return generateSequence({ findFirstOrNull(input, startIndex) }, { findFirstOrNull(input, it.range.first + 1) })
}

/** Improved and more explicit version of stdlib [Regex.replaceFirst]. */
fun Regex.replaceFirstOrNone(input: CharSequence, replacement: UReplacement): String =
  replaceFirst(input, replacement.raw)

/**
 * Improved and more explicit version of stdlib [Regex.replace].
 * Note: No overlapping here - searching with overlap wouldn't have much sense during replacing.
 */
fun Regex.replaceAll(input: CharSequence, replacement: UReplacement): String = replace(input, replacement.raw)

/**
 * Improved and more explicit version for stdlib [Regex.replace] with custom [transform] fun.
 * No overlapping here - searching with overlap wouldn't have much sense during replacing.
 */
fun Regex.replaceAll(input: CharSequence, transform: (MatchResult) -> CharSequence): String =
  replace(input) { transform(it) }

/** @throws BadArgErr if not found or found more than one */
fun Regex.replaceSingle(input: CharSequence, replacement: UReplacement): String {
  findSingle(input) // already throws when more than one or none; TODO_someday use returned range to speedup replace?
  return replaceFirstOrNone(input, replacement)
}


fun CharSequence.matchEntireOrNull(re: Regex) = re.matchEntireOrNull(this)

fun CharSequence.matchEntireOrThrow(re: Regex) = re.matchEntireOrThrow(this)

fun CharSequence.matchAtOrNull(re: Regex, index: Int) = re.matchAtOrNull(this, index)

fun CharSequence.matchAtOrThrow(re: Regex, index: Int) = re.matchAtOrThrow(this, index)

fun CharSequence.findFirstOrNull(re: Regex, startIndex: Int = 0) = re.findFirstOrNull(this, startIndex)

fun CharSequence.findFirst(re: Regex, startIndex: Int = 0) = re.findFirst(this, startIndex)

fun CharSequence.findSingle(re: Regex, startIndex: Int = 0) = re.findSingle(this, startIndex)

fun CharSequence.findSingleWithOverlap(re: Regex, startIndex: Int = 0) = re.findSingleWithOverlap(this, startIndex)

fun CharSequence.findAll(re: Regex, startIndex: Int = 0) = re.findAll(this, startIndex)

fun CharSequence.findAllWithOverlap(re: Regex, startIndex: Int = 0) = re.findAllWithOverlap(this, startIndex)

fun CharSequence.replaceFirstOrNone(re: Regex, replacement: UReplacement) = re.replaceFirstOrNone(this, replacement)

fun CharSequence.replaceAll(re: Regex, replacement: UReplacement) = re.replaceAll(this, replacement)

fun CharSequence.replaceAll(re: Regex, transform: (MatchResult) -> CharSequence) = re.replaceAll(this, transform)

fun CharSequence.replaceSingle(re: Regex, replacement: UReplacement) = re.replaceSingle(this, replacement)

