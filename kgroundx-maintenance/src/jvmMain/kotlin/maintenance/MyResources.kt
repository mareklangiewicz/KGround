package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.chkTrue


var MyKGroundRootPath = "/home/marek/code/kotlin/KGround".toPath()

private val resourcesRelPath = "kgroundx-maintenance/src/jvmMain/resources".toPath()
private val resourcesAbsPath = MyKGroundRootPath / resourcesRelPath

private val Path.isTmplSymlink
    get() = name.endsWith(".tmpl") && SYSTEM.metadata(this).symlinkTarget != null

fun updateKGroundResourcesSymLinks(log: (Any?) -> Unit = ::println) = SYSTEM.run {

    // remove all tmpl symlinks (but throw if other unexpected file found)
    listRecursively(resourcesAbsPath).forEach {
        if (metadata(it).isDirectory) return@forEach
        it.isTmplSymlink.chkTrue { "Unexpected file in resources: $it" }
        delete(it)
    }
    // remove all dirs (but throw if non directory still found)
    list(resourcesAbsPath).forEach {
        metadata(it).isDirectory.chkTrue { "Some non directory left in resources: $it" }
        deleteRecursively(it)
    }

    // prepare the list of buildfiles (*.gradle.kts)
    val buildFiles = findAllFiles(MyKGroundRootPath, maxDepth = 10)
        .filter { it.segments.any { it.startsWith("template-") } }
        .filterExt("gradle.kts")
        .toList()

    // prepare the list of gradlew files
    val gradlewFiles = gradlewRelPaths.map { MyKGroundRootPath / it }

    // generate .tmpl symlinks in resources (relative to MyKGroundRootPath)
    (buildFiles + gradlewFiles).forEach { srcAbs ->
        val srcRel = srcAbs.asRelativeTo(MyKGroundRootPath)
        val linkRel = resourcesRelPath / srcRel.withName { "$it.tmpl" }
        val linkAbs = MyKGroundRootPath / linkRel
        val targetDots = linkRel.parent!!.segments.joinToString("/") { ".." }
        val target = targetDots.toPath() / srcRel
        log("symlink $linkAbs -> $target")
        createDirectories(linkAbs.parent!!)
        createSymlink(linkAbs, target)
    }
}
