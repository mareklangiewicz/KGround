@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

/** anonymous kommand to use only if no more specific Kommand class defined */
@DelicateKommandApi
fun kommand(name: String, vararg args: String): Kommand = AKommand(name, args.toList())

@DelicateKommandApi
fun String.toKommand() = split(" ").run {
    kommand(first(), *drop(1).toTypedArray())
}

// TODO_someday_maybe: full documentation in kdoc (all commands, options, etc.)
//  (check in practice to make sure it's optimal for IDE users)

interface WithName { val name: String }

/** @param args as provided to some program */
interface WithArgs { val args: List<String> }

/**
 * Important:
 * ToArgs.toArgs() is a different and more fundamental concept than WithArgs.args.
 * toArgs() always constructs full List<String> representation of given structure (like Kommand or KOpt)
 * as required by CliPlatform. So in the case of Kommand, the first element of the list will be kommand name,
 * and then all other arguments. In the case of KOpt, the toArgs() will also return full representation of
 * a particular option, usually with option name as part of the first returned element.
 * As required by parent Kommand containing given KOpt.
 * On the other hand, the WithArgs.args property holds only additional arguments of given structure (Kommand/KOpt, ..)
 * without name etc. (if structure have name and/or some other parts besides .args)
 * So WithArgs.args is more like part of source data to be processed and checked before using it by ToArgs.toArgs(),
 * and ToArgs.toArgs() is always generating kind of target "internal representation"
 * (full "internal representation" to be used by platform or some parent structure/kommand)
 * Also toArgs() should perform some checking for forbidden/inconsistent chars/data, and fail fast in case of problems.
 * (Structures like Kommand and KOpt, etc. are mutable, because they are used as convenient builders,
 * so they can contain incorrect/inconsistent data during building)
 */
interface ToArgs { fun toArgs(): List<String> }

fun Iterable<ToArgs>.toArgsFlat() = flatMap {  it.toArgs() }

interface Kommand: WithName, WithArgs, ToArgs {
    override fun toArgs() = listOf(name) + args
}


/** Anonymous/Arbitrary Kommand implementation to use only if no more specific Kommand class defined */
@DelicateKommandApi
data class AKommand(override val name: String, override val args: List<String>) : Kommand

fun Kommand.line() = lineBash()

fun Kommand.lineBash() = toArgs().joinToString(" ") { bashQuoteMetaChars(it) }

fun Kommand.logLineRaw(logln: (String) -> Unit = ::println, separator: String = " ") = logln(lineRaw(separator))

@Deprecated("Use logLineRaw")
fun Kommand.println() = logLineRaw()

fun Kommand.lineRaw(separator: String = " ") = toArgs().joinToString(separator)

@DelicateKommandApi
fun Kommand.lineFun() = args.joinToString(separator = ", ", prefix = "$name(", postfix = ")")

/** Kommand option */
interface KOpt: ToArgs

@DelicateKommandApi
interface KOptTypical: KOpt, WithName, WithArgs {
    val namePrefix: String
    val nameSeparator: String
    val argsSeparator: String

    override fun toArgs() = when {
        args.isEmpty() -> listOf("$namePrefix$name")
        nameSeparator == " " -> listOf("$namePrefix$name") + joinArgs()
        nameSeparator.any { it.isWhitespace() } -> error("nameSeparator has to be one space or cannot contain any space")
        argsSeparator == " " && args.size > 1 -> error("argsSeparator can not be space when nameSeparator isn't")
        else -> listOf("$namePrefix$name$nameSeparator" + joinArgs().single())
    }

    private fun joinArgs(): List<String> = when {
        argsSeparator == " " -> args
        argsSeparator.any { it.isWhitespace() } -> error("argsSeparator has to be one space or cannot contain any space")
        else -> listOf(args.joinToString(argsSeparator))
    }
}

/** Long form of an option */
@DelicateKommandApi
open class KOptL(
    override val name: String,
    override val args: List<String>,
    override val namePrefix: String = "--",
    override val nameSeparator: String = "=",
    override val argsSeparator: String = ",",
) : KOptTypical {
    constructor(
        name: String,
        arg: String? = null,
        namePrefix: String = "--",
        nameSeparator: String = "=",
        argsSeparator: String = ",",
    ): this(name, listOfNotNull(arg), namePrefix, nameSeparator, argsSeparator)
}

/** Short form of an option */
@DelicateKommandApi
open class KOptS(
    override val name: String,
    override val args: List<String>,
    override val namePrefix: String = "-",
    override val nameSeparator: String = " ",
    override val argsSeparator: String = " ",
) : KOptTypical {
    constructor(
        name: String,
        arg: String? = null,
        namePrefix: String = "-",
        nameSeparator: String = " ",
        argsSeparator: String = " ",
    ): this(name, listOfNotNull(arg), namePrefix, nameSeparator, argsSeparator)
}

@DelicateKommandApi
interface KommandTypical<KOptT: KOptTypical>: Kommand {
    val opts: MutableList<KOptT>
    val nonopts: MutableList<String>
    override val args get() = opts.toArgsFlat() + nonopts
    operator fun KOptT.unaryMinus() = opts.add(this)
    operator fun String.unaryPlus() = nonopts.add(this)
}

/** Anonymus/Arbitrary implementation of KommandTypical to use only if no more specific Kommand class defined */
@DelicateKommandApi
data class AKommandTypical(
    override val name: String,
    override val opts: MutableList<KOptTypical> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
    ): KommandTypical<KOptTypical>

@DelicateKommandApi
fun kommandTypical(name: String, vararg opts: KOptTypical, init: AKommandTypical.() -> Unit) =
    AKommandTypical(name, opts.toMutableList()).apply(init)

// TODO NOW: update implementations to use (where appropriate): KOptTypical, KommandTypical, DelicateKommandApi,
// TODO NOW: update implementations to data classes and objects starting with big letter (normal kotlin convention)
