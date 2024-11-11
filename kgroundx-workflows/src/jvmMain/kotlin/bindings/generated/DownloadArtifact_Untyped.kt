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
import kotlin.Deprecated
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * ```text
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * !!!                             WARNING                             !!!
 * !!!                                                                 !!!
 * !!! This action binding has no typings provided. All inputs will    !!!
 * !!! have a default type of String.                                  !!!
 * !!! To be able to use this action in a type-safe way, ask the       !!!
 * !!! action's owner to provide the typings using                     !!!
 * !!!                                                                 !!!
 * !!! https://github.com/typesafegithub/github-actions-typing         !!!
 * !!!                                                                 !!!
 * !!! or if it's impossible, contribute typings to a community-driven !!!
 * !!!                                                                 !!!
 * !!! https://github.com/typesafegithub/github-actions-typing-catalog !!!
 * !!!                                                                 !!!
 * !!! This '_Untyped' binding will be available even once the typings !!!
 * !!! are added.                                                      !!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ```
 *
 * Action: Download a Build Artifact
 *
 * Download a build artifact that was previously uploaded in the workflow by the upload-artifact
 * action
 *
 * [Action on GitHub](https://github.com/actions/download-artifact)
 *
 * @param name_Untyped Name of the artifact to download. If unspecified, all artifacts for the run
 * are downloaded.
 * @param path_Untyped Destination path. Supports basic tilde expansion. Defaults to
 * $GITHUB_WORKSPACE
 * @param pattern_Untyped A glob pattern matching the artifacts that should be downloaded. Ignored
 * if name is specified.
 * @param mergeMultiple_Untyped When multiple artifacts are matched, this changes the behavior of
 * the destination directories. If true, the downloaded artifacts will be in the same directory
 * specified by path. If false, the downloaded artifacts will be extracted into individual named
 * directories within the specified path.
 * @param githubToken_Untyped The GitHub token used to authenticate with the GitHub API. This is
 * required when downloading artifacts from a different repository or from a different workflow run. If
 * this is not specified, the action will attempt to download artifacts from the current repository and
 * the current workflow run.
 * @param repository_Untyped The repository owner and the repository name joined together by "/". If
 * github-token is specified, this is the repository that artifacts will be downloaded from.
 * @param runId_Untyped The id of the workflow run where the desired download artifact was uploaded
 * from. If github-token is specified, this is the run that artifacts will be downloaded from.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("DownloadArtifact"),
)
@ExposedCopyVisibility
public data class DownloadArtifact_Untyped private constructor(
    /**
     * Name of the artifact to download. If unspecified, all artifacts for the run are downloaded.
     */
    public val name_Untyped: String? = null,
    /**
     * Destination path. Supports basic tilde expansion. Defaults to $GITHUB_WORKSPACE
     */
    public val path_Untyped: String? = null,
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
    public val mergeMultiple_Untyped: String? = null,
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
    public val repository_Untyped: String? = null,
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
) : RegularAction<DownloadArtifact_Untyped.Outputs>("actions", "download-artifact", _customVersion
        ?: "v4") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        name_Untyped: String? = null,
        path_Untyped: String? = null,
        pattern_Untyped: String? = null,
        mergeMultiple_Untyped: String? = null,
        githubToken_Untyped: String? = null,
        repository_Untyped: String? = null,
        runId_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(name_Untyped = name_Untyped, path_Untyped = path_Untyped, pattern_Untyped =
            pattern_Untyped, mergeMultiple_Untyped = mergeMultiple_Untyped, githubToken_Untyped =
            githubToken_Untyped, repository_Untyped = repository_Untyped, runId_Untyped =
            runId_Untyped, _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            name_Untyped?.let { "name" to it },
            path_Untyped?.let { "path" to it },
            pattern_Untyped?.let { "pattern" to it },
            mergeMultiple_Untyped?.let { "merge-multiple" to it },
            githubToken_Untyped?.let { "github-token" to it },
            repository_Untyped?.let { "repository" to it },
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

