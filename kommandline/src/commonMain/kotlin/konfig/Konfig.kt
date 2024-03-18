@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.kommand.konfig

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.upue.IMutMap
import pl.mareklangiewicz.upue.asCol


/** Represents some configuration in the form of a basic mutable map from keys:String to values:String. */
typealias IKonfig = IMutMap<String, String>


// TODO NOW: use IMap stuff to implement IKonfig manipulations,
//  then use it in DepsKt to share common configurations in build files/projects.
//  Instead of Project.ext.addAllFromSystemEnvs etc..
//  (konfig dir in MYKOTLIBS repos - encrypted private key, etc)


fun CLI.konfigInUserHomeConfigDir(
    isReadOnly: Boolean = false,
    checkForDangerousKeys: Boolean = true,
    checkForDangerousValues: Boolean = true,
) = konfigInDir(pathToUserHome!! + "/.config/konfig", this, isReadOnly, checkForDangerousKeys, checkForDangerousValues)

/**
 * Works best when values don't have special characters like new-line etc.
 * Consider wrapping it in IMutMap.asEncodedIfAbc16(..) or sth similar if you need some values with special characters.
 * Also, keys are implemented as files, so should be simple names without special chars like for example '/'.
 * I want to be able to use it over ssh and/or adb, so that's another reason to avoid special chars.
 */
fun konfigInDir(
    dir: String,
    cli: CLI = CLI.SYS,
    isReadOnly: Boolean = false,
    isClrAllowed: Boolean = false,
    checkForDangerousKeys: Boolean = true,
    checkForDangerousValues: Boolean = true,
) = KonfigInDirUnsafe(dir, cli)
    .withChecks(isReadOnly, isClrAllowed, checkForDangerousKeys, checkForDangerousValues)

private class KonfigInDirUnsafe(val dir: String, val cli: CLI = CLI.SYS): IKonfig {

    init { mkdir(dir, withParents = true).execb(cli) }

    override fun get(key: String): String? =
        try { readFileWithCat("$dir/$key").execb(cli).joinToString("\n") } catch (e: RuntimeException) { null }

    override fun set(key: String, item: String?) {
        val file = "$dir/$key"
        cli.run {
            if (item == null) rmIfFileExists(file).execb(CLI.SYS)
            else writeFileWithDD(inLines = listOf(item), outFile = file).execb(CLI.SYS)
        }
    }

    override val keys get() = lsRegFiles(dir).execb(cli).asCol()
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
        if (checkForDangerousKeys) chk(key.all { it.isSafe })
        return konfig[key]
    }

    override fun set(key: String, item: String?) {
        if (isReadOnly) error("This konfig is read only.")
        if (checkForDangerousKeys) chk(key.all { it.isSafe })
        if (checkForDangerousValues && item != null) chk(item.all { it.isSafe })
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
fun CLI.konfigInFile(file: String): IKonfig = TODO()

fun IKonfig.logEachKeyVal(logln: (String) -> Unit = ::println) = keys.forEach { logKeyVal(it, logln) }

fun IKonfig.logKeyVal(key: String, logln: (String) -> Unit = ::println) = logln("konfig[\"$key\"] == \"${this[key]}\"")