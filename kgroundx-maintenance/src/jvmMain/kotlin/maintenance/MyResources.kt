package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.i


private val resourcesRelPath = "kgroundx-maintenance/src/jvmMain/resources".toPath()
private val templatesRelPath = resourcesRelPath / "templates"
private val resourcesAbsPath = PathToKGroundProject / resourcesRelPath
private val templatesAbsPath = PathToKGroundProject / templatesRelPath

private val Path.isTmplSymlink
  get() = name.endsWith(".tmpl") && SYSTEM.metadata(this).symlinkTarget != null

fun updateKGroundTemplatesSymLinks() = SYSTEM.run {

  // remove all tmpl symlinks (but throw if other unexpected file found)
  listRecursively(templatesAbsPath).forEach {
    if (metadata(it).isDirectory) return@forEach
    it.isTmplSymlink.chkTrue { "Unexpected file in templates: $it" }
    delete(it)
  }
  // remove all dirs (but throw if non directory still found)
  list(templatesAbsPath).forEach {
    metadata(it).isDirectory.chkTrue { "Some non directory left in templates: $it" }
    deleteRecursively(it)
  }

  // prepare the list of buildfiles (*.gradle.kts)
  val buildFiles = findAllFiles(PathToKGroundProject, maxDepth = 10)
    .filter { it.segments.any { it.startsWith("template-") } }
    .filterExt("gradle.kts")
    .toList()

  // prepare the list of gradlew files
  val gradlewFiles = gradlewRelPaths.map { PathToKGroundProject / it }

  // generate .tmpl symlinks in resources/templates (relative to PathToKGroundProject)
  (buildFiles + gradlewFiles).forEach { srcAbs ->
    val srcRel = srcAbs.asRelativeTo(PathToKGroundProject)
    val linkRel = templatesRelPath / srcRel.withName { "$it.tmpl" }
    val linkAbs = PathToKGroundProject / linkRel
    val targetDots = linkRel.parent!!.segments.joinToString("/") { ".." }
    val target = targetDots.toPath() / srcRel
    ulog.i("symlink $linkAbs -> $target")
    createDirectories(linkAbs.parent!!)
    createSymlink(linkAbs, target)
  }
}
