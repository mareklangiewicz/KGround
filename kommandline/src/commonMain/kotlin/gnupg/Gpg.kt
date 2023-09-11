@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.PinentryMode.*

@OptIn(DelicateKommandApi::class)
fun gpgDecrypt(inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg"), cacheSymKey: Boolean = true) =
    gpg(Decrypt) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile }

@OptIn(DelicateKommandApi::class)
fun gpgEncryptSym(inFile: String, outFile: String = "$inFile.gpg", cacheSymKey: Boolean = true) =
    gpg(Symmetric) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile }

@DelicateKommandApi
@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgDecryptPass(password: String, inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg")) =
    gpg(Decrypt) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile }

@DelicateKommandApi
@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgEncryptPass(password: String, inFile: String, outFile: String = "$inFile.gpg") =
    gpg(Symmetric) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile }

private fun String.removeRequiredSuffix(suffix: CharSequence) =
    removeSuffix(suffix).also { req(length == it.length - suffix.length) }

@DelicateKommandApi
fun gpg(cmd: GpgCmd? = null, init: Gpg.() -> Unit = {}) = Gpg().apply { cmd?.let { -it }; init() }

/** [gnupg manual](https://gnupg.org/documentation/manuals/gnupg/Invoking-GPG.html#Invoking-GPG) */
@OptIn(DelicateKommandApi::class)
data class Gpg(
    override val opts: MutableList<GpgOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<GpgOpt> { override val name get() = "gpg" }

@DelicateKommandApi
interface GpgCmd : GpgOpt {

    /** make a signature */
    data object Sign: GpgCmd, KOptL("sign")
    /** make a clear text signature */
    data object ClearSign: GpgCmd, KOptL("clear-sign")
    /** make a detached signature */
    data object DetachSign: GpgCmd, KOptL("detach-sign")
    /** encrypt data */
    data object Encrypt: GpgCmd, KOptL("encrypt")
    /** encryption only with symmetric cipher */
    data object Symmetric: GpgCmd, KOptL("symmetric")
    /** decrypt data */
    data object Decrypt: GpgCmd, KOptL("decrypt")
    /** verify a signature */
    data object Verify: GpgCmd, KOptL("verify")
    /** list public keys */
    data object ListPublicKeys: GpgCmd, KOptL("list-public-keys")
    /** list secret keys */
    data object ListSecretKeys: GpgCmd, KOptL("list-secret-keys")

    data object Help : GpgCmd, KOptL("help")

    data object Version : GpgCmd, KOptL("version")

    data object Warranty : GpgCmd, KOptL("warranty")

    data object DumpOptions : GpgCmd, KOptL("dump-options")
}

@DelicateKommandApi
interface GpgOpt: KOptTypical {

    data object Verbose : GpgOpt, KOptL("verbose")

    /** Don't make any changes (this is not completely implemented). */
    data object DryRun : GpgOpt, KOptL("dry-run")

    /** Use batch mode.  Never ask, do not allow interactive commands. */
    data object Batch : GpgOpt, KOptL("batch")

    /** Do not use batch mode. */
    data object NoBatch : GpgOpt, KOptL("no-batch")

    /** Print key listings delimited by colons. Useful in scripts. */
    data object WithColons : GpgOpt, KOptL("with-colons")

    /** Set the pinentry mode to mode. */
    data class Pinentry(val mode: PinentryMode = Default) : GpgOpt, KOptL("pinentry-mode", mode.toString().lowercase())
    enum class PinentryMode { Default, Ask, Cancel, Error, LoopBack }

    /** create ascii armored output */
    data object Armor : GpgOpt, KOptL("armor")
    data object Interactive : GpgOpt, KOptL("interactive")

    /** use canonical text mode */
    data object TextMode : GpgOpt, KOptL("textmode")

    /** use strict OpenPGP behavior */
    data object OpenPGP : GpgOpt, KOptL("openpgp")

    /** Disable the passphrase cache used for symmetrical en- and decryption. */
    data object NoSymkeyCache : GpgOpt, KOptL("no-symkey-cache")
    data class HomeDir(val dir: String) : GpgOpt, KOptL("homedir", dir)
    data class OptionsFile(val file: String) : GpgOpt, KOptL("options", file)
    data class OutputFile(val file: String) : GpgOpt, KOptL("output", file)

    /** encrypt for userid */
    data class Recipient(val userId: String) : GpgOpt, KOptL("recipient", userId)

    /** use userid to sign or decrypt */
    data class LocalUser(val userId: String) : GpgOpt, KOptL("local-user", userId)
    data class CompressLevel(val level: Int) : GpgOpt, KOptL("compress-level", level.toString())
    data class StatusFile(val file: String) : GpgOpt, KOptL("status-file", file)
    data class StatusFd(val fileDescriptor: Int) : GpgOpt, KOptL("status-fd", fileDescriptor.toString())
    data class LoggerFile(val file: String) : GpgOpt, KOptL("logger-file", file)
    data class LoggerFd(val fileDescriptor: Int) : GpgOpt, KOptL("logger-fd", fileDescriptor.toString())
    data class CipherAlgo(val algo: String) : GpgOpt, KOptL("cipher-algo", algo)
    data class DigestAlgo(val algo: String) : GpgOpt, KOptL("digest-algo", algo)
    data class CompressAlgo(val algo: String) : GpgOpt, KOptL("compress-algo", algo)

    /**
     * Use string as the passphrase. This can only be used if only one passphrase is supplied.
     * Obviously, this is of very questionable security on a multi-user system.
     * Don't use this option if you can avoid it. Note that since Version 2.0
     * this passphrase is only used if the option --batch has also been given.
     * Since Version 2.1 the --pinentry-mode also needs to be set to loopback.
     */
    @Deprecated("dangerous - see man gpg")
    data class PassPhrase(val pass: String) : GpgOpt, KOptL("passphrase", pass)

    /** Read the passphrase from the file. Only the first line will be read from the file. */
    @Deprecated("dangerous - see man gpg")
    data class PassPhraseFile(val file: String) : GpgOpt, KOptL("passphrase-file", file)

    /** Read the passphrase from file descriptor. Only the first line will be read from file descriptor n. */
    @Deprecated("dangerous - see man gpg")
    data class PassPhraseFd(val fileDescriptor: Int) : GpgOpt, KOptL("passphrase-fd", fileDescriptor.toString())

    /** Specify how many times gpg will request a new passphrase be repeated. */
    data class PassPhraseRepeat(val repeat: Int = 1) : GpgOpt, KOptL("passphrase-repeat", repeat.toString())

}