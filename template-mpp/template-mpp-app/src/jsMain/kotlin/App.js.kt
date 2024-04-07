import kotlinx.browser.*
import pl.mareklangiewicz.templatempp.*

fun main() {
  console.log("TemplateMPPWebApp started.")
  console.log("Kotlin version: ${KotlinVersion.CURRENT}")
  helloEveryOneWithSomeHtml()
  tryToInstallAppIn(document.getElementById("rootForTemplateMPPWebApp"))
}

