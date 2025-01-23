package kill.online.helper.data

data class Release(
    val url: String,
    val assets_url: String,
    val upload_url: String,
    val html_url: String,
    val id: Long,
    val author: Author,
    val node_id: String,
    val tag_name: String,
    val target_commitish: String,
    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val created_at: String,
    val published_at: String,
    val assets: List<Asset>,
    val tarball_url: String,
    val zipball_url: String,
    val body: String,
    val reactions: Reactions
)

data class Author(
    val login: String,
    val id: Long,
    val node_id: String,
    val avatar_url: String,
    val gravatar_id: String?,
    val url: String,
    val html_url: String,
    val followers_url: String,
    val following_url: String,
    val gists_url: String,
    val starred_url: String,
    val subscriptions_url: String,
    val organizations_url: String,
    val repos_url: String,
    val events_url: String,
    val received_events_url: String,
    val type: String,
    val user_view_type: String,
    val site_admin: Boolean
)

data class Asset(
    val url: String,
    val id: Long,
    val node_id: String,
    val name: String,
    val label: String?,
    val uploader: Author,
    val content_type: String,
    val state: String,
    val size: Long,
    val download_count: Int,
    val created_at: String,
    val updated_at: String,
    val browser_download_url: String
)

data class Reactions(
    val url: String,
    val total_count: Int,
    val plus_one: Int,
    val minus_one: Int,
    val laugh: Int,
    val hooray: Int,
    val confused: Int,
    val heart: Int,
    val rocket: Int,
    val eyes: Int
)

const val AUTHOR = "majs007"
const val REPO = "kill_online_helper"
const val GITHUB_RELEASES_URL = "https://github.com/${AUTHOR}/${REPO}/releases"
const val API_GITHUB_RELEASES_LATEST =
    "https://api.github.com/repos/${AUTHOR}/${REPO}/releases/latest"

