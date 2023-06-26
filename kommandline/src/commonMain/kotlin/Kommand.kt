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

/** Kommand option */
interface KOpt {
    val name: String
    val value: String?
    val prefix: String
    val separator: String

    /**
     * Usually just one arg like: "--sort=size"
     * These args are meant to be just added to particular Kommand.args.
     * No separate property for option "name" here,
     * so usually first arg (or prefix of single arg) is actual option name.
     */
    val args: List<String> get() = when {
        value == null -> listOf("$prefix$name")
        separator == " " -> listOf("$prefix$name", value!!)
        else -> listOf("$prefix$name$separator$value")
    }
}

/** Long form of an option */
open class KOptL(
    override val name: String,
    override val value: String? = null,
    override val prefix: String = "--",
    override val separator: String = "=",
) : KOpt

/** Short form of an option */
open class KOptS(
    override val name: String,
    override val value: String? = null,
    override val prefix: String = "-",
    override val separator: String = " ",
) : KOpt
