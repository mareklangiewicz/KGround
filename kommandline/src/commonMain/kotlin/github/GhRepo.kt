package pl.mareklangiewicz.kommand.github

/**
 * Display the description and the README of a GitHub repository.
 * With no argument, the repository for the current directory is displayed.
 * @param repoPath Select another repository using the [HOST/]OWNER/REPO format.
 * @param branch Non-null means: View a specific branch of the repository.
 * @param web True means open repo in browser instead of printing info to stdout.
 */
fun ghRepoView(
    repoPath: String? = null,
    branch: String? = null,
    web: Boolean = false,
    init: GhRepoView.() -> Unit = {}
) =
    GhRepoView().apply { repoPath?.let { +it }; branch?.let { -Branch(it) }; web && -Web; init() }

fun ghRepoList(
    owner: String? = null,
    vararg useNamedArgs: Unit,
    limit: Int? = null,
    onlyLanguage: String? = null,
    onlyTopic: String? = null,
    onlyArchived: Boolean = false,
    onlyNotArchived: Boolean = false,
    onlyForks: Boolean = false,
    onlyNotForks: Boolean = false,
    onlyPublic: Boolean = false,
    onlyPrivate: Boolean = false,
    onlyInternal: Boolean = false,
    init: GhRepoList.() -> Unit = {}
) =
    GhRepoList().apply {
        owner?.let { +it }
        limit?.let { -Limit(it) }
        onlyLanguage?.let { -Language(it) }
        onlyTopic?.let { -Topic(it) }
        onlyArchived && -Archived
        onlyNotArchived && -NoArchived
        onlyForks && -Fork
        onlyNotForks && -Source
        onlyPublic && -Visibility("public")
        onlyPrivate && -Visibility("private")
        onlyInternal && -Visibility("internal")
        init()
    }

/**
 * For each repo, each output field is returned in separate line.
 * If no fields are provided, just output available fields. No actual data.
 */
fun GhRepoList.outputFields(vararg fields: String) = apply {
    -Json(*fields)
    fields.isEmpty() && return@apply
    -Jq(fields.joinToString(",", prefix = ".[]|") { ".$it" })
}
