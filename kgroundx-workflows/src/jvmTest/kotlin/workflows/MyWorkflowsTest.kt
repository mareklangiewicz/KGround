@file:Suppress("PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.workflows

import io.github.typesafegithub.workflows.yaml.generateYaml
import java.io.File
import kotlin.test.Test
import kotlin.test.fail

class MyWorkflowsTest {

  /**
   * KGround's dbuild.yml is generated, and CI runs whatever is committed -- so a generator change
   * that nobody re-injected would silently not happen. On mismatch the expected yaml is written
   * next to this module's build outputs; copying it over the committed one is the regeneration.
   */
  @Test fun kgroundDBuildYamlIsUpToDate() {
    val committed = File("../.github/workflows/dbuild.yml")
    val expected = myDefaultBuildWorkflowForProject("KGround").generateYaml()
    if (committed.readText() == expected) return
    val out = File("build/generated/dbuild.yml").apply { parentFile.mkdirs(); writeText(expected) }
    fail("${committed.canonicalPath} is out of date with the generator.\nRegenerate: cp ${out.canonicalPath} ${committed.canonicalPath}")
  }
}
