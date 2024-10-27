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
 * Action: Checkout
 *
 * Checkout a Git repository at a particular version
 *
 * [Action on GitHub](https://github.com/actions/checkout)
 *
 * @param repository_Untyped Repository name with owner. For example, actions/checkout
 * @param ref_Untyped The branch, tag or SHA to checkout. When checking out the repository that
 * triggered a workflow, this defaults to the reference or SHA for that event.  Otherwise, uses the
 * default branch.
 * @param token_Untyped Personal access token (PAT) used to fetch the repository. The PAT is
 * configured with the local git config, which enables your scripts to run authenticated git commands.
 * The post-job step removes the PAT.
 *
 * We recommend using a service account with the least permissions necessary. Also when generating a
 * new PAT, select the least scopes necessary.
 *
 * [Learn more about creating and using encrypted
 * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
 * @param sshKey_Untyped SSH key used to fetch the repository. The SSH key is configured with the
 * local git config, which enables your scripts to run authenticated git commands. The post-job step
 * removes the SSH key.
 *
 * We recommend using a service account with the least permissions necessary.
 *
 * [Learn more about creating and using encrypted
 * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
 * @param sshKnownHosts_Untyped Known hosts in addition to the user and global host key database.
 * The public SSH keys for a host may be obtained using the utility `ssh-keyscan`. For example,
 * `ssh-keyscan github.com`. The public key for github.com is always implicitly added.
 * @param sshStrict_Untyped Whether to perform strict host key checking. When true, adds the options
 * `StrictHostKeyChecking=yes` and `CheckHostIP=no` to the SSH command line. Use the input
 * `ssh-known-hosts` to configure additional hosts.
 * @param sshUser_Untyped The user to use when connecting to the remote SSH host. By default 'git'
 * is used.
 * @param persistCredentials_Untyped Whether to configure the token or SSH key with the local git
 * config
 * @param path_Untyped Relative path under $GITHUB_WORKSPACE to place the repository
 * @param clean_Untyped Whether to execute `git clean -ffdx && git reset --hard HEAD` before
 * fetching
 * @param filter_Untyped Partially clone against a given filter. Overrides sparse-checkout if set.
 * @param sparseCheckout_Untyped Do a sparse checkout on given patterns. Each pattern should be
 * separated with new lines.
 * @param sparseCheckoutConeMode_Untyped Specifies whether to use cone-mode when doing a sparse
 * checkout.
 * @param fetchDepth_Untyped Number of commits to fetch. 0 indicates all history for all branches
 * and tags.
 * @param fetchTags_Untyped Whether to fetch tags, even if fetch-depth &gt; 0.
 * @param showProgress_Untyped Whether to show progress status output when fetching.
 * @param lfs_Untyped Whether to download Git-LFS files
 * @param submodules_Untyped Whether to checkout submodules: `true` to checkout submodules or
 * `recursive` to recursively checkout submodules.
 *
 * When the `ssh-key` input is not provided, SSH URLs beginning with `git@github.com:` are converted
 * to HTTPS.
 * @param setSafeDirectory_Untyped Add repository path as safe.directory for Git global config by
 * running `git config --global --add safe.directory &lt;path&gt;`
 * @param githubServerUrl_Untyped The base URL for the GitHub instance that you are trying to clone
 * from, will use environment defaults to fetch from the same instance that the workflow is running
 * from unless specified. Example URLs are https://github.com or https://my-ghes-server.example.com
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("Checkout"),
)
@ExposedCopyVisibility
public data class Checkout_Untyped private constructor(
    /**
     * Repository name with owner. For example, actions/checkout
     */
    public val repository_Untyped: String? = null,
    /**
     * The branch, tag or SHA to checkout. When checking out the repository that triggered a
     * workflow, this defaults to the reference or SHA for that event.  Otherwise, uses the default
     * branch.
     */
    public val ref_Untyped: String? = null,
    /**
     * Personal access token (PAT) used to fetch the repository. The PAT is configured with the
     * local git config, which enables your scripts to run authenticated git commands. The post-job
     * step removes the PAT.
     *
     * We recommend using a service account with the least permissions necessary. Also when
     * generating a new PAT, select the least scopes necessary.
     *
     * [Learn more about creating and using encrypted
     * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
     */
    public val token_Untyped: String? = null,
    /**
     * SSH key used to fetch the repository. The SSH key is configured with the local git config,
     * which enables your scripts to run authenticated git commands. The post-job step removes the SSH
     * key.
     *
     * We recommend using a service account with the least permissions necessary.
     *
     * [Learn more about creating and using encrypted
     * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
     */
    public val sshKey_Untyped: String? = null,
    /**
     * Known hosts in addition to the user and global host key database. The public SSH keys for a
     * host may be obtained using the utility `ssh-keyscan`. For example, `ssh-keyscan github.com`. The
     * public key for github.com is always implicitly added.
     */
    public val sshKnownHosts_Untyped: String? = null,
    /**
     * Whether to perform strict host key checking. When true, adds the options
     * `StrictHostKeyChecking=yes` and `CheckHostIP=no` to the SSH command line. Use the input
     * `ssh-known-hosts` to configure additional hosts.
     */
    public val sshStrict_Untyped: String? = null,
    /**
     * The user to use when connecting to the remote SSH host. By default 'git' is used.
     */
    public val sshUser_Untyped: String? = null,
    /**
     * Whether to configure the token or SSH key with the local git config
     */
    public val persistCredentials_Untyped: String? = null,
    /**
     * Relative path under $GITHUB_WORKSPACE to place the repository
     */
    public val path_Untyped: String? = null,
    /**
     * Whether to execute `git clean -ffdx && git reset --hard HEAD` before fetching
     */
    public val clean_Untyped: String? = null,
    /**
     * Partially clone against a given filter. Overrides sparse-checkout if set.
     */
    public val filter_Untyped: String? = null,
    /**
     * Do a sparse checkout on given patterns. Each pattern should be separated with new lines.
     */
    public val sparseCheckout_Untyped: String? = null,
    /**
     * Specifies whether to use cone-mode when doing a sparse checkout.
     */
    public val sparseCheckoutConeMode_Untyped: String? = null,
    /**
     * Number of commits to fetch. 0 indicates all history for all branches and tags.
     */
    public val fetchDepth_Untyped: String? = null,
    /**
     * Whether to fetch tags, even if fetch-depth &gt; 0.
     */
    public val fetchTags_Untyped: String? = null,
    /**
     * Whether to show progress status output when fetching.
     */
    public val showProgress_Untyped: String? = null,
    /**
     * Whether to download Git-LFS files
     */
    public val lfs_Untyped: String? = null,
    /**
     * Whether to checkout submodules: `true` to checkout submodules or `recursive` to recursively
     * checkout submodules.
     *
     * When the `ssh-key` input is not provided, SSH URLs beginning with `git@github.com:` are
     * converted to HTTPS.
     */
    public val submodules_Untyped: String? = null,
    /**
     * Add repository path as safe.directory for Git global config by running `git
     * config --global --add safe.directory &lt;path&gt;`
     */
    public val setSafeDirectory_Untyped: String? = null,
    /**
     * The base URL for the GitHub instance that you are trying to clone from, will use environment
     * defaults to fetch from the same instance that the workflow is running from unless specified.
     * Example URLs are https://github.com or https://my-ghes-server.example.com
     */
    public val githubServerUrl_Untyped: String? = null,
    /**
     * Type-unsafe map where you can put any inputs that are not yet supported by the binding
     */
    public val _customInputs: Map<String, String> = mapOf(),
    /**
     * Allows overriding action's version, for example to use a specific minor version, or a newer
     * version that the binding doesn't yet know about
     */
    public val _customVersion: String? = null,
) : RegularAction<Checkout_Untyped.Outputs>("actions", "checkout", _customVersion ?: "v4") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        repository_Untyped: String? = null,
        ref_Untyped: String? = null,
        token_Untyped: String? = null,
        sshKey_Untyped: String? = null,
        sshKnownHosts_Untyped: String? = null,
        sshStrict_Untyped: String? = null,
        sshUser_Untyped: String? = null,
        persistCredentials_Untyped: String? = null,
        path_Untyped: String? = null,
        clean_Untyped: String? = null,
        filter_Untyped: String? = null,
        sparseCheckout_Untyped: String? = null,
        sparseCheckoutConeMode_Untyped: String? = null,
        fetchDepth_Untyped: String? = null,
        fetchTags_Untyped: String? = null,
        showProgress_Untyped: String? = null,
        lfs_Untyped: String? = null,
        submodules_Untyped: String? = null,
        setSafeDirectory_Untyped: String? = null,
        githubServerUrl_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(repository_Untyped = repository_Untyped, ref_Untyped = ref_Untyped, token_Untyped =
            token_Untyped, sshKey_Untyped = sshKey_Untyped, sshKnownHosts_Untyped =
            sshKnownHosts_Untyped, sshStrict_Untyped = sshStrict_Untyped, sshUser_Untyped =
            sshUser_Untyped, persistCredentials_Untyped = persistCredentials_Untyped, path_Untyped =
            path_Untyped, clean_Untyped = clean_Untyped, filter_Untyped = filter_Untyped,
            sparseCheckout_Untyped = sparseCheckout_Untyped, sparseCheckoutConeMode_Untyped =
            sparseCheckoutConeMode_Untyped, fetchDepth_Untyped = fetchDepth_Untyped,
            fetchTags_Untyped = fetchTags_Untyped, showProgress_Untyped = showProgress_Untyped,
            lfs_Untyped = lfs_Untyped, submodules_Untyped = submodules_Untyped,
            setSafeDirectory_Untyped = setSafeDirectory_Untyped, githubServerUrl_Untyped =
            githubServerUrl_Untyped, _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            repository_Untyped?.let { "repository" to it },
            ref_Untyped?.let { "ref" to it },
            token_Untyped?.let { "token" to it },
            sshKey_Untyped?.let { "ssh-key" to it },
            sshKnownHosts_Untyped?.let { "ssh-known-hosts" to it },
            sshStrict_Untyped?.let { "ssh-strict" to it },
            sshUser_Untyped?.let { "ssh-user" to it },
            persistCredentials_Untyped?.let { "persist-credentials" to it },
            path_Untyped?.let { "path" to it },
            clean_Untyped?.let { "clean" to it },
            filter_Untyped?.let { "filter" to it },
            sparseCheckout_Untyped?.let { "sparse-checkout" to it },
            sparseCheckoutConeMode_Untyped?.let { "sparse-checkout-cone-mode" to it },
            fetchDepth_Untyped?.let { "fetch-depth" to it },
            fetchTags_Untyped?.let { "fetch-tags" to it },
            showProgress_Untyped?.let { "show-progress" to it },
            lfs_Untyped?.let { "lfs" to it },
            submodules_Untyped?.let { "submodules" to it },
            setSafeDirectory_Untyped?.let { "set-safe-directory" to it },
            githubServerUrl_Untyped?.let { "github-server-url" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * The branch, tag or SHA that was checked out
         */
        public val ref: String = "steps.$stepId.outputs.ref"

        /**
         * The commit SHA that was checked out
         */
        public val commit: String = "steps.$stepId.outputs.commit"
    }
}

