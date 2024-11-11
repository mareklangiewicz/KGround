// This file was generated using action-binding-generator. Don't change it by hand, otherwise your
// changes will be overwritten with the next binding code regeneration.
// See https://github.com/typesafegithub/github-workflows-kt for more info.
@file:Suppress(
    "DataClassPrivateConstructor",
    "UNUSED_PARAMETER",
)

package io.github.typesafegithub.workflows.actions.endbug

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
 * Action: Add & Commit
 *
 * Automatically commit changes made in your workflow run directly to your repo
 *
 * [Action on GitHub](https://github.com/EndBug/add-and-commit)
 *
 * @param add_Untyped Arguments for the git add command
 * @param authorName_Untyped The name of the user that will be displayed as the author of the commit
 * @param authorEmail_Untyped The email of the user that will be displayed as the author of the
 * commit
 * @param commit_Untyped Additional arguments for the git commit command
 * @param committerName_Untyped The name of the custom committer you want to use
 * @param committerEmail_Untyped The email of the custom committer you want to use
 * @param cwd_Untyped The directory where your repository is located. You should use
 * actions/checkout first to set it up
 * @param defaultAuthor_Untyped How the action should fill missing author name or email.
 * @param fetch_Untyped Arguments for the git fetch command (if 'false', the action won't fetch the
 * repo)
 * @param message_Untyped The message for the commit
 * @param newBranch_Untyped The name of the branch to create.
 * @param pathspecErrorHandling_Untyped The way the action should handle pathspec errors from the
 * add and remove commands.
 * @param pull_Untyped Arguments for the git pull command. By default, the action does not pull.
 * @param push_Untyped Whether to push the commit and, if any, its tags to the repo. It can also be
 * used to set the git push arguments (more info in the README)
 * @param remove_Untyped Arguments for the git rm command
 * @param tag_Untyped Arguments for the git tag command (the tag name always needs to be the first
 * word not preceded by a hyphen)
 * @param tagPush_Untyped Arguments for the git push --tags command (any additional argument will be
 * added after --tags)
 * @param githubToken_Untyped The token used to make requests to the GitHub API. It's NOT used to
 * make commits and should not be changed.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("AddAndCommit"),
)
@ExposedCopyVisibility
public data class AddAndCommit_Untyped private constructor(
    /**
     * Arguments for the git add command
     */
    public val add_Untyped: String? = null,
    /**
     * The name of the user that will be displayed as the author of the commit
     */
    public val authorName_Untyped: String? = null,
    /**
     * The email of the user that will be displayed as the author of the commit
     */
    public val authorEmail_Untyped: String? = null,
    /**
     * Additional arguments for the git commit command
     */
    public val commit_Untyped: String? = null,
    /**
     * The name of the custom committer you want to use
     */
    public val committerName_Untyped: String? = null,
    /**
     * The email of the custom committer you want to use
     */
    public val committerEmail_Untyped: String? = null,
    /**
     * The directory where your repository is located. You should use actions/checkout first to set
     * it up
     */
    public val cwd_Untyped: String? = null,
    /**
     * How the action should fill missing author name or email.
     */
    public val defaultAuthor_Untyped: String? = null,
    /**
     * Arguments for the git fetch command (if 'false', the action won't fetch the repo)
     */
    public val fetch_Untyped: String? = null,
    /**
     * The message for the commit
     */
    public val message_Untyped: String? = null,
    /**
     * The name of the branch to create.
     */
    public val newBranch_Untyped: String? = null,
    /**
     * The way the action should handle pathspec errors from the add and remove commands.
     */
    public val pathspecErrorHandling_Untyped: String? = null,
    /**
     * Arguments for the git pull command. By default, the action does not pull.
     */
    public val pull_Untyped: String? = null,
    /**
     * Whether to push the commit and, if any, its tags to the repo. It can also be used to set the
     * git push arguments (more info in the README)
     */
    public val push_Untyped: String? = null,
    /**
     * Arguments for the git rm command
     */
    public val remove_Untyped: String? = null,
    /**
     * Arguments for the git tag command (the tag name always needs to be the first word not
     * preceded by a hyphen)
     */
    public val tag_Untyped: String? = null,
    /**
     * Arguments for the git push --tags command (any additional argument will be added
     * after --tags)
     */
    public val tagPush_Untyped: String? = null,
    /**
     * The token used to make requests to the GitHub API. It's NOT used to make commits and should
     * not be changed.
     */
    public val githubToken_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<AddAndCommit_Untyped.Outputs>("EndBug", "add-and-commit", _customVersion ?: "v9")
        {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        add_Untyped: String? = null,
        authorName_Untyped: String? = null,
        authorEmail_Untyped: String? = null,
        commit_Untyped: String? = null,
        committerName_Untyped: String? = null,
        committerEmail_Untyped: String? = null,
        cwd_Untyped: String? = null,
        defaultAuthor_Untyped: String? = null,
        fetch_Untyped: String? = null,
        message_Untyped: String? = null,
        newBranch_Untyped: String? = null,
        pathspecErrorHandling_Untyped: String? = null,
        pull_Untyped: String? = null,
        push_Untyped: String? = null,
        remove_Untyped: String? = null,
        tag_Untyped: String? = null,
        tagPush_Untyped: String? = null,
        githubToken_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(add_Untyped = add_Untyped, authorName_Untyped = authorName_Untyped, authorEmail_Untyped
            = authorEmail_Untyped, commit_Untyped = commit_Untyped, committerName_Untyped =
            committerName_Untyped, committerEmail_Untyped = committerEmail_Untyped, cwd_Untyped =
            cwd_Untyped, defaultAuthor_Untyped = defaultAuthor_Untyped, fetch_Untyped =
            fetch_Untyped, message_Untyped = message_Untyped, newBranch_Untyped = newBranch_Untyped,
            pathspecErrorHandling_Untyped = pathspecErrorHandling_Untyped, pull_Untyped =
            pull_Untyped, push_Untyped = push_Untyped, remove_Untyped = remove_Untyped, tag_Untyped
            = tag_Untyped, tagPush_Untyped = tagPush_Untyped, githubToken_Untyped =
            githubToken_Untyped, _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            add_Untyped?.let { "add" to it },
            authorName_Untyped?.let { "author_name" to it },
            authorEmail_Untyped?.let { "author_email" to it },
            commit_Untyped?.let { "commit" to it },
            committerName_Untyped?.let { "committer_name" to it },
            committerEmail_Untyped?.let { "committer_email" to it },
            cwd_Untyped?.let { "cwd" to it },
            defaultAuthor_Untyped?.let { "default_author" to it },
            fetch_Untyped?.let { "fetch" to it },
            message_Untyped?.let { "message" to it },
            newBranch_Untyped?.let { "new_branch" to it },
            pathspecErrorHandling_Untyped?.let { "pathspec_error_handling" to it },
            pull_Untyped?.let { "pull" to it },
            push_Untyped?.let { "push" to it },
            remove_Untyped?.let { "remove" to it },
            tag_Untyped?.let { "tag" to it },
            tagPush_Untyped?.let { "tag_push" to it },
            githubToken_Untyped?.let { "github_token" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * Whether the action has created a commit.
         */
        public val committed: String = "steps.$stepId.outputs.committed"

        /**
         * The complete SHA of the commit that has been created.
         */
        public val commitLongSha: String = "steps.$stepId.outputs.commit_long_sha"

        /**
         * The short SHA of the commit that has been created.
         */
        public val commitSha: String = "steps.$stepId.outputs.commit_sha"

        /**
         * Whether the action has pushed to the remote.
         */
        public val pushed: String = "steps.$stepId.outputs.pushed"

        /**
         * Whether the action has created a tag.
         */
        public val tagged: String = "steps.$stepId.outputs.tagged"

        /**
         * Whether the action has pushed a tag.
         */
        public val tagPushed: String = "steps.$stepId.outputs.tag_pushed"
    }
}
