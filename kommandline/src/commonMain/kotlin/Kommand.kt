@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.shell.bashQuoteMetaChars

/**
 * Anonymous kommand to use only if no more specific Kommand class defined
 *
 * General Note: The @DelicateApi annotation in KommandLine usually means that annotated construct is low level
 * and allows to generate incorrect kommands, which leads to bugs which can be very difficult to find later.
 * Try to use safer non-delicate wrappers instead, which either do not allow to create incorrect kommand lines,
 * or at least they try to "fail fast" in runtime instead of running some suspicious kommands on CLI.
 */
@DelicateApi
fun kommand(name: String, vararg args: String): Kommand = AKommand(name, args.toList())

@DelicateApi
fun String.toKommand() = split(" ").run {
  kommand(first(), *drop(1).toTypedArray())
}

// TODO_someday_maybe: full documentation in kdoc (all commands, options, etc.)
//  (check in practice to make sure it's optimal for IDE users)

interface WithName {
  val name: String
}

/** @param args as provided to some program */
interface WithArgs {
  val args: List<String>
}

/**
 * Important:
 * ToArgs.toArgs() is a different and more fundamental concept than WithArgs.args.
 * toArgs() always constructs full List<String> representation of given structure (like Kommand or KOpt)
 * as required by CLI. So in the case of Kommand, the first element of the list will be kommand name,
 * and then all other arguments. In the case of KOpt, the toArgs() will also return full representation of
 * a particular option, usually with option name as part of the first returned element.
 * As required by parent Kommand containing given KOpt.
 * On the other hand, the WithArgs.args property holds only additional arguments of given structure (Kommand/KOpt, ...)
 * without name etc. (if structure have name and/or some other parts besides .args)
 * So WithArgs.args is more like part of source data to be processed and checked before using it by ToArgs.toArgs(),
 * and ToArgs.toArgs() is always generating kind of target "internal representation"
 * (full "internal representation" to be used by CLI or some parent structure/kommand)
 * Also toArgs() should perform some checking for forbidden/inconsistent chars/data, and fail fast in case of problems.
 * (Structures like Kommand and KOpt, etc. are mutable, because they are used as convenient builders,
 * so they can contain incorrect/inconsistent data during building)
 */
interface ToArgs {
  fun toArgs(): List<String>
}

fun Iterable<ToArgs>.toArgsFlat() = flatMap { it.toArgs() }

interface Kommand : WithName, WithArgs, ToArgs {
  override fun toArgs() = listOf(name) + args
}


/** Anonymous/Arbitrary Kommand implementation to use only if no more specific Kommand class defined */
@DelicateApi
data class AKommand(override val name: String, override val args: List<String>) : Kommand

fun Kommand.line() = lineBash()

fun Kommand.lineBash() = toArgs().joinToString(" ") { bashQuoteMetaChars(it) }

fun Kommand.lineRaw(separator: String = " ") = toArgs().joinToString(separator)

@DelicateApi
fun Kommand.lineFun() = args.joinToString(separator = ", ", prefix = "$name(", postfix = ")")

/** Kommand option */
interface KOpt : ToArgs

@DelicateApi
interface KOptTypical : KOpt, WithName, WithArgs {
  val namePrefix: String
  val nameSeparator: String
  val argsSeparator: String

  override fun toArgs() = when {
    args.isEmpty() -> listOf("$namePrefix$name")
    nameSeparator == " " -> listOf("$namePrefix$name") + joinArgs()
    nameSeparator.any { it.isWhitespace() } -> bad { "nameSeparator has to be one space or cannot contain any space" }
    argsSeparator == " " && args.size > 1 -> bad { "argsSeparator can not be space when nameSeparator isn't" }
    else -> listOf("$namePrefix$name$nameSeparator" + joinArgs().single())
  }

  private fun joinArgs(): List<String> = when {
    argsSeparator == " " -> args
    argsSeparator.any { it.isWhitespace() } -> bad { "argsSeparator has to be one space or cannot contain any space" }
    else -> listOf(args.joinToString(argsSeparator))
  }
}

// Below there are three KOptTypical subclasses: KOptS, KOptL, KOptLN. These are kinda cryptic, not self-explanatory,
//   but in this case I optimize more for brevity on the call side, because they are used is so many places;
//   so they unfortunately have to be memorized by user.

/** Short form of an option */
@DelicateApi
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
  ) : this(name, listOfNotNull(arg), namePrefix, nameSeparator, argsSeparator)
}

/** Long form of an option */
@DelicateApi
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
  ) : this(name, listOfNotNull(arg), namePrefix, nameSeparator, argsSeparator)
}

/** Special form of an option, with automatically derived name as class name lowercase words separated by one hyphen */
@DelicateApi
open class KOptLN(
  override val args: List<String>,
  override val namePrefix: String = "--",
  override val nameSeparator: String = "=",
  override val argsSeparator: String = ",",
) : KOptTypical {
  constructor(
    arg: String? = null,
    namePrefix: String = "--",
    nameSeparator: String = "=",
    argsSeparator: String = ",",
  ) : this(listOfNotNull(arg), namePrefix, nameSeparator, argsSeparator)

  override val name: String get() = classlowords("-")
}

@DelicateApi
interface KommandTypical<KOptT : KOptTypical> : Kommand {
  val opts: MutableList<KOptT>
  val nonopts: MutableList<String>
  override val args get() = opts.toArgsFlat() + nonopts
  operator fun KOptT.unaryMinus() = opts.add(this)
  operator fun String.unaryPlus() = nonopts.add(this)
}

/** Anonymous/Arbitrary implementation of KommandTypical to use only if no more specific Kommand class defined */
@DelicateApi
data class AKommandTypical(
  override val name: String,
  override val opts: MutableList<KOptTypical> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<KOptTypical>

@DelicateApi
fun kommandTypical(name: String, vararg opts: KOptTypical, init: AKommandTypical.() -> Unit) =
  AKommandTypical(name, opts.toMutableList()).apply(init)

// TODO_later: update implementations to use (where appropriate): KOptTypical, KommandTypical, DelicateApi,
