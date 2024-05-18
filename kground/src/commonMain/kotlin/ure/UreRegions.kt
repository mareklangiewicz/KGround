package pl.mareklangiewicz.ure


import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.ure.core.Ure


@ExperimentalApi("I might change which chars are allowed in region labels, or at least default settings")
fun ureAnyRegionLabel(
  vararg useNamedArgs: Unit,
  lengthMin: Int = 1,
  lengthMax: Int = 1000,
  reluctant: Boolean = true,
  allowTildes: Boolean = true,
  allowBrackets: Boolean = true,
) = ure {
  x(lengthMin..lengthMax, reluctant = reluctant) of !chOfAnyExact(
    '\r', '\n', '['.takeUnless { allowBrackets }, ']'.takeUnless { allowBrackets }, '~'.takeUnless { allowTildes }
  )
}

@OptIn(ExperimentalApi::class)
@DelicateApi("regionLabelName must be unique (don't try nested regions, etc.); default regionLabel Ure is experimental.")
fun ureRegion(
  content: Ure = ureWhateva(),
  vararg useNamedArgs: Unit,
  regionLabel: Ure = ureAnyRegionLabel(),
  regionLabelName: String = "theRegionLabelMatched",
  lineNameFirst: String? = null, // name of ure that will capture line like: // region Some Region Label
  lineNameLast: String? = null, // name of ure that will capture line like: // endregion Some Region Label
) = ure {
  +ureCommentLine(ureKeywordAndOptArg("region", regionLabel.withName(regionLabelName)), traditional = false)
    .withName(lineNameFirst)
  +content
  +ureCommentLine(ureKeywordAndOptArg("endregion", ureRef(name = regionLabelName)), traditional = false)
    .withName(lineNameLast)
}

/**
 * By "special" we mean region with label wrapped in double squared brackets, and the promise is:
 * all special regions with some label should contain the same content (synced)
 * modulo optional postprocessing with very special arrows region [[$regionLabel <~~]]
 * [specialLabel] is a part of full regionLabel without prefix and postfix
 * [regionLabelPrefix] or [regionLabelPostfix] should almost never be changed, because it breaks "the promise",
 * except cases like "very special region" with regionLabelPostfix = " <~~]]"
 */
@Suppress("GrazieInspection")
@OptIn(ExperimentalApi::class)
@DelicateApi
fun ureSpecialRegion(
  content: Ure = ureWhateva(),
  vararg useNamedArgs: Unit,
  specialLabel: Ure = ureAnyRegionLabel(allowTildes = false, allowBrackets = false),
  regionLabelPrefix: String = "[[",
  regionLabelPostfix: String = "]]",
  regionLabelName: String = "theRegionLabelMatched",
  lineNameFirst: String? = null, // name of ure that will capture line like: // region [[Some Special Label]]
  lineNameLast: String? = null, // name of ure that will capture line like: // endregion [[Some Special Label]]
) = ureRegion(
  content,
  regionLabel = ureText(regionLabelPrefix) then specialLabel then ureText(regionLabelPostfix),
  regionLabelName = regionLabelName,
  lineNameFirst = lineNameFirst,
  lineNameLast = lineNameLast,
)

// TODO: support "Very Special Arrow Regions" like [[My Settings Stuff <~~]] that postprocess special regions contents
