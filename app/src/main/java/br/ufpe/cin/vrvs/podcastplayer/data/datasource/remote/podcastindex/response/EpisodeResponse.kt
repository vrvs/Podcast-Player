package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response

import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode.*
import com.google.gson.annotations.SerializedName

class EpisodeResponse {

    @SerializedName("id")
    var id: Int = -1

    @SerializedName("feedId")
    var podcastId: Int = -1

    @SerializedName("title")
    var title: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("enclosureUrl")
    var audioUrl: String? = null

    @SerializedName("enclosureType")
    var audioType: String? = null

    @SerializedName("feedImage")
    var imageUrl: String? = null

    @SerializedName("datePublished")
    var datePublished: Long = 0L

    @SerializedName("duration")
    var duration: Int = 0

    @SerializedName("episode")
    var episode: Int = -1

    @SerializedName("season")
    var season: Int = -1
}

class EpisodesResponse {

    @SerializedName("items")
    lateinit var episodes: List<EpisodeResponse>
}

class EpisodeByIdResponse {

    @SerializedName("episode")
    lateinit var episode: EpisodeResponse
}

fun Companion.toEpisode(episodeResponse: EpisodeResponse) = Episode(
    id = episodeResponse.id.toString(),
    podcastId = episodeResponse.podcastId.toString(),
    title = episodeResponse.title.orEmpty(),
    description = episodeResponse.description.orEmpty(),
    audioUrl = episodeResponse.audioUrl.orEmpty(),
    audioType = episodeResponse.audioType.orEmpty(),
    imageUrl = episodeResponse.imageUrl.orEmpty(),
    datePublished = episodeResponse.datePublished,
    duration = episodeResponse.duration,
    episode = episodeResponse.episode,
    season = episodeResponse.season
)