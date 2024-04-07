@file:Suppress("unused")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*


@DelicateApi
fun sshKeygen(vararg options: SshKeygenOpt) = sshKeygen { opts.addAll(options) }

@DelicateApi
fun sshKeygen(init: SshKeygen.() -> Unit = {}) = SshKeygen().apply(init)

/**
 * [openbsd man ssh-keygen](https://man.openbsd.org/ssh-keygen)
 * [openssh homepage](https://www.openssh.com/)
 * [openssh manuals](https://www.openssh.com/manual.html)
 */
@DelicateApi
data class SshKeygen(
    override val opts: MutableList<SshKeygenOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<SshKeygenOpt> {
    override val name get() = "ssh-keygen"
}

@DelicateApi
interface SshKeygenOpt : KOptTypical {

    /**
     * Generate host keys of all default key types (rsa, ecdsa, and ed25519) if they do not already exist.
     * The host keys are generated with the default key file path, an empty passphrase,
     * default bits for the key type, and default comment. If -f has also been specified,
     * its argument is used as a prefix to the default path for the resulting host key files.
     * This is used by /etc/rc to generate new host keys.
     */
    data object AllHostKeys : KOptS("A"), SshKeygenOpt

    /**
     * When saving a private key, this option specifies the number of KDF
     * (key derivation function, currently bcrypt_pbkdf(3)) rounds used.
     * Higher numbers result in slower passphrase verification
     * and increased resistance to brute-force password cracking (should the keys be stolen).
     */
    data class KdfRounds(val rounds: Int = 16) : KOptS("a", rounds.toString()), SshKeygenOpt

    /** Show the bubblebabble digest of specified private or public key file. */
    data object BubbleBabble : KOptS("B"), SshKeygenOpt

    data class KeySize(val bits: Int = 3072) : KOptS("b", bits.toString()), SshKeygenOpt

    /** Provides a new comment. */
    data class CommentNew(val comment: String) : KOptS("C", comment), SshKeygenOpt

    /**
     * Requests changing the comment in the private and public key files.
     * The program will prompt for the file containing the private keys,
     * for the passphrase if the key has one, and for the new comment.
     */
    data object CommentChange : KOptS("c"), SshKeygenOpt

    /**
     * Specifies the hash algorithm used when displaying key fingerprints.
     * Valid options are: “md5” and “sha256”. The default is “sha256”.
     */
    data class FingerprintHash(val algo: String = "sha256") : KOptS("E", algo), SshKeygenOpt

    /**
     * Show fingerprint of specified public key file.
     * For RSA and DSA keys ssh-keygen tries to find the matching public key file and prints its fingerprint.
     * If combined with -v, a visual ASCII art representation of the key is supplied with the fingerprint.
     */
    data object FingerprintShow : KOptS("l"), SshKeygenOpt

    /**
     * This option will read a private or public OpenSSH key file and print to stdout a public key
     * in one of the formats specified by the -m option. The default export format is “RFC4716”.
     * This option allows exporting OpenSSH keys for use by other programs,
     * including several commercial SSH implementations.
     */
    data object ExportKey : KOptS("e"), SshKeygenOpt

    /**
     * This option will read an unencrypted private (or public) key file
     * in the format specified by the -m option
     * and print an OpenSSH compatible private (or public) key to stdout.
     * This option allows importing keys from other software,
     * including several commercial SSH implementations.
     * The default import format is “RFC4716”.
     */
    data object ImportKey : KOptS("i"), SshKeygenOpt

    /** This option will read a private OpenSSH format file and print an OpenSSH public key to stdout. */
    data object PrintPublicKey : KOptS("y"), SshKeygenOpt

    /**
     * @property host format: 'hostname|hostname:port'
     * Search for the specified hostname (with optional port number) in a known_hosts file,
     * listing any occurrences found. This option is useful to find hashed host names or addresses
     * and may also be used in conjunction with the -H option to print found keys in a hashed format.
     */
    data class KnownHostFind(val host: String) : KOptS("F", host), SshKeygenOpt

    /**
     * @property host format: 'hostname|hostname:port'
     * Removes all keys belonging to the specified hostname (with optional port number) from a known_hosts file.
     * This option is useful to delete hashed hosts (see the -H option above).
     */
    data class KnownHostRemove(val host: String) : KOptS("R", host), SshKeygenOpt

    /**
     * Specify a key format for key generation, the -i (import),
     * -e (export) conversion options, and the -p change passphrase operation.
     * The latter may be used to convert between OpenSSH private key and PEM private key formats.
     * The supported key formats are: “RFC4716” (RFC 4716/SSH2 public or private key),
     * “PKCS8” (PKCS8 public or private key) or “PEM” (PEM public key).
     * By default OpenSSH will write newly-generated private keys in its own format,
     * but when converting public keys for export the default format is “RFC4716”.
     * Setting a format of “PEM” when generating or updating a supported private key type
     * will cause the key to be stored in the legacy PEM private key format.
     */
    data class KeyFormat(val format: String = "RFC4716") : KOptS("m", format), SshKeygenOpt

    /** Specifies the filename of the key file. */
    data class KeyFile(val file: String) : KOptS("f", file), SshKeygenOpt

    /**
     * Specifies the type of key to create.
     * The possible values are “dsa”, “ecdsa”, “ecdsa-sk”, “ed25519”, “ed25519-sk”, or “rsa”.
     * This flag may also be used to specify the desired signature type when signing certificates using an RSA CA key.
     * The available RSA signature variants are “ssh-rsa” (SHA1 signatures, not recommended), “rsa-sha2-256”,
     * and “rsa-sha2-512” (the default).
     * [man page fragment](https://man.openbsd.org/ssh-keygen#t)
     */
    data class KeyType(val keyType: String = "rsa") : KOptS("t", keyType), SshKeygenOpt


    /** Provides the new passphrase. */
    data class PassPhraseNew(val newPhrase: String) : KOptS("N", newPhrase), SshKeygenOpt

    /** Provides the (old) passphrase. */
    data class PassPhraseOld(val oldPhrase: String) : KOptS("P", oldPhrase), SshKeygenOpt

    /**
     * Requests changing the passphrase of a private key file instead of creating a new private key.
     * The program will prompt for the file containing the private key, for the old passphrase,
     * and twice for the new passphrase.
     */
    data object PassPhraseChange : KOptS("p"), SshKeygenOpt

    /**
     * Specify a key/value option. These are specific to the operation that ssh-keygen has been requested to perform.
     * @see [man page fragment](https://man.openbsd.org/ssh-keygen#O)
     */
    data class Option(val option: String) : KOptS("O", option), SshKeygenOpt

    /**
     * Verbose mode. Causes ssh-keygen to print debugging messages about its progress.
     * This is helpful for debugging moduli generation.
     * Multiple -v options increase the verbosity. The maximum is 3.
     */
    data object Verbose : KOptS("v"), SshKeygenOpt

    data object Quiet : KOptS("q"), SshKeygenOpt
}
