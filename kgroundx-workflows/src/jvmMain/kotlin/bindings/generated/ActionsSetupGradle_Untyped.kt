// This file was generated using action-binding-generator. Don't change it by hand, otherwise your
// changes will be overwritten with the next binding code regeneration.
// See https://github.com/typesafegithub/github-workflows-kt for more info.
@file:Suppress(
    "DataClassPrivateConstructor",
    "UNUSED_PARAMETER",
)

package io.github.typesafegithub.workflows.actions.gradle

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
 * Action: Setup Gradle
 *
 * Configures Gradle for GitHub actions, caching state and generating a dependency graph via
 * Dependency Submission.
 *
 * [Action on GitHub](https://github.com/gradle/actions/tree/v4/setup-gradle)
 *
 * @param gradleVersion_Untyped Gradle version to use. If specified, this Gradle version will be
 * downloaded, added to the PATH and used for invoking Gradle.
 * If not provided, it is assumed that the project uses the Gradle Wrapper.
 * @param cacheDisabled_Untyped When 'true', all caching is disabled. No entries will be written to
 * or read from the cache.
 * @param cacheReadOnly_Untyped When 'true', existing entries will be read from the cache but no
 * entries will be written.
 * By default this value is 'false' for workflows on the GitHub default branch and 'true' for
 * workflows on other branches.
 * @param cacheWriteOnly_Untyped When 'true', entries will not be restored from the cache but will
 * be saved at the end of the Job.
 * Setting this to 'true' implies cache-read-only will be 'false'.
 * @param cacheOverwriteExisting_Untyped When 'true', a pre-existing Gradle User Home will not
 * prevent the cache from being restored.
 * @param cacheEncryptionKey_Untyped A base64 encoded AES key used to encrypt the
 * configuration-cache data. The key is exported as 'GRADLE_ENCRYPTION_KEY' for later steps.
 * A suitable key can be generated with `openssl rand -base64 16`.
 * Configuration-cache data will not be saved/restored without an encryption key being provided.
 * @param cacheCleanup_Untyped Specifies if the action should attempt to remove any stale/unused
 * entries from the Gradle User Home prior to saving to the GitHub Actions cache.
 * By default ('on-success'), cleanup is performed when all Gradle builds succeed for the Job.
 * This behaviour can be disabled ('never'), or configured to always run irrespective of the build
 * outcome ('always').
 * Valid values are 'never', 'on-success' and 'always'.
 * @param gradleHomeCacheCleanup_Untyped When 'true', the action will attempt to remove any
 * stale/unused entries from the Gradle User Home prior to saving to the GitHub Actions cache.
 * @param gradleHomeCacheIncludes_Untyped Paths within Gradle User Home to cache.
 * @param gradleHomeCacheExcludes_Untyped Paths within Gradle User Home to exclude from cache.
 * @param addJobSummary_Untyped Specifies when a Job Summary should be inluded in the action
 * results. Valid values are 'never', 'always' (default), and 'on-failure'.
 * @param addJobSummaryAsPrComment_Untyped Specifies when each Job Summary should be added as a PR
 * comment. Valid values are 'never' (default), 'always', and 'on-failure'. No action will be taken if
 * the workflow was not triggered from a pull request.
 * @param dependencyGraph_Untyped Specifies if a GitHub dependency snapshot should be generated for
 * each Gradle build, and if so, how.
 * Valid values are 'disabled' (default), 'generate', 'generate-and-submit', 'generate-and-upload',
 * and 'download-and-submit'.
 * @param dependencyGraphReportDir_Untyped Specifies where the dependency graph report will be
 * generated.
 * Paths can relative or absolute. Relative paths are resolved relative to the workspace directory.
 * @param dependencyGraphContinueOnFailure_Untyped When 'false' a failure to generate or submit a
 * dependency graph will fail the Step or Job. When 'true' a warning will be emitted but no failure
 * will result.
 * @param dependencyGraphExcludeProjects_Untyped Gradle projects that should be excluded from
 * dependency graph (regular expression).
 * When set, any matching project will be excluded.
 * @param dependencyGraphIncludeProjects_Untyped Gradle projects that should be included in
 * dependency graph (regular expression).
 * When set, only matching projects will be included.
 * @param dependencyGraphExcludeConfigurations_Untyped Gradle configurations that should be included
 * in dependency graph (regular expression).
 * When set, anymatching configurations will be excluded.
 * @param dependencyGraphIncludeConfigurations_Untyped Gradle configurations that should be included
 * in dependency graph (regular expression).
 * When set, only matching configurations will be included.
 * @param artifactRetentionDays_Untyped Specifies the number of days to retain any artifacts
 * generated by the action. If not set, the default retention settings for the repository will apply.
 * @param buildScanPublish_Untyped Set to 'true' to automatically publish build results as a Build
 * Scan on scans.gradle.com.
 * For publication to succeed without user input, you must also provide values for
 * `build-scan-terms-of-use-url` and 'build-scan-terms-of-use-agree'.
 * @param buildScanTermsOfUseUrl_Untyped The URL to the Build Scan® terms of use. This input must be
 * set to 'https://gradle.com/terms-of-service' or 'https://gradle.com/help/legal-terms-of-use'.
 * @param buildScanTermsOfUseAgree_Untyped Indicate that you agree to the Build Scan® terms of use.
 * This input value must be "yes".
 * @param develocityAccessKey_Untyped Develocity access key. Should be set to a secret containing
 * the Develocity Access key.
 * @param develocityTokenExpiry_Untyped The Develocity short-lived access tokens expiry in hours.
 * Default is 2 hours.
 * @param develocityInjectionEnabled_Untyped Enables Develocity injection.
 * @param develocityUrl_Untyped The URL for the Develocity server.
 * @param develocityAllowUntrustedServer_Untyped Allow communication with an untrusted server; set
 * to _true_ if your Develocity instance is using a self-signed.
 * @param develocityCaptureFileFingerprints_Untyped Enables capturing the paths and content hashes
 * of each individual input file.
 * @param develocityEnforceUrl_Untyped Enforce the configured Develocity URL over a URL configured
 * in the project's build; set to _true_ to enforce publication of build scans to the configured
 * Develocity URL.
 * @param develocityPluginVersion_Untyped The version of the Develocity Gradle plugin to apply.
 * @param develocityCcudPluginVersion_Untyped The version of the Common Custom User Data Gradle
 * plugin to apply, if any.
 * @param gradlePluginRepositoryUrl_Untyped The URL of the repository to use when resolving the
 * Develocity and CCUD plugins; the Gradle Plugin Portal is used by default.
 * @param gradlePluginRepositoryUsername_Untyped The username for the repository URL to use when
 * resolving the Develocity and CCUD.
 * @param gradlePluginRepositoryPassword_Untyped The password for the repository URL to use when
 * resolving the Develocity and CCUD plugins; Consider using secrets to pass the value to this
 * variable.
 * @param validateWrappers_Untyped When 'true' (the default) the action will automatically validate
 * all wrapper jars found in the repository.
 * If the wrapper checksums are not valid, the action will fail.
 * @param allowSnapshotWrappers_Untyped When 'true', wrapper validation will include the checksums
 * of snapshot wrapper jars.
 * Use this if you are running with nightly or snapshot versions of the Gradle wrapper.
 * @param arguments_Untyped Gradle command line arguments (supports multi-line input)
 * @param gradleHomeCacheStrictMatch_Untyped When 'true', the action will not attempt to restore the
 * Gradle User Home entries from other Jobs.
 * @param workflowJobContext_Untyped Used to uniquely identify the current job invocation. Defaults
 * to the matrix values for this job; this should not be overridden by users (INTERNAL).
 * @param githubToken_Untyped The GitHub token used to authenticate when submitting via the
 * Dependency Submission API.
 * @param _customInputs Type-unsafe map where you can put any inputs that are not yet supported by
 * the binding
 * @param _customVersion Allows overriding action's version, for example to use a specific minor
 * version, or a newer version that the binding doesn't yet know about
 */
@Deprecated(
    "Use the typed class instead",
    ReplaceWith("ActionsSetupGradle"),
)
@ExposedCopyVisibility
public data class ActionsSetupGradle_Untyped private constructor(
    /**
     * Gradle version to use. If specified, this Gradle version will be downloaded, added to the
     * PATH and used for invoking Gradle.
     * If not provided, it is assumed that the project uses the Gradle Wrapper.
     */
    public val gradleVersion_Untyped: String? = null,
    /**
     * When 'true', all caching is disabled. No entries will be written to or read from the cache.
     */
    public val cacheDisabled_Untyped: String? = null,
    /**
     * When 'true', existing entries will be read from the cache but no entries will be written.
     * By default this value is 'false' for workflows on the GitHub default branch and 'true' for
     * workflows on other branches.
     */
    public val cacheReadOnly_Untyped: String? = null,
    /**
     * When 'true', entries will not be restored from the cache but will be saved at the end of the
     * Job.
     * Setting this to 'true' implies cache-read-only will be 'false'.
     */
    public val cacheWriteOnly_Untyped: String? = null,
    /**
     * When 'true', a pre-existing Gradle User Home will not prevent the cache from being restored.
     */
    public val cacheOverwriteExisting_Untyped: String? = null,
    /**
     * A base64 encoded AES key used to encrypt the configuration-cache data. The key is exported as
     * 'GRADLE_ENCRYPTION_KEY' for later steps.
     * A suitable key can be generated with `openssl rand -base64 16`.
     * Configuration-cache data will not be saved/restored without an encryption key being provided.
     */
    public val cacheEncryptionKey_Untyped: String? = null,
    /**
     * Specifies if the action should attempt to remove any stale/unused entries from the Gradle
     * User Home prior to saving to the GitHub Actions cache.
     * By default ('on-success'), cleanup is performed when all Gradle builds succeed for the Job.
     * This behaviour can be disabled ('never'), or configured to always run irrespective of the
     * build outcome ('always').
     * Valid values are 'never', 'on-success' and 'always'.
     */
    public val cacheCleanup_Untyped: String? = null,
    /**
     * When 'true', the action will attempt to remove any stale/unused entries from the Gradle User
     * Home prior to saving to the GitHub Actions cache.
     */
    public val gradleHomeCacheCleanup_Untyped: String? = null,
    /**
     * Paths within Gradle User Home to cache.
     */
    public val gradleHomeCacheIncludes_Untyped: String? = null,
    /**
     * Paths within Gradle User Home to exclude from cache.
     */
    public val gradleHomeCacheExcludes_Untyped: String? = null,
    /**
     * Specifies when a Job Summary should be inluded in the action results. Valid values are
     * 'never', 'always' (default), and 'on-failure'.
     */
    public val addJobSummary_Untyped: String? = null,
    /**
     * Specifies when each Job Summary should be added as a PR comment. Valid values are 'never'
     * (default), 'always', and 'on-failure'. No action will be taken if the workflow was not triggered
     * from a pull request.
     */
    public val addJobSummaryAsPrComment_Untyped: String? = null,
    /**
     * Specifies if a GitHub dependency snapshot should be generated for each Gradle build, and if
     * so, how.
     * Valid values are 'disabled' (default), 'generate', 'generate-and-submit',
     * 'generate-and-upload', and 'download-and-submit'.
     */
    public val dependencyGraph_Untyped: String? = null,
    /**
     * Specifies where the dependency graph report will be generated.
     * Paths can relative or absolute. Relative paths are resolved relative to the workspace
     * directory.
     */
    public val dependencyGraphReportDir_Untyped: String? = null,
    /**
     * When 'false' a failure to generate or submit a dependency graph will fail the Step or Job.
     * When 'true' a warning will be emitted but no failure will result.
     */
    public val dependencyGraphContinueOnFailure_Untyped: String? = null,
    /**
     * Gradle projects that should be excluded from dependency graph (regular expression).
     * When set, any matching project will be excluded.
     */
    public val dependencyGraphExcludeProjects_Untyped: String? = null,
    /**
     * Gradle projects that should be included in dependency graph (regular expression).
     * When set, only matching projects will be included.
     */
    public val dependencyGraphIncludeProjects_Untyped: String? = null,
    /**
     * Gradle configurations that should be included in dependency graph (regular expression).
     * When set, anymatching configurations will be excluded.
     */
    public val dependencyGraphExcludeConfigurations_Untyped: String? = null,
    /**
     * Gradle configurations that should be included in dependency graph (regular expression).
     * When set, only matching configurations will be included.
     */
    public val dependencyGraphIncludeConfigurations_Untyped: String? = null,
    /**
     * Specifies the number of days to retain any artifacts generated by the action. If not set, the
     * default retention settings for the repository will apply.
     */
    public val artifactRetentionDays_Untyped: String? = null,
    /**
     * Set to 'true' to automatically publish build results as a Build Scan on scans.gradle.com.
     * For publication to succeed without user input, you must also provide values for
     * `build-scan-terms-of-use-url` and 'build-scan-terms-of-use-agree'.
     */
    public val buildScanPublish_Untyped: String? = null,
    /**
     * The URL to the Build Scan® terms of use. This input must be set to
     * 'https://gradle.com/terms-of-service' or 'https://gradle.com/help/legal-terms-of-use'.
     */
    public val buildScanTermsOfUseUrl_Untyped: String? = null,
    /**
     * Indicate that you agree to the Build Scan® terms of use. This input value must be "yes".
     */
    public val buildScanTermsOfUseAgree_Untyped: String? = null,
    /**
     * Develocity access key. Should be set to a secret containing the Develocity Access key.
     */
    public val develocityAccessKey_Untyped: String? = null,
    /**
     * The Develocity short-lived access tokens expiry in hours. Default is 2 hours.
     */
    public val develocityTokenExpiry_Untyped: String? = null,
    /**
     * Enables Develocity injection.
     */
    public val develocityInjectionEnabled_Untyped: String? = null,
    /**
     * The URL for the Develocity server.
     */
    public val develocityUrl_Untyped: String? = null,
    /**
     * Allow communication with an untrusted server; set to _true_ if your Develocity instance is
     * using a self-signed.
     */
    public val develocityAllowUntrustedServer_Untyped: String? = null,
    /**
     * Enables capturing the paths and content hashes of each individual input file.
     */
    public val develocityCaptureFileFingerprints_Untyped: String? = null,
    /**
     * Enforce the configured Develocity URL over a URL configured in the project's build; set to
     * _true_ to enforce publication of build scans to the configured Develocity URL.
     */
    public val develocityEnforceUrl_Untyped: String? = null,
    /**
     * The version of the Develocity Gradle plugin to apply.
     */
    public val develocityPluginVersion_Untyped: String? = null,
    /**
     * The version of the Common Custom User Data Gradle plugin to apply, if any.
     */
    public val develocityCcudPluginVersion_Untyped: String? = null,
    /**
     * The URL of the repository to use when resolving the Develocity and CCUD plugins; the Gradle
     * Plugin Portal is used by default.
     */
    public val gradlePluginRepositoryUrl_Untyped: String? = null,
    /**
     * The username for the repository URL to use when resolving the Develocity and CCUD.
     */
    public val gradlePluginRepositoryUsername_Untyped: String? = null,
    /**
     * The password for the repository URL to use when resolving the Develocity and CCUD plugins;
     * Consider using secrets to pass the value to this variable.
     */
    public val gradlePluginRepositoryPassword_Untyped: String? = null,
    /**
     * When 'true' (the default) the action will automatically validate all wrapper jars found in
     * the repository.
     * If the wrapper checksums are not valid, the action will fail.
     */
    public val validateWrappers_Untyped: String? = null,
    /**
     * When 'true', wrapper validation will include the checksums of snapshot wrapper jars.
     * Use this if you are running with nightly or snapshot versions of the Gradle wrapper.
     */
    public val allowSnapshotWrappers_Untyped: String? = null,
    /**
     * Gradle command line arguments (supports multi-line input)
     */
    public val arguments_Untyped: String? = null,
    /**
     * When 'true', the action will not attempt to restore the Gradle User Home entries from other
     * Jobs.
     */
    public val gradleHomeCacheStrictMatch_Untyped: String? = null,
    /**
     * Used to uniquely identify the current job invocation. Defaults to the matrix values for this
     * job; this should not be overridden by users (INTERNAL).
     */
    public val workflowJobContext_Untyped: String? = null,
    /**
     * The GitHub token used to authenticate when submitting via the Dependency Submission API.
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
) : RegularAction<ActionsSetupGradle_Untyped.Outputs>("gradle", "actions/setup-gradle",
        _customVersion ?: "v4") {
    public constructor(
        vararg pleaseUseNamedArguments: Unit,
        gradleVersion_Untyped: String? = null,
        cacheDisabled_Untyped: String? = null,
        cacheReadOnly_Untyped: String? = null,
        cacheWriteOnly_Untyped: String? = null,
        cacheOverwriteExisting_Untyped: String? = null,
        cacheEncryptionKey_Untyped: String? = null,
        cacheCleanup_Untyped: String? = null,
        gradleHomeCacheCleanup_Untyped: String? = null,
        gradleHomeCacheIncludes_Untyped: String? = null,
        gradleHomeCacheExcludes_Untyped: String? = null,
        addJobSummary_Untyped: String? = null,
        addJobSummaryAsPrComment_Untyped: String? = null,
        dependencyGraph_Untyped: String? = null,
        dependencyGraphReportDir_Untyped: String? = null,
        dependencyGraphContinueOnFailure_Untyped: String? = null,
        dependencyGraphExcludeProjects_Untyped: String? = null,
        dependencyGraphIncludeProjects_Untyped: String? = null,
        dependencyGraphExcludeConfigurations_Untyped: String? = null,
        dependencyGraphIncludeConfigurations_Untyped: String? = null,
        artifactRetentionDays_Untyped: String? = null,
        buildScanPublish_Untyped: String? = null,
        buildScanTermsOfUseUrl_Untyped: String? = null,
        buildScanTermsOfUseAgree_Untyped: String? = null,
        develocityAccessKey_Untyped: String? = null,
        develocityTokenExpiry_Untyped: String? = null,
        develocityInjectionEnabled_Untyped: String? = null,
        develocityUrl_Untyped: String? = null,
        develocityAllowUntrustedServer_Untyped: String? = null,
        develocityCaptureFileFingerprints_Untyped: String? = null,
        develocityEnforceUrl_Untyped: String? = null,
        develocityPluginVersion_Untyped: String? = null,
        develocityCcudPluginVersion_Untyped: String? = null,
        gradlePluginRepositoryUrl_Untyped: String? = null,
        gradlePluginRepositoryUsername_Untyped: String? = null,
        gradlePluginRepositoryPassword_Untyped: String? = null,
        validateWrappers_Untyped: String? = null,
        allowSnapshotWrappers_Untyped: String? = null,
        arguments_Untyped: String? = null,
        gradleHomeCacheStrictMatch_Untyped: String? = null,
        workflowJobContext_Untyped: String? = null,
        githubToken_Untyped: String? = null,
        _customInputs: Map<String, String> = mapOf(),
        _customVersion: String? = null,
    ) : this(gradleVersion_Untyped = gradleVersion_Untyped, cacheDisabled_Untyped =
            cacheDisabled_Untyped, cacheReadOnly_Untyped = cacheReadOnly_Untyped,
            cacheWriteOnly_Untyped = cacheWriteOnly_Untyped, cacheOverwriteExisting_Untyped =
            cacheOverwriteExisting_Untyped, cacheEncryptionKey_Untyped = cacheEncryptionKey_Untyped,
            cacheCleanup_Untyped = cacheCleanup_Untyped, gradleHomeCacheCleanup_Untyped =
            gradleHomeCacheCleanup_Untyped, gradleHomeCacheIncludes_Untyped =
            gradleHomeCacheIncludes_Untyped, gradleHomeCacheExcludes_Untyped =
            gradleHomeCacheExcludes_Untyped, addJobSummary_Untyped = addJobSummary_Untyped,
            addJobSummaryAsPrComment_Untyped = addJobSummaryAsPrComment_Untyped,
            dependencyGraph_Untyped = dependencyGraph_Untyped, dependencyGraphReportDir_Untyped =
            dependencyGraphReportDir_Untyped, dependencyGraphContinueOnFailure_Untyped =
            dependencyGraphContinueOnFailure_Untyped, dependencyGraphExcludeProjects_Untyped =
            dependencyGraphExcludeProjects_Untyped, dependencyGraphIncludeProjects_Untyped =
            dependencyGraphIncludeProjects_Untyped, dependencyGraphExcludeConfigurations_Untyped =
            dependencyGraphExcludeConfigurations_Untyped,
            dependencyGraphIncludeConfigurations_Untyped =
            dependencyGraphIncludeConfigurations_Untyped, artifactRetentionDays_Untyped =
            artifactRetentionDays_Untyped, buildScanPublish_Untyped = buildScanPublish_Untyped,
            buildScanTermsOfUseUrl_Untyped = buildScanTermsOfUseUrl_Untyped,
            buildScanTermsOfUseAgree_Untyped = buildScanTermsOfUseAgree_Untyped,
            develocityAccessKey_Untyped = develocityAccessKey_Untyped, develocityTokenExpiry_Untyped
            = develocityTokenExpiry_Untyped, develocityInjectionEnabled_Untyped =
            develocityInjectionEnabled_Untyped, develocityUrl_Untyped = develocityUrl_Untyped,
            develocityAllowUntrustedServer_Untyped = develocityAllowUntrustedServer_Untyped,
            develocityCaptureFileFingerprints_Untyped = develocityCaptureFileFingerprints_Untyped,
            develocityEnforceUrl_Untyped = develocityEnforceUrl_Untyped,
            develocityPluginVersion_Untyped = develocityPluginVersion_Untyped,
            develocityCcudPluginVersion_Untyped = develocityCcudPluginVersion_Untyped,
            gradlePluginRepositoryUrl_Untyped = gradlePluginRepositoryUrl_Untyped,
            gradlePluginRepositoryUsername_Untyped = gradlePluginRepositoryUsername_Untyped,
            gradlePluginRepositoryPassword_Untyped = gradlePluginRepositoryPassword_Untyped,
            validateWrappers_Untyped = validateWrappers_Untyped, allowSnapshotWrappers_Untyped =
            allowSnapshotWrappers_Untyped, arguments_Untyped = arguments_Untyped,
            gradleHomeCacheStrictMatch_Untyped = gradleHomeCacheStrictMatch_Untyped,
            workflowJobContext_Untyped = workflowJobContext_Untyped, githubToken_Untyped =
            githubToken_Untyped, _customInputs = _customInputs, _customVersion = _customVersion)

    @Suppress("SpreadOperator")
    override fun toYamlArguments(): LinkedHashMap<String, String> = linkedMapOf(
        *listOfNotNull(
            gradleVersion_Untyped?.let { "gradle-version" to it },
            cacheDisabled_Untyped?.let { "cache-disabled" to it },
            cacheReadOnly_Untyped?.let { "cache-read-only" to it },
            cacheWriteOnly_Untyped?.let { "cache-write-only" to it },
            cacheOverwriteExisting_Untyped?.let { "cache-overwrite-existing" to it },
            cacheEncryptionKey_Untyped?.let { "cache-encryption-key" to it },
            cacheCleanup_Untyped?.let { "cache-cleanup" to it },
            gradleHomeCacheCleanup_Untyped?.let { "gradle-home-cache-cleanup" to it },
            gradleHomeCacheIncludes_Untyped?.let { "gradle-home-cache-includes" to it },
            gradleHomeCacheExcludes_Untyped?.let { "gradle-home-cache-excludes" to it },
            addJobSummary_Untyped?.let { "add-job-summary" to it },
            addJobSummaryAsPrComment_Untyped?.let { "add-job-summary-as-pr-comment" to it },
            dependencyGraph_Untyped?.let { "dependency-graph" to it },
            dependencyGraphReportDir_Untyped?.let { "dependency-graph-report-dir" to it },
            dependencyGraphContinueOnFailure_Untyped?.let {
                    "dependency-graph-continue-on-failure" to it },
            dependencyGraphExcludeProjects_Untyped?.let { "dependency-graph-exclude-projects" to it
                    },
            dependencyGraphIncludeProjects_Untyped?.let { "dependency-graph-include-projects" to it
                    },
            dependencyGraphExcludeConfigurations_Untyped?.let {
                    "dependency-graph-exclude-configurations" to it },
            dependencyGraphIncludeConfigurations_Untyped?.let {
                    "dependency-graph-include-configurations" to it },
            artifactRetentionDays_Untyped?.let { "artifact-retention-days" to it },
            buildScanPublish_Untyped?.let { "build-scan-publish" to it },
            buildScanTermsOfUseUrl_Untyped?.let { "build-scan-terms-of-use-url" to it },
            buildScanTermsOfUseAgree_Untyped?.let { "build-scan-terms-of-use-agree" to it },
            develocityAccessKey_Untyped?.let { "develocity-access-key" to it },
            develocityTokenExpiry_Untyped?.let { "develocity-token-expiry" to it },
            develocityInjectionEnabled_Untyped?.let { "develocity-injection-enabled" to it },
            develocityUrl_Untyped?.let { "develocity-url" to it },
            develocityAllowUntrustedServer_Untyped?.let { "develocity-allow-untrusted-server" to it
                    },
            develocityCaptureFileFingerprints_Untyped?.let {
                    "develocity-capture-file-fingerprints" to it },
            develocityEnforceUrl_Untyped?.let { "develocity-enforce-url" to it },
            develocityPluginVersion_Untyped?.let { "develocity-plugin-version" to it },
            develocityCcudPluginVersion_Untyped?.let { "develocity-ccud-plugin-version" to it },
            gradlePluginRepositoryUrl_Untyped?.let { "gradle-plugin-repository-url" to it },
            gradlePluginRepositoryUsername_Untyped?.let { "gradle-plugin-repository-username" to it
                    },
            gradlePluginRepositoryPassword_Untyped?.let { "gradle-plugin-repository-password" to it
                    },
            validateWrappers_Untyped?.let { "validate-wrappers" to it },
            allowSnapshotWrappers_Untyped?.let { "allow-snapshot-wrappers" to it },
            arguments_Untyped?.let { "arguments" to it },
            gradleHomeCacheStrictMatch_Untyped?.let { "gradle-home-cache-strict-match" to it },
            workflowJobContext_Untyped?.let { "workflow-job-context" to it },
            githubToken_Untyped?.let { "github-token" to it },
            *_customInputs.toList().toTypedArray(),
        ).toTypedArray()
    )

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)

    public class Outputs(
        stepId: String,
    ) : Action.Outputs(stepId) {
        /**
         * Link to the Build Scan® generated by a Gradle build. Note that this output applies to a
         * Step executing Gradle, not to the `setup-gradle` Step itself.
         */
        public val buildScanUrl: String = "steps.$stepId.outputs.build-scan-url"

        /**
         * Path to the GitHub Dependency Graph snapshot file generated by a Gradle build. Note that
         * this output applies to a Step executing Gradle, not to the `setup-gradle` Step itself.
         */
        public val dependencyGraphFile: String = "steps.$stepId.outputs.dependency-graph-file"

        /**
         * Version of Gradle that was setup by the action
         */
        public val gradleVersion: String = "steps.$stepId.outputs.gradle-version"
    }
}

