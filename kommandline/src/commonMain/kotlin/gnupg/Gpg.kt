@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.PinentryMode.*

fun gpgDecrypt(inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg"), cacheSymKey: Boolean = true) =
    gpg(Decrypt) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile }

fun gpgEncryptSym(inFile: String, outFile: String = "$inFile.gpg", cacheSymKey: Boolean = true) =
    gpg(Symmetric) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile }

@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgDecryptPass(password: String, inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg")) =
    gpg(Decrypt) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile }

@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgEncryptPass(password: String, inFile: String, outFile: String = "$inFile.gpg") =
    gpg(Symmetric) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile }

private fun String.removeRequiredSuffix(suffix: CharSequence) =
    removeSuffix(suffix).also { req(length == it.length - suffix.length) }

fun gpg(cmd: Cmd? = null, init: Gpg.() -> Unit = {}) = Gpg(cmd).apply(init)

/** [gnupg manual](https://gnupg.org/documentation/manuals/gnupg/Invoking-GPG.html#Invoking-GPG) */
data class Gpg(
    val cmd: Cmd? = null,
    val options: MutableList<Option> = mutableListOf(),
    val cmdargs: MutableList<String> = mutableListOf(),
) : Kommand {

    override val name get() = "gpg"
    override val args get() = options.flatMap { it.str }.plusIfNN(cmd?.str) + cmdargs

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNN arg
        data object Help : Option("--help")
        data object Version : Option("--version")
        data object Verbose : Option("--verbose")
        data object DryRun : Option("--dry-run")
        /** Use batch mode.  Never ask, do not allow interactive commands. */
        data object Batch : Option("--batch")
        /** Do not use batch mode. */
        data object NoBatch : Option("--no-batch")
        /** Print key listings delimited by colons. Useful in scripts. */
        data object WithColons : Option("--with-colons")
        /** Set the pinentry mode to mode. */
        data class Pinentry(val mode: PinentryMode = Default): Option("--pinentry-mode", mode.toString())
        enum class PinentryMode { Default, Ask, Cancel, Error, LoopBack;
            override fun toString() = super.toString().lowercase()
        }
        /** create ascii armored output */
        data object Armor : Option("--armor")
        data object Interactive : Option("--interactive")
        /** use canonical text mode */
        data object TextMode : Option("--textmode")
        /** use strict OpenPGP behavior */
        data object OpenPGP : Option("--openpgp")
        /** Disable the passphrase cache used for symmetrical en- and decryption. */
        data object NoSymkeyCache : Option("--no-symkey-cache")
        data class HomeDir(val dir: String): Option("--homedir", dir)
        data class OptionsFile(val file: String): Option("--options", file)
        data class OutputFile(val file: String): Option("--output", file)
        /** encrypt for userid */
        data class Recipient(val userId: String): Option("--recipient", userId)
        /** use userid to sign or decrypt */
        data class LocalUser(val userId: String): Option("--local-user", userId)
        data class CompressLevel(val level: Int): Option("-z", level.toString())
        data class StatusFile(val file: String): Option("--status-file", file)
        data class StatusFd(val fileDescriptor: Int): Option("--status-fd", fileDescriptor.toString())
        data class LoggerFile(val file: String): Option("--logger-file", file)
        data class LoggerFd(val fileDescriptor: Int): Option("--logger-fd", fileDescriptor.toString())
        data class CipherAlgo(val algo: String): Option("--cipher-algo", algo)
        data class DigestAlgo(val algo: String): Option("--digest-algo", algo)
        data class CompressAlgo(val algo: String): Option("--compress-algo", algo)

        /**
         * Use  string  as the passphrase. This can only be used if only one passphrase is supplied.
         * Obviously, this is of very questionable  security  on  a  multi-user  system.
         * Don't use this option if you can avoid it. Note  that  since Version 2.0
         * this passphrase is only used if the option --batch has also been given.
         * Since Version 2.1 the --pinentry-mode also needs to be set to loopback.
         */
        @Deprecated("dangerous - see man gpg")
        data class PassPhrase(val pass: String): Option("--passphrase", pass)

        /** Read the passphrase from file. Only the first line will be read from file. */
        @Deprecated("dangerous - see man gpg")
        data class PassPhraseFile(val file: String): Option("--passphrase-file", file)

        /** Read the passphrase from file descriptor. Only the first line will  be  read  from file descriptor n. */
        @Deprecated("dangerous - see man gpg")
        data class PassPhraseFd(val fileDescriptor: Int): Option("--passphrase-fd", fileDescriptor.toString())

        /** Specify how many times gpg will request a new passphrase be repeated. */
        data class PassPhraseRepeat(val repeat: Int = 1): Option("--passphrase-repeat", repeat.toString())
    }

    enum class Cmd(val str: String) {
        /** make a signature */
        Sign("--sign"),
        /** make a clear text signature */
        ClearSign("--clear-sign"),
        /** make a detached signature */
        DetachSign("--detach-sign"),
        /** encrypt data */
        Encrypt("--encrypt"),
        /** encryption only with symmetric cipher */
        Symmetric("--symmetric"),
        /** decrypt data */
        Decrypt("--decrypt"),
        /** verify a signature */
        Verify("--verify"),
        /** list keys */
        ListKeys("--list-keys"),
        /** list secret keys */
        ListSecretKeys("--list-secret-keys"),
    }
    operator fun String.unaryPlus() = cmdargs.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}
