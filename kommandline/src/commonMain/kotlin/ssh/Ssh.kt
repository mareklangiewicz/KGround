@file:Suppress("unused")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*


@DelicateApi
fun ssh(destination: String, kommand: Kommand, vararg options: SshOpt) =
  ssh(destination, kommand.line()) { opts.addAll(options) }

@DelicateApi
fun ssh(destination: String, command: String? = null, vararg options: SshOpt) =
  ssh(destination, command) { opts.addAll(options) }

@DelicateApi
fun ssh(destination: String, command: String? = null, init: Ssh.() -> Unit = {}) =
  Ssh().apply { +destination; command?.let { +it }; init() }

@OptIn(DelicateApi::class)
fun sshVersion() = Ssh().apply { -SshOpt.Version }

/**
 * [openssh homepage](https://www.openssh.com/)
 * [openssh manuals](https://www.openssh.com/manual.html)
 * [openbsd man ssh](https://man.openbsd.org/ssh)
 * [linux man ssh](https://man7.org/linux/man-pages/man1/ssh.1.html)
 */
@DelicateApi
data class Ssh(
  override val opts: MutableList<SshOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<SshOpt> {
  override val name get() = "ssh"
}


@DelicateApi
interface SshOpt : KOptTypical {
  data object IpV4 : KOptS("4"), SshOpt
  data object IpV6 : KOptS("6"), SshOpt
  data class AuthAgentForwarding(val enable: Boolean) : KOptS(if (enable) "A" else "a"), SshOpt

  /**
   * Bind to the address of bind_interface before attempting to connect to the destination host.
   * This is only useful on systems with more than one address.
   */
  data class BindIface(val iface: String) : KOptS("B", iface), SshOpt

  /**
   * Use bind_address on the local machine as the source address of the connection.
   * Only useful on systems with more than one address.
   */
  data class BindAddr(val addr: String) : KOptS("b", addr), SshOpt

  /**
   * Requests compression of all data
   * (including stdin, stdout, stderr, and data for forwarded X11, TCP and UNIX-domain connections).
   * The compression algorithm is the same used by gzip(1).
   * Compression is desirable on modem lines and other slow connections,
   * but will only slow down things on fast networks.
   * The default value can be set on a host-by-host basis in the configuration files;
   * see the Compression option in ssh_config(5).
   */
  data object Compress : KOptS("C"), SshOpt

  /**
   * Selects the cipher specification for encrypting the session.
   * @property spec is a comma-separated list of ciphers listed in order of preference.
   * See the Ciphers keyword in ssh_config(5) for more information.
   */
  data class Cipher(val spec: String) : KOptS("c", spec), SshOpt

  /**
   * Specifies a local "dynamic" application-level port forwarding.
   * This works by allocating a socket to listen to port on the local side,
   * optionally bound to the specified bind_address.
   * Whenever a connection is made to this port, the connection is forwarded over the secure channel,
   * and the application protocol is then used to determine where to connect to from the remote machine.
   * Currently, the SOCKS4 and SOCKS5 protocols are supported, and ssh will act as a SOCKS server.
   * Only root can forward privileged ports.
   * Dynamic port forwardings can also be specified in the configuration file.
   */
  data class DynamicForward(val localBindAddr: String? = null, val localPort: Int) :
    KOptS("D", localBindAddr col localPort), SshOpt

  /** Append debug logs to log_file instead of standard error. */
  data class LogDebugTo(val file: String) : KOptS("E", file), SshOpt

  /**
   * Sets the escape character for sessions with a pty (default: '~').
   * The escape character is only recognized at the beginning of a line.
   * The escape character followed by a dot (".") closes the connection;
   * followed by control-Z suspends the connection;
   * and followed by itself sends the escape character once.
   * Setting the character to "none" disables any escapes and makes the session fully transparent.
   */
  data class Escape(val character: String = "~") : KOptS("e", character), SshOpt

  /**
   * Specifies an alternative per-user configuration file.
   * If a configuration file is given on the command line,
   * the system-wide configuration file (/etc/ssh/ssh_config) will be ignored.
   * The default for the per-user configuration file is ~/.ssh/config.
   * If set to "none", no configuration files will be read.
   * @see <a href="https://man.openbsd.org/ssh_config.5">man ssh_config</a>
   */
  data class Config(val file: String?) : KOptS("F", file ?: "none"), SshOpt

  /**
   * Requests ssh to go to background just before command execution.
   * This is useful if ssh is going to ask for passwords or passphrases,
   * but the user wants it in the background. This implies -n.
   * The recommended way to start X11 programs at a remote site is with something like ssh -f host xterm.
   *
   * If the ExitOnForwardFailure configuration option is set to "yes",
   * then a client started with -f will wait for all remote port forwards
   * to be successfully established before placing itself in the background.
   * Refer to the description of ForkAfterAuthentication in ssh_config(5) for details.
   */
  data object RunInBackground : KOptS("f"), SshOpt

  /** Causes ssh to print its configuration after evaluating Host and Match blocks and exit. */
  data object PrintConfig : KOptS("G"), SshOpt

  /**
   * Allows remote hosts to connect to local forwarded ports.
   * If used on a multiplexed connection, then this option must be specified on the master process.
   */
  data object AllowRemoteToLocalForwardedPorts : KOptS("g"), SshOpt

  /**
   * Selects a file from which the identity (private key) for public key authentication is read.
   * You can also specify a public key file to use the corresponding private key
   * that is loaded in ssh-agent(1) when the private key file is not present locally.
   * The default is ~/.ssh/id_rsa, ~/.ssh/id_ecdsa, ~/.ssh/id_ecdsa_sk, ~/.ssh/id_ed25519,
   * ~/.ssh/id_ed25519_sk and ~/.ssh/id_dsa.
   * Identity files may also be specified on a per-host basis in the configuration file.
   * It is possible to have multiple -i options (and multiple identities specified in configuration files).
   * If no certificates have been explicitly specified by the CertificateFile directive,
   * ssh will also try to load certificate information from the filename
   * obtained by appending -cert.pub to identity filenames.
   */
  data class IdentityFile(val file: String) : KOptS("i", file), SshOpt

  /**
   * Connect to the target host by first making an ssh connection to the jump host described by destination,
   * and then establishing a TCP forwarding to the ultimate destination from there.
   * Multiple jump hops may be specified separated by comma characters.
   * This is a shortcut to specify a ProxyJump configuration directive.
   * Note that configuration directives supplied on the command-line
   * generally apply to the destination host and not any specified jump hosts.
   * Use ~/.ssh/config to specify configuration for jump hosts.
   */
  data class ProxyJump(val destination: String) : KOptS("J", destination), SshOpt


  /**
   * Specifies that connections to the given TCP port or Unix socket on the local (client) host
   * are to be forwarded to the given host and port, or Unix socket, on the remote side.
   * This works by allocating a socket to listen to either a TCP port on the local side,
   * optionally bound to the specified bind_address, or to a Unix socket.
   * Whenever a connection is made to the local port or socket,
   * the connection is forwarded over the secure channel,
   * and a connection is made to either host port,
   * or the Unix socket remote_socket, from the remote machine.
   * Port forwardings can also be specified in the configuration file.
   * Only the superuser can forward privileged ports.
   * IPv6 addresses can be specified by enclosing the address in square brackets.
   * By default, the local port is bound in accordance with the GatewayPorts setting.
   * However, an explicit bind_address may be used to bind the connection to a specific address.
   * The bind_address of "localhost" indicates that the listening port be bound for local use only,
   * while an empty address or '*' indicates that the port should be available from all interfaces.
   */
  data class LocalForward(val specFromLocalToRemote: String) : KOptS("L", specFromLocalToRemote) {

    constructor(
      vararg useNamedArgs: Unit,
      localBindAddr: String? = null,
      localPort: Int,
      remoteHost: String,
      remotePort: Int,
    ) : this(localBindAddr col localPort col remoteHost col remotePort)

    constructor(
      vararg useNamedArgs: Unit,
      localBindAddr: String? = null,
      localPort: Int,
      remoteSocket: String,
    ) : this(localBindAddr col localPort col remoteSocket)

    constructor(
      vararg useNamedArgs: Unit,
      localSocket: String,
      remoteHost: String,
      remotePort: Int,
    ) : this(localSocket col remoteHost col remotePort)

    constructor(
      vararg useNamedArgs: Unit,
      localSocket: String,
      remoteSocket: String,
    ) : this(localSocket col remoteSocket)
  }


  /**
   * Specifies that connections to the given TCP port or Unix socket
   * on the remote (server) host are to be forwarded to the local side.
   * This works by allocating a socket to listen to either a TCP port or to a Unix socket on the remote side.
   * Whenever a connection is made to this port or Unix socket,
   * the connection is forwarded over the secure channel,
   * and a connection is made from the local machine
   * to either an explicit destination specified by host port,
   * or local_socket, or, if no explicit destination was specified,
   * ssh will act as a SOCKS 4/5 proxy and forward connections
   * to the destinations requested by the remote SOCKS client.
   *
   * Port forwardings can also be specified in the configuration file.
   * Privileged ports can be forwarded only when logging in as root on the remote machine.
   * IPv6 addresses can be specified by enclosing the address in square brackets.
   *
   * By default, TCP listening sockets on the server will be bound to the loopback interface only.
   * This may be overridden by specifying a bind_address.
   * An empty bind_address, or the address '*',
   * indicates that the remote socket should listen on all interfaces.
   * Specifying a remote bind_address will only succeed
   * if the server's GatewayPorts option is enabled (see sshd_config(5)).
   *
   * If the port argument is '0', the listen port will be dynamically allocated on the server
   * and reported to the client at run time. When used together with -O forward,
   * the allocated port will be printed to the standard output.
   */
  data class RemoteForward(val specFromRemoteToLocal: String) : KOptS("R", specFromRemoteToLocal) {

    constructor(
      vararg useNamedArgs: Unit,
      remoteBindAddr: String? = null,
      remotePort: Int,
      localHost: String,
      localPort: Int,
    ) : this(remoteBindAddr col remotePort col localHost col localPort)

    constructor(
      vararg useNamedArgs: Unit,
      remoteBindAddr: String? = null,
      remotePort: Int,
      localSocket: String,
    ) : this(remoteBindAddr col remotePort col localSocket)

    constructor(
      vararg useNamedArgs: Unit,
      remoteBindAddr: String? = null,
      remotePort: Int,
    ) : this(remoteBindAddr col remotePort)

    constructor(
      vararg useNamedArgs: Unit,
      remoteSocket: String,
      localHost: String,
      localPort: Int,
    ) : this(remoteSocket col localHost col localPort)

    constructor(
      vararg useNamedArgs: Unit,
      remoteSocket: String,
      localSocket: String,
    ) : this(remoteSocket col localSocket)
  }

  /**
   * Specifies the user to log in as on the remote machine.
   * This also may be specified on a per-host basis in the configuration file.
   */
  data class LoginName(val user: String) : KOptS("l", user), SshOpt

  /**
   * Places the ssh client into "master" mode for connection sharing.
   * Multiple -M options places ssh into "master" mode
   * but with confirmation required using ssh-askpass(1)
   * before each operation that changes the multiplexing state (e.g. opening a new session).
   * Refer to the description of ControlMaster in ssh_config(5) for details.
   */
  data object MasterMode : KOptS("M"), SshOpt

  /**
   * A comma-separated list of MAC (message authentication code) algorithms, specified in order of preference.
   * See the MACs keyword in ssh_config(5) for more information.
   */
  data class MACs(val macSpec: String) : KOptS("m", macSpec), SshOpt

  /**
   * Do not execute a remote command. This is useful for just forwarding ports.
   * Refer to the description of SessionType in ssh_config(5) for details.
   */
  data object SessionTypeNone : KOptS("N"), SshOpt

  /**
   * May be used to request invocation of a subsystem on the remote system.
   * Subsystems facilitate the use of SSH as a secure transport for other applications (e.g. sftp(1)).
   * The subsystem is specified as the remote command.
   * Refer to the description of SessionType in ssh_config(5) for details.
   */
  data object SessionTypeSubSystem : KOptS("s"), SshOpt

  /**
   * Redirects stdin from /dev/null (actually, prevents reading from stdin).
   * This must be used when ssh is run in the background.
   * A common trick is to use this to run X11 programs on a remote machine.
   * For example, ssh -n shadows.cs.hut.fi emacs & will start an emacs on shadows.cs.hut.fi,
   * and the X11 connection will be automatically forwarded over an encrypted channel.
   * The ssh program will be put in the background.
   * (This does not work if ssh needs to ask for a password or passphrase; see also the -f option.)
   * Refer to the description of StdinNull in ssh_config(5) for details.
   */
  data object StdinNull : KOptS("n"), SshOpt

  /**
   * Control an active connection multiplexing master process.
   * When the -O option is specified, the ctl_cmd argument is interpreted and passed to the master process.
   * Valid commands are: "check" (check that the master process is running),
   * "forward" (request forwardings without command execution),
   * "cancel" (cancel forwardings), "exit" (request the master to exit),
   * and "stop" (request the master to stop accepting further multiplexing requests).
   */
  data class MasterCtlCmd(val ctlCmd: String) : KOptS("O", ctlCmd), SshOpt

  /**
   * Can be used to give options in the format used in the configuration file.
   * This is useful for specifying options for which there is no separate command-line flag.
   * For full details of the options, and their possible values, see ssh_config(5).
   */
  data class ConfigOption(val option: String) : KOptS("o", option), SshOpt

  /**
   * Specify a tag name that may be used to select configuration in ssh_config(5).
   * Refer to the Tag and Match keywords in ssh_config(5) for more information.
   */
  data class Tag(val tag: String) : KOptS("P", tag), SshOpt

  /**
   * Port to connect to on the remote host.
   * This can be specified on a per-host basis in the configuration file.
   */
  data class Port(val port: Int) : KOptS("p", port.toString()), SshOpt

  /** Quiet mode. Causes most warning and diagnostic messages to be suppressed. */
  data object Quiet : KOptS("q"), SshOpt

  /**
   * Disable or enable(force) PTY allocation.
   *
   * @property enable
   *   false: Disable pseudo-terminal allocation.
   *   true: Force pseudo-terminal allocation.
   *     This can be used to execute arbitrary screen-based programs on a remote machine,
   *     which can be very useful, e.g. when implementing menu services.
   *     Multiple -t options force tty allocation, even if ssh has no local tty.
   */
  data class PTY(val enable: Boolean) : KOptS(if (enable) "t" else "T"), SshOpt


  /** Display the version number and exit. */
  data object Version : KOptS("V"), SshOpt

  /**
   * Verbose mode. Causes ssh to print debugging messages about its progress.
   * This is helpful in debugging connection, authentication, and configuration problems.
   * Multiple -v options increase the verbosity. The maximum is 3.
   */
  data object Verbose : KOptS("v"), SshOpt

  /**
   * Send log information using the syslog(3) system module.
   * By default this information is sent to stderr.
   */
  data object SysLog : KOptS("y"), SshOpt

  // TODO_someday: All other options from: https://man.openbsd.org/ssh
  // TODO_maybe: rename most options here to match corresponding keywords in: https://man.openbsd.org/ssh_config.5
}

private infix fun String?.col(that: Any?) = this?.let { "$it:" }.orEmpty() + that?.toString().orEmpty()
