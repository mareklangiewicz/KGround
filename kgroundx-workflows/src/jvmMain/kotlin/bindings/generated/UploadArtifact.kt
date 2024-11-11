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
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: Upload a Build Artifact
 *
 * Upload a build artifact that can be used by subsequent workflow steps
 *
 * [Action on GitHub](https://github.com/actions/upload-artifact)
 *
 * @param name Artifact name
 * @param name_Untyped Artifact name
 * @param path &lt;required&gt; A file, directory or wildcard pattern that describes what to upload
 * @param path_Untyped &lt;required&gt; A file, directory or wildcard pattern that describes what to
 * upload
 * @param ifNoFilesFound The desired behavior if no files are found using the provided path.
 * Available Options:
 *   warn: Output a warning but do not fail the action
 *   error: Fail the action with an error message
 *   ignore: Do not output any warnings or errors, the action does not fail
 * @param ifNoFilesFound_Untyped The desired behavior if no files are found using the provided path.
 * Available Options:
 *   warn: Output a warning but do not fail the action
 *   error: Fail the action with an error message
 *   ignore: Do not output any warnings or errors, the action does not fail
 * @param retentionDays Duration after which artifact will expire in days. 0 means using default
 * retention.
 * Minimum 1 day. Maximum 90 days unless changed from the repository settings page.
 * @param retentionDays_Untyped Duration after which artifact will expire in days. 0 means using
 * default retention.
 * Minimum 1 day. Maximum 90 days unless changed from the repository settings page.
 * @param compressionLevel The level of compression for Zlib to be applied to the artifact archive.
 * The value can range from 0 to 9: - 0: No compression - 1: Best speed - 6: Default compression (same
 * as GNU Gzip) - 9: Best compression Higher levels will result in better compression, but will take
 * longer to complete. For large files that are not easily compressed, a value of 0 is recommended for
 * significantly faster uploads.
 * @param compressionLevel_Untyped The level of compression for Zlib to be applied to the artifact
 * archive. The value can range from 0 to 9: - 0: No compression - 1: Best speed - 6: Default
 * compression (same as GNU Gzip) - 9: Best compression Higher levels will result in better
 * compression, but will take longer to complete. For large files that are not easily compressed, a
 * value of 0 is recommended for significantly faster uploads.
 * @param overwrite If true, an artifact with a matching name will be deleted before a new one is
 * uploaded. If false, the action will fail if an artifact for the given name already exists. Does not
 * fail if the artifact does not exist.
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
@ExposedCopyVisibility
public data class UploadArtifact private constructor(
    /**
     * Artifact name
     */
    public val name: String? = null,
    /**
     * Artifact name
     */
    public val name_Untyped: String? = null,
    /**
     * &lt;required&gt; A file, directory or wildcard pattern that describes what to upload
     */
    public val path: List<String>? = null,
    /**
     * &lt;required&gt; A file, directory or wildcard pattern that describes what to upload
     */
    public val path_Untyped: String? = null,
    /**
     * The desired behavior if no files are found using the provided path.
     * Available Options:
     *   warn: Output a warning but do not fail the action
     *   error: Fail the action with an error message
     *   ignore: Do not output any warnings or errors, the action does not fail
     */
    public val ifNoFilesFound: UploadArtifact.BehaviorIfNoFilesFound? = null,
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
    public val retentionDays: UploadArtifact.RetentionPeriod? = null,
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
    public val compressionLevel: UploadArtifact.CompressionLevel? = null,
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
    public val overwrite: Boolean? = null,
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
) : RegularAction<UploadArtifact.Outputs>("actions", "upload-artifact", _customVersion ?: "v4") {
    init {
        require(!((name != null) && (name_Untyped != null))) {
            "Only name or name_Untyped must be set, but not both"
        }

        require(!((path != null) && (path_Untyped != null))) {
            "Only path or path_Untyped must be set, but not both"
        }
        require((path != null) || (path_Untyped != null)) {
            "Either path or path_Untyped must be set, one of them is required"
        }

        require(!((ifNoFilesFound != null) && (ifNoFilesFound_Untyped != null))) {
            "Only ifNoFilesFound or ifNoFilesFound_Untyped must be set, but not both"
        }

        require(!((retentionDays != null) && (retentionDays_Untyped != null))) {
            "Only retentionDays or retentionDays_Untyped must be set, but not both"
        }

        require(!((compressionLevel != null) && (compressionLevel_Untyped != null))) {
            "Only compressionLevel or compressionLevel_Untyped must be set, but not both"
        }

        require(!((overwrite != null) && (overwrite_Untyped != null))) {
            "Only overwrite or overwrite_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        name: String? = null,
        name_Untyped: String? = null,
        path: List<String>? = null,
        path_Untyped: String? = null,
        ifNoFilesFound: UploadArtifact.BehaviorIfNoFilesFound? = null,
        ifNoFilesFound_Untyped: String? = null,
        retentionDays: UploadArtifact.RetentionPeriod? = null,
        retentionDays_Untyped: String? = null,
        compressionLevel: UploadArtifact.CompressionLevel? = null,
        compressionLevel_Untyped: String? = null,
        overwrite: Boolean? = null,
        overwrite_Untyped: String? = null,
        includeHiddenFiles_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(name = name, name_Untyped = name_Untyped, path = path, path_Untyped = path_Untyped,
            ifNoFilesFound = ifNoFilesFound, ifNoFilesFound_Untyped = ifNoFilesFound_Untyped,
            retentionDays = retentionDays, retentionDays_Untyped = retentionDays_Untyped,
            compressionLevel = compressionLevel, compressionLevel_Untyped =
            compressionLevel_Untyped, overwrite = overwrite, overwrite_Untyped = overwrite_Untyped,
            includeHiddenFiles_Untyped = includeHiddenFiles_Untyped, _customInputs = _customInputs,
            _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            name?.let { "name" to it },
            name_Untyped?.let { "name" to it },
            path?.let { "path" to it.joinToString("\n") },
            path_Untyped?.let { "path" to it },
            ifNoFilesFound?.let { "if-no-files-found" to it.stringValue },
            ifNoFilesFound_Untyped?.let { "if-no-files-found" to it },
            retentionDays?.let { "retention-days" to it.integerValue.toString() },
            retentionDays_Untyped?.let { "retention-days" to it },
            compressionLevel?.let { "compression-level" to it.integerValue.toString() },
            compressionLevel_Untyped?.let { "compression-level" to it },
            overwrite?.let { "overwrite" to it.toString() },
            overwrite_Untyped?.let { "overwrite" to it },
            includeHiddenFiles_Untyped?.let { "include-hidden-files" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public sealed class BehaviorIfNoFilesFound(
        public val stringValue: String,
    ) {
        public object Warn : UploadArtifact.BehaviorIfNoFilesFound("warn")

        public object Error : UploadArtifact.BehaviorIfNoFilesFound("error")

        public object Ignore : UploadArtifact.BehaviorIfNoFilesFound("ignore")

        public class Custom(
            customStringValue: String,
        ) : UploadArtifact.BehaviorIfNoFilesFound(customStringValue)
    }

    public sealed class RetentionPeriod(
        public val integerValue: Int,
    ) {
        public class Value(
            requestedValue: Int,
        ) : UploadArtifact.RetentionPeriod(requestedValue)

        public object Default : UploadArtifact.RetentionPeriod(0)
    }

    public sealed class CompressionLevel(
        public val integerValue: Int,
    ) {
        public class Value(
            requestedValue: Int,
        ) : UploadArtifact.CompressionLevel(requestedValue)

        public object NoCompression : UploadArtifact.CompressionLevel(0)

        public object BestSpeed : UploadArtifact.CompressionLevel(1)

        public object DefaultCompression : UploadArtifact.CompressionLevel(6)

        public object BestCompression : UploadArtifact.CompressionLevel(9)
    }

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

