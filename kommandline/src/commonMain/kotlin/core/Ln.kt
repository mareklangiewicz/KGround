package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LnOpt.*
import pl.mareklangiewicz.udata.strf


@OptIn(DelicateApi::class)
fun lnSymSingle(
  fileP: Path,
  linkP: Path? = null,
  vararg useNamedArgs: Unit,
  relative: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
) = lnSingle(fileP, linkP, symbolic = true, relative = relative, force = force, verbose = verbose)

/**
 * Crate a single [linkP] to [fileP].
 * @param linkP path (with name) to created link, or just name (current dir); if null then name is taken from [fileP].
 * Details: [gnu ln invocation](https://www.gnu.org/software/coreutils/manual/html_node/ln-invocation.html)
 */
@OptIn(DelicateApi::class)
fun lnSingle(
  fileP: Path,
  linkP: Path? = null,
  vararg useNamedArgs: Unit,
  symbolic: Boolean = false,
  relative: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
) = ln(fileP, linkP, symbolic = symbolic, relative = relative, force = force, verbose = verbose) { -TargetDirNone }


/**
 * Create links to all [srcPaths] in specified [targetDir], using the sources names.
 * Details: [gnu ln invocation](https://www.gnu.org/software/coreutils/manual/html_node/ln-invocation.html)
 */
@OptIn(DelicateApi::class)
fun lnInto(
  targetDir: Path,
  vararg srcPaths: Path?,
  symbolic: Boolean = false,
  relative: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
) = ln(*srcPaths, symbolic = symbolic, relative = relative, force = force, verbose = verbose) { -TargetDir(targetDir) }





/**
 * Note: This version is delicate because some path can be conditionally interpreted as "target dir" or "target",
 * depending on exact command line format.
 * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
 * Make sure to use option [TargetDir] / [lnInto] / [lnSingle] to avoid potential issues.
 */
@DelicateApi
fun ln(
  vararg paths: Path?,
  symbolic: Boolean = false,
  relative: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
  init: Ln.() -> Unit,
) = Ln(nonopts = paths.mapNotNull { it.strf }.toMutableList()).apply {
  if (symbolic) -Symbolic
  if (relative) -Relative
  if (force) -Force
  if (verbose) -Verbose
  init()
}


/**
 * [linux man](https://man7.org/linux/man-pages/man1/ln.1.html)
 * [gnu ln invocation](https://www.gnu.org/software/coreutils/manual/html_node/ln-invocation.html)
 * [backup opts](https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html)
 */
@DelicateApi
data class Ln(
  override val opts: MutableList<LnOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<LnOpt> {
  override val name get() = "ln"
}

@DelicateApi
interface LnOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), LnOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), LnOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), LnOpt
  // endregion [GNU Common Opts]

  // region [GNU Backup Opts]

  // https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html

  /**
   * Make numbered backups of files that already have them, simple backups of the others.
   * Using -b is equivalent to using --backup=existing; -b does not accept any argument.
   */
  data object BackupExisting : KOptS("b"), LnOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupExistingExplicit : KOptL("backup", "existing"), LnOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupNilExplicit : KOptL("backup", "nil"), LnOpt

  /** Never make backups */
  data object BackupOff : KOptL("backup", "off"), LnOpt

  @Deprecated("Use BackupOff which works the same.", ReplaceWith("BackupOff"))
  data object BackupNone : KOptL("backup", "none"), LnOpt

  /** Always make numbered backups. */
  data object BackupNumbered : KOptL("backup", "t"), LnOpt

  @Deprecated("Use BackupNumbered which works the same and is a bit shorter.", ReplaceWith("BackupNumbered"))
  data object BackupNumberedExplicit : KOptL("backup", "numbered"), LnOpt

  /** Always make simple (never numbered) backups. */
  data object BackupSimple : KOptL("backup", "simple"), LnOpt

  @Deprecated("Use BackupSimple which works the same and doesn't have confusing name.", ReplaceWith("BackupSimple"))
  data object BackupNeverConfusing : KOptL("backup", "never"), LnOpt

  /**
   * No method here, so it's a special case where the value of the VERSION_CONTROL environment variable is used.
   * And if VERSION_CONTROL is not set, the default backup type is ‘existing’.
   */
  data object BackupDefaultEnv : KOptL("backup"), LnOpt

  /**
   * Append suffix to each backup file made with -b.
   * If this option is not specified, the value of the SIMPLE_BACKUP_SUFFIX environment variable is used.
   * And if SIMPLE_BACKUP_SUFFIX is not set, the default is '~', just as in Emacs.
   */
  data class BackupSuffix(val suffix: String) : KOptS("S", suffix), LnOpt

  // endregion [GNU Backup Opts]

  // region [GNU Target Dir Opts]

  /**
   * @param dir will be target dir to put files into. If null there will be NO target dir.
   * (prevents traps/races with treating last arg as target dir if happen to exist and is dir or symlink to dir)
   * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
   */
  data class TargetDir(val dir: Path?) : KOptS(dir?.let { "t" } ?: "T", dir?.strf), LnOpt

  /** Same as TargetDir(null). See [TargetDir] */
  data object TargetDirNone : KOptS("T"), LnOpt

  // endregion [GNU Target Dir Opts]


  /**
   * Make symbolic links instead of hard links.
   * This option merely produces an error message on systems that do not support symbolic links.
   */
  data object Symbolic : KOptS("s"), LnOpt

  /**
   * Make symbolic links relative to the link location. This option is only valid with the [Symbolic] option.
   * Relative symbolic links are generated based on their canonicalized containing directory,
   * and canonicalized targets. I.e., all symbolic links in these file names will be resolved.
   * Details: [gnu ln invocation](https://www.gnu.org/software/coreutils/manual/html_node/ln-invocation.html)
   */
  data object Relative : KOptS("r"), LnOpt

  /**
   * Allow users with appropriate privileges to attempt to make hard links to directories.
   * However, this will probably fail due to system restrictions, even for the super-user.
   */
  @DelicateApi("Especially delicate as this will probably fail due to system restrictions, even for the super-user.")
  data object Directory : KOptLN(), LnOpt

  /**
   * If -s is NOT in effect, and the source file is a symbolic link,
   * create the hard link to the file referred to by the symbolic link,
   * rather than the symbolic link itself.
   */
  data object Logical : KOptLN(), LnOpt

  /**
   * If -s is not in effect, and the source file is a symbolic link, create the hard link to the symbolic link itself.
   * On platforms where this is not supported by the kernel, this option creates a symbolic link
   * with identical contents; since symbolic link contents cannot be edited,
   * any file name resolution performed through either link will be the same as if a hard link had been created.
   */
  data object Physical : KOptLN(), LnOpt

  /**
   * Do not treat the last operand specially when it is a symbolic link to a directory.
   * Instead, treat it as if it were a normal file.
   * When the destination is an actual directory (not a symlink to one), there is no ambiguity.
   * The link is created in that directory. But when the specified destination is a symlink to a directory,
   * there are two ways to treat the user’s request.
   * ln can treat the destination just as it would a normal directory and create the link in it.
   * On the other hand, the destination can be viewed as a non-directory – as the symlink itself.
   * In that case, ln must delete or backup that symlink before creating the new link.
   * The default is to treat a destination that is a symlink to a directory just like a directory.
   *
   * This option is weaker than the --no-target-directory (-T) option, so it has no effect if both options are given.
   */
  data object NoDereference : KOptLN(), LnOpt


  /** Print the name of each file before moving it. */
  data object Verbose : KOptLN(), LnOpt // Don't risk short -v (ambiguity with "version")

  /** Remove existing destination files. */
  data object Force : KOptLN(), LnOpt // Don't risk short -f (better to be explicit with FORCE)

  /** Prompt whether to remove existing destination files, and fail if the response is not affirmative. */
  data object Interactive : KOptS("i"), LnOpt

}
