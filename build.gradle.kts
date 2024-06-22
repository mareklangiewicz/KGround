import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
  plug(plugs.NexusPublish)
  plug(plugs.KotlinMulti) apply false
}

val enableJs = true
val enableNative = true

val enablePublishing = findProject(":kommandline") == null && findProject(":kommandsamples") == null
// don't publish to sonatype from my machine, because I include local kommandline and kommandsample
// modules (see settings.gradle.kts) so it would also publish these with wrong description and ver etc.
// exception: publishToMavenLocal for debugging

// https://s01.oss.sonatype.org/content/repositories/releases/pl/mareklangiewicz/kommandline/
rootExtString["verKL"] = "0.0.65"


defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "KGround",
    description = "Kotlin Common Ground.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 0, 59),
    // https://s01.oss.sonatype.org/content/repositories/releases/pl/mareklangiewicz/kground/
    // https://github.com/mareklangiewicz/KGround/releases
    settings = LibSettings(
      withJs = enableJs,
      withNativeLinux64 = enableNative,
      compose = null,
      withSonatypeOssPublishing = enablePublishing,
    ),
  ),
)

// FIXME: make sure this region below is synced, but not as a part of "self-sync" as it was in DepsKt,
//   but as normal sync when syncing all regions in all projects.
//   The "self-sync" should only sync templates (and be renamed to templates-sync or sth)

// region [[Root Build Template]]

/** Publishing to Sonatype OSSRH has to be explicitly allowed here, by setting withSonatypeOssPublishing to true. */
fun Project.defaultBuildTemplateForRootProject(details: LibDetails? = null) {
  ext.addDefaultStuffFromSystemEnvs()
  details?.let {
    rootExtLibDetails = it
    defaultGroupAndVerAndDescription(it)
    if (it.settings.withSonatypeOssPublishing) defaultSonatypeOssNexusPublishing()
  }

  // kinda workaround for kinda issue with kotlin native
  // https://youtrack.jetbrains.com/issue/KT-48410/Sync-failed.-Could-not-determine-the-dependencies-of-task-commonizeNativeDistribution.#focus=Comments-27-5144160.0-0
  repositories { mavenCentral() }
}

/**
 * System.getenv() should contain six env variables with given prefix, like:
 * * MYKOTLIBS_signing_keyId
 * * MYKOTLIBS_signing_password
 * * MYKOTLIBS_signing_keyFile (or MYKOTLIBS_signing_key with whole signing key)
 * * MYKOTLIBS_ossrhUsername
 * * MYKOTLIBS_ossrhPassword
 * * MYKOTLIBS_sonatypeStagingProfileId
 * * First three of these used in fun pl.mareklangiewicz.defaults.defaultSigning
 * * See DepsKt/template-mpp/template-mpp-lib/build.gradle.kts
 */
fun ExtraPropertiesExtension.addDefaultStuffFromSystemEnvs(envKeyMatchPrefix: String = "MYKOTLIBS_") =
  addAllFromSystemEnvs(envKeyMatchPrefix)

fun Project.defaultSonatypeOssNexusPublishing(
  sonatypeStagingProfileId: String = rootExtString["sonatypeStagingProfileId"],
  ossrhUsername: String = rootExtString["ossrhUsername"],
  ossrhPassword: String = rootExtString["ossrhPassword"],
) {
  nexusPublishing {
    this.repositories {
      sonatype {  // only for users registered in Sonatype after 24 Feb 2021
        stagingProfileId put sonatypeStagingProfileId
        username put ossrhUsername
        password put ossrhPassword
        nexusUrl put repos.sonatypeOssNexus
        snapshotRepositoryUrl put repos.sonatypeOssSnapshots
      }
    }
  }
}

// endregion [[Root Build Template]]
