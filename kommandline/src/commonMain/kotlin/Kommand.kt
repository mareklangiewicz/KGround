@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

/** anonymous kommand to use only if no more specific Kommand class defined */
fun kommand(name: String, vararg args: String): Kommand = AKommand(name, args.toList())

fun String.toKommand() = split(" ").run {
    kommand(first(), *drop(1).toTypedArray())
}

// TODO_later: full documentation in kdoc (all commands, options, etc)
//  (check in practice to make sure it's optimal for IDE users)

interface Kommand {
    val name: String
    val args: List<String>
}

/** anonymous kommand to use only if no more specific Kommand class defined */
data class AKommand(override val name: String, override val args: List<String>) : Kommand

fun Kommand.line() = lineBash()
fun Kommand.lineBash() = (listOf(name) + args.map { bashQuoteMetaChars(it) }).joinToString(" ")
fun Kommand.lineFun() = args.joinToString(separator = ", ", prefix = "$name(", postfix = ")")
fun Kommand.println() = println(line())


