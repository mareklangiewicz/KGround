import kotlinx.browser.*
import pl.mareklangiewicz.templatebasic.*

fun main() {
  console.log("TemplateBasicWebApp started.")
  console.log("Kotlin version: ${KotlinVersion.CURRENT}")
  helloEveryOneWithSomeHtml()
  tryToInstallAppIn(document.getElementById("rootForTemplateBasicWebApp"))
}

