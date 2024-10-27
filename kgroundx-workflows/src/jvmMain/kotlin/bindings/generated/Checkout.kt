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
import kotlin.collections.Map
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Action: Checkout
 *
 * Checkout a Git repository at a particular version
 *
 * [Action on GitHub](https://github.com/actions/checkout)
 *
 * @param repository Repository name with owner. For example, actions/checkout
 * @param repository_Untyped Repository name with owner. For example, actions/checkout
 * @param ref The branch, tag or SHA to checkout. When checking out the repository that triggered a
 * workflow, this defaults to the reference or SHA for that event.  Otherwise, uses the default branch.
 * @param ref_Untyped The branch, tag or SHA to checkout. When checking out the repository that
 * triggered a workflow, this defaults to the reference or SHA for that event.  Otherwise, uses the
 * default branch.
 * @param token Personal access token (PAT) used to fetch the repository. The PAT is configured with
 * the local git config, which enables your scripts to run authenticated git commands. The post-job
 * step removes the PAT.
 *
 * We recommend using a service account with the least permissions necessary. Also when generating a
 * new PAT, select the least scopes necessary.
 *
 * [Learn more about creating and using encrypted
 * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
 * @param token_Untyped Personal access token (PAT) used to fetch the repository. The PAT is
 * configured with the local git config, which enables your scripts to run authenticated git commands.
 * The post-job step removes the PAT.
 *
 * We recommend using a service account with the least permissions necessary. Also when generating a
 * new PAT, select the least scopes necessary.
 *
 * [Learn more about creating and using encrypted
 * secrets](https://help.github.com/en/actions/automating-your-workflow-with-github-actions/creating-and-using-encrypted-secrets)
 * @param sshKey SSH key used to fetch the repository. The SSH key is configured with the local git
 * config, which enables your scripts to run authenticated git commands. The post-job step removes the
 * SSH key.
 *
 * We recommend using a service account with the least permissions necessary.
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
 * @param sshKnownHosts Known hosts in addition to the user and global host key database. The public
 * SSH keys for a host may be obtained using the utility `ssh-keyscan`. For example, `ssh-keyscan
 * github.com`. The public key for github.com is always implicitly added.
 * @param sshKnownHosts_Untyped Known hosts in addition to the user and global host key database.
 * The public SSH keys for a host may be obtained using the utility `ssh-keyscan`. For example,
 * `ssh-keyscan github.com`. The public key for github.com is always implicitly added.
 * @param sshStrict Whether to perform strict host key checking. When true, adds the options
 * `StrictHostKeyChecking=yes` and `CheckHostIP=no` to the SSH command line. Use the input
 * `ssh-known-hosts` to configure additional hosts.
 * @param sshStrict_Untyped Whether to perform strict host key checking. When true, adds the options
 * `StrictHostKeyChecking=yes` and `CheckHostIP=no` to the SSH command line. Use the input
 * `ssh-known-hosts` to configure additional hosts.
 * @param sshUser_Untyped The user to use when connecting to the remote SSH host. By default 'git'
 * is used.
 * @param persistCredentials Whether to configure the token or SSH key with the local git config
 * @param persistCredentials_Untyped Whether to configure the token or SSH key with the local git
 * config
 * @param path Relative path under $GITHUB_WORKSPACE to place the repository
 * @param path_Untyped Relative path under $GITHUB_WORKSPACE to place the repository
 * @param clean Whether to execute `git clean -ffdx && git reset --hard HEAD` before fetching
 * @param clean_Untyped Whether to execute `git clean -ffdx && git reset --hard HEAD` before
 * fetching
 * @param filter_Untyped Partially clone against a given filter. Overrides sparse-checkout if set.
 * @param sparseCheckout Do a sparse checkout on given patterns. Each pattern should be separated
 * with new lines.
 * @param sparseCheckout_Untyped Do a sparse checkout on given patterns. Each pattern should be
 * separated with new lines.
 * @param sparseCheckoutConeMode Specifies whether to use cone-mode when doing a sparse checkout.
 * @param sparseCheckoutConeMode_Untyped Specifies whether to use cone-mode when doing a sparse
 * checkout.
 * @param fetchDepth Number of commits to fetch. 0 indicates all history for all branches and tags.
 * @param fetchDepth_Untyped Number of commits to fetch. 0 indicates all history for all branches
 * and tags.
 * @param fetchTags Whether to fetch tags, even if fetch-depth &gt; 0.
 * @param fetchTags_Untyped Whether to fetch tags, even if fetch-depth &gt; 0.
 * @param showProgress Whether to show progress status output when fetching.
 * @param showProgress_Untyped Whether to show progress status output when fetching.
 * @param lfs Whether to download Git-LFS files
 * @param lfs_Untyped Whether to download Git-LFS files
 * @param submodules Whether to checkout submodules: `true` to checkout submodules or `recursive` to
 * recursively checkout submodules.
 *
 * When the `ssh-key` input is not provided, SSH URLs beginning with `git@github.com:` are converted
 * to HTTPS.
 * @param submodules_Untyped Whether to checkout submodules: `true` to checkout submodules or
 * `recursive` to recursively checkout submodules.
 *
 * When the `ssh-key` input is not provided, SSH URLs beginning with `git@github.com:` are converted
 * to HTTPS.
 * @param setSafeDirectory Add repository path as safe.directory for Git global config by running
 * `git config --global --add safe.directory &lt;path&gt;`
 * @param setSafeDirectory_Untyped Add repository path as safe.directory for Git global config by
 * running `git config --global --add safe.directory &lt;path&gt;`
 * @param githubServerUrl The base URL for the GitHub instance that you are trying to clone from,
 * will use environment defaults to fetch from the same instance that the workflow is running from
 * unless specified. Example URLs are https://github.com or https://my-ghes-server.example.com
 * @param githubServerUrl_Untyped The base URL for the GitHub instance that you are trying to clone
 * from, will use environment defaults to fetch from the same instance that the workflow is running
 * from unless specified. Example URLs are https://github.com or https://my-ghes-server.example.com
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@ExposedCopyVisibility
public data class Checkout private constructor(
    /**
     * Repository name with owner. For example, actions/checkout
     */
    public val repository: String? = null,
    /**
     * Repository name with owner. For example, actions/checkout
     */
    public val repository_Untyped: String? = null,
    /**
     * The branch, tag or SHA to checkout. When checking out the repository that triggered a
     * workflow, this defaults to the reference or SHA for that event.  Otherwise, uses the default
     * branch.
     */
    public val ref: String? = null,
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
    public val token: String? = null,
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
    public val sshKey: String? = null,
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
    public val sshKnownHosts: String? = null,
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
    public val sshStrict: Boolean? = null,
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
    public val persistCredentials: Boolean? = null,
    /**
     * Whether to configure the token or SSH key with the local git config
     */
    public val persistCredentials_Untyped: String? = null,
    /**
     * Relative path under $GITHUB_WORKSPACE to place the repository
     */
    public val path: String? = null,
    /**
     * Relative path under $GITHUB_WORKSPACE to place the repository
     */
    public val path_Untyped: String? = null,
    /**
     * Whether to execute `git clean -ffdx && git reset --hard HEAD` before fetching
     */
    public val clean: Boolean? = null,
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
    public val sparseCheckout: Boolean? = null,
    /**
     * Do a sparse checkout on given patterns. Each pattern should be separated with new lines.
     */
    public val sparseCheckout_Untyped: String? = null,
    /**
     * Specifies whether to use cone-mode when doing a sparse checkout.
     */
    public val sparseCheckoutConeMode: Boolean? = null,
    /**
     * Specifies whether to use cone-mode when doing a sparse checkout.
     */
    public val sparseCheckoutConeMode_Untyped: String? = null,
    /**
     * Number of commits to fetch. 0 indicates all history for all branches and tags.
     */
    public val fetchDepth: Checkout.FetchDepth? = null,
    /**
     * Number of commits to fetch. 0 indicates all history for all branches and tags.
     */
    public val fetchDepth_Untyped: String? = null,
    /**
     * Whether to fetch tags, even if fetch-depth &gt; 0.
     */
    public val fetchTags: Boolean? = null,
    /**
     * Whether to fetch tags, even if fetch-depth &gt; 0.
     */
    public val fetchTags_Untyped: String? = null,
    /**
     * Whether to show progress status output when fetching.
     */
    public val showProgress: Boolean? = null,
    /**
     * Whether to show progress status output when fetching.
     */
    public val showProgress_Untyped: String? = null,
    /**
     * Whether to download Git-LFS files
     */
    public val lfs: Boolean? = null,
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
    public val submodules: Boolean? = null,
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
    public val setSafeDirectory: Boolean? = null,
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
    public val githubServerUrl: String? = null,
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
) : RegularAction<Checkout.Outputs>("actions", "checkout", _customVersion ?: "v4") {
    init {
        require(!((repository != null) && (repository_Untyped != null))) {
            "Only repository or repository_Untyped must be set, but not both"
        }

        require(!((ref != null) && (ref_Untyped != null))) {
            "Only ref or ref_Untyped must be set, but not both"
        }

        require(!((token != null) && (token_Untyped != null))) {
            "Only token or token_Untyped must be set, but not both"
        }

        require(!((sshKey != null) && (sshKey_Untyped != null))) {
            "Only sshKey or sshKey_Untyped must be set, but not both"
        }

        require(!((sshKnownHosts != null) && (sshKnownHosts_Untyped != null))) {
            "Only sshKnownHosts or sshKnownHosts_Untyped must be set, but not both"
        }

        require(!((sshStrict != null) && (sshStrict_Untyped != null))) {
            "Only sshStrict or sshStrict_Untyped must be set, but not both"
        }

        require(!((persistCredentials != null) && (persistCredentials_Untyped != null))) {
            "Only persistCredentials or persistCredentials_Untyped must be set, but not both"
        }

        require(!((path != null) && (path_Untyped != null))) {
            "Only path or path_Untyped must be set, but not both"
        }

        require(!((clean != null) && (clean_Untyped != null))) {
            "Only clean or clean_Untyped must be set, but not both"
        }

        require(!((sparseCheckout != null) && (sparseCheckout_Untyped != null))) {
            "Only sparseCheckout or sparseCheckout_Untyped must be set, but not both"
        }

        require(!((sparseCheckoutConeMode != null) && (sparseCheckoutConeMode_Untyped != null))) {
           
                "Only sparseCheckoutConeMode or sparseCheckoutConeMode_Untyped must be set, but not both"
        }

        require(!((fetchDepth != null) && (fetchDepth_Untyped != null))) {
            "Only fetchDepth or fetchDepth_Untyped must be set, but not both"
        }

        require(!((fetchTags != null) && (fetchTags_Untyped != null))) {
            "Only fetchTags or fetchTags_Untyped must be set, but not both"
        }

        require(!((showProgress != null) && (showProgress_Untyped != null))) {
            "Only showProgress or showProgress_Untyped must be set, but not both"
        }

        require(!((lfs != null) && (lfs_Untyped != null))) {
            "Only lfs or lfs_Untyped must be set, but not both"
        }

        require(!((submodules != null) && (submodules_Untyped != null))) {
            "Only submodules or submodules_Untyped must be set, but not both"
        }

        require(!((setSafeDirectory != null) && (setSafeDirectory_Untyped != null))) {
            "Only setSafeDirectory or setSafeDirectory_Untyped must be set, but not both"
        }

        require(!((githubServerUrl != null) && (githubServerUrl_Untyped != null))) {
            "Only githubServerUrl or githubServerUrl_Untyped must be set, but not both"
        }
    }

    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        repository: String? = null,
        repository_Untyped: String? = null,
        ref: String? = null,
        ref_Untyped: String? = null,
        token: String? = null,
        token_Untyped: String? = null,
        sshKey: String? = null,
        sshKey_Untyped: String? = null,
        sshKnownHosts: String? = null,
        sshKnownHosts_Untyped: String? = null,
        sshStrict: Boolean? = null,
        sshStrict_Untyped: String? = null,
        sshUser_Untyped: String? = null,
        persistCredentials: Boolean? = null,
        persistCredentials_Untyped: String? = null,
        path: String? = null,
        path_Untyped: String? = null,
        clean: Boolean? = null,
        clean_Untyped: String? = null,
        filter_Untyped: String? = null,
        sparseCheckout: Boolean? = null,
        sparseCheckout_Untyped: String? = null,
        sparseCheckoutConeMode: Boolean? = null,
        sparseCheckoutConeMode_Untyped: String? = null,
        fetchDepth: Checkout.FetchDepth? = null,
        fetchDepth_Untyped: String? = null,
        fetchTags: Boolean? = null,
        fetchTags_Untyped: String? = null,
        showProgress: Boolean? = null,
        showProgress_Untyped: String? = null,
        lfs: Boolean? = null,
        lfs_Untyped: String? = null,
        submodules: Boolean? = null,
        submodules_Untyped: String? = null,
        setSafeDirectory: Boolean? = null,
        setSafeDirectory_Untyped: String? = null,
        githubServerUrl: String? = null,
        githubServerUrl_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(repository = repository, repository_Untyped = repository_Untyped, ref = ref,
            ref_Untyped = ref_Untyped, token = token, token_Untyped = token_Untyped, sshKey =
            sshKey, sshKey_Untyped = sshKey_Untyped, sshKnownHosts = sshKnownHosts,
            sshKnownHosts_Untyped = sshKnownHosts_Untyped, sshStrict = sshStrict, sshStrict_Untyped
            = sshStrict_Untyped, sshUser_Untyped = sshUser_Untyped, persistCredentials =
            persistCredentials, persistCredentials_Untyped = persistCredentials_Untyped, path =
            path, path_Untyped = path_Untyped, clean = clean, clean_Untyped = clean_Untyped,
            filter_Untyped = filter_Untyped, sparseCheckout = sparseCheckout, sparseCheckout_Untyped
            = sparseCheckout_Untyped, sparseCheckoutConeMode = sparseCheckoutConeMode,
            sparseCheckoutConeMode_Untyped = sparseCheckoutConeMode_Untyped, fetchDepth =
            fetchDepth, fetchDepth_Untyped = fetchDepth_Untyped, fetchTags = fetchTags,
            fetchTags_Untyped = fetchTags_Untyped, showProgress = showProgress, showProgress_Untyped
            = showProgress_Untyped, lfs = lfs, lfs_Untyped = lfs_Untyped, submodules = submodules,
            submodules_Untyped = submodules_Untyped, setSafeDirectory = setSafeDirectory,
            setSafeDirectory_Untyped = setSafeDirectory_Untyped, githubServerUrl = githubServerUrl,
            githubServerUrl_Untyped = githubServerUrl_Untyped, _customInputs = _customInputs,
            _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            repository?.let { "repository" to it },
            repository_Untyped?.let { "repository" to it },
            ref?.let { "ref" to it },
            ref_Untyped?.let { "ref" to it },
            token?.let { "token" to it },
            token_Untyped?.let { "token" to it },
            sshKey?.let { "ssh-key" to it },
            sshKey_Untyped?.let { "ssh-key" to it },
            sshKnownHosts?.let { "ssh-known-hosts" to it },
            sshKnownHosts_Untyped?.let { "ssh-known-hosts" to it },
            sshStrict?.let { "ssh-strict" to it.toString() },
            sshStrict_Untyped?.let { "ssh-strict" to it },
            sshUser_Untyped?.let { "ssh-user" to it },
            persistCredentials?.let { "persist-credentials" to it.toString() },
            persistCredentials_Untyped?.let { "persist-credentials" to it },
            path?.let { "path" to it },
            path_Untyped?.let { "path" to it },
            clean?.let { "clean" to it.toString() },
            clean_Untyped?.let { "clean" to it },
            filter_Untyped?.let { "filter" to it },
            sparseCheckout?.let { "sparse-checkout" to it.toString() },
            sparseCheckout_Untyped?.let { "sparse-checkout" to it },
            sparseCheckoutConeMode?.let { "sparse-checkout-cone-mode" to it.toString() },
            sparseCheckoutConeMode_Untyped?.let { "sparse-checkout-cone-mode" to it },
            fetchDepth?.let { "fetch-depth" to it.integerValue.toString() },
            fetchDepth_Untyped?.let { "fetch-depth" to it },
            fetchTags?.let { "fetch-tags" to it.toString() },
            fetchTags_Untyped?.let { "fetch-tags" to it },
            showProgress?.let { "show-progress" to it.toString() },
            showProgress_Untyped?.let { "show-progress" to it },
            lfs?.let { "lfs" to it.toString() },
            lfs_Untyped?.let { "lfs" to it },
            submodules?.let { "submodules" to it.toString() },
            submodules_Untyped?.let { "submodules" to it },
            setSafeDirectory?.let { "set-safe-directory" to it.toString() },
            setSafeDirectory_Untyped?.let { "set-safe-directory" to it },
            githubServerUrl?.let { "github-server-url" to it },
            githubServerUrl_Untyped?.let { "github-server-url" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public sealed class FetchDepth(
        public val integerValue: Int,
    ) {
        public class Value(
            requestedValue: Int,
        ) : Checkout.FetchDepth(requestedValue)

        public object Infinite : Checkout.FetchDepth(0)
    }

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

