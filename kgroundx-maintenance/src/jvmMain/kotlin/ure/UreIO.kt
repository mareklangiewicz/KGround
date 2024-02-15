package pl.mareklangiewicz.ure

import okio.*
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.ure.core.Ure
import pl.mareklangiewicz.ure.core.UreConcatenation


fun FileSystem.readAndMatchUre(file: Path, init: UreConcatenation.() -> Unit): MatchResult? =
    readAndMatchUre(file, ure(init = init))

fun FileSystem.readAndMatchUre(file: Path, ure: Ure): MatchResult? = readUtf8(file).let { ure.compile().matchEntire(it) }

@NotPortableApi
fun FileSystem.commentOutMultiplatformFunInFile(file: Path) {
    println("\ncommenting: $file") // FIXME:remove/ulog
    processFile(file, file, String::commentOutMultiplatformFun)
}

@NotPortableApi
fun FileSystem.undoCommentOutMultiplatformFunInFile(file: Path) {
    println("\nundo comments: $file") // FIXME:remove/ulog
    processFile(file, file, String::undoCommentOutMultiplatformFun)
}

@NotPortableApi
fun FileSystem.commentOutMultiplatformFunInEachKtFile(root: Path) =
    findAllFiles(root).filterExt("kt").forEach { commentOutMultiplatformFunInFile(it) }

@NotPortableApi
fun FileSystem.undoCommentOutMultiplatformFunInEachKtFile(root: Path) =
    findAllFiles(root).filterExt("kt").forEach { undoCommentOutMultiplatformFunInFile(it) }
