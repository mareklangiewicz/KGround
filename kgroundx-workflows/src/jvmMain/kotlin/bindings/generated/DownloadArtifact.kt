// This file was generated using action-binding-generator. Don't change it by hand, otherwise your
// changes will be overwritten with the next binding code regeneration.
// See https://github.com/typesafegithub/github-workflows-kt for more info.
@file:Suppress(
    "DataClassPrivateConstructor",
    "UNUSED_PARAMETER",
)

package io.github.typesafegithub.workflows.actions.actions

import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.actions.RegularAction
import java.util.LinkedHashMap
import kotlin.Boolean
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: Download a Build Artifact
 *
 * Download a build artifact that was previously uploaded in the workflow by the upload-artifact
 * action
 *
 * [Action on GitHub](https://github.com/actions/download-artifact)
 *
 * @param name Name of the artifact to download. If unspecified, all artifacts for the run are
 * downloaded.
 * @param name_Untyped Name of the artifact to download. If unspecified, all artifacts for the run
 * are downloaded.
 * @param path Destination path. Supports basic tilde expansion. Defaults to $GITHUB_WORKSPACE
 * @param path_Untyped Destination path. Supports basic tilde expansion. Defaults to
 * $GITHUB_WORKSPACE
 * @param pattern A glob pattern matching the artifacts that should be downloaded. Ignored if name
 * is specified.
 * @param pattern_Untyped A glob pattern matching the artifacts that should be downloaded. Ignored
 * if name is specified.
 * @param mergeMultiple When multiple artifacts are matched, this changes the behavior of the
 * destination directories. If true, the downloaded artifacts will be in the same directory specified
 * by path. If false, the downloaded artifacts will be extracted into individual named directories
 * within the specified path.
 * @param mergeMultiple_Untyped When multiple artifacts are matched, this changes the behavior of
 * the destination directories. If true, the downloaded artifacts will be in the same directory
 * specified by path. If false, the downloaded artifacts will be extracted into individual named
 * directories within the specified path.
 * @param githubToken The GitHub token used to authenticate with the GitHub API. This is required
 * when downloading artifacts from a different repository or from a different workflow run. If this is
 * not specified, the action will attempt to download artifacts from the current repository and the
 * current workflow run.
 * @param githubToken_Untyped The GitHub token used to authenticate with the GitHub API. This is
 * required when downloading artifacts from a different repository or from a different workflow run. If
 * this is not specified, the action will attempt to download artifacts from the current repository and
 * the current workflow run.
 * @param repository The repository owner and the repository name joined together by "/". If
 * github-token is specified, this is the repository that artifacts will be downloaded from.
 * @param repository_Untyped The repository owner and the repository name joined together by "/". If
 * github-token is specified, this is the repository that artifacts will be downloaded from.
 * @param runId The id of the workflow run where the desired download artifact was uploaded from. If
 * github-token is specified, this is the run that artifacts will be downloaded from.
 * @param runId_Untyped The id of the workflow run where the desired download artifact was uploaded
 * from. If github-token is specified, this is the run that artifacts will be downloaded from.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@ExposedCopyVisibility
public data class DownloadArtifact private constructor(
    /**
     * Name of the artifact to download. If unspecified, all artifacts for the run are downloaded.
     */
    public val name: String? = null,
    /**
     * Name of the artifact to download. If unspecified, all artifacts for the run are downloaded.
     */
    public val name_Untyped: String? = null,
    /**
     * Destination path. Supports basic tilde expansion. Defaults to $GITHUB_WORKSPACE
     */
    public val path: String? = null,
    /**
     * Destination path. Supports basic tilde expansion. Defaults to $GITHUB_WORKSPACE
     */
    public val path_Untyped: String? = null,
    /**
     * A glob pattern matching the artifacts that should be downloaded. Ignored if name is
     * specified.
     */
    public val pattern: String? = null,
    /**
     * A glob pattern matching the artifacts that should be downloaded. Ignored if name is
     * specified.
     */
    public val pattern_Untyped: String? = null,
    /**
     * When multiple artifacts are matched, this changes the behavior of the destination
     * directories. If true, the downloaded artifacts will be in the same directory specified by path.
     * If false, the downloaded artifacts will be extracted into individual named directories within
     * the specified path.
     */
    public val mergeMultiple: Boolean? = null,
    /**
     * When multiple artifacts are matched, this changes the behavior of the destination
     * directories. If true, the downloaded artifacts will be in the same directory specified by path.
     * If false, the downloaded artifacts will be extracted into individual named directories within
     * the specified path.
     */
    public val mergeMultiple_Untyped: String? = null,
    /**
     * The GitHub token used to authenticate with the GitHub API. This is required when downloading
     * artifacts from a different repository or from a different workflow run. If this is not
     * specified, the action will attempt to download artifacts from the current repository and the
     * current workflow run.
     */
    public val githubToken: String? = null,
    /**
     * The GitHub token used to authenticate with the GitHub API. This is required when downloading
     * artifacts from a different repository or from a different workflow run. If this is not
     * specified, the action will attempt to download artifacts from the current repository and the
     * current workflow run.
     */
    public val githubToken_Untyped: String? = null,
    /**
     * The repository owner and the repository name joined together by "/". If github-token is
     * specified, this is the repository that artifacts will be downloaded from.
     */
    public val repository: String? = null,
    /**
     * The repository owner and the repository name joined together by "/". If github-token is
     * specified, this is the repository that artifacts will be downloaded from.
     */
    public val repository_Untyped: String? = null,
    /**
     * The id of the workflow run where the desired download artifact was uploaded from. If
     * github-token is specified, this is the run that artifacts will be downloaded from.
     */
    public val runId: String? = null,
    /**
     * The id of the workflow run where the desired download artifact was uploaded from. If
     * github-token is specified, this is the run that artifacts will be downloaded from.
     */
    public val runId_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<DownloadArtifact.Outputs>("actions", "download-artifact", _customVersion ?: "v4")
        {
    init {
        require(!((name != null) && (name_Untyped != null))) {
            "Only name or name_Untyped must be set, but not both"
        }

        require(!((path != null) && (path_Untyped != null))) {
            "Only path or path_Untyped must be set, but not both"
        }

        require(!((pattern != null) && (pattern_Untyped != null))) {
            "Only pattern or pattern_Untyped must be set, but not both"
        }

        require(!((mergeMultiple != null) && (mergeMultiple_Untyped != null))) {
            "Only mergeMultiple or mergeMultiple_Untyped must be set, but not both"
        }

        require(!((githubToken != null) && (githubToken_Untyped != null))) {
            "Only githubToken or githubToken_Untyped must be set, but not both"
        }

        require(!((repository != null) && (repository_Untyped != null))) {
            "Only repository or repository_Untyped must be set, but not both"
        }

        require(!((runId != null) && (runId_Untyped != null))) {
            "Only runId or runId_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        name: String? = null,
        name_Untyped: String? = null,
        path: String? = null,
        path_Untyped: String? = null,
        pattern: String? = null,
        pattern_Untyped: String? = null,
        mergeMultiple: Boolean? = null,
        mergeMultiple_Untyped: String? = null,
        githubToken: String? = null,
        githubToken_Untyped: String? = null,
        repository: String? = null,
        repository_Untyped: String? = null,
        runId: String? = null,
        runId_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(name = name, name_Untyped = name_Untyped, path = path, path_Untyped = path_Untyped,
            pattern = pattern, pattern_Untyped = pattern_Untyped, mergeMultiple = mergeMultiple,
            mergeMultiple_Untyped = mergeMultiple_Untyped, githubToken = githubToken,
            githubToken_Untyped = githubToken_Untyped, repository = repository, repository_Untyped =
            repository_Untyped, runId = runId, runId_Untyped = runId_Untyped, _customInputs =
            _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            name?.let { "name" to it },
            name_Untyped?.let { "name" to it },
            path?.let { "path" to it },
            path_Untyped?.let { "path" to it },
            pattern?.let { "pattern" to it },
            pattern_Untyped?.let { "pattern" to it },
            mergeMultiple?.let { "merge-multiple" to it.toString() },
            mergeMultiple_Untyped?.let { "merge-multiple" to it },
            githubToken?.let { "github-token" to it },
            githubToken_Untyped?.let { "github-token" to it },
            repository?.let { "repository" to it },
            repository_Untyped?.let { "repository" to it },
            runId?.let { "run-id" to it },
            runId_Untyped?.let { "run-id" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * Path of artifact download
         */
        public val downloadPath: String = "steps.$stepId.outputs.download-path"
    }
}

