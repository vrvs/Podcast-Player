package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex

import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.EpisodeByIdResponse
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.EpisodesResponse
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.PodcastByIdResponse
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.PodcastsRecentResponse
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.PodcastsSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.QueryName

interface PodcastIndexApi {

    companion object {
        const val URL = "https://api.podcastindex.org/api/1.0/"
    }

    @Headers("Accept: application/json")
    @GET("recent/feeds")
    fun getRecentFeed(
        @Query("max") max: Int,
        @QueryName pretty: String = "pretty"
    ) : Call<PodcastsRecentResponse>

    @Headers("Accept: application/json")
    @GET("podcasts/byfeedid")
    fun getPodcast(
        @Query("id") id: Int,
        @QueryName pretty: String = "pretty"
    ) : Call<PodcastByIdResponse>

    @Headers("Accept: application/json")
    @GET("search/byterm")
    fun searchPodcasts(
        @Query("q") query: String,
        @QueryName pretty: String = "pretty"
    ) : Call<PodcastsSearchResponse>

    @Headers("Accept: application/json")
    @GET("episodes/byfeedid")
    fun getEpisodes(
        @Query("id") podcastId: String,
        @QueryName pretty: String = "pretty"
    ) : Call<EpisodesResponse>

    @Headers("Accept: application/json")
    @GET("episodes/byid")
    fun getEpisode(
        @Query("id") episodeId: Int,
        @QueryName pretty: String = "pretty"
    ) : Call<EpisodeByIdResponse>
}