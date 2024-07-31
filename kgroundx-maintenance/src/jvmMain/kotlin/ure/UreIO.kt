package pl.mareklangiewicz.ure

import okio.*
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.ure.core.UreConcatenation


fun FileSystem.readAndMatchUre(file: Path, init: UreConcatenation.() -> Unit): MatchResult? =
  readAndMatchUre(file, ure(init = init))

fun FileSystem.readAndMatchUre(file: Path, ure: Ure): MatchResult? =
  readUtf8(file).let { ure.compile().matchEntire(it) }

@NotPortableApi
suspend fun commentOutMultiplatformFunInFile(file: Path) {
  val log = localULog()
  log.d("\ncommenting: $file")
  processFile(file, file, String::commentOutMultiplatformFun)
}

@NotPortableApi
suspend fun undoCommentOutMultiplatformFunInFile(file: Path) {
  val log = localULog()
  log.d("\nundo comments: $file")
  processFile(file, file, String::undoCommentOutMultiplatformFun)
}

@NotPortableApi
suspend fun commentOutMultiplatformFunInEachKtFile(root: Path) =
  findAllFiles(root).filterExt("kt").forEach { commentOutMultiplatformFunInFile(it) }

@NotPortableApi
suspend fun undoCommentOutMultiplatformFunInEachKtFile(root: Path) =
  findAllFiles(root).filterExt("kt").forEach { undoCommentOutMultiplatformFunInFile(it) }
