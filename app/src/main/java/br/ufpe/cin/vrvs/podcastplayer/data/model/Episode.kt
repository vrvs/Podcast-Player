package br.ufpe.cin.vrvs.podcastplayer.data.model

data class Episode(
    var id: String,
    var podcastId: String,
    var title: String,
    var description: String,
    var audioUrl: String,
    var audioType: String,
    var imageUrl: String,
    var datePublished: Long,
    var duration: Int,
    var episode: Int,
    var season: Int,
    var downloadId: Long? = null,
    var path: String = "",
    var playing: Boolean = false
) {
    companion object
}