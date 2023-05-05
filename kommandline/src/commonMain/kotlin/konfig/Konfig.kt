package pl.mareklangiewicz.kommand.konfig

import pl.mareklangiewicz.kommand.Platform
import pl.mareklangiewicz.kommand.coreutils.*
import pl.mareklangiewicz.kommand.coreutils.MkDir.Option.parents
import pl.mareklangiewicz.upue.IMutMap
import pl.mareklangiewicz.upue.asCol

// the ".enabled" suffix is important, so it's clear the user explicitly enabled a boolean "flag"
fun Platform.userEnabled(key: String) = konfigInUserHomeConfigDir()["$key.enabled"]?.trim() == "true"

/** Represents some configuration in the form of a basic mutable map from keys:String to values:String. */
typealias IKonfig = IMutMap<String, String>

fun Platform.konfigInUserHomeConfigDir(
    isReadOnly: Boolean = false,
    checkForDangerousKeys: Boolean = true,
    checkForDangerousValues: Boolean = true,
) = konfigInDir(pathToUserHome!! + "/.config/konfig", isReadOnly, checkForDangerousKeys, checkForDangerousValues)

/**
 * Works best when values don't have special characters like new-line etc.
 * Consider wrapping it in IMutMap.asEncodedIfAbc16(..) or sth similar if you need some values with special characters.
 * Also, keys are implemented as files, so should be simple names without special chars like for example '/'.
 * I want to be able to use it over ssh and/or adb, so that's another reason to avoid special chars.
 */
fun Platform.konfigInDir(
    dir: String,
    isReadOnly: Boolean = false,
    isClrAllowed: Boolean = false,
    checkForDangerousKeys: Boolean = true,
    checkForDangerousValues: Boolean = true,
) = KonfigInDirUnsafe(dir, this)
    .withChecks(isReadOnly, isClrAllowed, checkForDangerousKeys, checkForDangerousValues)

private class KonfigInDirUnsafe(val dir: String, val platform: Platform = Platform.SYS): IKonfig {

    init { platform.run { mkdir { -parents; +dir }() } }

    override fun get(key: String): String? = platform.tryToReadFileWithCat("$dir/$key")

    override fun set(key: String, item: String?) {
        val file = "$dir/$key"
        platform.run {
            if (item == null) rmIfFileIsThere(file)
            else writeFileWithEcho(item, outFile = file)
                // TODO_someday: Use sth else to make it work on platforms without isRedirectSupported
        }
    }

    override val keys get() = platform.lsRegFiles(dir).asCol()
}

private class KonfigWithChecks(
    private val konfig: IKonfig,
    private val isReadOnly: Boolean = false,
    private val isClrAllowed: Boolean = false,
    private val checkForDangerousKeys: Boolean = true,
    private val checkForDangerousValues: Boolean = true,
): IKonfig {
    override fun clr() = when {
        isReadOnly -> error("This konfig is read only.")
        isClrAllowed -> konfig.clr()
        else -> error(
            "Forbidden. Do manual 'for (k in keys) this[k] = null' if you really want to delete ALL konfig values."
        )
    }

    override fun get(key: String): String? {
        if (checkForDangerousKeys) check(key.all { it.isSafe })
        return konfig[key]
    }

    override fun set(key: String, item: String?) {
        if (isReadOnly) error("This konfig is read only.")
        if (checkForDangerousKeys) check(key.all { it.isSafe })
        if (checkForDangerousValues && item != null) check(item.all { it.isSafe })
        konfig[key] = item
    }

    override val keys get() = konfig.keys

    private val Char.isSafe get() = isLetterOrDigit() || this == '_' || this == '.'
        // It's important to allow dots (especially in keys),
        // because it will be common to use file extensions as value types
        // (also dots are pretty safe - shells treat it as normal characters)
}

fun IKonfig.withChecks(
    isReadOnly: Boolean = false,
    isClrAllowed: Boolean = false,
    checkForDangerousKeys: Boolean = true,
    checkForDangerousValues: Boolean = true,
): IKonfig = KonfigWithChecks(this, isReadOnly, isClrAllowed, checkForDangerousKeys, checkForDangerousValues)


// TODO: implement it with kommands so it's possible to use over ssh/adb.
//  Use URE to store values as special regions with keys as names
//  (have default UREs, but let user define other (so it can for example match the java .properties format)
//  Warning: Think about characters in region markers that can interfere with kommands I use,
//  even when via ssh or adb or via some strange shell,
//  so maybe additional encoding of whole file is required for reading/writing over ssh/adb.
@Deprecated("TODO: implement")
fun Platform.konfigInFile(file: String): IKonfig = TODO()

fun IKonfig.printAll() = keys.forEach(::print)
fun IKonfig.print(key: String) = println("    konfig[\"$key\"] == \"${this[key]}\"")