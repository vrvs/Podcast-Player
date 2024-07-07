package br.ufpe.cin.vrvs.podcastplayer.ui.contract.search

import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BasePresenter
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BaseView

interface PodcastSearchContract {

    interface View : BaseView<Presenter> {
        fun showPodcasts(podcasts: List<Podcast>)
    }

    interface Presenter : BasePresenter {
        fun searchPodcasts(query: String)
        fun getPodcastFeed()
    }
}