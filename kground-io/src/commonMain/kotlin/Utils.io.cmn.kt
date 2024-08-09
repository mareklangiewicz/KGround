package pl.mareklangiewicz.kground.io

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import okio.ByteString
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.plusIfNN
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.udata.strf


@Deprecated("Use .P", ReplaceWith("P"))
inline val String.pth get() = P

@Deprecated("Use .PNorm", ReplaceWith("PNorm"))
inline val String.pthn get() = PNorm

inline val String.P get() = toPath(normalize = false)
inline val String.PNorm get() = toPath(normalize = true)


/** Starting point for absolute unix paths. PAbs / "abc" / "de" == "/abc/de".P */
val PAbs = "/".P

/** Starting point for relative paths. PRel / "abc" / "de" == "abc/de".P (leading dot is removed which is good) */
val PRel = ".".P

inline val Path.rootOrPRel get() = root ?: PRel


// JVM signature clash :(
// operator fun Path.div(newSegments: Iterable<String>): Path =
  // newSegments.fold(this) { path, segment -> path / segment }

operator fun Path.div(newSegments: Iterable<ByteString>): Path =
  newSegments.fold(this) { path, segment -> path / segment }

// Note: it should pop up as alternative autocompletion to okio deleteRecursively.
// I think it's good idea to always double-check some specific part of rootPath
// to make sure we're not accidentally deleting totally wrong dir tree.
fun FileSystem.deleteTreeWithDoubleChk(
  rootPath: Path,
  mustExist: Boolean = true,
  mustBeDir: Boolean = true,
  doubleChk: (rootPathString: String) -> Boolean, // mandatory on purpose
) {
  val rootPathString = rootPath.strf
  val md = metadataOrNull(rootPath) ?: run {
    mustExist.chkFalse { "Tree rootPath: $rootPathString does NOT exist."}
    return // So it doesn't exist and it's fine; nothing to delete.
  }
  chk(!mustBeDir || md.isDirectory) { "Tree rootPath: $rootPathString is NOT directory." }
  doubleChk(rootPathString).chkTrue { "Can NOT remove $rootPathString tree because doubleChk failed." }
  deleteRecursively(rootPath, mustExist)
}

// See: https://publicobject.com/2023/04/16/read-a-project-file-in-a-kotlin-multiplatform-test/
@NotPortableApi("TODO: test on different platforms. Browser?")
expect fun getSysEnv(name: String): String?

/**
 * Current list of stable MPP properties:
 * - None
 * BTW:
 * Goal is to implement pretty stable key->value pairs for growing collection of keys in MPP.
 * Will reuse some keys from JVM (with the same meaning), will also add some new keys (not conflicting with JVM keys)
 */
@NotPortableApi("Mostly wrapper on JVM System.getProperty, but later I will add more keys->values (more MPP).")
expect fun getSysProp(name: String): String?

// For now, I'll use String, because returned types are not yet stable enough, not sure if it will ever be enum.
/** JVM or JS or NATIVE-Linux, etc.. */
expect fun getSysPlatformType(): String?

// TODO_later: implement "os.arch" sys prop on different platforms
@OptIn(NotPortableApi::class) fun getSysPlatformArch(): String? = getSysProp("os.arch")

// TODO_later: implement "os.name" sys prop on different platforms
@OptIn(NotPortableApi::class) fun getSysPlatformName(): String? = getSysProp("os.name")

fun getSysPlatformInfo() = "${getSysPlatformType()}:${getSysPlatformArch()}:${getSysPlatformName()}"

@DelicateApi("In most cases it's better to always assume/use forward slash instead. It's NOT JVM 'path.separator'!")
@OptIn(NotPortableApi::class) fun getSysDirEnd(): String = getSysProp("file.separator") ?: "/"

// https://square.github.io/okio/recipes/#write-a-text-file-javakotlin
@DelicateApi("In most cases it's better to always use unix style LF (0x0A) instead.")
@OptIn(NotPortableApi::class) fun getSysLineEnd(): String = getSysProp("line.separator") ?: "\n"

// TODO_later: implement some default value for key "user.name" on different platforms
@OptIn(NotPortableApi::class) fun getSysPathToUserName(): String? = getSysProp("user.name")

// TODO_later: implement some default value for key "user.home" on different platforms
@OptIn(NotPortableApi::class) fun getSysPathToUserHome(): Path? = getSysProp("user.home")?.P

// TODO_later: Add and use some custom MPP key returning sane values on different platforms ("kground.io.tmpdir"??)
@OptIn(NotPortableApi::class) fun getSysPathToSysTmp(): Path? = getSysProp("java.io.tmpdir")?.P

// FIXME_maybe: other paths for specific systems?
fun getSysPathToUserTmp(): Path? = getSysPathToUserHome()?.let { it / "tmp" }


@NotPortableApi("Currently on JS it will use Dispatchers.Default")
expect fun getSysDispatcherForIO(): CoroutineDispatcher

@NotPortableApi("Currently on JS it will fail.")
expect fun getSysUFileSys(): UFileSys

/** The working directory with which the current process was started. */
fun UFileSys.getSysWorkingDir(): Path = canonicalize(PRel)


/** Setting some param explicitly to null means we don't add any (even default) to context. */
@OptIn(NotPortableApi::class)
suspend inline fun <R> uctxWithIO(
  context: CoroutineContext = EmptyCoroutineContext,
  name: String? = null,
  dispatcher: CoroutineDispatcher? = getSysDispatcherForIO(),
  fs: UFileSys? = getSysUFileSys(),
  wd: UWorkDir? = fs?.getSysWorkingDir()?.let(::UWorkDir),
  noinline block: suspend CoroutineScope.() -> R,
) = uctx(context plusIfNN dispatcher plusIfNN fs plusIfNN wd, name = name, block)
