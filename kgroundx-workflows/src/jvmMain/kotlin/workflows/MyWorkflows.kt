@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.workflows

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.endbug.AddAndCommit
import io.github.typesafegithub.workflows.actions.gradle.ActionsDependencySubmission_Untyped
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.RegularAction
import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.WorkflowBuilder
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.*
import okio.*
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.udata.MO
import pl.mareklangiewicz.udata.MONN
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.ulog.*


@ExampleApi suspend fun checkMyDWorkflowsInMyProjects(onlyPublic: Boolean) =
  getMyProjectsNames(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPaths()
    .forEach { checkMyDWorkflowsInProject(it) }


@ExampleApi suspend fun injectMyDWorkflowsToMyProjects(onlyPublic: Boolean) =
  getMyProjectsNames(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPaths()
    .forEach { injectDWorkflowsToProject(it) }

@ExampleApi private suspend fun Iterable<String>.mapFilterLocalDWorkflowsProjectsPaths() =
  mapFilterLocalKotlinProjectsPaths {
    val log = localULog()
    val fs = localUFileSys()
    val isGradleRootProject = fs.exists(it / "settings.gradle.kts") || fs.exists(it / "settings.gradle")
    if (!isGradleRootProject) {
      log.w("Ignoring dworkflows in non-gradle project: $it")
    }
    // FIXME_maybe: Change when I have dworkflows for non-gradle projects
    isGradleRootProject
  }


private val myFork = expr { "${github.repository_owner} == 'mareklangiewicz'" }

private val myOssPublishingSecretsEnv = LO(
  "signingInMemoryKey",
  "signingInMemoryKeyId",
  "signingInMemoryKeyPassword",
  "mavenCentralUsername",
  "mavenCentralPassword",
)
  .associate { "ORG_GRADLE_PROJECT_$it" to expr("secrets.KL_" + it.uppercase()) }



// Github cron is UTC so about 2 hours behind Warsaw.
// https://www.timeanddate.com/worldclock/timezone/utc
// Github can delay or even drop scheduled events depending on
// high load times (like full hours), repo usage, and many different things.
// https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#schedule
private val everydayAfter5amUTC = Cron(hour = "5", minute = "37")
// BTW refreshDeps should take less than 10min
private val everydayBefore6amUTC = Cron(hour = "5", minute = "53")


fun myWorkflow(
  name: String,
  on: List<Trigger>,
  env: Map<String, String> = MO(),
  block: WorkflowBuilder.() -> Unit,
): Workflow {
  lateinit var result: Workflow
  workflow(name = name, on = on, env = env, block = block, useWorkflow = { result = it })
  return result
}

// FIXME_maybe: something less hacky/hardcoded
fun injectHackyGenerateDepsWorkflowToRefreshDepsRepo() = myWorkflow(
  "Generate Deps", LO(Schedule(LO(everydayAfter5amUTC)), WorkflowDispatch()),
) {
  job(
    id = "generate-deps",
    runsOn = RunnerType.UbuntuLatest,
    permissions = MO(Permission.Contents to Mode.Write),
  ) {
    uses(action = Checkout())
    usesJdk()
    usesGradle(gradleVersion = "8.11") // FIXME_someday: I had errors when null (when trying to use wrapper)
    run(
      name = "MyExperiments.generateDeps",
      env = MO("GENERATE_DEPS" to "true"),
      workingDirectory = "plugins",
      command = "gradle --info :refreshVersions:test --tests MyExperiments.generateDeps"
    )
    usesAddAndCommitFile("plugins/dependencies/src/test/resources/objects-for-deps.txt")
  }
}.write("generate-deps.yml", PProjRefreshDeps)


// FIXME: something less hacky/hardcoded/repetitive
fun injectUpdateGeneratedDepsWorkflowToDepsKtRepo() {
  myWorkflow(
    name = "Update Generated Deps",
    on = LO(Schedule(LO(everydayBefore6amUTC)), WorkflowDispatch()),
  ) {
    job(
      id = "update-generated-deps",
      runsOn = RunnerType.UbuntuLatest,
      permissions = MO(Permission.Contents to Mode.Write),
    ) {
      uses(action = Checkout())
      usesJdk()
      usesGradle()
      runGradleW("updateGeneratedDeps")
      usesAddAndCommitFile("src/main/kotlin/deps/Deps.kt")
    }
  }.write("update-generated-deps.yml", PProjDepsKt)
}


/**
 * Each name is the name of both: workflow, and file name in .github/workflows (without .yml extension)
 * hacky "d" prefix in all recognized names is mostly to avoid clashing with other workflows.
 * (if I add it to existing repos/forks) (and it means "default")
 */
private val MyDWorkflowNames = LO("dbuild", "drelease", "ddepsub")


suspend fun checkMyDWorkflowsInProject(
  projectPath: Path,
  yamlFilesPath: Path = projectPath / ".github" / "workflows",
  yamlFilesExt: String = "yml",
  failIfUnknownWorkflowFound: Boolean = false,
  failIfKnownWorkflowNotFound: Boolean = false,
) {
  val log = localULog()
  val fs = localUFileSys()
  log.i("Check my dworkflows in project: $projectPath")
  @Suppress("DEPRECATION")
  val yamlFiles = findAllFiles(yamlFilesPath, maxDepth = 1).filterExt(yamlFilesExt)
  val yamlNames = yamlFiles.map { it.name.substringBeforeLast('.') }
  for (dname in MyDWorkflowNames) {
    if (dname !in yamlNames) {
      val summary = "Workflow $dname not found."
      log.e("ERR project:${projectPath.name}: $summary")
      if (failIfKnownWorkflowNotFound) bad { summary }
    }
  }

  for (file in yamlFiles) {
    val dname = file.name.substringBeforeLast('.')
    val contentExpected = try {
      myDefaultWorkflowForProject(dname, projectPath.name).generateYaml()
    } catch (e: IllegalStateException) {
      if (failIfUnknownWorkflowFound) throw e
      else {
        log.e(e.message); continue
      }
    }
    val contentActual = fs.readUtf8(file)
    contentActual.chkEq(contentExpected) {
      val summary = "Workflow $dname was modified."
      log.e("ERR project:${projectPath.name}: $summary")
      summary
    }
    log.i("OK project:${projectPath.name} workflow:$dname")
  }
}

suspend fun injectDWorkflowsToProject(
  projectPath: Path,
  yamlFilesPath: Path = projectPath / ".github" / "workflows",
  yamlFilesExt: String = "yml",
) {
  val log = localULog()
  val fs = localUFileSys()
  log.i("Inject default workflows to project: $projectPath")
  for (dname in MyDWorkflowNames) {
    val file = yamlFilesPath / "$dname.$yamlFilesExt"
    val contentOld = try {
      fs.readUtf8(file)
    } catch (e: FileNotFoundException) {
      ""
    }
    val contentNew = myDefaultWorkflowForProject(dname, projectPath.name).generateYaml()
    fs.writeUtf8(file, contentNew, createParentDir = true)
    val summary =
      if (contentNew == contentOld) "No changes."
      else "Changes detected (len ${contentOld.length}->${contentNew.length})"
    log.i("Inject workflow to project:${projectPath.name} dname:$dname - $summary")
  }
}

@OptIn(ExampleApi::class)
private suspend fun myDefaultWorkflowForProject(dname: String, projectName: String) = myDefaultWorkflow(
  dname = dname,
  dreleasePackage = when(projectName) {
    "UWidgets" -> "packageDeb" // I can't do packageReleaseDeb because proguard only supports jvm18 and fails.
    "AreaKim" -> "packageDeb"
    "kokpit667" -> "packageDeb"
    else -> null
  },
  dreleaseUpload = when(projectName) {
    "KGround" -> LO(
      "kgroundx-app/build/distributions/*.zip" // let's ignore tars (zips better for normies)
    )
    "UWidgets" -> LO(
      "uwidgets-demo-app/build/compose/binaries/main/deb/*.deb",
      "uwidgets-demo-app/build/outputs/apk/debug/*.apk",
      "uwidgets-demo-app/build/outputs/apk/release/*.apk",
    )
    "AreaKim" -> LO(
      "areakim-demo-app/build/compose/binaries/main/deb/*.deb",
      "areakim-demo-app/build/outputs/apk/debug/*.apk",
      "areakim-demo-app/build/outputs/apk/release/*.apk",
    )
    "kokpit667" -> LO(
      "kodeskapp/build/compose/binaries/main/deb/*.deb",
      "kodrapp/build/outputs/apk/debug/*.apk",
      "kodrapp/build/outputs/apk/release/*.apk",
      "kmd/build/distributions/*.zip"
    )
    else -> LO()
  },
  dreleaseOssPublish = projectName in getMyPublicProjectsNames()
)

/**
 * @dname name of both: workflow, and file name in .github/workflows (without .yml extension)
 * hacky "d" prefix in all recognized names is mostly to avoid clashing with other workflows.
 * (if I add it to existing repos/forks) (and it means "default")
 */
private fun myDefaultWorkflow(
  dname: String,
  dreleasePackage: String? = null,
  dreleaseUpload: List<String> = LO(),
  dreleaseOssPublish: Boolean = false,
) = when (dname) {
  "dbuild" -> myDefaultBuildWorkflow()
  "drelease" -> myDefaultReleaseWorkflow(
    env = if (dreleaseOssPublish) myOssPublishingSecretsEnv else MO(),
    dreleasePackage = dreleasePackage,
    dreleaseUpload = dreleaseUpload,
    dreleaseOssPublish = dreleaseOssPublish,
  )
  "ddepsub" -> myDefaultDependencySubmissionWorkflow()
  else -> bad { "Unknown default workflow dname: $dname" }
}

private fun myDefaultBuildWorkflow(
  runners: List<RunnerType> = LO(RunnerType.UbuntuLatest),
  env: Map<String, String> = MO(),
) = myWorkflow(
  name = "dbuild",
  on = LO(Push(branches = LO("master", "main")), PullRequest(), WorkflowDispatch()),
  env = env,
) {
  runners.forEach { runnerType ->
    job(
      id = "build-for-${runnerType::class.simpleName}",
      runsOn = runnerType,
    ) { usesDefaultBuild() }
  }
}

private fun myDefaultReleaseWorkflow(
  runner: RunnerType = RunnerType.UbuntuLatest,
  env: Map<String, String> = MO(),
  dreleasePackage: String? = null,
  dreleaseUpload: List<String> = LO(),
  dreleaseOssPublish: Boolean = false,
) =
  myWorkflow(
    name = "drelease",
    on = LO(Push(tags = LO("v*.*.*"))),
    env = env,
  ) {
    job(
      id = "release",
      runsOn = runner,
    ) {
      usesDefaultBuild()
      dreleasePackage?.let { runGradleW(it) }
      if (dreleaseUpload.isNotEmpty()) uses(action = UploadArtifact(path = dreleaseUpload))
      if (dreleaseOssPublish) runGradleW("publishAndReleaseToMavenCentral")
      // TODO_someday: consider sth like: https://github.com/ansman/sonatype-publish-fix
      // TODO_someday: something more like
      // github-workflows-kt/.github/workflows/release.main.kts
      // github-workflows-kt/buildSrc/src/main/kotlin/buildsrc/tasks/AwaitMavenCentralDeployTask.kt
    }
  }

private fun myDefaultDependencySubmissionWorkflow(
  runner: RunnerType = RunnerType.UbuntuLatest,
  env: Map<String, String> = MO(),
) =
  myWorkflow(
    name = "ddepsub",
    on = LO(Push(branches = LO("master", "main")), WorkflowDispatch()),
    env = env,
  ) {
    job(
      id = "dependency-submission-on-${runner::class.simpleName}",
      runsOn = runner,
      permissions = MO(Permission.Contents to Mode.Write),
    ) {
      uses(action = Checkout())
      usesJdk()
      uses(action = ActionsDependencySubmission_Untyped())
    }
  }

fun JobBuilder<JobOutputs.EMPTY>.usesJdk(
  name: String? = "Set up JDK",
  version: String? = "23", // fixme_maybe: somehow take from DepsKt:Vers:JvmDefaultVer ?
  distribution: SetupJava.Distribution = SetupJava.Distribution.Zulu, // fixme_later: which dist?
) = uses(
  name = name,
  action = SetupJava(
    javaVersion = version,
    distribution = distribution,
  ),
)
fun JobBuilder<JobOutputs.EMPTY>.usesDefaultBuild() {
  uses(action = Checkout())
  usesJdk()
  usesGradle()
  runGradleW("build")
}

fun JobBuilder<JobOutputs.EMPTY>.usesGradle(
  vararg useNamedArgs: Unit,
  name: String? = null,
  env: Map<String, String> = MO(),
  gradleVersion: String? = null, // null means it should try to use wrapper
) = uses(
  name = name,
  action = ActionsSetupGradle(
    gradleVersion = gradleVersion,
  ),
  env = env,
)

fun JobBuilder<JobOutputs.EMPTY>.runGradleW(tasks: String) =
  run(name = tasks, command = "./gradlew $tasks --no-configuration-cache --no-parallel")

// Not deleting for a while.
@Deprecated("Use generated: bindings/generated/ActionsSetupGradle.kt")
class MyActionsSetupGradle(
  private val gradleVersion: String? = null, // null means it should try to use wrapper
) : RegularAction<Action.Outputs>("gradle", "actions/setup-gradle", "v4") {
  override fun toYamlArguments() = MONN("gradle-version" to gradleVersion)
  override fun buildOutputObject(stepId: String) = Outputs(stepId)
}

fun JobBuilder<JobOutputs.EMPTY>.usesAddAndCommitFile(filePath: String, name: String? = "Add and commit file") =
  uses(
    name = name,
    action = AddAndCommit(
      add = filePath,
      defaultAuthor = AddAndCommit.DefaultAuthor.UserInfo,
      // without it, I get commits authored with my old username: langara
    ),
  )

fun Workflow.write(fullPath: Path, createParentDir: Boolean = false, fs: FileSystem = FileSystem.SYSTEM) {
  fs.writeUtf8(fullPath, generateYaml(), createParentDir)
}

fun Workflow.write(fileName: String, gitRootDir: Path, fs: FileSystem = FileSystem.SYSTEM) {
  write(gitRootDir / ".github" / "workflows" / fileName, createParentDir = true, fs = fs)
}
