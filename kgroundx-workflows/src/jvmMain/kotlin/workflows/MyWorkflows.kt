@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.workflows

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.endbug.AddAndCommit
import io.github.typesafegithub.workflows.actions.gradle.ActionsDependencySubmission_Untyped
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.JobOutputs
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
import kotlin.collections.LinkedHashMap
import kotlinx.coroutines.flow.Flow
import okio.*
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.ulog.*


@ExampleApi suspend fun checkMyDWorkflowsInMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { checkMyDWorkflowsInProject(it) }


@ExampleApi suspend fun injectMyDWorkflowsToMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { injectDWorkflowsToProject(it) }

@ExampleApi private fun Flow<String>.mapFilterLocalDWorkflowsProjectsPathS() =
  mapFilterLocalKotlinProjectsPathS {
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

private val mySecretsEnv = listOf(
  "signing_keyId", "signing_password", "signing_key",
  "ossrhUsername", "ossrhPassword", "sonatypeStagingProfileId",
)
  .map { "MYKOTLIBS_$it" }
  .associateWith { expr("secrets.$it") } as LinkedHashMap<String, String>



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
  block: WorkflowBuilder.() -> Unit,
): Workflow {
  lateinit var result: Workflow
  workflow(name = name, on = on, block = block, useWorkflow = { result = it })
  return result
}

// FIXME_maybe: something less hacky/hardcoded
fun injectHackyGenerateDepsWorkflowToRefreshDepsRepo() = myWorkflow(
  "Generate Deps", listOf(Schedule(listOf(everydayAfter5amUTC)), WorkflowDispatch()),
) {
  job(
    id = "generate-deps",
    runsOn = RunnerType.UbuntuLatest,
    _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
  ) {
    uses(action = Checkout())
    usesJdk()
    usesGradle(gradleVersion = "8.10.2") // FIXME_someday: I had errors when null (when trying to use wrapper)
    run(
      name = "MyExperiments.generateDeps",
      env = linkedMapOf("GENERATE_DEPS" to "true"),
      workingDirectory = "plugins",
      command = "gradle --info :refreshVersions:test --tests MyExperiments.generateDeps"
    )
    usesAddAndCommitFile("plugins/dependencies/src/test/resources/objects-for-deps.txt")
  }
}.write("generate-deps.yml", PProjRefreshDeps)


// FIXME: something less hacky/hardcoded/repetitive
fun injectUpdateGeneratedDepsWorkflowToDepsKtRepo() {
  myWorkflow("Update Generated Deps", listOf(Schedule(listOf(everydayBefore6amUTC)), WorkflowDispatch())) {
    job(
      id = "update-generated-deps",
      runsOn = RunnerType.UbuntuLatest,
      env = mySecretsEnv,
      _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
    ) {
      uses(action = Checkout())
      usesJdk()
      usesGradle()
      run(
        name = "updateGeneratedDeps",
        command = "./gradlew updateGeneratedDeps --no-configuration-cache --no-parallel"
      )
      usesAddAndCommitFile("src/main/kotlin/deps/Deps.kt")
    }
  }.write("update-generated-deps.yml", PProjDepsKt)
}


private val MyDWorkflowNames = listOf("dbuild", "drelease", "ddepsub")


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
      myDefaultWorkflow(dname).generateYaml()
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
    val contentNew = myDefaultWorkflow(dname).generateYaml()
    fs.writeUtf8(file, contentNew, createParentDir = true)
    val summary =
      if (contentNew == contentOld) "No changes."
      else "Changes detected (len ${contentOld.length}->${contentNew.length})"
    log.i("Inject workflow to project:${projectPath.name} dname:$dname - $summary")
  }
}


/**
 * @dname name of both: workflow, and file name in .github/workflows (without .yml extension)
 * hacky "d" prefix in all recognized names is mostly to avoid clashing with other workflows.
 * (if I add it to existing repos/forks) (and it means "default")
 */
internal fun myDefaultWorkflow(dname: String) = when (dname) {
  "dbuild" -> myDefaultBuildWorkflow()
  "drelease" -> myDefaultReleaseWorkflow()
  "ddepsub" -> myDefaultDependencySubmissionWorkflow()
  else -> bad { "Unknown default workflow dname: $dname" }
}

private fun myDefaultBuildWorkflow(runners: List<RunnerType> = listOf(RunnerType.UbuntuLatest)) =
  myWorkflow("dbuild", listOf(Push(branches = listOf("master", "main")), PullRequest(), WorkflowDispatch()),
  ) {
    runners.forEach { runnerType ->
      job(
        id = "build-for-${runnerType::class.simpleName}",
        runsOn = runnerType,
        env = mySecretsEnv,
      ) {
        uses(action = Checkout())
        usesJdk()
        usesGradle()
        run(name = "Build", command = "./gradlew build --no-configuration-cache --no-parallel")
      }
    }
  }

private fun myDefaultReleaseWorkflow() =
  myWorkflow("drelease", listOf(Push(tags = listOf("v*.*.*")))) {
    job(
      id = "release",
      env = mySecretsEnv,
      runsOn = RunnerType.UbuntuLatest,
    ) {
      uses(action = Checkout())
      usesJdk()
      usesGradle()
      run(name = "Build", command = "./gradlew build --no-configuration-cache --no-parallel")
      run(
        name = "Publish to Sonatype",
        command = "./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository --no-configuration-cache --no-parallel",
      )
      // TODO_someday: consider sth like: https://github.com/ansman/sonatype-publish-fix
      // TODO_someday: something more like
      // github-workflows-kt/.github/workflows/release.main.kts
      // github-workflows-kt/buildSrc/src/main/kotlin/buildsrc/tasks/AwaitMavenCentralDeployTask.kt
    }
  }

private fun myDefaultDependencySubmissionWorkflow(runner: RunnerType = RunnerType.UbuntuLatest) =
  myWorkflow("ddepsub", listOf(Push(branches = listOf("master", "main")), WorkflowDispatch()),
  ) {
    job(
      id = "dependency-submission-on-${runner::class.simpleName}",
      runsOn = runner,
      env = mySecretsEnv,
    ) {
      uses(action = Checkout())
      usesJdk()
      uses(action = ActionsDependencySubmission_Untyped())
    }
  }

fun JobBuilder<JobOutputs.EMPTY>.usesJdk(
  name: String? = "Set up JDK",
  version: String? = "22", // fixme_maybe: somehow take from DepsKt:Vers:JvmDefaultVer ?
  distribution: SetupJava.Distribution = SetupJava.Distribution.Zulu, // fixme_later: which dist?
) = uses(
  name = name,
  action = SetupJava(
    javaVersion = version,
    distribution = distribution,
  ),
)

fun JobBuilder<JobOutputs.EMPTY>.usesGradle(
  vararg useNamedArgs: Unit,
  name: String? = null,
  env: Map<String, String> = mapOf(),
  gradleVersion: String? = null, // null means it should try to use wrapper
) = uses(
  name = name,
  action = ActionsSetupGradle(
    gradleVersion = gradleVersion,
  ),
  env = env,
)

// Not deleting for a while.
@Deprecated("Use generated: bindings/generated/ActionsSetupGradle.kt")
class MyActionsSetupGradle(
  private val gradleVersion: String? = null, // null means it should try to use wrapper
) : RegularAction<Action.Outputs>("gradle", "actions/setup-gradle", "v4") {
  override fun toYamlArguments() = linkedMapOfNotNull(
    "gradle-version" to gradleVersion,
  )
  override fun buildOutputObject(stepId: String) = Outputs(stepId)
}


@Suppress("UNCHECKED_CAST")
fun <K: Any, V: Any> linkedMapOfNotNull(vararg pairs: Pair<K, V?>): LinkedHashMap<K, V> =
  linkedMapOf(*pairs.mapNotNull { if (it.second == null) null else (it as Pair<K, V>) }.toTypedArray())

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
