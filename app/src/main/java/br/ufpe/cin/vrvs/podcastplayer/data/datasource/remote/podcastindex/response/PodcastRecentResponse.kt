package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response

import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast.*
import com.google.gson.annotations.SerializedName

open class PodcastRecentResponse {

    @SerializedName("id")
    var id: Int = -1

    @SerializedName("title")
    var title: String? = null

    @SerializedName("image")
    var imageUrl: String? = null

    @SerializedName("categories")
    var categories: Map<String, String>? = emptyMap()
}

class PodcastsRecentResponse {

    @SerializedName("feeds")
    lateinit var feeds: List<PodcastRecentResponse>
}

fun Companion.toPodcast(podcastRecentResponse: PodcastRecentResponse) = Podcast(
    id = podcastRecentResponse.id.toString(),
    title = podcastRecentResponse.title.orEmpty(),
    imageUrl = podcastRecentResponse.imageUrl.orEmpty(),
    categories = podcastRecentResponse.categories ?: emptyMap()
)