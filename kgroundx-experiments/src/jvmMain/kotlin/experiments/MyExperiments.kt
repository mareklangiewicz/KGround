@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.experiments

import kotlinx.coroutines.delay
import okio.Path
import org.hildan.chrome.devtools.domains.dom.Node
import org.hildan.chrome.devtools.protocol.ChromeDPClient
import org.hildan.chrome.devtools.protocol.ExperimentalChromeApi
import org.hildan.chrome.devtools.sessions.goto
import org.hildan.chrome.devtools.sessions.newPage
import org.hildan.chrome.devtools.sessions.use
import org.w3c.dom.Node as W3CNode
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kground.logEach
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.ure.MAX
import pl.mareklangiewicz.ure.logseq.Card
import pl.mareklangiewicz.ure.logseq.processAllCardsInLogseqGraph

@OptIn(ExperimentalChromeApi::class)
@ExampleApi
object MyExperiments {

  @OptIn(DelicateApi::class, NotPortableApi::class)
  suspend fun collectGabrysCards() = buildList<Pair<Path, Card>> {
    "/home/marek/gtdgabrys".P.processAllCardsInLogseqGraph { file, card -> add(file to card) }
  }

  suspend fun collectSomeGabrysProgress() = collectGabrysCards()
    .map { (path, card) ->
      val q = card.question
      val l = card.props["card-last-reviewed"]
      val s = card.props["card-last-score"]
      "$l :: score: $s question: $q"
    }

  suspend fun playWithBrowser() {
    delay(1000)
    ChromeDPClient("http://localhost:9222").webSocket().use {
      delay(1000)
      println(it)


      // val infos = it.target.getTargets{
      //   filter = lO(FilterEntry(type = "page"))
      // }.targetInfos
      //
      // println(infos)
      //
      // val theone = infos.first { "chrome-devtools-kotlin" in it.title }
      // it.target.activateTarget(theone.targetId)
      // delay(1000)
      //
      // val pageSession = it.attachToTarget(theone.targetId).asPageSession()

      val pageSession = it.newPage {
        incognito = false
      }

      pageSession.use {
        it.goto("http://x.com")
        delay(4000)
        // This page session has access to many useful protocol domains (e.g. dom, page...)
        val doc = it.dom.getDocument {
          depth = -1
        }.root
        val lines = doc.toTextLines()
        lines.logEach()

        // val html = it.dom.getOuterHTML {
        //   nodeId = doc.nodeId
        // }.outerHTML
        // localUFileSys().writeUtf8("myexample.html".P, html)
        // println(html)
        // val base64Img = it.page.captureScreenshot {
        //   format = ScreenshotFormat.jpeg
        //   quality = 80
        // }
        // it.page.captureScreenshotToFile(java.nio.file.Path.of("img.jpg"))
        delay(3000)
      }
    }
  }

}

fun Node.toTextLines(
  depth: Int = 0,
  depthMax: Int = MAX,
  linePrefix: String = "  ".repeat(depth) + "- ",
): List<String> = when {
  depth > depthMax -> LO()
  nodeType == W3CNode.TEXT_NODE.toInt() -> LO(linePrefix + nodeValue)
  nodeType == W3CNode.ELEMENT_NODE.toInt() -> when {
    looksLikeAnyTagOf("script", "style", "meta", "link", "<style>")
      // -> LO(linePrefix + nodeName)
      -> LO()
    children.isNullOrEmpty() -> LO()
    children?.size == 1 -> toChildrenTextLines(depth, depthMax) // so flattening tags with one child
    else -> LO(linePrefix + nodeName) + toChildrenTextLines(depth + 1, depthMax)
  }
  else -> toChildrenTextLines(depth, depthMax)
  // so, flattening unknown tags (not increasing depth)
}

private fun Node.toChildrenTextLines(
  depth: Int,
  depthMax: Int,
  linePrefix: String = "  ".repeat(depth) + "- ",
): List<String> = children.orEmpty().flatMap { it.toTextLines(depth, depthMax, linePrefix) }

fun Node.looksLikeAnyTagOf(vararg tagNames: String): Boolean =
  tagNames.any { tagName -> tagName.lowercase() in LO(nodeName, localName).map { it.lowercase() } }
