package br.ufpe.cin.vrvs.podcastplayer.ui.presenter.podcast

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.podcast.PodcastDetailsContract

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

class PodcastDetailsPresenter(private val view: PodcastDetailsContract.View) : PodcastDetailsContract.Presenter, KoinComponent {

    private val podcastRepository: PodcastRepository by inject()
    private lateinit var context: WeakReference<Context>

    private var answer: LiveData<Result<Podcast>>? = null
    private val podcastObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> view.showPodcast(it.data)
            is Result.Loading -> view.showLoading()
            is Result.Error -> context.get()?. let { c -> view.showError(Utils.getString(c, it.error)) }
        }
    }

    private var answerUpdate: LiveData<Result<Podcast>>? = null
    private val podcastUpdateObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> view.showPodcast(it.data)
            is Result.Loading -> {}
            is Result.Error -> context.get()?. let { c -> view.showError(Utils.getString(c, it.error)) }
        }
    }

    private var answerSubs: LiveData<Result<String>>? = null
    private val subsObserver = Observer<Result<String>> {
        when (it) {
            is Result.Success -> getPodcast(it.data)
            is Result.Loading -> view.showLoading()
            is Result.Error -> context.get()?. let { c -> view.showError(Utils.getString(c, it.error)) }
        }
    }

    override fun getPodcast(id: String) {
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.getPodcast(id)
        answer?.observeForever(podcastObserver)
    }

    override fun updatePodcast(id: String) {
        answerUpdate?.removeObserver(podcastUpdateObserver)
        answerUpdate = podcastRepository.getPodcast(id)
        answerUpdate?.observeForever(podcastUpdateObserver)
    }

    override fun subscribePodcast(id: String) {
        answerSubs?.removeObserver(subsObserver)
        answerSubs = podcastRepository.subscribePodcast(id)
        answerSubs?.observeForever(subsObserver)
    }

    override fun unsubscribePodcast(id: String) {
        answerSubs?.removeObserver(subsObserver)
        answerSubs = podcastRepository.unsubscribePodcast(id)
        answerSubs?.observeForever(subsObserver)
    }

    override fun subscribe(context: Context?) {
        context?.let { this.context = WeakReference(context) }
    }

    override fun unsubscribe() {
        this.context.clear()
        answer?.removeObserver(podcastObserver)
        answerUpdate?.removeObserver(podcastObserver)
        answerSubs?.removeObserver(subsObserver)
    }
}