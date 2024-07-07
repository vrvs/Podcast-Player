package br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcasts.search

import android.content.Context
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcasts.BasePodcastsViewModel

class SearchPodcastsViewModel(context: Context) : BasePodcastsViewModel(context) {

    fun searchPodcasts(query: String) {
        postValues(loading = false, error = "", podcasts = emptyList())
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.searchPodcast(query)
        answer?.observeForever(podcastObserver)
    }

    fun getPodcastsFeed() {
        postValues(loading = false, error = "", podcasts = emptyList())
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.getPodcastFeed()
        answer?.observeForever(podcastObserver)
    }
}