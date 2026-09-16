package pl.mareklangiewicz.templatefull

// Kotlin/Native entry point has to return Unit, so this can NOT be an expression body over
// helloEveryOneWithSomeHtml() (it returns String) - the linker rejects it with
// "Could not find 'pl/mareklangiewicz/templatefull/main' function."
fun main() {
  println(helloEveryOneWithSomeHtml())
}
