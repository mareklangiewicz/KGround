@file:OptIn(NotPortableApi::class)

package pl.mareklangiewicz.ure.logseq

import okio.Path
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.reqTrue
import pl.mareklangiewicz.io.processSingleFile
import pl.mareklangiewicz.io.processWholeTree
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.ure.*

/**
 * Micro regular expressions for markdown/yaml content like in Logseq graphs. https://logseq.com/
 * TODO_later: Separate into more general/robust UreMarkdown.kt and UreYaml.kt
 */

// TODO_later: change ure defs below to functions for any block and it's properties (and optional sub-block tree/list).
//   Taking ureBlock as parameter (default ureWhatevaInLine). Then just use it providing #card like ure for ureBlock..

val ureCardFirstLineContent = ure {
  +chDash
  +chSpace
  +ureWhatevaInLine().withName("cardQuestion")
  +chSpace.timesAny()
  +ureText("#card")
}

val ureCardFirstLine = ureLineWithContent(ureCardFirstLineContent)

val urePropContent = ure {
  +ureIdent(allowDashesInside = true).withName("cardPropName")
  +ureText(":: ")
  +ureWhatevaInLine().withName("cardPropValue")
}

val urePropLine = ureLineWithContent(urePropContent)

val urePropLines = urePropLine.timesAny().withName("cardPropLines")

val ureCard = ure {
  +ureCardFirstLine
  +urePropLines
  // I don't want to complicate YET with capturing answer (would have to rely on indentation level..)
}

// TODO_someday: add full answer too
data class Card(val question: String, val props: Map<String, String>)

suspend fun Path.processAllCardsInLogseqGraph(
  process: suspend (file: Path, card: Card) -> Unit,
) {
  val fs = localUFileSys()
  fs.exists(this / "logseq").reqTrue { "Doesn't look like a Logseq Graph: $this/logseq doesn't exist." }
  (this / "pages").processAllCardsInAllMarkdownFiles(process)
  (this / "journals").processAllCardsInAllMarkdownFiles(process)
}

suspend fun Path.processAllCardsInAllMarkdownFiles(
  process: suspend (file: Path, card: Card) -> Unit,
) = processWholeTree { file, _, content ->
  file.name.endsWith(".md") || return@processWholeTree null
  file.processAllCardsInSingleFile { process(file, it) }
  null
}

suspend fun Path.processAllCardsInSingleFile(process: suspend (card: Card) -> Unit) =
  processSingleFile { content ->
    content.findAll(ureCard).forEach {
      val cardQuestion by it
      val cardPropLines by it
      val props: Map<String, String> = cardPropLines.findAll(urePropLine).associate {
        val cardPropName by it
        val cardPropValue by it
        cardPropName to cardPropValue
      }
      process(Card(cardQuestion, props))
    }
    null
  }
