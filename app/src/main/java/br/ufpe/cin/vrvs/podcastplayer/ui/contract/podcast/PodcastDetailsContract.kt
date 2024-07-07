package br.ufpe.cin.vrvs.podcastplayer.ui.contract.podcast

import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BasePresenter
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.BaseView

interface PodcastDetailsContract {

    interface View : BaseView<Presenter> {
        fun showPodcast(podcast: Podcast)
    }

    interface Presenter : BasePresenter {
        fun getPodcast(id: String)
        fun updatePodcast(id: String)
        fun subscribePodcast(id: String)
        fun unsubscribePodcast(id: String)
    }
}