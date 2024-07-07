package br.ufpe.cin.vrvs.podcastplayer.ui.presenter.search

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.search.PodcastSearchContract
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

class PodcastSearchPresenter(private val view: PodcastSearchContract.View) : PodcastSearchContract.Presenter, KoinComponent {

    private val podcastRepository: PodcastRepository by inject()
    private lateinit var context: WeakReference<Context>

    private var answer: LiveData<Result<List<Podcast>>>? = null
    private val podcastObserver = Observer<Result<List<Podcast>>> {
        when (it) {
            is Result.Success -> view.showPodcasts(it.data)
            is Result.Loading -> view.showLoading()
            is Result.Error -> context.get()?. let { c -> view.showError(Utils.getString(c, it.error))  }
        }
    }

    override fun searchPodcasts(query: String) {
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.searchPodcast(query)
        answer?.observeForever(podcastObserver)
    }

    override fun getPodcastFeed() {
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.getPodcastFeed()
        answer?.observeForever(podcastObserver)
    }

    override fun subscribe(context: Context?) {
        context?.let { this.context = WeakReference(context) }
    }

    override fun unsubscribe() {
        this.context.clear()
        answer?.removeObserver(podcastObserver)
    }
}