package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response

import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast.*
import com.google.gson.annotations.SerializedName

class PodcastCompleteResponse: PodcastRecentResponse() {

    @SerializedName("author")
    var author: String? = null

    @SerializedName("description")
    var description: String? = null
}

class PodcastsSearchResponse {

    @SerializedName("feeds")
    lateinit var feeds: List<PodcastCompleteResponse>
}

class PodcastByIdResponse {

    @SerializedName("feed")
    lateinit var feed: PodcastCompleteResponse
}

fun Companion.toPodcast(podcastCompleteResponse: PodcastCompleteResponse) : Podcast {
    val result = Podcast.toPodcast(podcastCompleteResponse as PodcastRecentResponse)
    result.author = podcastCompleteResponse.author.orEmpty()
    result.description = podcastCompleteResponse.description.orEmpty()
    return result
}