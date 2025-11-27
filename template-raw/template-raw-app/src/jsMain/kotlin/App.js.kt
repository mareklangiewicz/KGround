import kotlinx.browser.*
import pl.mareklangiewicz.templateraw.*

fun main() {
  console.log("TemplateRawWebApp started.")
  console.log("Kotlin version: ${KotlinVersion.CURRENT}")
  helloEveryOneWithSomeHtml()
  tryToInstallAppIn(document.getElementById("rootForTemplateRawWebApp"))
}

