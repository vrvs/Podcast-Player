package br.ufpe.cin.vrvs.podcastplayer.ui.contract.home

import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BasePresenter
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BaseView

interface PodcastHomeContract {

    interface View: BaseView<Presenter> {
        fun showSubscribedPodcasts(podcasts: List<Podcast>)
    }

    interface Presenter: BasePresenter {
        fun refreshSubscribedPodcasts()
    }
}