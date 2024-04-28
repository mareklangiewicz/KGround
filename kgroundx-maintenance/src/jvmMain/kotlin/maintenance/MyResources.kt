package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.Path.Companion.toPath
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.implictx
import pl.mareklangiewicz.ulog.*


private val resourcesRelPath = "kgroundx-maintenance/src/jvmMain/resources".toPath()
private val templatesRelPath = resourcesRelPath / "templates"
private val resourcesAbsPath = PathToKGroundProject / resourcesRelPath
private val templatesAbsPath = PathToKGroundProject / templatesRelPath

private suspend fun Path.isTmplSymlink() =
  name.endsWith(".tmpl") && implictx<UFileSys>().metadata(this).symlinkTarget != null

suspend fun updateKGroundTemplatesSymLinks() {
  val log = implictx<ULog>()
  val fs = implictx<UFileSys>()
  // remove all tmpl symlinks (but throw if other unexpected file found)
  fs.listRecursively(templatesAbsPath).forEach {
    if (fs.metadata(it).isDirectory) return@forEach
    it.isTmplSymlink().chkTrue { "Unexpected file in templates: $it" }
    fs.delete(it)
  }
  // remove all dirs (but throw if non directory still found)
  fs.list(templatesAbsPath).forEach {
    fs.metadata(it).isDirectory.chkTrue { "Some non directory left in templates: $it" }
    fs.deleteRecursively(it)
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
    log.i("symlink $linkAbs -> $target")
    fs.createDirectories(linkAbs.parent!!)
    fs.createSymlink(linkAbs, target)
  }
}
