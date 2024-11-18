@file:OptIn(DelicateApi::class, NotPortableApi::class)

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.text.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.tuplek.fo
import pl.mareklangiewicz.tuplek.tre
import pl.mareklangiewicz.udata.lO
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ure.bad.chkIR
import pl.mareklangiewicz.ure.core.IR
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.ure.core.UreCharClass
import pl.mareklangiewicz.uspek.*

fun testUreCharClasses() {
  testSomeBasicCharClasses()
  testSomeUreCharClassPairsEtc()
  testSomeWeirdCharClasses()
}

fun testSomeBasicCharClasses() {

  "On basic char classes syntax" o {

    // Kotlin/JS Regex uses only unicode ("u") mode, but not the unicodeSets ("v") mode,
    // That's why intersections, some unions, subtractions, etc. are not supported on JS platform at all.
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/RegExp/unicode
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/RegExp/unicodeSets
    "class intersection compiles on JVM and LINUX but not on JS" o {
      ureRaw("[a-z&&[^cd]]").tstCompilesOnlyOn("JVM", "LINUX", alsoCheckNegation = false)
    }
    "class with basic union compiles everywhere" o {
      ureRaw("[^a-dx-z]").tstCompiles(alsoCheckNegation = false)
    }
    "class with a bit more complex union compiles on JVM and LINUX but not on JS" o {
      ureRaw("[a-d[^x-z]]").tstCompilesOnlyOn("JVM", "LINUX", alsoCheckNegation = false)
    }
    "all ready to use char class unions compile everywhere" o {
      lO(
        chAlpha, chHexDigit, chAlnum, chGraph, chWhiteSpaceInLine, chPrint,
        chAnyAtAll, chWordFirst, chWordOrDot, chWordOrDash, chWordOrDotOrDash, chPunct,
      ).forEachIndexed { i, u -> "union nr $i ${u.toIR().str}" o { u.tstCompiles() } }
    }
  }
  "On some already created chXXX vals" o {
    "chSlash str is just slash" o { chSlash.str chkEq "/" }
    "chSlash IR is just slash" o { chSlash chkIR "/" }
    "chBackSlash str is just backslash" o { chBackSlash.str chkEq "\\" }
    "chBackSlash IR is quoted" o { chBackSlash chkIR "\\\\" }
    "chTab str is just actual tab char code 9" o { chTab.str chkEq "\t" }
    "chTab IR is special t quoted" o { chTab chkIR "\\t" }
    "chAlert str is just actual alert char code 7" o { chTab.str.single().code chkEq 9 }
    "chAlert IR is special a quoted" o { chAlert chkIR "\\a" }

    "chWordFirst IR is flattened" o { chWordFirst chkIR "[a-zA-Z_]" }
  }

  "On fun ch constructing UreCharExact" o {
    "fails with more than single unicode character".oThrows<BadArgErr> { ch("ab") }
    "works correctly with single backslash" o {
      val ure = ch("\\") // using the version with string arg to be analogous to next tests with surrogate pairs
      ure.str chkEq "\\" // it is remembering just one char - no quoting here yet
      ure chkIR "\\\\" // it is quoting (if necessary) when generating IR
    }
    "On copyright character" o {
      val copyRight = "\u00a9"
      "two ways of encoding in kotlin src result in the same single utf16 char" o {
        copyRight.length chkEq 1
        copyRight[0].isSurrogate.chkFalse()
        "©" chkEq copyRight
      }
      "On ch with copyright" o {
        val ure = ch(copyRight)
        val re = ure.toIR()
        "created correctly" o {
          ure.str chkEq copyRight // it is remembering just the copyright character - no quoting here
          re chkEq IR("\\xa9") // it is using \xhh notation for IR (only two digits)
        }
        onUreClass(
          name = "copy right",
          ure = ure,
          match = lO(copyRight),
          matchNot = lO("a", "0", " ", "⛔", "✅", "❌"),
        )
      }
    }
    "On bitcoin symbol" o {
      val bitcoin = "\u20bf"
      "two ways of encoding in kotlin src result in the same single utf16 char" o {
        bitcoin.length chkEq 1
        bitcoin[0].isSurrogate.chkFalse()
        "₿" chkEq bitcoin
      }
      "On ch with bitcoin" o {
        val ure = ch(bitcoin)
        val re = ure.toIR()
        "created correctly" o {
          ure.str chkEq bitcoin // it is remembering just the bitcoin character - no quoting here yet
          re chkEq IR("\\u20bf") // it is using \uhhhh notation for IR
        }
        onUreClass(
          name = "bitcoin",
          ure = ure,
          match = lO(bitcoin),
          matchNot = lO("a", "0", " ", "⛔", "✅", "❌"),
        )
      }
    }
    "On The Devil character surrogate" o {
      val devil = "\uD83D\uDE08"
      "two ways of encoding in kotlin src result in the same surrogate pair" o {
        devil.length chkEq 2
        devil[0].isSurrogateHigh.chkTrue()
        devil[1].isSurrogateLow.chkTrue()
        "😈" chkEq devil
      }
      "On ch with The Devil" o {
        val ure = ch(devil)
        val re = ure.toIR()
        "created correctly" o {
          ure.str chkEq devil // it is remembering just the stop sign character - no quoting here yet
          re chkEq IR("\\x{1f608}") // it is using \x{hhhhh} notation for IR
        }
        onUreClass(
          name = "devil",
          ure = ure,
          match = lO(devil),
          matchNot = lO("a", "0", " ", "⛔", "✅", "❌", "🛑"),
          onPlatforms = lO("JVM"),
        )
      }
    }
  }
}

fun testSomeUreCharClassPairsEtc() {
  onUreClassPair(
    "chLower", "chPLower", chLower, chPLower,
    match = lO("a", "b", "x"), // on JS (only!) also matches letters like: "λ", "ξ", etc.
    matchNot = lO("A", "B", "Z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
    // verbose = true,
  )
  onUreClassPair(
    "chUpper", "chPUpper", chUpper, chPUpper,
    match = lO("A", "B", "X"), // on JS (only!) also matches letters like: "Λ", "Ξ", "Ż", etc.
    matchNot = lO("a", "b", "z", "@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
  )
  onUreClassPair(
    "chAlpha", "chPAlpha", chAlpha, chPAlpha,
    match = lO("A", "B", "X", "c", "d"), // on JS (only!) also matches letters like: "ą", "ć", "Λ", "Ξ", "Ż", etc.
    matchNot = lO("@", "#", ":", "-", ")", "¥", "₿", "₤", "2", "😈"),
  )
  onUreClassPair(
    "chDigit", "chPDigit", chDigit, chPDigit,
    match = lO("1", "2", "3", "8", "9"),
    matchNot = lO("A", "b", "c", "@", "#", ":", "-", ")", "¥", "₿", "₤", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClassPair(
    "chHexDigit", "chPHexDigit", chHexDigit, chPHexDigit,
    match = lO("1", "2", "3", "8", "9", "a", "A", "c", "E", "F"),
    matchNot = lO("@", "#", ":", "-", ")", "¥", "₿", "₤", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClassPair(
    "chAlnum", "chPAlnum", chAlnum, chPAlnum,
    match = lO("A", "B", "X", "c", "d", "1", "2", "8", "9", "0"),
    matchNot = lO("@", "#", ":", "-", ")", "¥", "₿", "₤", "😈", "ε", "β", "δ", "Λ", "Ξ", "ξ"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClassPair(
    "chPunct", "chPPunct", chPunct, chPPunct,
    match = lO(".", ",", ":", "@", "#"), // on LINUX, it also matches numbers like "2", "3", etc. Why??
    matchNot = lO("A", "a", "x", "¥", "₿", "₤", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClassPair(
    "chGraph", "chPGraph", chGraph, chPGraph,
    match = lO(".", ",", ":", "@", "#", "2", "3", "a", "B"),
    matchNot = lO("¥", "₿", "₤", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  // TODO_later: chPPrint is broken on LINUX. Doesn't match anything I tried. Report it, but for now it's deprecated.
  // onUreClassPair("chPrint", "chPPrint", chPrint, chPPrint,
  //     match = lO(".", ",", ":", "@", "#", "2", "3", "a", "B", " "),
  //     matchNot = lO("¥", "₿", "₤", "😈", "\t"),
  //     onPlatforms = lO("JVM", "LINUX"),
  // )
  // So let's just test portable version for now.
  onUreClass(
    "chPrint", chPrint,
    match = lO(".", ",", ":", "@", "#", "2", "3", "a", "B", " "),
    matchNot = lO("¥", "₿", "₤", "😈", "\t"),
  )
  onUreClassPair(
    "chWhiteSpaceInLine", "chPBlank", chWhiteSpaceInLine, chPBlank,
    match = lO(" ", "\t"),
    matchNot = lO("\n", "\r", "\u000B", "A", "a", "x", "¥", "₿", "₤", "2", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClassPair(
    "chSpace", "chPSpace", chWhiteSpace, chPWhiteSpace,
    match = lO(" ", "\t", "\n", "\r", "\u000B"),
    matchNot = lO("A", "a", "x", "¥", "₿", "₤", "2", "😈"),
    onPlatforms = lO("JVM", "LINUX"),
  )
  onUreClass(
    name = "chPCurrency", ure = chPCurrency,
    match = lO("¥", "₿", "₤"),
    matchNot = lO("@", "#", ":", "-", ")", "a", "Y", "😀", "1", "2", "😈"),
  )
  onUreClass(
    name = "chPLatin", ure = chPLatin,
    match = lO("a", "B", "Ż", "ó", "ł", "Ź"),
    matchNot = lO("@", "#", ":", "-", ")", "ε", "β", "δ", "Λ", "Ξ", "ξ", "😈"),
    onPlatforms = lO("JVM", "JS"),
  )
  onUreClass(
    name = "chPGreek", ure = chPGreek,
    match = lO("ε", "β", "δ", "Λ", "Ξ", "ξ"),
    matchNot = lO("@", "#", ":", "-", ")", "a", "B", "Ż", "ó", "ł", "Ź", "😈"),
    onPlatforms = lO("JVM", "JS"),
  )
  onUreClass(
    name = "chPExtPict", ure = chPExtPict,
    match = lO("😀", "🫠", "🥶", "😈"),
    matchNot = lO("@", "#", ":", "-", ")", "a", "b", "X", "Y", "1", "2"),
    onPlatforms = lO("JS"),
  )
}

// Warning: Sibling calls have to have different names, so USpek tree can differentiate branches.
private fun onUreClass(
  name: String,
  ure: Ure,
  match: List<String>,
  matchNot: List<String> = lO(),
  vararg useNamedArgs: Unit,
  onPlatforms: List<String> = lO("JVM", "JS", "LINUX"),
  alsoCheckNegation: Boolean = true,
  verbose: Boolean = false,
) {
  "On ure class $name on $platform" o {
    if (platform in onPlatforms) {
      ure.tstCompiles(alsoCheckNegation = alsoCheckNegation)
      ure.tstMatchCorrectInputs(match, matchNot, alsoCheckNegation = alsoCheckNegation, verbose = verbose)
    } else ure.tstDoesNotCompile()
  }
}

// Warning: Sibling calls have to have different names, so USpek tree can differentiate branches.
private fun onUreClassPair(
  namePortable: String,
  namePlatform: String,
  urePortable: Ure,
  urePlatform: Ure,
  match: List<String>,
  matchNot: List<String>,
  onPlatforms: List<String> = lO("JVM", "JS", "LINUX"),
  verbose: Boolean = false,
) {
  "On ure class pair $namePortable and $namePlatform" o {
    onUreClass(namePortable, urePortable, match, matchNot, verbose = verbose)
    onUreClass(namePlatform, urePlatform, match, matchNot, onPlatforms = onPlatforms, verbose = verbose)
  }
}


@Suppress("SpellCheckingInspection")
fun testSomeWeirdCharClasses() {

  // Note: in char classes right side of intersection (&&) always goes all the way to the closing bracket.
  // Even if right side contains union (unintuitive operator precedences)
  // See "Intersection of Multiple Classes" https://www.regular-expressions.info/charclassintersect.html
  // (search: "if you do not use square brackets around the right...")
  "On kotlin native bug with intersection of union" o {
    val ureReproducer = ureRaw("[0-9&&[1]a]")
    val ureWorkaround = ureRaw("[0-9&&[[1]a]]") // additional brackets make it work correctly on native too

    onUreClass(
      "ure workaround", ureWorkaround, // ureReproducer should work the same way
      match = lO("1"),
      matchNot = lO("a", "B", "*", "0", "3", "4", "7", "9"),
      onPlatforms = lO("JVM", "LINUX"),
      alsoCheckNegation = false,
    )
    when (platform) {
      // This is as expected.
      "JS" -> ureReproducer.tstDoesNotCompile()
      // This is as expected.
      "JVM" -> ureReproducer.tstMatchCorrectChars("1", "aB*03479", alsoCheckNegation = false)
      // This is WRONG; it should work exactly the same as on JVM
      "LINUX" -> ureReproducer.tstMatchCorrectChars("1a", "B*03479", alsoCheckNegation = false)
      else -> bad { "Unknown platform" }
    }
  }

  // Trying to reproduce bug described (search: "But Java has bugs that cause it to treat...") here:
  // https://www.regular-expressions.info/charclassintersect.html
  // It works correctly for me, so I guess it's fixed on jvm already (and works on kotlin/native too)
  "On potential java bug described in Intersection of Multiple Classes" o {
    val ureSuspicious = ureRaw("[0-9&&[12]56]")

    onUreClass(
      "ure suspicious", ureSuspicious,
      match = lO("1", "2", "5", "6"),
      matchNot = lO("a", "B", "*", "0", "3", "4", "7", "9"),
      onPlatforms = lO("JVM", "LINUX"),
      alsoCheckNegation = false,
    )
  }

  // Trying to reproduce issue described (search: "In Java and PowerGREP, negation takes precedence...") here:
  // https://www.regular-expressions.info/charclassintersect.html
  // Result: It works correctly only on JVM. Different bugs on JS and on LINUX.
  "On potential java inconsistency described in Intersection in Negated Classes" o {
    val ureNegated = !chOfAll(chOfAnyExact('1', '2', '3', '4'), chOfAnyExact('3', '4', '5', '6'))
    ureNegated chkIR "[^1234&&3456]"
    when (platform) {
      // JVM works correctly (eveything except 3 and 4)
      "JVM" -> ureNegated.tstMatchCorrectChars("&aB*01256789", "34")
      // JS compiles it but incorrectly! it doesn't really support intersection at all, but treats "&" literally.
      "JS" -> ureNegated.tstMatchCorrectChars("aB*0789", "1234&56")
      // LINUX compiles it incorrectly: as if negation was applied only to the first part of intersection.
      "LINUX" -> ureNegated.tstMatchCorrectChars("56", "1234&*079", alsoCheckNegation = false)
      // Checking "unnegated" version has to be disabled.
      // It would fail because native doesn't negate whole intersection.
      else -> bad { "Unknown platform" }
    }
  }

  // This weird test below is also to show off how expressive is USpek.
  // Using full kotlin language to dynamically generate matrix of tests.

  "On weird char class variants" o {
    val data = lO(
      // starts with 0: intersection outside (no need to wrap intersection in [..])
      "0000" to "[ [^A-C]   [^E-Z]    &&   [^G-K]   ]" tre "0129 ABCDEF     LMNZ *+-&" fo "           GHIJK         ",
      "0001" to "[ [^A-C]   [^E-Z]    &&     G-K    ]" tre "           GHIJK         " fo "0129 ABCDEF     LMNZ *+-&",
      "0010" to "[ [^A-C]     E-Z     &&   [^G-K]   ]" tre "0129    DEF     LMNZ *+-&" fo "     ABC   GHIJK         ",
      "0011" to "[ [^A-C]     E-Z     &&     G-K    ]" tre "           GHIJK         " fo "0129 ABCDEF     LMNZ *+-&",
      "0100" to "[   A-C    [^E-Z]    &&   [^G-K]   ]" tre "0129 ABCD            *+-&" fo "         EFGHIJKLMNZ     ",
      "0101" to "[   A-C    [^E-Z]    &&     G-K    ]" tre "                         " fo "0129 ABCDEFGHIJKLMNZ *+-&",
      "0110" to "[   A-C      E-Z     &&   [^G-K]   ]" tre "     ABC EF     LMNZ     " fo "0129    D  GHIJK     *+-&",
      "0111" to "[   A-C      E-Z     &&     G-K    ]" tre "           GHIJK         " fo "0129 ABCDEF     LMNZ *+-&",
      // starts with 1: union outside (intersection correctly wrapped in additional [..])
      "1000" to "[ [^A-C] [ [^E-Z]    &&   [^G-K] ] ]" tre "0129 ABCDEFGHIJKLMNZ *+-&" fo "                         ",
      "1001" to "[ [^A-C] [ [^E-Z]    &&     G-K  ] ]" tre "0129    DEFGHIJKLMNZ *+-&" fo "     ABC                 ",
      "1010" to "[ [^A-C] [   E-Z     &&   [^G-K] ] ]" tre "0129    DEFGHIJKLMNZ *+-&" fo "     ABC                 ",
      "1011" to "[ [^A-C] [   E-Z     &&     G-K  ] ]" tre "0129    DEFGHIJKLMNZ *+-&" fo "     ABC                 ",
      "1100" to "[   A-C  [ [^E-Z]    &&   [^G-K] ] ]" tre "0129 ABCD            *+-&" fo "         EFGHIJKLMNZ     ",
      "1101" to "[   A-C  [ [^E-Z]    &&     G-K  ] ]" tre "     ABC                 " fo "0129    DEFGHIJKLMNZ *+-&",
      "1110" to "[   A-C  [   E-Z     &&   [^G-K] ] ]" tre "     ABC EF     LMNZ     " fo "0129    D  GHIJK     *+-&",
      "1111" to "[   A-C  [   E-Z     &&     G-K  ] ]" tre "     ABC   GHIJK         " fo "0129    DEF     LMNZ *+-&",
    )
    data.forEachIndexed { idx, row ->
      val variant = row.a
      // Note: The one without any negation will compile on JS, but incorrectly (JS treats & chars literally).
      val fakeJS = platform == "JS" && variant == "0111"
      val wrongLinux = platform == "LINUX" && variant in lO("0010", "0011")

      "On variant $variant on $platform" o {
        val ureWeird = ureWeirdCharClass(variant)

        "ir as expected" o { ureWeird.toIR() chkEq IR(row.b.condensed) }
        "ir negated as expected" o {
          ureWeird.not().toIR() chkEq IR("[^" + row.b.condensed.drop(1))
        }

        onUreClass(
          "ure weird", ureWeird,
          match = if (fakeJS || wrongLinux) emptyList() else row.c.condensed.toList().map { it.strf },
          matchNot = if (fakeJS || wrongLinux) emptyList() else row.d.condensed.toList().map { it.strf },
          onPlatforms = if (fakeJS) lO("JVM", "LINUX", "JS") else lO("JVM", "LINUX"),
          alsoCheckNegation = false, // disabled because practically all negated fail on linux. jvm is fine.
          verbose = false, // try true to see tons of independent micro tests generated in the uspek tree :)
        )
      }
    }
  }
}

private val String.condensed get() = filterNot { it.isWhitespace() }


private fun ureWeirdCharClass(flags: String) =
  ureWeirdCharClass(flags[0] == '1', flags[1] == '1', flags[2] == '1', flags[3] == '1')

private fun ureWeirdCharClass(
  unionOutside: Boolean,
  firstPositiveAC: Boolean = true,
  orPositiveEZ: Boolean = true,
  andPositiveGK: Boolean = true,
): UreCharClass {
  val first = if (firstPositiveAC) chOf('A'..'C') else !chOf('A'..'C')
  val second = if (orPositiveEZ) chOf('E'..'Z') else !chOf('E'..'Z')
  val third = if (andPositiveGK) chOf('G'..'K') else !chOf('G'..'K')

  return if (unionOutside)
    chOfAny(first, chOfAll(second, third))
  else
    chOfAll(chOfAny(first, second), third)
}
