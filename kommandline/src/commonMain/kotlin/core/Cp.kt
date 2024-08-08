package pl.mareklangiewicz.kommand.core

import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.namelowords
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.CpOpt.*
import pl.mareklangiewicz.udata.strf

/**
 * Copy single [src] to [dst].
 * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
 */
@OptIn(DelicateApi::class)
fun cpSingle(
  src: Path,
  dst: Path,
  vararg useNamedArgs: Unit,
  force: Boolean = false,
  verbose: Boolean = false,
) = cp(src, dst, force = force, verbose = verbose) { -TargetDirNone }

/**
 * Copy each [srcPaths] into the specified [targetDir], using the sources names.
 * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
 */
@OptIn(DelicateApi::class)
fun cpInto(
  targetDir: Path,
  vararg srcPaths: Path?,
  force: Boolean = false,
  verbose: Boolean = false,
) = cp(*srcPaths, force = force, verbose = verbose) { -TargetDir(targetDir) }

/**
 * Note1: As [Path] kdoc says: The only path that ends with "/" is the file system root "/".
 * So we avoid POSIX gotcha with dereferencing symlinks, so we don't have to use [StripTrailingSlashes].
 * Details: https://www.gnu.org/software/coreutils/manual/html_node/Trailing-slashes.html
 *
 * Note2: This version is delicate because last Path can be conditionally interpreted as "target dir"
 * (if it happens to exist and is dir/symlink-to-dir). It's popular use-case, but can cause race conditions etc.
 * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
 * Make sure to use option [TargetDir] / [cpInto] / [cpSingle] to avoid potential issues.
 */
@DelicateApi
fun cp(
  vararg paths: Path?,
  force: Boolean = false,
  verbose: Boolean = false,
  init: Cp.() -> Unit,
) = Cp(nonopts = paths.mapNotNull { it.strf }.toMutableList())
  .apply { if (force) -Force; if (verbose) -Verbose; init() }


/**
 * The [Cp] kommand copies files (or, optionally, directories). The copy is completely independent of the original.
 * You can either copy one file to another, or copy arbitrarily many files to a destination directory.
 *
 * [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/cp.1.html)
 * [backup opts](https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html)
 *
 * Generally, files are written just as they are read. For exceptions, see the [Sparse] option below.
 *
 * By default, [Cp] does not copy directories. However, [Recursive] or [Archive] cause cp to copy recursively
 * by descending into source directories and copying files to corresponding destination directories.
 *
 * When copying from a symbolic link, cp normally follows the link
 * only when NOT copying recursively or when [MakeHardLinks] is used. This default can be overridden
 * with the [Archive], [CopySymLinks], [CopyAllLinks], [FollowAllSymLinks], [FollowCmdSymLinks]  options.
 * If more than one of these options is specified, the last one silently overrides the others.
 *
 * When copying to a symbolic link, cp follows the link only when it refers to an existing regular file.
 * However, when copying to a dangling symbolic link, cp refuses by default, and fails with a diagnostic,
 * since the operation is inherently dangerous. This behavior is contrary to historical practice and to POSIX.
 * Set POSIXLY_CORRECT to make cp attempt to create the target of a dangling destination symlink,
 * in spite of the possible risk.
 * Also, when an options like [BackupExisting] or [MakeHardLinks] acts to rename or remove the destination
 * before copying, cp renames or removes the symbolic link rather than the file it points to.
 *
 * By default, cp copies the contents of special files only when not copying recursively.
 * This default can be overridden with the [CopyContents] option.
 *
 * cp generally refuses to copy a file onto itself, with the following exception:
 * If [Force] and [BackupExisting] is specified with source and dest identical, and referring to a regular file,
 * cp will make a backup file, either regular or numbered, as specified in the usual ways (see Backup options).
 * This is useful when you simply want to make a backup of an existing file before changing it.
 * Example: `cp --backup --force --preserve=all -- "$fname" "$fname"`
 */
@DelicateApi
data class Cp(
  override val opts: MutableList<CpOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<CpOpt> {
  override val name get() = "cp"
}

@DelicateApi
interface CpOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), CpOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), CpOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), CpOpt
  // endregion [GNU Common Opts]

  // region [GNU Backup Opts]

  // https://www.gnu.org/software/coreutils/manual/html_node/Backup-options.html

  /**
   * Make numbered backups of files that already have them, simple backups of the others.
   * Using -b is equivalent to using --backup=existing; -b does not accept any argument.
   */
  data object BackupExisting : KOptS("b"), CpOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupExistingExplicit : KOptL("backup", "existing"), CpOpt

  @Deprecated("Use BackupExisting which works the same and is short.", ReplaceWith("BackupExisting"))
  data object BackupNilExplicit : KOptL("backup", "nil"), CpOpt

  /** Never make backups */
  data object BackupOff : KOptL("backup", "off"), CpOpt

  @Deprecated("Use BackupOff which works the same.", ReplaceWith("BackupOff"))
  data object BackupNone : KOptL("backup", "none"), CpOpt

  /** Always make numbered backups. */
  data object BackupNumbered : KOptL("backup", "t"), CpOpt

  @Deprecated("Use BackupNumbered which works the same and is a bit shorter.", ReplaceWith("BackupNumbered"))
  data object BackupNumberedExplicit : KOptL("backup", "numbered"), CpOpt

  /** Always make simple (never numbered) backups. */
  data object BackupSimple : KOptL("backup", "simple"), CpOpt

  @Deprecated("Use BackupSimple which works the same and doesn't have confusing name.", ReplaceWith("BackupSimple"))
  data object BackupNeverConfusing : KOptL("backup", "never"), CpOpt

  /**
   * No method here, so it's a special case where the value of the VERSION_CONTROL environment variable is used.
   * And if VERSION_CONTROL is not set, the default backup type is ‘existing’.
   */
  data object BackupDefaultEnv : KOptL("backup"), CpOpt

  /**
   * Append suffix to each backup file made with -b.
   * If this option is not specified, the value of the SIMPLE_BACKUP_SUFFIX environment variable is used.
   * And if SIMPLE_BACKUP_SUFFIX is not set, the default is '~', just as in Emacs.
   */
  data class BackupSuffix(val suffix: String) : KOptS("S", suffix), CpOpt

  // endregion [GNU Backup Opts]

  // region [GNU Target Dir Opts]

  /**
   * @param dir will be target dir to put files into. If null there will be NO target dir.
   * (prevents traps/races with treating last arg as target dir if happen to exist and is dir or symlink to dir)
   * Details:[GNU CoreUtils Target Dir](https://www.gnu.org/software/coreutils/manual/html_node/Target-directory.html)
   */
  data class TargetDir(val dir: Path?) : KOptS(dir?.let { "t" } ?: "T", dir?.strf), CpOpt

  /** Same as TargetDir(null). See [TargetDir] */
  data object TargetDirNone : KOptS("T"), CpOpt

  // endregion [GNU Target Dir Opts]


  /**
   * Preserve as much as possible of the structure and attributes of the original files in the copy
   * (but do not attempt to preserve internal directory structure;
   * i.e., ‘ls -U’ may list the entries in a copied directory in a different order).
   * Try to preserve SELinux security context and extended attributes (xattr),
   * but ignore any failure to do that and print no corresponding diagnostic.
   * Equivalent to -dR --preserve=all with the reduced diagnostics.
   */
  data object Archive : KOptS("a"), CpOpt

  /**
   * Copy only the specified attributes of the source file to the destination.
   * If the destination already exists, do not alter its contents.
   * See the --preserve option for controlling which attributes to copy.
   */
  data object AttributesOnly : KOptLN(), CpOpt


  /**
   * If copying recursively, copy the contents of any special files (e.g., FIFOs and device files)
   * as if they were regular files. This means trying to read the data in each source file
   * and writing it to the destination. It is usually a mistake to use this option,
   * as it normally has undesirable effects on special files like FIFOs and the ones
   * typically found in the /dev directory. In most cases, cp -R --copy-contents will hang indefinitely
   * trying to read from FIFOs and special files like /dev/console,
   * and it will fill up your destination file system if you use it to copy /dev/zero.
   * This option has no effect unless copying recursively,
   * and it does not affect the copying of symbolic links.
   */
  @DelicateApi("Especially delicate as in many cases it can hang indefinitely or fill entire FS with generated bytes.")
  data object CopyContents : KOptLN(), CpOpt

  /**
   * Copy symbolic links as symbolic links rather than copying the files that they point to,
   * and preserve hard links between source files in the copies.
   * Equivalent to --no-dereference --preserve=links.
   */
  data object CopyAllLinks : KOptS("d"), CpOpt

  /**
   * Copy symbolic links as symbolic links rather than copying the files that they point to.
   * This option affects only symbolic links in the source;
   * symbolic links in the destination are always followed if possible.
   */
  data object CopySymLinks : KOptS("P"), CpOpt

  /** Print the name of each file before moving it. */
  data object Verbose : KOptLN(), CpOpt // Don't risk short -v (ambiguity with "version")

  /** Print extra information to stdout, explaining how files are copied. This option implies [Verbose]. */
  data object VerboseDebug : KOptL("debug"), CpOpt

  /**
   * When copying without this option and an existing destination file cannot be opened for writing, the copy fails.
   * However, with --force, when a destination file cannot be opened,
   * cp then tries to recreate the file by first removing it.
   * The --force option alone will not remove dangling symlinks.
   * When this option is combined with --link (-l) or --symbolic-link (-s), the destination link is replaced,
   * and unless --backup (-b) is also given there is no brief moment when the destination does not exist.
   * Also see the description of --remove-destination.
   *
   * This option is independent of the --interactive or -i option: neither cancels the effect of the other.
   *
   * This option is ignored when the --no-clobber or -n option is also used.
   */
  data object Force : KOptLN(), CpOpt // Don't risk short -f (better to be explicit with FORCE)

  /** Remove each existing destination file before attempting to open it (contrast with [Force] above). */
  data object ForceRemoval : KOptL("remove-destination"), CpOpt

  /**
   * If a command line argument specifies a symbolic link,
   * then copy the file it points to rather than the symbolic link itself.
   * However, copy (preserving its nature) any symbolic link that is encountered via recursive traversal.
   */
  data object FollowCmdSymLinks : KOptS("H"), CpOpt

  /**
   * Follow symbolic links when copying from them. With this option, cp cannot create a symbolic link.
   * For example, a symlink (to regular file) in the source tree
   * will be copied to a regular file in the destination tree.
   */
  data object FollowAllSymLinks : KOptS("L"), CpOpt

  /**
   * When copying a file other than a directory, prompt whether to overwrite an existing destination file,
   * and fail if the response is not affirmative. The -i option overrides a previous -n option.
   */
  data object Interactive : KOptS("i"), CpOpt

  /** Make hard links instead of copies of non-directories. */
  data object MakeHardLinks : KOptS("l"), CpOpt


  /**
   * Make symbolic links instead of copies of non-directories.
   * All source file names must be absolute (starting with ‘/’) unless the destination files
   * are in the current directory. This option merely results in an error message on systems
   * that do not support symbolic links.
   */
  data object MakeSymLinks : KOptS("s"), CpOpt

  /**
   * Do not overwrite an existing file; silently skip instead. This option overrides a previous -i option.
   * This option is mutually exclusive with -b or --backup option.
   * This option is deprecated due to having a different exit status from other platforms.
   * See also the --update option which will give more control over how to deal with existing files in the destination,
   * and over the exit status in particular.
   */
  @Deprecated("Deprecated due to having a different exit status from other platforms")
  data object NoClobber : KOptS("n"), CpOpt

  /**
   * Preserve the specified attributes [FileAttrs] of the original files.
   * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
   */
  data class Preserve(val attrs: List<FileAttrs>) : KOptLN(attrs.joinToString(",") { it.namelowords() }), CpOpt {
    constructor(vararg attrs: FileAttrs) : this(attrs.toList())
  }

  /** Works the same as Preserve(Mode, Ownership, Timestamp), but uses short notation. [Preserve] */
  data object PreserveBasic : KOptS("p"), CpOpt

  /**
   * Do not preserve the specified attributes. The attribute list has the same form as for [Preserve]
   * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
   */
  data class NoPreserve(val attrs: List<FileAttrs>) : KOptLN(attrs.joinToString(",") { it.namelowords() }), CpOpt {
    constructor(vararg attrs: FileAttrs) : this(attrs.toList())
  }

  enum class FileAttrs { Mode, Ownership, Timestamps, Links, Context, Xattr, All }


  /**
   * Form the name of each destination file by appending to the target directory a slash and the specified name
   * of the source file. The last argument given to cp must be the name of an existing directory.
   * For example, the command: `cp --parents a/b/c existing_dir` copies the file `a/b/c` to `existing_dir/a/b/c`,
   * creating any missing intermediate directories.
   */
  data object Parents : KOptLN(), CpOpt

  /**
   * Copy directories recursively. By default, do not follow symbolic links in the source
   * unless used together with the --link (-l) option;
   * see the --archive (-a), -d, --dereference (-L), --no-dereference (-P), and -H options.
   * Special files are copied by creating a destination file of the same type as the source;
   * see the --copy-contents option. It is not portable to use -r to copy symbolic links or special files.
   * On some non-GNU systems, -r implies the equivalent of -L and --copy-contents for historical reasons.
   * Also, it is not portable to use -R to copy symbolic links unless you also specify -P,
   * as POSIX allows implementations that dereference symbolic links by default.
   */
  data object Recursive : KOptLN(), CpOpt // Don't risk short -r or -R (better to be explicit about RECURSIVE)

  /**
   * Perform a lightweight, copy-on-write (COW) copy, if supported by the file system.
   * Once it has succeeded, beware that the source and destination files share the same data blocks
   * as long as they remain unmodified. Thus, if an I/O error affects data blocks of one of the files,
   * the other suffers the same fate.
   * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
   */
  data class Reflink(val reflinkWhen: ReflinkWhen) : KOptLN(reflinkWhen.namelowords()), CpOpt

  enum class ReflinkWhen { Always, Auto, Never }

  /**
   * A sparse file contains holes – a sequence of zero bytes that does not occupy any file system blocks;
   * the ‘read’ system call reads these as zeros. This can both save considerable space and increase speed,
   * since many binary files contain lots of consecutive zero bytes.
   * By default, cp detects holes in input source files via a crude heuristic
   * and makes the corresponding output file sparse as well. Only regular files may be sparse.
   * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
   */
  data class Sparse(val sparseWhen: SparseWhen) : KOptLN(sparseWhen.namelowords()), CpOpt

  enum class SparseWhen { Always, Auto, Never }

  /**
   * Remove any trailing slashes from each source argument (avoids POSIX gotcha with dereferencing symlinks).
   * Details: https://www.gnu.org/software/coreutils/manual/html_node/Trailing-slashes.html
   * Note: when using [Path] it's not a problem because [Path] doesn't use trailing slashes except for unix root path.
   */
  data object StripTrailingSlashes : KOptLN(), CpOpt

  /**
   * Conditionally skip some files without failing. Exact behavior depends on [UpdateWhich].
   * Details: [gnu cp invocation](https://www.gnu.org/software/coreutils/manual/html_node/cp-invocation.html)
   */
  data class Update(val which: UpdateWhich) : KOptLN(which.namelowords("-")), CpOpt
  enum class UpdateWhich { All, None, NoneFail, Older }

  /** Same as Update(UpdateWhich.Older) [Update], but using short representation. */
  data object UpdateOlder : KOptS("u"), CpOpt

  /**
   * Skip subdirectories that are on different file systems from the one that the copy started on.
   * However, mount point directories are copied.
   */
  data object OneFileSystem : KOptLN(), CpOpt

  /**
   * Without a specified context, adjust the SELinux security context according to the system default type
   * for destination files, similarly to the restorecon command.
   * The long form of this option with a specific context specified,
   * will set the context for newly created files only.
   * With a specified context, if both SELinux and SMACK are disabled,a warning is issued.
   * This option is mutually exclusive with the --preserve=context option,
   * and overrides the --preserve=all and -a options.
   */
  data class SEContext(val context: String?) : KOptL("context", context), CpOpt

  /**
   * Works the same as SEContext(null) but using short notation.
   * Adjusts the SELinux security context according to the system default type for destination files,
   * similarly to the restorecon command.
   */
  data object SEContextDefault : KOptS("Z"), CpOpt

}
