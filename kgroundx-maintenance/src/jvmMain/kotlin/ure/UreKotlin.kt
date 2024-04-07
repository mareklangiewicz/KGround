package pl.mareklangiewicz.ure

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.ure.core.Ure
import kotlin.text.RegexOption.*

fun urePackageLine(withNamePrefix: String = "ktPackage") = ureKtKeywordLine("package", withNamePrefix)
fun ureImportLine(withNamePrefix: String = "ktImport") = ureKtKeywordLine("import", withNamePrefix)

fun ureKtKeywordLine(keyword: String, withNamePrefix: String = keyword) =
  ureLineWithContent(
    ureKeywordAndOptArg(
      keyword = keyword,
      arg = ureChain(ureIdent(), chDot).withName(withNamePrefix + "Name"),
    ),
  ).withName(withNamePrefix + "Line")


@OptIn(DelicateApi::class, NotPortableApi::class)
private val ureLicenceMarker = (ureText("licence") or ureText("copyright")).withOptionsEnabled(IGNORE_CASE)

fun ureLicenceComment(licenceMarker: Ure = ureLicenceMarker, withName: String = "ktLicenceComment") = ure {
  +ureWhateva()
  +licenceMarker
  +ureWhateva()
}.commentedOut(traditional = true).withName(withName)

fun ureKtComposeTestOutline() = ure {
  +ureLicenceComment().withOptSpacesAround()
  +ureWhateva(reluctant = false).withName("ktOtherStuffBeforePackageLine")
  +urePackageLine().withOptSpacesAround()
  +ureWhateva(reluctant = false).withName("ktRest")
}

fun ureKtOutline(withNamePrefix: String = "ktPart") = ure {
  TODO("NOW")
}


