package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.ure.core.Ure

/**
 * Micro regular expressions for XML, HTML, etc.
 * WARNING: regular expressions are generally TERRIBLE for ML languages.
 * Because these aren't just regular languages.
 * Most problems are with "recursive" elements like <div><div>...</div></div>
 * (unable to find correct matching end-tag)
 * Also commented out tags mess up RE matching, etc.
 * So people are shaming using RE for ML and say only real parsers are acceptable.
 * But… if you feel like a hacker (and need a lightweight "solution" that sometimes works)..
 * REMEMBER:
 * - Be prepared for any unexpected errors/mismatches
 * - Use only for scraping where mismatches are kinda OK, and easily recoverable/presented to user
 * - Use mostly for "leafy" elements
 *   - empty tags <tagname attrs.. />; <tag-name attrs..>only spaces</tagname>
 *   - elements that can't contain other elements of the same type (reluctant RE matching end tag)
 * - Maybe use for some "global" elements (possessive RE) - maybe use expected indentation?
 * - Maybe first check with another RE if there is some dangerous comment with tag or sth like that?
 * https://stackoverflow.com/questions/8577060/why-is-it-such-a-bad-idea-to-parse-xml-with-regex
 * https://flapenguin.me/xml-regex
 */



/**
 * @param expectedAttrs
 * Use ureExpectedAttr(..) or be very careful with providing own Ure for any expected attribute.
 * Attrs not on the list are matched but ignored. It only matches expectedAttrs in given order!
 * (With optional other attrs in between ignored)
 */
fun ureStartTag(name: String, vararg expectedAttrs: Ure) = ureSomeTag(name, *expectedAttrs)

fun ureEndTag(name: String) = ureSomeTag(name, ureBegin = ureText("</"))

@OptIn(DelicateApi::class)
fun ureCollapsedTag(name: String, vararg expectedAttrs: Ure) =
  ureSomeTag(name, *expectedAttrs, ureEnd = ureText("/>"))

fun Ure.withTagAround(name: String, vararg expectedAttrs: Ure, withOptSpacesAroundContent: Boolean = true) = ure {
  +ureStartTag(name, *expectedAttrs)
  +this@withTagAround.withOptSpacesAround(
    allowBefore = withOptSpacesAroundContent,
    allowAfter = withOptSpacesAroundContent,
  )
  +ureEndTag(name)
}

/**
 * Makes most sense if we ignore content anyway.
 * (like ureWhateva().withTagAroundOrJustTagCollapsed(..) ure { 0..MAX of space }.withTAOJTC..)
 * Purposefully long name to discourage using it directly. Use ureEmptyContentElement, ureWhatevaContentElement..
 */
@OptIn(DelicateApi::class, NotPortableApi::class)
fun Ure.withTagAroundOrJustTagCollapsed(
  name: String,
  vararg expectedAttrs: Ure,
  withOptSpacesAroundContent: Boolean = true,
  allowJustTagCollapsed: Boolean = true,
) = ure {
  +ureSomeTag(name, *expectedAttrs, ureEnd = ure { 0..1 of chSlash; +ch('>') })
  val collapsed = ureText("/>").lookBehind()
  val notCollapsed = ure {
    +ureText("/>").lookBehind(positive = false)
    +this@withTagAroundOrJustTagCollapsed
      .withOptSpacesAround(allowBefore = withOptSpacesAroundContent, allowAfter = withOptSpacesAroundContent)
    +ureEndTag(name)
  }
  if (allowJustTagCollapsed)
    +(collapsed or notCollapsed)
  else
    +notCollapsed
}

fun ureEmptyContentElement(name: String, vararg expectedAttrs: Ure, allowCollapsed: Boolean = false) =
  ure { 0..MAX of chWhiteSpace }
    .withTagAroundOrJustTagCollapsed(
      name, *expectedAttrs, withOptSpacesAroundContent = false, allowJustTagCollapsed = allowCollapsed,
    )

fun ureWhatevaContentElement(
  name: String,
  vararg expectedAttrs: Ure,
  withOptSpacesAroundContent: Boolean = true,
  whatevaContentName: String? = null,
  allowCollapsed: Boolean = false,
) = ureWhateva().withName(whatevaContentName)
  .withTagAroundOrJustTagCollapsed(
    name, *expectedAttrs,
    withOptSpacesAroundContent = withOptSpacesAroundContent, allowJustTagCollapsed = allowCollapsed,
  )


private val ureOptIgnoredAttrsOptSpaces = ureChain(ureTagAttr(), times = 0..MAX, reluctant = true)
  .withOptSpacesAround()

private fun ureSomeTag(
  name: String,
  vararg expectedAttrs: Ure,
  ureBegin: Ure = ch('<'),
  ureEnd: Ure = ch('>'),
) = ure {
  +ureBegin
  +ureText(name).withWordBoundaries().withOptSpacesAround()
  // boundaries in case we have no space after (we need to match <tag> but not glued <tagargname>)
  +ureOptIgnoredAttrsOptSpaces
  for (expectedAttr in expectedAttrs) {
    +expectedAttr
    +ureOptIgnoredAttrsOptSpaces
  }
  +ureEnd
}

fun ureExpectAttr(
  name: String,
  value: Ure = ureWhatevaInLine(), // let's keep default inline, matching long multiline whateva can lead to hard to debug mismatches
  valueGroupName: String? = name.filter { it != '-' },
  optional: Boolean = false,
) = ureTagAttr(ureText(name), value, valueGroupName).let { if (optional) ure { 0..1 of it } else it }

fun ureTagAttr(
  name: Ure = ureIdent(allowDashesInside = true),
  value: Ure = ureWhatevaInLine(), // let's keep default inline, matching long multiline whateva can lead to hard to debug mismatches
  valueGroupName: String? = null,
) = ure {
  // boundaries in case user changed name: Ure to sth that doesn't check it (like default ureIdent() does)
  +name.withWordBoundaries().withOptSpacesAround()
  +ch('=').withOptSpacesAround(allowBefore = false)
  +ch('"')
  +value.withName(valueGroupName)
  +ch('"')
}
