import kotlinx.browser.*
import pl.mareklangiewicz.templatefull.*

fun main() {
  console.log("TemplateFullWebApp started.")
  console.log("Kotlin version: ${KotlinVersion.CURRENT}")
  helloEveryOneWithSomeHtml()
  tryToInstallAppIn(document.getElementById("rootForTemplateFullWebApp"))
}

