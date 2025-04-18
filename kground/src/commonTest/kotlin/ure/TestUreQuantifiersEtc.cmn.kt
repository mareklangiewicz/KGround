@file:OptIn(NotPortableApi::class, DelicateApi::class)

package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.udata.toL
import pl.mareklangiewicz.ure.bad.chkIR
import pl.mareklangiewicz.ure.bad.chkMatchEntire
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.uspek.*


fun testUreQuantifiersAndAtomicGroups() {

  "On quantifiers and atomic groups" o {

    "On basic quantifier syntax" o {
      "reluctant quantifier compiles everywhere" o {
        ureRaw("a+?").tstCompiles(alsoCheckNegation = false)
      }
      "possessive quantifier does not compile on JS" o {
        ureRaw("a++").tstCompilesOnlyOn("JVM", "LINUX", alsoCheckNegation = false)
      }

      // This is kinda fine these don't compile on some platforms, these can always be simplified.
      "dangling quantifiers compile only on JVM" o {
        LO("a{4}{3}", "b*{3}", "c+{3}").forEachIndexed { i, u ->
          "ure $i: \"$u\"" o { ureRaw(u).tstCompilesOnlyOn("JVM", alsoCheckNegation = false) }
        }
      }

      // This is kinda bad, but workaround for JS is: just wrap a{2} in non-capt group.
      "legitimate quantifier composition does not compile on JS" o {
        ureRaw("a{2}+").tstCompilesOnlyOn("JVM", "LINUX", alsoCheckNegation = false)
      }

      "ure quantifiers are safe on all platforms" o {
        // Currently UreQuantifier.toClosedIR() wraps it in non-capt group to avoid any issues like above.
        // But someday I might optimize it by carefully multiplying stacked quantifiers min and max values.
        // Then some additional tests here would be necessary.
        val ure = ch('a').times(2).timesMin(1).chkIR("(?:a{2})+")
        ure.tstCompiles(alsoCheckNegation = false)
      }
    }

    val chAnyBD = chOfAnyExact('B', 'D').chkIR("[BD]")

    val ureReluctantAMP = chAnyBD then chAnyAtAll.timesAny(reluctant = true) then chAnyBD
    val ureGreedyAMP = chAnyBD then chAnyAtAll.timesAny() then chAnyBD
    val urePossessiveAMP = chAnyBD then chAnyAtAll.timesAny(possessive = true) then chAnyBD
    val ureAtomicAMP =
      chAnyBD then chAnyAtAll.timesAny().groupAtomic() then chAnyBD // should work the same as possessive
    // TODO_someday_maybe: test sth with more complex atomic groups
    val ureT27RelAMP = chAnyBD then chAnyAtAll.times(2..7, reluctant = true) then chAnyBD
    val ureT27GrAMP = chAnyBD then chAnyAtAll.times(2..7) then chAnyBD
    // TODO_someday_maybe: test sth with other flavors or .timesXXX
    "ure reluctant any-middle-part ir ok" o { ureReluctantAMP chkIR "[BD][\\s\\S]*?[BD]" }
    "ure greedy any-middle-part ir ok" o { ureGreedyAMP chkIR "[BD][\\s\\S]*[BD]" }
    "ure possessive any-middle-part ir ok" o { urePossessiveAMP chkIR "[BD][\\s\\S]*+[BD]" }
    "ure atomic any-middle-part ir ok" o { ureAtomicAMP chkIR "[BD](?>[\\s\\S]*)[BD]" }
    "ure t 2..7 reluctant any-middle-part ir ok" o { ureT27RelAMP chkIR "[BD][\\s\\S]{2,7}?[BD]" }
    "ure t 2..7 greedy any-middle-part ir ok" o { ureT27GrAMP chkIR "[BD][\\s\\S]{2,7}[BD]" }

    //  expExample list is mostly for sanity check and to align visually with the expected results below
    val expExample = LO("            aBcDe\\naBcDe\\nABCDE   ",
      "                              a....\\n.....\\n....E   ",
      "                              .B...\\n.....\\n...D.   ",
    )
    val expReluctantAMPFA = LO("      B.D                    " to 1..3,
      "                                       B.D            " to 7..9,
      "                                               B.D    " to 13..15,
    )
    val expReluctantAMPFAWO = LO("    B.D                    " to 1..3,
      "                                 D.\\n.B              " to 3..7,
      "                                       B.D            " to 7..9,
      "                                         D.\\n.B      " to 9..13,
      "                                               B.D    " to 13..15,
    )
    val expGreedyAMPFA = LO("         B...\\n.....\\n...D    " to 1..15)
    val expGreedyAMPFAWO = LO("       B...\\n.....\\n...D    " to 1..15,
      "                                 D.\\n.....\\n...D    " to 3..15,
      "                                       B...\\n...D    " to 7..15,
      "                                         D.\\n...D    " to 9..15,
      "                                               B.D    " to 13..15,
    )
    val expT27RelAMPFA = LO("         B...\\n.B              " to 1..7,
      "                                         D.\\n.B      " to 9..13,
    )
    val expT27RelAMPFAWO = LO("       B...\\n.B              " to 1..7,
      "                                 D.\\n.B              " to 3..7,
      "                                       B...\\n.B      " to 7..13,
      "                                         D.\\n.B      " to 9..13,
    )
    val expT27GrAMPFA = LO("          B...\\n...D            " to 1..9)
    val expT27GrAMPFAWO = LO("        B...\\n...D            " to 1..9,
      "                                 D.\\n...D            " to 3..9,
      "                                       B...\\n...D    " to 7..15,
      "                                         D.\\n...D    " to 9..15,
    )

    "sanity check example input" o { expExample.forEach { exampleABCDEx3.chkMatchEntire(ureRaw(it.trim())) } }

    "On ure with reluctant any-middle-part" o {
      "find all" o { ureReluctantAMP.findAll(exampleABCDEx3).chkEachResultRaw(expReluctantAMPFA) }
      "find all with overlap" o {
        ureReluctantAMP.findAllWithOverlap(exampleABCDEx3).chkEachResultRaw(expReluctantAMPFAWO)
      }
    }
    "On ure with greedy any-middle-part" o {
      "find all" o { ureGreedyAMP.findAll(exampleABCDEx3).chkEachResultRaw(expGreedyAMPFA) }
      "find all with overlap" o { ureGreedyAMP.findAllWithOverlap(exampleABCDEx3).chkEachResultRaw(expGreedyAMPFAWO) }
    }
    "On ure with possessive any-middle-part on $platform" o {
      if (platform == "JS") urePossessiveAMP.tstDoesNotCompile()
      else "finds none" o { urePossessiveAMP.findAll(exampleABCDEx3).count() chkEq 0 }
      // possessive middle eats all chars and never backtracks so last part never matches
    }
    "On ure with atomic greedy any-middle-part on $platform" o { // atomic makes greedy act like possessive
      if (platform == "JS") ureAtomicAMP.tstDoesNotCompile()
      else "finds none" o { ureAtomicAMP.findAll(exampleABCDEx3).count() chkEq 0 }
      // atomic-greedy middle eats all chars and never backtracks so last part never matches
    }
    "On ure with t 2..7 reluctant any-middle-part" o {
      "find all" o { ureT27RelAMP.findAll(exampleABCDEx3).chkEachResultRaw(expT27RelAMPFA) }
      "find all with overlap" o { ureT27RelAMP.findAllWithOverlap(exampleABCDEx3).chkEachResultRaw(expT27RelAMPFAWO) }
    }
    "On ure with t 2..7 greedy any-middle-part" o {
      "find all" o { ureT27GrAMP.findAll(exampleABCDEx3).chkEachResultRaw(expT27GrAMPFA) }
      "find all with overlap" o { ureT27GrAMP.findAllWithOverlap(exampleABCDEx3).chkEachResultRaw(expT27GrAMPFAWO) }
    }
  }
}

private fun Sequence<MatchResult>.chkEachResultRaw(exps: List<Pair<String, IntRange?>>) {
  chkEachResult(exps.map { it.second to ureRaw(it.first.trim()) })
}

internal fun Sequence<MatchResult>.chkEachResult(vararg exps: Pair<IntRange?, Ure?>) {
  chkEachResult(exps.toL)
}

internal fun Sequence<MatchResult>.chkEachResult(exps: List<Pair<IntRange?, Ure?>>) {
  var idx = 0
  for (result in this) {
    chk(idx < exps.size) { "expected only ${exps.size} results" }
    val (expRange, expVal) = exps[idx++]
    result.chkResult(expRange, expVal, "result $idx:${result.str}")
  }
  idx chkEq exps.size
}

internal fun MatchResult.chkResult(
  expRange: IntRange? = null,
  expValueMatch: Ure? = null,
  errMsgPrefix: String = "result $str",
): MatchResult = apply {
  expRange?.let { range.chkEq(it) { "$errMsgPrefix range: $range is not as expected: $it" } }
  expValueMatch?.let {
    value.chkMatchEntire(it) { "$errMsgPrefix does not match entire expected ure ir: \"${it.toIR()}\"" }
  }
}

private val MatchResult.str get() = "$range:\"${value.preview}\""

private val String.preview
  get() = toL.joinToString("", limit = 30) {
    when (it) {
      '\n' -> "\\n"; '\r' -> "\\r"; '\t' -> "\\t"; else -> "$it"
    }
  }
