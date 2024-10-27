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
import kotlin.ExposedCopyVisibility
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: Add & Commit
 *
 * Automatically commit changes made in your workflow run directly to your repo
 *
 * [Action on GitHub](https://github.com/EndBug/add-and-commit)
 *
 * @param add Arguments for the git add command
 * @param add_Untyped Arguments for the git add command
 * @param authorName The name of the user that will be displayed as the author of the commit
 * @param authorName_Untyped The name of the user that will be displayed as the author of the commit
 * @param authorEmail The email of the user that will be displayed as the author of the commit
 * @param authorEmail_Untyped The email of the user that will be displayed as the author of the
 * commit
 * @param commit Additional arguments for the git commit command
 * @param commit_Untyped Additional arguments for the git commit command
 * @param committerName The name of the custom committer you want to use
 * @param committerName_Untyped The name of the custom committer you want to use
 * @param committerEmail The email of the custom committer you want to use
 * @param committerEmail_Untyped The email of the custom committer you want to use
 * @param cwd The directory where your repository is located. You should use actions/checkout first
 * to set it up
 * @param cwd_Untyped The directory where your repository is located. You should use
 * actions/checkout first to set it up
 * @param defaultAuthor How the action should fill missing author name or email.
 * @param defaultAuthor_Untyped How the action should fill missing author name or email.
 * @param fetch Arguments for the git fetch command (if 'false', the action won't fetch the repo)
 * @param fetch_Untyped Arguments for the git fetch command (if 'false', the action won't fetch the
 * repo)
 * @param message The message for the commit
 * @param message_Untyped The message for the commit
 * @param newBranch The name of the branch to create.
 * @param newBranch_Untyped The name of the branch to create.
 * @param pathspecErrorHandling The way the action should handle pathspec errors from the add and
 * remove commands.
 * @param pathspecErrorHandling_Untyped The way the action should handle pathspec errors from the
 * add and remove commands.
 * @param pull Arguments for the git pull command. By default, the action does not pull.
 * @param pull_Untyped Arguments for the git pull command. By default, the action does not pull.
 * @param push Whether to push the commit and, if any, its tags to the repo. It can also be used to
 * set the git push arguments (more info in the README)
 * @param push_Untyped Whether to push the commit and, if any, its tags to the repo. It can also be
 * used to set the git push arguments (more info in the README)
 * @param remove Arguments for the git rm command
 * @param remove_Untyped Arguments for the git rm command
 * @param tag Arguments for the git tag command (the tag name always needs to be the first word not
 * preceded by a hyphen)
 * @param tag_Untyped Arguments for the git tag command (the tag name always needs to be the first
 * word not preceded by a hyphen)
 * @param tagPush Arguments for the git push --tags command (any additional argument will be added
 * after --tags)
 * @param tagPush_Untyped Arguments for the git push --tags command (any additional argument will be
 * added after --tags)
 * @param githubToken The token used to make requests to the GitHub API. It's NOT used to make
 * commits and should not be changed.
 * @param githubToken_Untyped The token used to make requests to the GitHub API. It's NOT used to
 * make commits and should not be changed.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@ExposedCopyVisibility
public data class AddAndCommit private constructor(
    /**
     * Arguments for the git add command
     */
    public val add: String? = null,
    /**
     * Arguments for the git add command
     */
    public val add_Untyped: String? = null,
    /**
     * The name of the user that will be displayed as the author of the commit
     */
    public val authorName: String? = null,
    /**
     * The name of the user that will be displayed as the author of the commit
     */
    public val authorName_Untyped: String? = null,
    /**
     * The email of the user that will be displayed as the author of the commit
     */
    public val authorEmail: String? = null,
    /**
     * The email of the user that will be displayed as the author of the commit
     */
    public val authorEmail_Untyped: String? = null,
    /**
     * Additional arguments for the git commit command
     */
    public val commit: String? = null,
    /**
     * Additional arguments for the git commit command
     */
    public val commit_Untyped: String? = null,
    /**
     * The name of the custom committer you want to use
     */
    public val committerName: String? = null,
    /**
     * The name of the custom committer you want to use
     */
    public val committerName_Untyped: String? = null,
    /**
     * The email of the custom committer you want to use
     */
    public val committerEmail: String? = null,
    /**
     * The email of the custom committer you want to use
     */
    public val committerEmail_Untyped: String? = null,
    /**
     * The directory where your repository is located. You should use actions/checkout first to set
     * it up
     */
    public val cwd: String? = null,
    /**
     * The directory where your repository is located. You should use actions/checkout first to set
     * it up
     */
    public val cwd_Untyped: String? = null,
    /**
     * How the action should fill missing author name or email.
     */
    public val defaultAuthor: AddAndCommit.DefaultAuthor? = null,
    /**
     * How the action should fill missing author name or email.
     */
    public val defaultAuthor_Untyped: String? = null,
    /**
     * Arguments for the git fetch command (if 'false', the action won't fetch the repo)
     */
    public val fetch: String? = null,
    /**
     * Arguments for the git fetch command (if 'false', the action won't fetch the repo)
     */
    public val fetch_Untyped: String? = null,
    /**
     * The message for the commit
     */
    public val message: String? = null,
    /**
     * The message for the commit
     */
    public val message_Untyped: String? = null,
    /**
     * The name of the branch to create.
     */
    public val newBranch: String? = null,
    /**
     * The name of the branch to create.
     */
    public val newBranch_Untyped: String? = null,
    /**
     * The way the action should handle pathspec errors from the add and remove commands.
     */
    public val pathspecErrorHandling: AddAndCommit.PathspecErrorHandling? = null,
    /**
     * The way the action should handle pathspec errors from the add and remove commands.
     */
    public val pathspecErrorHandling_Untyped: String? = null,
    /**
     * Arguments for the git pull command. By default, the action does not pull.
     */
    public val pull: String? = null,
    /**
     * Arguments for the git pull command. By default, the action does not pull.
     */
    public val pull_Untyped: String? = null,
    /**
     * Whether to push the commit and, if any, its tags to the repo. It can also be used to set the
     * git push arguments (more info in the README)
     */
    public val push: String? = null,
    /**
     * Whether to push the commit and, if any, its tags to the repo. It can also be used to set the
     * git push arguments (more info in the README)
     */
    public val push_Untyped: String? = null,
    /**
     * Arguments for the git rm command
     */
    public val remove: String? = null,
    /**
     * Arguments for the git rm command
     */
    public val remove_Untyped: String? = null,
    /**
     * Arguments for the git tag command (the tag name always needs to be the first word not
     * preceded by a hyphen)
     */
    public val tag: String? = null,
    /**
     * Arguments for the git tag command (the tag name always needs to be the first word not
     * preceded by a hyphen)
     */
    public val tag_Untyped: String? = null,
    /**
     * Arguments for the git push --tags command (any additional argument will be added
     * after --tags)
     */
    public val tagPush: String? = null,
    /**
     * Arguments for the git push --tags command (any additional argument will be added
     * after --tags)
     */
    public val tagPush_Untyped: String? = null,
    /**
     * The token used to make requests to the GitHub API. It's NOT used to make commits and should
     * not be changed.
     */
    public val githubToken: String? = null,
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
) : RegularAction<AddAndCommit.Outputs>("EndBug", "add-and-commit", _customVersion ?: "v9") {
    init {
        require(!((add != null) && (add_Untyped != null))) {
            "Only add or add_Untyped must be set, but not both"
        }

        require(!((authorName != null) && (authorName_Untyped != null))) {
            "Only authorName or authorName_Untyped must be set, but not both"
        }

        require(!((authorEmail != null) && (authorEmail_Untyped != null))) {
            "Only authorEmail or authorEmail_Untyped must be set, but not both"
        }

        require(!((commit != null) && (commit_Untyped != null))) {
            "Only commit or commit_Untyped must be set, but not both"
        }

        require(!((committerName != null) && (committerName_Untyped != null))) {
            "Only committerName or committerName_Untyped must be set, but not both"
        }

        require(!((committerEmail != null) && (committerEmail_Untyped != null))) {
            "Only committerEmail or committerEmail_Untyped must be set, but not both"
        }

        require(!((cwd != null) && (cwd_Untyped != null))) {
            "Only cwd or cwd_Untyped must be set, but not both"
        }

        require(!((defaultAuthor != null) && (defaultAuthor_Untyped != null))) {
            "Only defaultAuthor or defaultAuthor_Untyped must be set, but not both"
        }

        require(!((fetch != null) && (fetch_Untyped != null))) {
            "Only fetch or fetch_Untyped must be set, but not both"
        }

        require(!((message != null) && (message_Untyped != null))) {
            "Only message or message_Untyped must be set, but not both"
        }

        require(!((newBranch != null) && (newBranch_Untyped != null))) {
            "Only newBranch or newBranch_Untyped must be set, but not both"
        }

        require(!((pathspecErrorHandling != null) && (pathspecErrorHandling_Untyped != null))) {
            "Only pathspecErrorHandling or pathspecErrorHandling_Untyped must be set, but not both"
        }

        require(!((pull != null) && (pull_Untyped != null))) {
            "Only pull or pull_Untyped must be set, but not both"
        }

        require(!((push != null) && (push_Untyped != null))) {
            "Only push or push_Untyped must be set, but not both"
        }

        require(!((remove != null) && (remove_Untyped != null))) {
            "Only remove or remove_Untyped must be set, but not both"
        }

        require(!((tag != null) && (tag_Untyped != null))) {
            "Only tag or tag_Untyped must be set, but not both"
        }

        require(!((tagPush != null) && (tagPush_Untyped != null))) {
            "Only tagPush or tagPush_Untyped must be set, but not both"
        }

        require(!((githubToken != null) && (githubToken_Untyped != null))) {
            "Only githubToken or githubToken_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        add: String? = null,
        add_Untyped: String? = null,
        authorName: String? = null,
        authorName_Untyped: String? = null,
        authorEmail: String? = null,
        authorEmail_Untyped: String? = null,
        commit: String? = null,
        commit_Untyped: String? = null,
        committerName: String? = null,
        committerName_Untyped: String? = null,
        committerEmail: String? = null,
        committerEmail_Untyped: String? = null,
        cwd: String? = null,
        cwd_Untyped: String? = null,
        defaultAuthor: AddAndCommit.DefaultAuthor? = null,
        defaultAuthor_Untyped: String? = null,
        fetch: String? = null,
        fetch_Untyped: String? = null,
        message: String? = null,
        message_Untyped: String? = null,
        newBranch: String? = null,
        newBranch_Untyped: String? = null,
        pathspecErrorHandling: AddAndCommit.PathspecErrorHandling? = null,
        pathspecErrorHandling_Untyped: String? = null,
        pull: String? = null,
        pull_Untyped: String? = null,
        push: String? = null,
        push_Untyped: String? = null,
        remove: String? = null,
        remove_Untyped: String? = null,
        tag: String? = null,
        tag_Untyped: String? = null,
        tagPush: String? = null,
        tagPush_Untyped: String? = null,
        githubToken: String? = null,
        githubToken_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(add = add, add_Untyped = add_Untyped, authorName = authorName, authorName_Untyped =
            authorName_Untyped, authorEmail = authorEmail, authorEmail_Untyped =
            authorEmail_Untyped, commit = commit, commit_Untyped = commit_Untyped, committerName =
            committerName, committerName_Untyped = committerName_Untyped, committerEmail =
            committerEmail, committerEmail_Untyped = committerEmail_Untyped, cwd = cwd, cwd_Untyped
            = cwd_Untyped, defaultAuthor = defaultAuthor, defaultAuthor_Untyped =
            defaultAuthor_Untyped, fetch = fetch, fetch_Untyped = fetch_Untyped, message = message,
            message_Untyped = message_Untyped, newBranch = newBranch, newBranch_Untyped =
            newBranch_Untyped, pathspecErrorHandling = pathspecErrorHandling,
            pathspecErrorHandling_Untyped = pathspecErrorHandling_Untyped, pull = pull, pull_Untyped
            = pull_Untyped, push = push, push_Untyped = push_Untyped, remove = remove,
            remove_Untyped = remove_Untyped, tag = tag, tag_Untyped = tag_Untyped, tagPush =
            tagPush, tagPush_Untyped = tagPush_Untyped, githubToken = githubToken,
            githubToken_Untyped = githubToken_Untyped, _customInputs = _customInputs, _customVersion
            = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            add?.let { "add" to it },
            add_Untyped?.let { "add" to it },
            authorName?.let { "author_name" to it },
            authorName_Untyped?.let { "author_name" to it },
            authorEmail?.let { "author_email" to it },
            authorEmail_Untyped?.let { "author_email" to it },
            commit?.let { "commit" to it },
            commit_Untyped?.let { "commit" to it },
            committerName?.let { "committer_name" to it },
            committerName_Untyped?.let { "committer_name" to it },
            committerEmail?.let { "committer_email" to it },
            committerEmail_Untyped?.let { "committer_email" to it },
            cwd?.let { "cwd" to it },
            cwd_Untyped?.let { "cwd" to it },
            defaultAuthor?.let { "default_author" to it.stringValue },
            defaultAuthor_Untyped?.let { "default_author" to it },
            fetch?.let { "fetch" to it },
            fetch_Untyped?.let { "fetch" to it },
            message?.let { "message" to it },
            message_Untyped?.let { "message" to it },
            newBranch?.let { "new_branch" to it },
            newBranch_Untyped?.let { "new_branch" to it },
            pathspecErrorHandling?.let { "pathspec_error_handling" to it.stringValue },
            pathspecErrorHandling_Untyped?.let { "pathspec_error_handling" to it },
            pull?.let { "pull" to it },
            pull_Untyped?.let { "pull" to it },
            push?.let { "push" to it },
            push_Untyped?.let { "push" to it },
            remove?.let { "remove" to it },
            remove_Untyped?.let { "remove" to it },
            tag?.let { "tag" to it },
            tag_Untyped?.let { "tag" to it },
            tagPush?.let { "tag_push" to it },
            tagPush_Untyped?.let { "tag_push" to it },
            githubToken?.let { "github_token" to it },
            githubToken_Untyped?.let { "github_token" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public sealed class DefaultAuthor(
        public val stringValue: String,
    ) {
        public object GithubActor : AddAndCommit.DefaultAuthor("github_actor")

        public object UserInfo : AddAndCommit.DefaultAuthor("user_info")

        public object GithubActions : AddAndCommit.DefaultAuthor("github_actions")

        public class Custom(
            customStringValue: String,
        ) : AddAndCommit.DefaultAuthor(customStringValue)
    }

    public sealed class PathspecErrorHandling(
        public val stringValue: String,
    ) {
        public object Ignore : AddAndCommit.PathspecErrorHandling("ignore")

        public object ExitImmediately : AddAndCommit.PathspecErrorHandling("exitImmediately")

        public object ExitAtEnd : AddAndCommit.PathspecErrorHandling("exitAtEnd")

        public class Custom(
            customStringValue: String,
        ) : AddAndCommit.PathspecErrorHandling(customStringValue)
    }

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

