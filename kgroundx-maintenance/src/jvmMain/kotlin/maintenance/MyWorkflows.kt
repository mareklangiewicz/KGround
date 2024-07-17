@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.maintenance

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.actions.endbug.AddAndCommitV9
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradleV3
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.*
import okio.*
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.implictx

private val myFork = expr { "${github.repository_owner} == 'mareklangiewicz'" }

private val mySecretsEnv = listOf(
  "signing_keyId", "signing_password", "signing_key",
  "ossrhUsername", "ossrhPassword", "sonatypeStagingProfileId",
)
  .map { "MYKOTLIBS_$it" }
  .associateWith { expr("secrets.$it") } as LinkedHashMap<String, String>



// Github cron is UTC so about 2 hours behind Warsaw.
// https://www.timeanddate.com/worldclock/timezone/utc
// Github can delay or even drop sheduled events depending on
// high load times (like full hours), repo usage, and many different things.
// https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#schedule
private val everydayAfter5amUTC = Cron(hour = "5", minute = "37")
// BTW refreshDeps should take less than 10min
private val everydayBefore6amUTC = Cron(hour = "5", minute = "53")

// FIXME: something less hacky/hardcoded
@Suppress("IdentifierGrammar")
fun injectHackyGenerateDepsWorkflowToRefreshDepsRepo() {
  val workflow = workflow(
    name = "Generate Deps",
    on = listOf(Schedule(listOf(everydayAfter5amUTC)), WorkflowDispatch()),
  ) {
    job(
      id = "generate-deps",
      runsOn = RunnerType.UbuntuLatest,
      _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
    ) {
      uses(action = CheckoutV4())
      usesJdk()
      usesGradle(
        name = "MyExperiments.generateDeps",
        env = linkedMapOf("GENERATE_DEPS" to "true"),
        gradleVersion = "8.6", // FIXME_someday: I have errors when null (when trying to use wrapper)
        arguments = "--info :refreshVersions:test --tests MyExperiments.generateDeps",
        buildRootDirectory = "plugins",
      )
      usesAddAndCommitFile("plugins/dependencies/src/test/resources/objects-for-deps.txt")
    }
  }
  workflow.write("generate-deps.yml", PathToRefreshDepsProject)
}


// FIXME: something less hacky/hardcoded/repetitive
fun injectUpdateGeneratedDepsWorkflowToDepsKtRepo() {
  val workflow = workflow(
    name = "Update Generated Deps",
    on = listOf(Schedule(listOf(everydayBefore6amUTC)), WorkflowDispatch()),
  ) {
    job(
      id = "update-generated-deps",
      runsOn = RunnerType.UbuntuLatest,
      env = mySecretsEnv,
      _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
    ) {
      uses(action = CheckoutV4())
      usesJdk()
      usesGradle(
        name = "updateGeneratedDeps",
        arguments = "updateGeneratedDeps",
      )
      usesAddAndCommitFile("src/main/kotlin/deps/Deps.kt")
    }
  }
  workflow.write("update-generated-deps.yml", PathToDepsKtProject)
}


private val MyDWorkflowNames = listOf("dbuild", "drelease")


suspend fun checkMyDWorkflowsInProject(
  projectPath: Path,
  yamlFilesPath: Path = projectPath / ".github" / "workflows",
  yamlFilesExt: String = "yml",
  failIfUnknownWorkflowFound: Boolean = false,
  failIfKnownWorkflowNotFound: Boolean = false,
) {
  val log = implictx<ULog>()
  val fs = implictx<UFileSys>()
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
      defaultWorkflow(dname).generateYaml()
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

@ExampleApi suspend fun injectDWorkflowsToKotlinProject(projectName: String) =
  injectDWorkflowsToProject(PathToKotlinProjects / projectName)

suspend fun injectDWorkflowsToProject(
  projectPath: Path,
  yamlFilesPath: Path = projectPath / ".github" / "workflows",
  yamlFilesExt: String = "yml",
) {
  val log = implictx<ULog>()
  val fs = implictx<UFileSys>()
  log.i("Inject default workflows to project: $projectPath")
  for (dname in MyDWorkflowNames) {
    val file = yamlFilesPath / "$dname.$yamlFilesExt"
    val contentOld = try {
      fs.readUtf8(file)
    } catch (e: FileNotFoundException) {
      ""
    }
    val contentNew = defaultWorkflow(dname).generateYaml()
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
internal fun defaultWorkflow(dname: String) = when (dname) {
  "dbuild" -> defaultBuildWorkflow()
  "drelease" -> defaultReleaseWorkflow()
  else -> bad { "Unknown default workflow dname: $dname" }
}

private fun defaultBuildWorkflow(runners: List<RunnerType> = listOf(RunnerType.UbuntuLatest)) =
  workflow(
    name = "dbuild",
    on = listOf(
      Push(branches = listOf("master", "main")),
      PullRequest(),
      WorkflowDispatch(),
    ),
  ) {
    runners.forEach { runnerType ->
      job(
        id = "build-for-${runnerType::class.simpleName}",
        runsOn = runnerType,
        env = mySecretsEnv,
      ) {
        uses(action = CheckoutV4())
        usesJdk()
        usesGradleBuild()
      }
    }
  }

private fun defaultReleaseWorkflow() =
  workflow(
    name = "drelease",
    on = listOf(Push(tags = listOf("v*.*.*"))),
  ) {
    job(
      id = "release",
      env = mySecretsEnv,
      runsOn = RunnerType.UbuntuLatest,
    ) {
      uses(action = CheckoutV4())
      usesJdk()
      usesGradleBuild()
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

fun JobBuilder<JobOutputs.EMPTY>.usesJdk(
  name: String? = "Set up JDK",
  version: String? = "22", // fixme_maybe: somehow take from DepsKt:Vers:JvmDefaultVer ?
  distribution: SetupJavaV4.Distribution = SetupJavaV4.Distribution.Zulu, // fixme_later: which dist?
) = uses(
  name = name,
  action = SetupJavaV4(
    javaVersion = version,
    distribution = distribution,
  ),
)

fun JobBuilder<JobOutputs.EMPTY>.usesGradle(
  vararg useNamedArgs: Unit,
  name: String? = null,
  env: Map<String, String> = mapOf(),
  gradleVersion: String? = null, // null means it should try to use wrapper
  arguments: String? = null,
  buildRootDirectory: String? = null,
) = uses(
  name = name,
  action = ActionsSetupGradleV3(
    gradleVersion = gradleVersion,
    arguments = arguments,
    buildRootDirectory = buildRootDirectory,
  ),
  env = env,
)

fun JobBuilder<JobOutputs.EMPTY>.usesGradleBuild(
  name: String? = "Build",
  env: Map<String, String> = mapOf(),
  gradleVersion: String? = null, // null means it should try to use wrapper
  buildRootDirectory: String? = null,
) = usesGradle(
  name = name,
  env = env,
  gradleVersion = gradleVersion,
  arguments = "build",
  buildRootDirectory = buildRootDirectory,
)

fun JobBuilder<JobOutputs.EMPTY>.usesAddAndCommitFile(filePath: String, name: String? = "Add and commit file") =
  uses(
    name = name,
    action = AddAndCommitV9(
      add = filePath,
      defaultAuthor = AddAndCommitV9.DefaultAuthor.UserInfo,
      // without it, I get commits authored with my old username: langara
    ),
  )

fun Workflow.write(fullPath: Path, createParentDir: Boolean = false, fs: FileSystem = FileSystem.SYSTEM) {
  fs.writeUtf8(fullPath, generateYaml(), createParentDir)
}

fun Workflow.write(fileName: String, gitRootDir: Path, fs: FileSystem = FileSystem.SYSTEM) {
  write(gitRootDir / ".github" / "workflows" / fileName, createParentDir = true, fs = fs)
}
