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
 * Action: Upload a Build Artifact
 *
 * Upload a build artifact that can be used by subsequent workflow steps
 *
 * [Action on GitHub](https://github.com/actions/upload-artifact)
 *
 * @param name_Untyped Artifact name
 * @param path_Untyped A file, directory or wildcard pattern that describes what to upload
 * @param ifNoFilesFound_Untyped The desired behavior if no files are found using the provided path.
 * Available Options:
 *   warn: Output a warning but do not fail the action
 *   error: Fail the action with an error message
 *   ignore: Do not output any warnings or errors, the action does not fail
 * @param retentionDays_Untyped Duration after which artifact will expire in days. 0 means using
 * default retention.
 * Minimum 1 day. Maximum 90 days unless changed from the repository settings page.
 * @param compressionLevel_Untyped The level of compression for Zlib to be applied to the artifact
 * archive. The value can range from 0 to 9: - 0: No compression - 1: Best speed - 6: Default
 * compression (same as GNU Gzip) - 9: Best compression Higher levels will result in better
 * compression, but will take longer to complete. For large files that are not easily compressed, a
 * value of 0 is recommended for significantly faster uploads.
 * @param overwrite_Untyped If true, an artifact with a matching name will be deleted before a new
 * one is uploaded. If false, the action will fail if an artifact for the given name already exists.
 * Does not fail if the artifact does not exist.
 * @param includeHiddenFiles_Untyped If true, hidden files will be included in the artifact. If
 * false, hidden files will be excluded from the artifact.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("UploadArtifact"),
)
@ExposedCopyVisibility
public data class UploadArtifact_Untyped private constructor(
    /**
     * Artifact name
     */
    public val name_Untyped: String? = null,
    /**
     * A file, directory or wildcard pattern that describes what to upload
     */
    public val path_Untyped: String,
    /**
     * The desired behavior if no files are found using the provided path.
     * Available Options:
     *   warn: Output a warning but do not fail the action
     *   error: Fail the action with an error message
     *   ignore: Do not output any warnings or errors, the action does not fail
     */
    public val ifNoFilesFound_Untyped: String? = null,
    /**
     * Duration after which artifact will expire in days. 0 means using default retention.
     * Minimum 1 day. Maximum 90 days unless changed from the repository settings page.
     */
    public val retentionDays_Untyped: String? = null,
    /**
     * The level of compression for Zlib to be applied to the artifact archive. The value can range
     * from 0 to 9: - 0: No compression - 1: Best speed - 6: Default compression (same as GNU Gzip) -
     * 9: Best compression Higher levels will result in better compression, but will take longer to
     * complete. For large files that are not easily compressed, a value of 0 is recommended for
     * significantly faster uploads.
     */
    public val compressionLevel_Untyped: String? = null,
    /**
     * If true, an artifact with a matching name will be deleted before a new one is uploaded. If
     * false, the action will fail if an artifact for the given name already exists. Does not fail if
     * the artifact does not exist.
     */
    public val overwrite_Untyped: String? = null,
    /**
     * If true, hidden files will be included in the artifact. If false, hidden files will be
     * excluded from the artifact.
     */
    public val includeHiddenFiles_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<UploadArtifact_Untyped.Outputs>("actions", "upload-artifact", _customVersion ?:
        "v4") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        name_Untyped: String? = null,
        path_Untyped: String,
        ifNoFilesFound_Untyped: String? = null,
        retentionDays_Untyped: String? = null,
        compressionLevel_Untyped: String? = null,
        overwrite_Untyped: String? = null,
        includeHiddenFiles_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(name_Untyped = name_Untyped, path_Untyped = path_Untyped, ifNoFilesFound_Untyped =
            ifNoFilesFound_Untyped, retentionDays_Untyped = retentionDays_Untyped,
            compressionLevel_Untyped = compressionLevel_Untyped, overwrite_Untyped =
            overwrite_Untyped, includeHiddenFiles_Untyped = includeHiddenFiles_Untyped,
            _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            name_Untyped?.let { "name" to it },
            "path" to path_Untyped,
            ifNoFilesFound_Untyped?.let { "if-no-files-found" to it },
            retentionDays_Untyped?.let { "retention-days" to it },
            compressionLevel_Untyped?.let { "compression-level" to it },
            overwrite_Untyped?.let { "overwrite" to it },
            includeHiddenFiles_Untyped?.let { "include-hidden-files" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * A unique identifier for the artifact that was just uploaded. Empty if the artifact upload
         * failed.
         * This ID can be used as input to other APIs to download, delete or get more information
         * about an artifact: https://docs.github.com/en/rest/actions/artifacts
         */
        public val artifactId: String = "steps.$stepId.outputs.artifact-id"

        /**
         * A download URL for the artifact that was just uploaded. Empty if the artifact upload
         * failed.
         * This download URL only works for requests Authenticated with GitHub. Anonymous downloads
         * will be prompted to first login.  If an anonymous download URL is needed than a short time
         * restricted URL can be generated using the download artifact API:
         * https://docs.github.com/en/rest/actions/artifacts#download-an-artifact
         * This URL will be valid for as long as the artifact exists and the workflow run and
         * repository exists. Once an artifact has expired this URL will no longer work. Common uses
         * cases for such a download URL can be adding download links to artifacts in descriptions or
         * comments on pull requests or issues.
         */
        public val artifactUrl: String = "steps.$stepId.outputs.artifact-url"
    }
}

