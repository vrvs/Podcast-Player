package br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference

interface PodcastSharedPreferences {
    var podcastEpisodeId: String?
    fun clearPodcastEpisodeId()
    var podcastTime: Long?
    fun clearPodcastTime()
}