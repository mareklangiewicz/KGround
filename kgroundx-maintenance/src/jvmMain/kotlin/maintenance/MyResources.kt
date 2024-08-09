package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.ulog.*


private val PResRel = "kgroundx-maintenance/src/jvmMain/resources".P
private val PResAbs = PProjKGround / PResRel
private val PResRelTmpl = PResRel / "templates"
private val PResAbsTmpl = PResAbs / "templates"

private suspend fun Path.isTmplSymlink() =
  name.endsWith(".tmpl") && localUFileSys().metadata(this).symlinkTarget != null

suspend fun updateKGroundTemplatesSymLinks() {
  val log = localULog()
  val fs = localUFileSys()
  // remove all tmpl symlinks (but throw if other unexpected file found)
  fs.listRecursively(PResAbsTmpl).forEach {
    if (fs.metadata(it).isDirectory) return@forEach
    it.isTmplSymlink().chkTrue { "Unexpected file in templates: $it" }
    fs.delete(it)
  }
  // remove all dirs (but throw if non directory still found)
  fs.list(PResAbsTmpl).forEach {
    fs.metadata(it).isDirectory.chkTrue { "Some non directory left in templates: $it" }
    fs.deleteRecursively(it)
  }

  // prepare the list of buildfiles (*.gradle.kts)
  val buildFiles = findAllFiles(PProjKGround, maxDepth = 10)
    .filter { it.segments.any { it.startsWith("template-") } }
    .filterExt("gradle.kts")
    .toList()

  // prepare the list of gradlew files
  val gradlewFiles = gradlewRelPaths.map { PProjKGround / it }

  // generate .tmpl symlinks in resources/templates (relative to PathToKGroundProject)
  (buildFiles + gradlewFiles).forEach { srcAbs ->
    val srcRel = srcAbs.asRelativeTo(PProjKGround)
    val linkRel = PResRelTmpl / srcRel.withName { "$it.tmpl" }
    val linkAbs = PProjKGround / linkRel
    val targetDots = linkRel.parent!!.segments.joinToString("/") { ".." }
    val target = targetDots.P / srcRel
    log.i("symlink $linkAbs -> $target")
    fs.createDirectories(linkAbs.parent!!)
    fs.createSymlink(linkAbs, target)
  }
}
