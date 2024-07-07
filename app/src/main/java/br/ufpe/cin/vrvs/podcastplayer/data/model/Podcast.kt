package br.ufpe.cin.vrvs.podcastplayer.data.model

data class Podcast(
    var id: String,
    var author: String? = null,
    var description: String? = null,
    var title: String,
    var imageUrl: String,
    var categories: Map<String, String>,
    var subscribed: Boolean = false,
    var episodes: List<Episode> = emptyList()
) {
    companion object

    fun getEpisodesSorted(): List<Episode> = episodes.sortedByDescending { episode -> episode.datePublished }
}