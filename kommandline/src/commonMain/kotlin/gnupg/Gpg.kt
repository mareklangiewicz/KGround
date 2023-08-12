@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.gnupg

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Cmd.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.*
import pl.mareklangiewicz.kommand.gnupg.Gpg.Option.pinentryMode.*

fun gpgDecrypt(inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg"), cacheSymKey: Boolean = true) =
    gpg(decrypt) { if (!cacheSymKey) -nosymkeycache; -outputfile(outFile); +inFile }

fun gpgEncryptSym(inFile: String, outFile: String = "$inFile.gpg", cacheSymKey: Boolean = true) =
    gpg(symmetric) { if (!cacheSymKey) -nosymkeycache; -outputfile(outFile); +inFile }

@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgDecryptPass(password: String, inFile: String, outFile: String = inFile.removeRequiredSuffix(".gpg")) =
    gpg(decrypt) { -passphrase(password); -batch; pinentry(LOOPBACK); -outputfile(outFile); +inFile }

@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgEncryptPass(password: String, inFile: String, outFile: String = "$inFile.gpg") =
    gpg(symmetric) { -passphrase(password); -batch; pinentry(LOOPBACK); -outputfile(outFile); +inFile }

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
        data object help : Option("--help")
        data object version : Option("--version")
        data object verbose : Option("--verbose")
        data object dryrun : Option("--dry-run")
        /** Use batch mode.  Never ask, do not allow interactive commands. */
        data object batch : Option("--batch")
        /** Do not use batch mode. */
        data object nobatch : Option("--no-batch")
        /** Print key listings delimited by colons. Useful in scripts. */
        data object withcolons : Option("--with-colons")
        /** Set the pinentry mode to mode. */
        data class pinentry(val mode: pinentryMode = DEFAULT): Option("--pinentry-mode", mode.toString())
        enum class pinentryMode { DEFAULT, ASK, CANCEL, ERROR, LOOPBACK;
            override fun toString() = super.toString().lowercase()
        }
        /** create ascii armored output */
        data object armor : Option("--armor")
        data object interactive : Option("--interactive")
        /** use canonical text mode */
        data object textmode : Option("--textmode")
        /** use strict OpenPGP behavior */
        data object openpgp : Option("--openpgp")
        /** Disable the passphrase cache used for symmetrical en- and decryption. */
        data object nosymkeycache : Option("--no-symkey-cache")
        data class homedir(val dir: String): Option("--homedir", dir)
        data class optionsfile(val file: String): Option("--options", file)
        data class outputfile(val file: String): Option("--output", file)
        /** encrypt for userid */
        data class recipient(val userid: String): Option("--recipient", userid)
        /** use userid to sign or decrypt */
        data class localuser(val userid: String): Option("--local-user", userid)
        data class compresslevel(val level: Int): Option("-z", level.toString())
        data class statusfile(val file: String): Option("--status-file", file)
        data class statusfd(val filedescriptor: Int): Option("--status-fd", filedescriptor.toString())
        data class loggerfile(val file: String): Option("--logger-file", file)
        data class loggerfd(val filedescriptor: Int): Option("--logger-fd", filedescriptor.toString())
        data class cipheralgo(val algo: String): Option("--cipher-algo", algo)
        data class digestalgo(val algo: String): Option("--digest-algo", algo)
        data class compressalgo(val algo: String): Option("--compress-algo", algo)

        /**
         * Use  string  as the passphrase. This can only be used if only one passphrase is supplied.
         * Obviously, this is of very questionable  security  on  a  multi-user  system.
         * Don't use this option if you can avoid it. Note  that  since Version 2.0
         * this passphrase is only used if the option --batch has also been given.
         * Since Version 2.1 the --pinentry-mode also needs to be set to loopback.
         */
        @Deprecated("dangerous - see man gpg")
        data class passphrase(val pass: String): Option("--passphrase", pass)

        /** Read the passphrase from file. Only the first line will be read from file. */
        @Deprecated("dangerous - see man gpg")
        data class passphrasefile(val file: String): Option("--passphrase-file", file)

        /** Read the passphrase from file descriptor. Only the first line will  be  read  from file descriptor n. */
        @Deprecated("dangerous - see man gpg")
        data class passphrasefd(val filedescriptor: Int): Option("--passphrase-fd", filedescriptor.toString())

        /** Specify how many times gpg will request a new passphrase be repeated. */
        data class passphraserepeat(val repeat: Int = 1): Option("--passphrase-repeat", repeat.toString())
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
