@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.maintenance

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.actions.endbug.AddAndCommitV9
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV3
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.*
import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.bad.*

private val myFork = expr { "${github.repository_owner} == 'langara'" }

private val mySecretsEnv = listOf(
    "signing_keyId", "signing_password", "signing_key",
    "ossrhUsername", "ossrhPassword", "sonatypeStagingProfileId"
)
    .map { "MYKOTLIBS_$it" }
    .associateWith { expr("secrets.$it") } as LinkedHashMap<String, String>


// FIXME: something less hacky/hardcoded
@Suppress("IdentifierGrammar")
fun injectHackyGenerateDepsWorkflowToRefreshDepsRepo() {
    val everyMondayAt7am = Cron(minute = "0", hour = "7", dayWeek = "1")
    val workflow = workflow(
        name = "Generate Deps",
        on = listOf(Schedule(listOf(everyMondayAt7am)), WorkflowDispatch()),
        targetFileName = "generate-deps.yml",
    ) {
        job(
            id = "generate-deps",
            runsOn = RunnerType.UbuntuLatest,
            _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
        ) {
            uses(action = CheckoutV4())
            usesJdk()
            uses(
                name = "MyExperiments.generateDeps",
                action = GradleBuildActionV3(
                    gradleVersion = "8.6", // FIXME NOW remove this workaround and change action to SetupGradleV3 when available
                    arguments = "--info :refreshVersions:test --tests MyExperiments.generateDeps",
                    buildRootDirectory = "plugins",
                ),
                env = linkedMapOf("GENERATE_DEPS" to "true"),
            )
            usesAddAndCommitFile("plugins/dependencies/src/test/resources/objects-for-deps.txt")
        }
    }
    workflow.writeToFile(gitRootDir = "/home/marek/code/kotlin/refreshDeps".toPath().toNioPath())
}


// FIXME: something less hacky/hardcoded/repetitive
fun injectUpdateGeneratedDepsWorkflowToDepsKtRepo() {
    val everyMondayAt8am = Cron(minute = "0", hour = "8", dayWeek = "1")
    val workflow = workflow(
        name = "Update Generated Deps",
        on = listOf(Schedule(listOf(everyMondayAt8am)), WorkflowDispatch()),
        targetFileName = "update-generated-deps.yml",
    ) {
        job(
            id = "update-generated-deps",
            runsOn = RunnerType.UbuntuLatest,
            env = mySecretsEnv,
            _customArguments = mapOf("permissions" to mapOf("contents" to "write")),
        ) {
            uses(action = CheckoutV4())
            usesJdk()
            uses(
                name = "updateGeneratedDeps",
                action = GradleBuildActionV3(
                    arguments = "updateGeneratedDeps",
                ),
            )
            usesAddAndCommitFile("src/main/kotlin/deps/Deps.kt")
        }
    }
    workflow.writeToFile(gitRootDir = "/home/marek/code/kotlin/DepsKt".toPath().toNioPath())
}


private val MyDWorkflowNames = listOf("dbuild", "drelease")


fun FileSystem.checkMyDWorkflowsInProject(
    projectPath: Path,
    yamlFilesPath: Path = projectPath / ".github" / "workflows",
    yamlFilesExt: String = "yml",
    failIfUnknownWorkflowFound: Boolean = false,
    failIfKnownWorkflowNotFound: Boolean = false,
    verbose: Boolean = false,
    log: (Any?) -> Unit = ::println,
) {
    if (verbose) log("Check my dworkflows in project: $projectPath")
    @Suppress("DEPRECATION")
    val yamlFiles = findAllFiles(yamlFilesPath, maxDepth = 1).filterExt(yamlFilesExt)
    val yamlNames = yamlFiles.map { it.name.substringBeforeLast('.') }
    for (dname in MyDWorkflowNames) {
        if (dname !in yamlNames) {
            val summary = "Workflow $dname not found."
            if (verbose) log("ERR project:${projectPath.name}: $summary")
            if (failIfKnownWorkflowNotFound) error(summary)
        }
    }

    for (file in yamlFiles) {
        val dname = file.name.substringBeforeLast('.')
        val contentExpected = try { defaultWorkflow(dname).toYaml() }
        catch (e: IllegalStateException) {
            if (failIfUnknownWorkflowFound) throw e
            else { if (verbose) log(e.message); continue }
        }
        val contentActual = readUtf8(file)
        contentActual.chkEq(contentExpected) {
            val summary = "Workflow $dname was modified."
            if (verbose) log("ERR project:${projectPath.name}: $summary")
            summary
        }
        if (verbose) log("OK project:${projectPath.name} workflow:$dname")
    }
}

@ExampleApi fun injectDWorkflowsToKotlinProject(
    projectName: String,
    log: (Any?) -> Unit = ::println,
) = SYSTEM.injectDWorkflowsToProject(PathToMyKotlinProjects / projectName, log = log)

fun FileSystem.injectDWorkflowsToProject(
    projectPath: Path,
    yamlFilesPath: Path = projectPath / ".github" / "workflows",
    yamlFilesExt: String = "yml",
    log: (Any?) -> Unit = ::println,
) {
    log("Inject default workflows to project: $projectPath")
    for (dname in MyDWorkflowNames) {
        val file = yamlFilesPath / "$dname.$yamlFilesExt"
        val contentOld = try { readUtf8(file) } catch (e: FileNotFoundException) { "" }
        val contentNew = defaultWorkflow(dname).toYaml()
        SYSTEM.writeUtf8(file, contentNew, createParentDir = true)
        val summary =
            if (contentNew == contentOld) "No changes."
            else "Changes detected (len ${contentOld.length}->${contentNew.length})"
        log("Inject workflow to project:${projectPath.name} dname:$dname - $summary")
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
            uses(
                name = "Publish to Sonatype",
                action = GradleBuildActionV3(
                    arguments = "publishToSonatype closeAndReleaseSonatypeStagingRepository",
                )
            )
            // TODO_someday: something like
            // github-workflows-kt/.github/workflows/release.main.kts#L49
            // github-workflows-kt/buildSrc/src/main/kotlin/buildsrc/tasks/AwaitMavenCentralDeployTask.kt
        }
    }

private fun JobBuilder<JobOutputs.EMPTY>.usesJdk(
    name: String? = "Set up JDK",
    version: String? = "21", // fixme_maybe: take from DepsNew.ver...? [Deps Selected]
    distribution: SetupJavaV4.Distribution = SetupJavaV4.Distribution.Zulu, // fixme_later: which dist?
) = uses(
        name = name,
        action = SetupJavaV4(
            javaVersion = version,
            distribution = distribution
        )
    )

private fun JobBuilder<JobOutputs.EMPTY>.usesGradleBuild(name: String? = "Build") =
    uses(name = name, action = GradleBuildActionV3(arguments = "build"))

private fun JobBuilder<JobOutputs.EMPTY>.usesAddAndCommitFile(filePath: String, name: String? = "Add and commit file") = uses(
        name = name,
        action = AddAndCommitV9(
            add = filePath,
            defaultAuthor = AddAndCommitV9.DefaultAuthor.UserInfo,
                // without it, I get commits authored with my old username: langara
        ),
    )
