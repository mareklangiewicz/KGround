@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.gnupg

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kground.namelowords
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.PinentryMode.*
import pl.mareklangiewicz.udata.strf

@OptIn(DelicateApi::class)
fun gpgDecrypt(inFile: Path, outFile: Path = inFile.removeRequiredSuffix(".gpg"), cacheSymKey: Boolean = true) =
  gpg(Decrypt) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile.strf }

@OptIn(DelicateApi::class)
fun gpgEncryptSym(inFile: Path, outFile: Path = "$inFile.gpg".P, cacheSymKey: Boolean = true) =
  gpg(Symmetric) { if (!cacheSymKey) -NoSymkeyCache; -OutputFile(outFile); +inFile.strf }

@DelicateApi
@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgDecryptPass(password: String, inFile: Path, outFile: Path = inFile.removeRequiredSuffix(".gpg")) =
  gpg(Decrypt) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile.strf }

@DelicateApi
@Suppress("DEPRECATION")
@Deprecated("Be careful with password in command line - see man gpg")
fun gpgEncryptPass(password: String, inFile: Path, outFile: Path = "$inFile.gpg".P) =
  gpg(Symmetric) { -PassPhrase(password); -Batch; Pinentry(LoopBack); -OutputFile(outFile); +inFile.strf }

private fun Path.removeRequiredSuffix(suffix: CharSequence): Path =
  strf.removeSuffix(suffix).also { req(strf.length == it.strf.length - suffix.length) }.P

@DelicateApi
fun gpg(cmd: GpgCmd? = null, init: Gpg.() -> Unit = {}) = Gpg().apply { cmd?.let { -it }; init() }

/** [gnupg manual](https://gnupg.org/documentation/manuals/gnupg/Invoking-GPG.html#Invoking-GPG) */
@OptIn(DelicateApi::class)
data class Gpg(
  override val opts: MutableList<GpgOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<GpgOpt> {
  override val name get() = "gpg"
}

@DelicateApi
open class GpgCmd : GpgOpt, KOptLN() {

  /** make a signature */
  data object Sign : GpgCmd()
  /** make a clear text signature */
  data object ClearSign : GpgCmd()
  /** make a detached signature */
  data object DetachSign : GpgCmd()
  /** encrypt data */
  data object Encrypt : GpgCmd()
  /** encryption only with symmetric cipher */
  data object Symmetric : GpgCmd()
  /** decrypt data */
  data object Decrypt : GpgCmd()
  /** verify a signature */
  data object Verify : GpgCmd()
  /** list public keys */
  data object ListPublicKeys : GpgCmd()
  /** list secret keys */
  data object ListSecretKeys : GpgCmd()

  data object Help : GpgCmd()

  data object Version : GpgCmd()

  data object Warranty : GpgCmd()

  data object DumpOptions : GpgCmd()
}

@DelicateApi
interface GpgOpt : KOptTypical {

  data object Verbose : GpgOpt, KOptLN() // Don't risk short -v (ambiguity with "version")

  /** Don't make any changes (this is not completely implemented). */
  data object DryRun : GpgOpt, KOptLN()

  /** Use batch mode.  Never ask, do not allow interactive commands. */
  data object Batch : GpgOpt, KOptLN()

  /** Do not use batch mode. */
  data object NoBatch : GpgOpt, KOptLN()

  /** Print key listings delimited by colons. Useful in scripts. */
  data object WithColons : GpgOpt, KOptLN()

  /** Set the pinentry mode to mode. */
  data class Pinentry(val mode: PinentryMode = Default) : GpgOpt, KOptL("pinentry-mode", mode.namelowords(""))
  enum class PinentryMode { Default, Ask, Cancel, Error, LoopBack }

  /** create ascii armored output */
  data object Armor : GpgOpt, KOptLN()
  data object Interactive : GpgOpt, KOptLN()

  /** use canonical text mode */
  data object TextMode : GpgOpt, KOptL("textmode")

  /** use strict OpenPGP behavior */
  data object OpenPGP : GpgOpt, KOptL("openpgp")

  /** Disable the passphrase cache used for symmetrical en- and decryption. */
  data object NoSymkeyCache : GpgOpt, KOptLN()
  data class HomeDir(val dir: Path) : GpgOpt, KOptL("homedir", dir.strf)
  data class OptionsFile(val file: Path) : GpgOpt, KOptL("options", file.strf)
  data class OutputFile(val file: Path) : GpgOpt, KOptL("output", file.strf)

  /** encrypt for userid */
  data class Recipient(val userId: String) : GpgOpt, KOptLN(userId)

  /** use userid to sign or decrypt */
  data class LocalUser(val userId: String) : GpgOpt, KOptLN(userId)
  data class CompressLevel(val level: Int) : GpgOpt, KOptLN(level.strf)
  data class StatusFile(val file: Path) : GpgOpt, KOptLN(file.strf)
  data class StatusFd(val fileDescriptor: Int) : GpgOpt, KOptLN(fileDescriptor.strf)
  data class LoggerFile(val file: Path) : GpgOpt, KOptLN(file.strf)
  data class LoggerFd(val fileDescriptor: Int) : GpgOpt, KOptLN(fileDescriptor.strf)
  data class CipherAlgo(val algo: String) : GpgOpt, KOptLN(algo)
  data class DigestAlgo(val algo: String) : GpgOpt, KOptLN(algo)
  data class CompressAlgo(val algo: String) : GpgOpt, KOptLN(algo)

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
  data class PassPhraseFile(val file: Path) : GpgOpt, KOptL("passphrase-file", file.strf)

  /** Read the passphrase from file descriptor. Only the first line will be read from file descriptor n. */
  @Deprecated("dangerous - see man gpg")
  data class PassPhraseFd(val fileDescriptor: Int) : GpgOpt, KOptL("passphrase-fd", fileDescriptor.strf)

  /** Specify how many times gpg will request a new passphrase be repeated. */
  data class PassPhraseRepeat(val repeat: Int = 1) : GpgOpt, KOptL("passphrase-repeat", repeat.strf)

}
