package pl.mareklangiewicz.io

import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.FileSystem.Companion.SYSTEM_TEMPORARY_DIRECTORY
import okio.Path.Companion.toPath
import pl.mareklangiewicz.bad.*
import kotlin.math.*
import kotlin.random.*
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.implictx
import pl.mareklangiewicz.ulog.*

// FIXME NOW: this file is moved from DepsKt as is temporarily.
//   analyze this code and move stuff to more common code and/or other KGround modules

@Throws(IOException::class)
@Deprecated("Okio has listRecursively. Use that or maybe implement new listRecursively with additional params.")
// TODO_later: analyze Okio listRecursively more, before possibly implementing any own version
// (traversal order, infinite loops with symlinks, etc.).
fun FileSystem.findAllFiles(path: Path, maxDepth: Int = Int.MAX_VALUE): Sequence<Path> {
  req(maxDepth >= 0)
  val md = metadata(path)
  return when {
    md.isRegularFile -> sequenceOf(path)
    maxDepth < 1 || !md.isDirectory -> emptySequence()
    else -> list(path).asSequence().flatMap { findAllFiles(it, maxDepth - 1) }
  }
}

@Throws(IOException::class)
@Deprecated("Okio has listRecursively. Use that or maybe implement new listRecursively with additional params.")
suspend fun findAllFiles(path: Path, maxDepth: Int = Int.MAX_VALUE): Sequence<Path> =
  implictx<UFileSys>().findAllFiles(path, maxDepth)

fun Path.withName(getNewName: (oldName: String) -> String) =
  parent?.let { it / getNewName(name) } ?: getNewName(name).toPath()

fun Sequence<Path>.filterExt(ext: String) = filter { it.name.endsWith(".$ext") }

/**
 * @param inputRoot path of input root dir
 * @param outputRoot path of output root dir - can be the same as inputRootDir;
 * nothing is written to file system if it's null;
 * @param process file content transformation; if it returns null - output file is not even touched
 */
suspend fun processEachFile(
  inputRoot: Path,
  outputRoot: Path? = null,
  process: suspend (input: Path, output: Path?, content: String) -> String?,
) {
  req(inputRoot.isAbsolute)
  req(outputRoot?.isAbsolute != false)
  findAllFiles(inputRoot).forEach { inputPath ->
    val outputPath = if (outputRoot == null) null else outputRoot / inputPath.asRelativeTo(inputRoot)
    processFile(inputPath, outputPath) { content -> process(inputPath, outputPath, content) }
  }
}

@Deprecated("Use okio fun Path.relativeTo")
fun Path.asRelativeTo(path: Path): Path {
  req(this.isAbsolute)
  req(path.isAbsolute)
  return when {
    this == path -> ".".toPath()
    parent == path -> this.name.toPath()
    parent == null -> bad { "Can not find $path in $this" }
    else -> parent!!.asRelativeTo(path) / name
  }
}

tailrec fun Path?.commonPartWith(that: Path?): Path? = when {
  this == that -> this
  this == null || that == null -> null
  segmentsBytes.size > that.segmentsBytes.size -> parent.commonPartWith(that)
  segmentsBytes.size < that.segmentsBytes.size -> commonPartWith(that.parent)
  else -> parent.commonPartWith(that.parent)
}

fun List<Path?>.commonPart(): Path? = when {
  isEmpty() -> null
  size == 1 -> this[0]
  else -> reduce { path1, path2 -> path1.commonPartWith(path2) }
}

fun FileSystem.readUtf8(file: Path): String = read(file) { readUtf8() }
fun FileSystem.readByteString(file: Path): ByteString = read(file) { readByteString() }

fun FileSystem.writeUtf8(file: Path, content: String, createParentDir: Boolean = false) {
  if (createParentDir) createDirectories(file.parent!!)
  write(file) { writeUtf8(content) }
}

fun FileSystem.writeByteString(file: Path, content: ByteString, createParentDir: Boolean = false) {
  if (createParentDir) createDirectories(file.parent!!)
  write(file) { write(content) }
}

/**
 * @param inputPath path of input file
 * @param outputPath path of output file - can be the same as inputPath;
 * nothing is written to file system if it's null;
 * @param process file content transformation; if it returns null - outputPath is not even touched
 */
suspend fun processFile(inputPath: Path, outputPath: Path? = null, process: suspend (String) -> String?) {
  val fs = implictx<UFileSys>()
  val log = implictx<ULog>()
  val input = fs.readUtf8(inputPath)
  val output = process(input)
  if (outputPath == null) {
    if (output != null) log.d("Ignoring non-null output because outputPath is null")
    return
  }
  if (output == null) {
    log.d("Ignoring outputPath because output to write is null")
    return
  }
  fs.createDirectories(outputPath.parent!!)
  fs.writeUtf8(outputPath, output)
}

fun FileSystem.withTempDir(tempDirPrefix: String, code: FileSystem.(tempDir: Path) -> Unit) {
  val tempDir = createTempDir(tempDirPrefix)
  try {
    code(tempDir)
  } finally {
    deleteRecursively(tempDir)
  }
}

fun FileSystem.createTempDir(tempDirPrefix: String): Path {
  reqSame(SYSTEM) { "SYSTEM_TEMPORARY_DIRECTORY is available only on FileSystem.SYSTEM" }
  return createUniqueDir(SYSTEM_TEMPORARY_DIRECTORY, tempDirPrefix)
}

fun FileSystem.createUniqueDir(parentDir: Path, namePrefix: String = "", nameSuffix: String = "") =
  (parentDir / Random.name(namePrefix, nameSuffix)).also { createDirectory(it, mustCreate = true) }

fun FileSystem.openTempFile(namePrefix: String = "", nameSuffix: String = ""): FileHandle {
  reqSame(SYSTEM) { "SYSTEM_TEMPORARY_DIRECTORY is available only on FileSystem.SYSTEM" }
  return openUniqueFile(SYSTEM_TEMPORARY_DIRECTORY, namePrefix, nameSuffix)
}

fun FileSystem.openUniqueFile(parentDir: Path, namePrefix: String = "", nameSuffix: String = "") =
  openReadWrite(parentDir / Random.name(namePrefix, nameSuffix), mustCreate = true)

private fun Random.name(prefix: String = "", suffix: String = "") = "$prefix${nextLong().absoluteValue}$suffix"
