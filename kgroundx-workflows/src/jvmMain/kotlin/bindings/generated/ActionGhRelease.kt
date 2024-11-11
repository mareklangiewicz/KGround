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
import kotlin.Boolean
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: GH Release
 *
 * Github Action for creating Github Releases
 *
 * [Action on GitHub](https://github.com/softprops/action-gh-release)
 *
 * @param body Note-worthy description of changes in release
 * @param body_Untyped Note-worthy description of changes in release
 * @param bodyPath Path to load note-worthy description of changes in release from
 * @param bodyPath_Untyped Path to load note-worthy description of changes in release from
 * @param name Gives the release a custom name. Defaults to tag name
 * @param name_Untyped Gives the release a custom name. Defaults to tag name
 * @param tagName Gives a tag name. Defaults to github.GITHUB_REF
 * @param tagName_Untyped Gives a tag name. Defaults to github.GITHUB_REF
 * @param draft Creates a draft release. Defaults to false
 * @param draft_Untyped Creates a draft release. Defaults to false
 * @param prerelease Identify the release as a prerelease. Defaults to false
 * @param prerelease_Untyped Identify the release as a prerelease. Defaults to false
 * @param files Newline-delimited list of path globs for asset files to upload
 * @param files_Untyped Newline-delimited list of path globs for asset files to upload
 * @param failOnUnmatchedFiles Fails if any of the `files` globs match nothing. Defaults to false
 * @param failOnUnmatchedFiles_Untyped Fails if any of the `files` globs match nothing. Defaults to
 * false
 * @param repository Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt; format
 * @param repository_Untyped Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt;
 * format
 * @param token Authorized secret GitHub Personal Access Token. Defaults to github.token
 * @param token_Untyped Authorized secret GitHub Personal Access Token. Defaults to github.token
 * @param targetCommitish Commitish value that determines where the Git tag is created from. Can be
 * any branch or commit SHA.
 * @param targetCommitish_Untyped Commitish value that determines where the Git tag is created from.
 * Can be any branch or commit SHA.
 * @param discussionCategoryName If specified, a discussion of the specified category is created and
 * linked to the release. The value must be a category that already exists in the repository. If there
 * is already a discussion linked to the release, this parameter is ignored.
 * @param discussionCategoryName_Untyped If specified, a discussion of the specified category is
 * created and linked to the release. The value must be a category that already exists in the
 * repository. If there is already a discussion linked to the release, this parameter is ignored.
 * @param generateReleaseNotes Whether to automatically generate the name and body for this release.
 * If name is specified, the specified name will be used; otherwise, a name will be automatically
 * generated. If body is specified, the body will be pre-pended to the automatically generated notes.
 * @param generateReleaseNotes_Untyped Whether to automatically generate the name and body for this
 * release. If name is specified, the specified name will be used; otherwise, a name will be
 * automatically generated. If body is specified, the body will be pre-pended to the automatically
 * generated notes.
 * @param appendBody Append to existing body instead of overwriting it. Default is false.
 * @param appendBody_Untyped Append to existing body instead of overwriting it. Default is false.
 * @param makeLatest Specifies whether this release should be set as the latest release for the
 * repository. Drafts and prereleases cannot be set as latest. Can be `true`, `false`, or `legacy`.
 * Uses GitHub api default if not provided
 * @param makeLatest_Untyped Specifies whether this release should be set as the latest release for
 * the repository. Drafts and prereleases cannot be set as latest. Can be `true`, `false`, or `legacy`.
 * Uses GitHub api default if not provided
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@ExposedCopyVisibility
public data class ActionGhRelease private constructor(
    /**
     * Note-worthy description of changes in release
     */
    public val body: String? = null,
    /**
     * Note-worthy description of changes in release
     */
    public val body_Untyped: String? = null,
    /**
     * Path to load note-worthy description of changes in release from
     */
    public val bodyPath: String? = null,
    /**
     * Path to load note-worthy description of changes in release from
     */
    public val bodyPath_Untyped: String? = null,
    /**
     * Gives the release a custom name. Defaults to tag name
     */
    public val name: String? = null,
    /**
     * Gives the release a custom name. Defaults to tag name
     */
    public val name_Untyped: String? = null,
    /**
     * Gives a tag name. Defaults to github.GITHUB_REF
     */
    public val tagName: String? = null,
    /**
     * Gives a tag name. Defaults to github.GITHUB_REF
     */
    public val tagName_Untyped: String? = null,
    /**
     * Creates a draft release. Defaults to false
     */
    public val draft: Boolean? = null,
    /**
     * Creates a draft release. Defaults to false
     */
    public val draft_Untyped: String? = null,
    /**
     * Identify the release as a prerelease. Defaults to false
     */
    public val prerelease: Boolean? = null,
    /**
     * Identify the release as a prerelease. Defaults to false
     */
    public val prerelease_Untyped: String? = null,
    /**
     * Newline-delimited list of path globs for asset files to upload
     */
    public val files: List<String>? = null,
    /**
     * Newline-delimited list of path globs for asset files to upload
     */
    public val files_Untyped: String? = null,
    /**
     * Fails if any of the `files` globs match nothing. Defaults to false
     */
    public val failOnUnmatchedFiles: Boolean? = null,
    /**
     * Fails if any of the `files` globs match nothing. Defaults to false
     */
    public val failOnUnmatchedFiles_Untyped: String? = null,
    /**
     * Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt; format
     */
    public val repository: String? = null,
    /**
     * Repository to make releases against, in &lt;owner&gt;/&lt;repo&gt; format
     */
    public val repository_Untyped: String? = null,
    /**
     * Authorized secret GitHub Personal Access Token. Defaults to github.token
     */
    public val token: String? = null,
    /**
     * Authorized secret GitHub Personal Access Token. Defaults to github.token
     */
    public val token_Untyped: String? = null,
    /**
     * Commitish value that determines where the Git tag is created from. Can be any branch or
     * commit SHA.
     */
    public val targetCommitish: String? = null,
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
    public val discussionCategoryName: String? = null,
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
    public val generateReleaseNotes: Boolean? = null,
    /**
     * Whether to automatically generate the name and body for this release. If name is specified,
     * the specified name will be used; otherwise, a name will be automatically generated. If body is
     * specified, the body will be pre-pended to the automatically generated notes.
     */
    public val generateReleaseNotes_Untyped: String? = null,
    /**
     * Append to existing body instead of overwriting it. Default is false.
     */
    public val appendBody: Boolean? = null,
    /**
     * Append to existing body instead of overwriting it. Default is false.
     */
    public val appendBody_Untyped: String? = null,
    /**
     * Specifies whether this release should be set as the latest release for the repository. Drafts
     * and prereleases cannot be set as latest. Can be `true`, `false`, or `legacy`. Uses GitHub api
     * default if not provided
     */
    public val makeLatest: ActionGhRelease.MakeLatest? = null,
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
) : RegularAction<ActionGhRelease.Outputs>("softprops", "action-gh-release", _customVersion ?: "v2")
        {
    init {
        require(!((body != null) && (body_Untyped != null))) {
            "Only body or body_Untyped must be set, but not both"
        }

        require(!((bodyPath != null) && (bodyPath_Untyped != null))) {
            "Only bodyPath or bodyPath_Untyped must be set, but not both"
        }

        require(!((name != null) && (name_Untyped != null))) {
            "Only name or name_Untyped must be set, but not both"
        }

        require(!((tagName != null) && (tagName_Untyped != null))) {
            "Only tagName or tagName_Untyped must be set, but not both"
        }

        require(!((draft != null) && (draft_Untyped != null))) {
            "Only draft or draft_Untyped must be set, but not both"
        }

        require(!((prerelease != null) && (prerelease_Untyped != null))) {
            "Only prerelease or prerelease_Untyped must be set, but not both"
        }

        require(!((files != null) && (files_Untyped != null))) {
            "Only files or files_Untyped must be set, but not both"
        }

        require(!((failOnUnmatchedFiles != null) && (failOnUnmatchedFiles_Untyped != null))) {
            "Only failOnUnmatchedFiles or failOnUnmatchedFiles_Untyped must be set, but not both"
        }

        require(!((repository != null) && (repository_Untyped != null))) {
            "Only repository or repository_Untyped must be set, but not both"
        }

        require(!((token != null) && (token_Untyped != null))) {
            "Only token or token_Untyped must be set, but not both"
        }

        require(!((targetCommitish != null) && (targetCommitish_Untyped != null))) {
            "Only targetCommitish or targetCommitish_Untyped must be set, but not both"
        }

        require(!((discussionCategoryName != null) && (discussionCategoryName_Untyped != null))) {
           
                "Only discussionCategoryName or discussionCategoryName_Untyped must be set, but not both"
        }

        require(!((generateReleaseNotes != null) && (generateReleaseNotes_Untyped != null))) {
            "Only generateReleaseNotes or generateReleaseNotes_Untyped must be set, but not both"
        }

        require(!((appendBody != null) && (appendBody_Untyped != null))) {
            "Only appendBody or appendBody_Untyped must be set, but not both"
        }

        require(!((makeLatest != null) && (makeLatest_Untyped != null))) {
            "Only makeLatest or makeLatest_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        body: String? = null,
        body_Untyped: String? = null,
        bodyPath: String? = null,
        bodyPath_Untyped: String? = null,
        name: String? = null,
        name_Untyped: String? = null,
        tagName: String? = null,
        tagName_Untyped: String? = null,
        draft: Boolean? = null,
        draft_Untyped: String? = null,
        prerelease: Boolean? = null,
        prerelease_Untyped: String? = null,
        files: List<String>? = null,
        files_Untyped: String? = null,
        failOnUnmatchedFiles: Boolean? = null,
        failOnUnmatchedFiles_Untyped: String? = null,
        repository: String? = null,
        repository_Untyped: String? = null,
        token: String? = null,
        token_Untyped: String? = null,
        targetCommitish: String? = null,
        targetCommitish_Untyped: String? = null,
        discussionCategoryName: String? = null,
        discussionCategoryName_Untyped: String? = null,
        generateReleaseNotes: Boolean? = null,
        generateReleaseNotes_Untyped: String? = null,
        appendBody: Boolean? = null,
        appendBody_Untyped: String? = null,
        makeLatest: ActionGhRelease.MakeLatest? = null,
        makeLatest_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(body = body, body_Untyped = body_Untyped, bodyPath = bodyPath, bodyPath_Untyped =
            bodyPath_Untyped, name = name, name_Untyped = name_Untyped, tagName = tagName,
            tagName_Untyped = tagName_Untyped, draft = draft, draft_Untyped = draft_Untyped,
            prerelease = prerelease, prerelease_Untyped = prerelease_Untyped, files = files,
            files_Untyped = files_Untyped, failOnUnmatchedFiles = failOnUnmatchedFiles,
            failOnUnmatchedFiles_Untyped = failOnUnmatchedFiles_Untyped, repository = repository,
            repository_Untyped = repository_Untyped, token = token, token_Untyped = token_Untyped,
            targetCommitish = targetCommitish, targetCommitish_Untyped = targetCommitish_Untyped,
            discussionCategoryName = discussionCategoryName, discussionCategoryName_Untyped =
            discussionCategoryName_Untyped, generateReleaseNotes = generateReleaseNotes,
            generateReleaseNotes_Untyped = generateReleaseNotes_Untyped, appendBody = appendBody,
            appendBody_Untyped = appendBody_Untyped, makeLatest = makeLatest, makeLatest_Untyped =
            makeLatest_Untyped, _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            body?.let { "body" to it },
            body_Untyped?.let { "body" to it },
            bodyPath?.let { "body_path" to it },
            bodyPath_Untyped?.let { "body_path" to it },
            name?.let { "name" to it },
            name_Untyped?.let { "name" to it },
            tagName?.let { "tag_name" to it },
            tagName_Untyped?.let { "tag_name" to it },
            draft?.let { "draft" to it.toString() },
            draft_Untyped?.let { "draft" to it },
            prerelease?.let { "prerelease" to it.toString() },
            prerelease_Untyped?.let { "prerelease" to it },
            files?.let { "files" to it.joinToString("\n") },
            files_Untyped?.let { "files" to it },
            failOnUnmatchedFiles?.let { "fail_on_unmatched_files" to it.toString() },
            failOnUnmatchedFiles_Untyped?.let { "fail_on_unmatched_files" to it },
            repository?.let { "repository" to it },
            repository_Untyped?.let { "repository" to it },
            token?.let { "token" to it },
            token_Untyped?.let { "token" to it },
            targetCommitish?.let { "target_commitish" to it },
            targetCommitish_Untyped?.let { "target_commitish" to it },
            discussionCategoryName?.let { "discussion_category_name" to it },
            discussionCategoryName_Untyped?.let { "discussion_category_name" to it },
            generateReleaseNotes?.let { "generate_release_notes" to it.toString() },
            generateReleaseNotes_Untyped?.let { "generate_release_notes" to it },
            appendBody?.let { "append_body" to it.toString() },
            appendBody_Untyped?.let { "append_body" to it },
            makeLatest?.let { "make_latest" to it.stringValue },
            makeLatest_Untyped?.let { "make_latest" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public sealed class MakeLatest(
        public val stringValue: String,
    ) {
        public object True : ActionGhRelease.MakeLatest("true")

        public object False : ActionGhRelease.MakeLatest("false")

        public object Legacy : ActionGhRelease.MakeLatest("legacy")

        public class Custom(
            customStringValue: String,
        ) : ActionGhRelease.MakeLatest(customStringValue)
    }

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

