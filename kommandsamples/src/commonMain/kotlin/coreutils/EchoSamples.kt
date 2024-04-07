package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.EchoOpt.*
import pl.mareklangiewicz.kommand.samples.*

@OptIn(DelicateApi::class)
data object EchoSamples {
  val echoHelp = echo { -Help } s "echo --help"
  val echoVersion = echo { -Version } s "echo --version"
  val echoBla = echo("bla") s "echo bla"
  val echoBlaWithoutNewLine = echo("bla", withNewLine = false) s "echo -n bla"
  val echoTwoParagraphsWithEscapes =
    echo(paragraphsEscaped, withEscapes = true) s "echo -e $paragraphsEscaped"
}

// it's escaped on echo input level. the kotlin string doesn't contain any special chars.
// that's why backslash has to be repeated here, so kotlin compiler doesn't interpret \t \n , etc. as special chars.
private const val paragraphsEscaped =
  "\\tparagraph 1 line1\\nparagraph 2 line 2\\n\\n\\tparagraph 2 line 1\\nparagraph 2 line 2"

