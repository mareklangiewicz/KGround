@file:Suppress("unused")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.kommand.*


@DelicateKommandApi
fun sshKeygen(vararg options: SshKeygenOpt) = sshKeygen { opts.addAll(options) }

@DelicateKommandApi
fun sshKeygen(init: SshKeygen.() -> Unit = {}) = SshKeygen().apply(init)

/**
 * [openbsd man ssh-keygen](https://man.openbsd.org/ssh-keygen)
 * [openssh homepage](https://www.openssh.com/)
 * [openssh manuals](https://www.openssh.com/manual.html)
 */
@DelicateKommandApi
class SshKeygen(
    override val opts: MutableList<SshKeygenOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<SshKeygenOpt> {
    override val name get() = "ssh-keygen"
}

// TODO NOW: -f -N -p -c -m

@DelicateKommandApi
interface SshKeygenOpt: KOptTypical {

    /**
     * Generate host keys of all default key types (rsa, ecdsa, and ed25519) if they do not already exist.
     * The host keys are generated with the default key file path, an empty passphrase,
     * default bits for the key type, and default comment. If -f has also been specified,
     * its argument is used as a prefix to the default path for the resulting host key files.
     * This is used by /etc/rc to generate new host keys.
     */
    data object AllHostKeys: KOptS("A"), SshKeygenOpt

    /**
     * When saving a private key, this option specifies the number of KDF
     * (key derivation function, currently bcrypt_pbkdf(3)) rounds used.
     * Higher numbers result in slower passphrase verification
     * and increased resistance to brute-force password cracking (should the keys be stolen).
     */
    data class KdfRounds(val rounds: Int = 16): KOptS("a", rounds.toString()), SshKeygenOpt

    /** Show the bubblebabble digest of specified private or public key file. */
    data object BubbleBabble: KOptS("B"), SshKeygenOpt

    data class KeySize(val bits: Int = 3072): KOptS("b", bits.toString()), SshKeygenOpt

    /** Provides a new comment. */
    data class CommentNew(val comment: String): KOptS("C", comment), SshKeygenOpt

    /**
     * Requests changing the comment in the private and public key files.
     * The program will prompt for the file containing the private keys,
     * for the passphrase if the key has one, and for the new comment.
     */
    data object CommentChange: KOptS("c"), SshKeygenOpt

    /**
     * Specifies the hash algorithm used when displaying key fingerprints.
     * Valid options are: “md5” and “sha256”. The default is “sha256”.
     */
    data class FingerprintHash(val algo: String = "sha256"): KOptS("E", keyType), SshKeygenOpt

    /**
     * This option will read a private or public OpenSSH key file and print to stdout a public key
     * in one of the formats specified by the -m option. The default export format is “RFC4716”.
     * This option allows exporting OpenSSH keys for use by other programs,
     * including several commercial SSH implementations.
     */
    data object ExportKey: KOptS("e"), SshKeygenOpt

    /**
     * This option will read an unencrypted private (or public) key file
     * in the format specified by the -m option
     * and print an OpenSSH compatible private (or public) key to stdout.
     * This option allows importing keys from other software,
     * including several commercial SSH implementations.
     * The default import format is “RFC4716”.
     */
    data object ImportKey: KOptS("i"), SshKeygenOpt

    /**
     * @property host format: 'hostname|[hostname]:port'
     * Search for the specified hostname (with optional port number) in a known_hosts file,
     * listing any occurrences found. This option is useful to find hashed host names or addresses
     * and may also be used in conjunction with the -H option to print found keys in a hashed format.
     */
    data class FindKnownHost(val host: String): KOptS("F", keyType), SshKeygenOpt

    /** Specifies the filename of the key file. */
    data class KeyFile(val file: String): KOptS("f", file), SshKeygenOpt

    /**
     * Specifies the type of key to create.
     * The possible values are “dsa”, “ecdsa”, “ecdsa-sk”, “ed25519”, “ed25519-sk”, or “rsa”.
     * This flag may also be used to specify the desired signature type when signing certificates using an RSA CA key.
     * The available RSA signature variants are “ssh-rsa” (SHA1 signatures, not recommended), “rsa-sha2-256”,
     * and “rsa-sha2-512” (the default).
     * [man page fragment](https://man.openbsd.org/ssh-keygen#t)
     */
    data class KeyType(val keyType: String = "rsa"): KOptS("t", keyType), SshKeygenOpt

    /**
     * Verbose mode. Causes ssh-keygen to print debugging messages about its progress.
     * This is helpful for debugging moduli generation.
     * Multiple -v options increase the verbosity. The maximum is 3.
     */
    data object Verbose: KOptS("v"), SshKeygenOpt

    data object Quiet: KOptS("q"), SshKeygenOpt
}
