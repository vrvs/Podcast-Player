package br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcasts.subscribed

import android.content.Context
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcasts.BasePodcastsViewModel

class SubscribedPodcastsViewModel(context: Context) : BasePodcastsViewModel(context) {

    fun refreshSubscribedPodcast() {
        postValues(loading = false, error = "", podcasts = emptyList())
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.getSubscribedPodcast()
        answer?.observeForever(podcastObserver)
    }
}