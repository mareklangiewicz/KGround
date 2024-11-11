// This file was generated using action-binding-generator. Don't change it by hand, otherwise your
// changes will be overwritten with the next binding code regeneration.
// See https://github.com/typesafegithub/github-workflows-kt for more info.
@file:Suppress(
    "DataClassPrivateConstructor",
    "UNUSED_PARAMETER",
)

package io.github.typesafegithub.workflows.actions.softprops

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
 * Action: GH Release
 *
 * Github Action for creating Github Releases
 *
 * [Action on GitHub](https://github.com/softprops/action-gh-release)
 *
 * @param body_Untyped Note-worthy description of changes in release
 * @param bodyPath_Untyped Path to load note-worthy description of changes in release from
 * @param name_Untyped Gives the release a custom name. Defaults to tag name
 * @param tagName_Untyped Gives a tag name. Defaults to github.GITHUB_REF
 * @param draft_Untyped Creates a draft release. Defaults to false
 * @param prerelease_Untyped Identify the release as a prerelease. Defaults to false
 * @param files_Untyped Newline-delimited list of path globs for asset files to upload
 * @param failOnUnmatchedFiles_Untyped Fails if any of the `files` globs match nothing. Defaults to
 * false
 * @param repository_Untyped Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt;
 * format
 * @param token_Untyped Authorized secret GitHub Personal Access Token. Defaults to github.token
 * @param targetCommitish_Untyped Commitish value that determines where the Git tag is created from.
 * Can be any branch or commit SHA.
 * @param discussionCategoryName_Untyped If specified, a discussion of the specified category is
 * created and linked to the release. The value must be a category that already exists in the
 * repository. If there is already a discussion linked to the release, this parameter is ignored.
 * @param generateReleaseNotes_Untyped Whether to automatically generate the name and body for this
 * release. If name is specified, the specified name will be used; otherwise, a name will be
 * automatically generated. If body is specified, the body will be pre-pended to the automatically
 * generated notes.
 * @param appendBody_Untyped Append to existing body instead of overwriting it. Default is false.
 * @param makeLatest_Untyped Specifies whether this release should be set as the latest release for
 * the repository. Drafts and prereleases cannot be set as latest. Can be `true`, `false`, or `legacy`.
 * Uses GitHub api default if not provided
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("ActionGhRelease"),
)
@ExposedCopyVisibility
public data class ActionGhRelease_Untyped private constructor(
    /**
     * Note-worthy description of changes in release
     */
    public val body_Untyped: String? = null,
    /**
     * Path to load note-worthy description of changes in release from
     */
    public val bodyPath_Untyped: String? = null,
    /**
     * Gives the release a custom name. Defaults to tag name
     */
    public val name_Untyped: String? = null,
    /**
     * Gives a tag name. Defaults to github.GITHUB_REF
     */
    public val tagName_Untyped: String? = null,
    /**
     * Creates a draft release. Defaults to false
     */
    public val draft_Untyped: String? = null,
    /**
     * Identify the release as a prerelease. Defaults to false
     */
    public val prerelease_Untyped: String? = null,
    /**
     * Newline-delimited list of path globs for asset files to upload
     */
    public val files_Untyped: String? = null,
    /**
     * Fails if any of the `files` globs match nothing. Defaults to false
     */
    public val failOnUnmatchedFiles_Untyped: String? = null,
    /**
     * Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt; format
     */
    public val repository_Untyped: String? = null,
    /**
     * Authorized secret GitHub Personal Access Token. Defaults to github.token
     */
    public val token_Untyped: String? = null,
    /**
     * Commitish value that determines where the Git tag is created from. Can be any branch or
     * commit SHA.
     */
    public val targetCommitish_Untyped: String? = null,
    /**
     * If specified, a discussion of the specified category is created and linked to the release.
     * The value must be a category that already exists in the repository. If there is already a
     * discussion linked to the release, this parameter is ignored.
     */
    public val discussionCategoryName_Untyped: String? = null,
    /**
     * Whether to automatically generate the name and body for this release. If name is specified,
     * the specified name will be used; otherwise, a name will be automatically generated. If body is
     * specified, the body will be pre-pended to the automatically generated notes.
     */
    public val generateReleaseNotes_Untyped: String? = null,
    /**
     * Append to existing body instead of overwriting it. Default is false.
     */
    public val appendBody_Untyped: String? = null,
    /**
     * Specifies whether this release should be set as the latest release for the repository. Drafts
     * and prereleases cannot be set as latest. Can be `true`, `false`, or `legacy`. Uses GitHub api
     * default if not provided
     */
    public val makeLatest_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<ActionGhRelease_Untyped.Outputs>("softprops", "action-gh-release", _customVersion
        ?: "v2") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        body_Untyped: String? = null,
        bodyPath_Untyped: String? = null,
        name_Untyped: String? = null,
        tagName_Untyped: String? = null,
        draft_Untyped: String? = null,
        prerelease_Untyped: String? = null,
        files_Untyped: String? = null,
        failOnUnmatchedFiles_Untyped: String? = null,
        repository_Untyped: String? = null,
        token_Untyped: String? = null,
        targetCommitish_Untyped: String? = null,
        discussionCategoryName_Untyped: String? = null,
        generateReleaseNotes_Untyped: String? = null,
        appendBody_Untyped: String? = null,
        makeLatest_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(body_Untyped = body_Untyped, bodyPath_Untyped = bodyPath_Untyped, name_Untyped =
            name_Untyped, tagName_Untyped = tagName_Untyped, draft_Untyped = draft_Untyped,
            prerelease_Untyped = prerelease_Untyped, files_Untyped = files_Untyped,
            failOnUnmatchedFiles_Untyped = failOnUnmatchedFiles_Untyped, repository_Untyped =
            repository_Untyped, token_Untyped = token_Untyped, targetCommitish_Untyped =
            targetCommitish_Untyped, discussionCategoryName_Untyped =
            discussionCategoryName_Untyped, generateReleaseNotes_Untyped =
            generateReleaseNotes_Untyped, appendBody_Untyped = appendBody_Untyped,
            makeLatest_Untyped = makeLatest_Untyped, _customInputs = _customInputs, _customVersion =
            _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            body_Untyped?.let { "body" to it },
            bodyPath_Untyped?.let { "body_path" to it },
            name_Untyped?.let { "name" to it },
            tagName_Untyped?.let { "tag_name" to it },
            draft_Untyped?.let { "draft" to it },
            prerelease_Untyped?.let { "prerelease" to it },
            files_Untyped?.let { "files" to it },
            failOnUnmatchedFiles_Untyped?.let { "fail_on_unmatched_files" to it },
            repository_Untyped?.let { "repository" to it },
            token_Untyped?.let { "token" to it },
            targetCommitish_Untyped?.let { "target_commitish" to it },
            discussionCategoryName_Untyped?.let { "discussion_category_name" to it },
            generateReleaseNotes_Untyped?.let { "generate_release_notes" to it },
            appendBody_Untyped?.let { "append_body" to it },
            makeLatest_Untyped?.let { "make_latest" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * URL to the Release HTML Page
         */
        public val url: String = "steps.$stepId.outputs.url"

        /**
         * Release ID
         */
        public val id: String = "steps.$stepId.outputs.id"

        /**
         * URL for uploading assets to the release
         */
        public val uploadUrl: String = "steps.$stepId.outputs.upload_url"

        /**
         * JSON array containing information about each uploaded asset, in the format given
         * [here](https://docs.github.com/en/rest/reference/repos#upload-a-release-asset--code-samples)
         * (minus the `uploader` field)
         */
        public val assets: String = "steps.$stepId.outputs.assets"
    }
}

