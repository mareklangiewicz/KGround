package pl.mareklangiewicz.templatebasic

import org.w3c.dom.*

fun tryToInstallAppIn(rootElement: Element?) {
  when (rootElement as? HTMLElement) {
    null -> console.warn("TemplateBasicWebApp: Incorrect rootElement")
    else -> {
      console.log("Template Basic Web App")
      console.log("TODO: append some html to the page.")
      console.log(helloCommon())
      console.log(helloPlatform())
      console.log(helloSomeHtml())
    }
  }
}
