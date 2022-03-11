@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*

fun gpgDecrypt(inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg"), cacheSymKey: Boolean = true) =
    gpg(decrypt) { if (!cacheSymKey) -nosymkeycache; -outputfile(outFile); +inFile }

fun gpgEncryptSym(inFile: String, outFile: String = "$inFile.gpg", cacheSymKey: Boolean = true) =
    gpg(symmetric) { if (!cacheSymKey) -nosymkeycache; -outputfile(outFile); +inFile }

private fun String.removeRequiredSuffix(suffix: CharSequence) =
    removeSuffix(suffix).also { require(length == it.length - suffix.length) }

fun gpg(cmd: Cmd? = null, init: Gpg.() -> Unit = {}) = Gpg(cmd).apply(init)

/** [gnupg manual](https://gnupg.org/documentation/manuals/gnupg/Invoking-GPG.html#Invoking-GPG) */
data class Gpg(
    val cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val cmdargs: MutableList<String> = mutableListOf(),
) : Kommand {

    override val name get() = "gpg"
    override val args get() = (options.flatMap { it.str } plusIfNotNull cmd?.str) + cmdargs

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNotNull arg
        object help : Option("--help")
        object version : Option("--version")
        object verbose : Option("--verbose")
        object dryrun : Option("--dry-run")
        /** create ascii armored output */
        object armor : Option("--armor")
        object interactive : Option("--interactive")
        /** use canonical text mode */
        object textmode : Option("--textmode")
        /** use strict OpenPGP behavior */
        object openpgp : Option("--openpgp")
        /** Disable the passphrase cache used for symmetrical en- and decryption. */
        object nosymkeycache : Option("--no-symkey-cache")
        data class homedir(val dir: String): Option("--homedir", dir)
        data class optionsfile(val file: String): Option("--options", file)
        data class outputfile(val file: String): Option("--output", file)
        /** encrypt for userid */
        data class recipient(val userid: String): Option("--recipient", userid)
        /** use userid to sign or decrypt */
        data class localuser(val userid: String): Option("--local-user", userid)
        data class compresslevel(val level: Int): Option("-z", level.toString())
    }

    enum class Cmd(val str: String) {
        /** make a signature */
        sign("--sign"),
        /** make a clear text signature */
        clearsign("--clear-sign"),
        /** make a detached signature */
        detachsign("--detach-sign"),
        /** encrypt data */
        encrypt("--encrypt"),
        /** encryption only with symmetric cipher */
        symmetric("--symmetric"),
        /** decrypt data */
        decrypt("--decrypt"),
        /** verify a signature */
        verify("--verify"),
        /** list keys */
        listkeys("--list-keys"),
        /** list secret keys */
        listsecretkeys("--list-secret-keys"),
    }
    operator fun String.unaryPlus() = cmdargs.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}
