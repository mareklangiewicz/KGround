package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.namelowords
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.MvOpt.*
import pl.mareklangiewicz.udata.strf

/**
 * Move/rename single [src] to [dst]. If possible it just renames, but if not (dst on different file system etc),
 * then tries to copy as if "cp -a" and remove src if succeeded (if copy fails it removes any partial copy).
 * Details: [gnu mv invocation](https://www.gnu.org/software/coreutils/manual/html_node/mv-invocation.html)
 */
@OptIn(DelicateApi::class)
fun mvSingle(
  src: Path,
  dst: Path,
  vararg useNamedArgs: Unit,
  force: Boolean = false,
  verbose: Boolean = false,
) = mv(src, dst, force = force, verbose = verbose) { -TargetDirNone }

/**
 * Move/rename each [srcPaths] into the specified [targetDir], using the sources names.
 * Details: [gnu mv invocation](https://www.gnu.org/software/coreutils/manual/html_node/mv-invocation.html)
 */
@OptIn(DelicateApi::class)
fun mvInto(
  targetDir: Path,
  vararg srcPaths: Path?,
  force: Boolean = false,
  verbose: Boolean = false,
) = mv(*srcPaths, force = force, verbose = verbose) { -TargetDir(targetDir) }

@OptIn(DelicateApi::class)
fun mvExchange(
  first: Path,
  second: Path,
  vararg useNamedArgs: Unit,
  force: Boolean = false,
  verbose: Boolean = false,
) = mv(first, second, force = force, verbose = verbose) { -TargetDirNone; -NoCopy; -Exchange }

/**
 * Note1: As [Path] kdoc says: The only path that ends with "/" is the file system root "/".
 * So we avoid POSIX gotcha with dereferencing symlinks, so we don't have to use [MvOpt.StripTrailingSlashes].
 * Details: https://www.gnu.org/software/coreutils/manual/html_node/Trailing-slashes.html
 *
 * Note2: This version is delicate because last Path can be conditionally interpreted as "target dir"
 * (if it happen to exist and is dir/symlink-to-dir). It's popular use-case, but can cause race conditions etc.
 * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
 * Make sure to use option [MvOpt.TargetDir] / [mvInto] / [mvSingle] to avoid potential issues.
 */
@DelicateApi
fun mv(
  vararg paths: Path?,
  force: Boolean = false,
  verbose: Boolean = false,
  init: Mv.() -> Unit,
) = Mv(nonopts = paths.mapNotNull { it?.strf }.toMutableList())
  .apply { if (force) -Force; if (verbose) -Verbose; init() }


/**
 * [linux man](https://man7.org/linux/man-pages/man1/mv.1.html)
 * [gnu mv invocation](https://www.gnu.org/software/coreutils/manual/html_node/mv-invocation.html)
 * [backup opts](https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html)
 */
@DelicateApi
data class Mv(
  override val opts: MutableList<MvOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<MvOpt> {
  override val name get() = "mv"
}

// TODO_later: core kommands: cp, install, ln, mv (all important have the same backup/target-dir options which is nice)
// TODO_later: core kommands: df, du, ls (all are important and have the same block size options which is nice)

@DelicateApi
interface MvOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), MvOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), MvOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), MvOpt
  // endregion [GNU Common Opts]

  // region [GNU Backup Opts]

  // https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html

  /**
   * Make numbered backups of files that already have them, simple backups of the others.
   * Using -b is equivalent to using --backup=existing; -b does not accept any argument.
   */
  data object BackupExisting : KOptS("b"), MvOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupExistingExplicit : KOptL("backup", "existing"), MvOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupNilExplicit : KOptL("backup", "nil"), MvOpt

  /** Never make backups */
  data object BackupOff : KOptL("backup", "off"), MvOpt

  @Deprecated("Use BackupOff which works the same.", ReplaceWith("BackupOff"))
  data object BackupNone : KOptL("backup", "none"), MvOpt

  /** Always make numbered backups. */
  data object BackupNumbered : KOptL("backup", "t"), MvOpt

  @Deprecated("Use BackupNumbered which works the same and is a bit shorter.", ReplaceWith("BackupNumbered"))
  data object BackupNumberedExplicit : KOptL("backup", "numbered"), MvOpt

  /** Always make simple (never numbered) backups. */
  data object BackupSimple : KOptL("backup", "simple"), MvOpt

  @Deprecated("Use BackupSimple which works the same and doesn't have confusing name.", ReplaceWith("BackupSimple"))
  data object BackupNeverConfusing : KOptL("backup", "never"), MvOpt

  /**
   * No method here, so it's a special case where the value of the VERSION_CONTROL environment variable is used.
   * And if VERSION_CONTROL is not set, the default backup type is ‘existing’.
   */
  data object BackupDefaultEnv : KOptL("backup"), MvOpt

  /**
   * Append suffix to each backup file made with -b.
   * If this option is not specified, the value of the SIMPLE_BACKUP_SUFFIX environment variable is used.
   * And if SIMPLE_BACKUP_SUFFIX is not set, the default is '~', just as in Emacs.
   */
  data class BackupSuffix(val suffix: String) : KOptS("S", suffix), MvOpt

  // endregion [GNU Backup Opts]

  // region [GNU Target Dir Opts]

  /**
   * @param dir will be target dir to put files into. If null there will be NO target dir.
   * (prevents traps/races with treating last arg as target dir if happen to exist and is dir or symlink to dir)
   * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
   */
  data class TargetDir(val dir: Path?) : KOptS(dir?.let { "t" } ?: "T", dir?.strf), MvOpt

  /** Same as TargetDir(null). See [TargetDir] */
  data object TargetDirNone : KOptS("T"), MvOpt

  // endregion [GNU Target Dir Opts]

  /** Print the name of each file before moving it. */
  data object Verbose : KOptLN(), MvOpt // Don't risk short -v (ambiguity with "version")

  /** Print extra information to stdout, explaining how files are copied. This option implies [Verbose]. */
  data object VerboseDebug : KOptL("debug"), MvOpt

  /**
   * Do not prompt the user before removing a destination file.
   * If you specify more than one of the -i, -f, -n options, only the final one takes effect.
   */
  data object Force : KOptLN(), MvOpt // Don't risk short -f (better to be explicit with FORCE)

  /**
   * Prompt whether to overwrite each existing destination file, regardless of its permissions,
   * and fail if the response is not affirmative.
   * If you specify more than one of the -i, -f, -n options, only the final one takes effect.
   */
  data object Interactive : KOptS("i"), MvOpt

  /**
   * Do not overwrite an existing file; silently fail instead.
   * If you specify more than one of the -i, -f, -n options, only the final one takes effect.
   * This option is mutually exclusive with -b or --backup option.
   * See also the Update(None) [Update] which will skip existing files but not fail.
   */
  data object NoClobber : KOptS("n"), MvOpt

  /**
   * If a file cannot be renamed because the destination file system differs,
   * fail with a diagnostic instead of copying and then removing the file.
   */
  data object NoCopy : KOptLN(), MvOpt

  /**
   * Exchange source and destination instead of renaming source to destination.
   * Both files must exist; they need not be the same type.
   * This exchanges all data and metadata.
   * Details: [gnu mv invocation](https://www.gnu.org/software/coreutils/manual/html_node/mv-invocation.html)
   */
  data object Exchange : KOptLN(), MvOpt


  /**
   * Conditionally skip some files without failing. Exact behavior depends on [UpdateWhich].
   * Details: [gnu mv invocation](https://www.gnu.org/software/coreutils/manual/html_node/mv-invocation.html)
   */
  data class Update(val which: UpdateWhich) : KOptLN(which.namelowords("-")), MvOpt
  enum class UpdateWhich { All, None, NoneFail, Older }

  /** Same as Update(UpdateWhich.Older) [Update], but using short representation. */
  data object UpdateOlder : KOptS("u"), MvOpt

  /**
   * Follow existing symlinks to directories when copying.
   * Use this option only when the destination directory’s contents are trusted,
   * as an attacker can place symlinks in the destination to cause cp write to arbitrary target directories.
   */
  data object KeepDirectorySymlink : KOptLN(), MvOpt

  /**
   * Remove any trailing slashes from each source argument (avoids POSIX gotcha with dereferencing symlinks).
   * Details: https://www.gnu.org/software/coreutils/manual/html_node/Trailing-slashes.html
   * Note: when using [Path] it's not a problem because [Path] doesn't use trailing slashes except for unix root path.
   */
  data object StripTrailingSlashes : KOptLN(), MvOpt

  /**
   * Set SELinux security context of destination files (and created dirs) to default type.
   * This option functions similarly to the `restorecon` command, by adjusting the SELinux security context
   * according to the system default type for destination files and each created directory.
   */
  data object SEContext : KOptS("Z"), MvOpt
}
