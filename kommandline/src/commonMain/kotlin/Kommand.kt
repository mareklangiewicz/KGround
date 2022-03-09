@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

/** anonymous kommand to use only if no actual Kommand class defined */
fun kommand(name: String, vararg args: String) = object : Kommand {
    override val name get() = name
    override val args get() = args.toList()
}

// TODO_later: full documentation in kdoc (all commands, options, etc)
//  (check in practice to make sure it's optimal for IDE users)

interface Kommand {
    val name: String
    val args: List<String>
}

fun Kommand.line() = lineBash()
fun Kommand.lineBash() = (listOf(name) + args.map { bashQuoteMetaChars(it) }).joinToString(" ")
fun Kommand.lineFun() = args.joinToString(separator = ", ", prefix = "$name(", postfix = ")")
fun Kommand.println() = println(line())


