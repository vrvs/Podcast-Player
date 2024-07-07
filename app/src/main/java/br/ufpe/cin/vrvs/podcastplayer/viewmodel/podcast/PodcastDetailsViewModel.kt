package br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcast

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.BaseViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PodcastDetailsViewModel(context: Context) : BaseViewModel(), KoinComponent {

    private val podcastRepository: PodcastRepository by inject()

    private val _podcast =  MutableLiveData<Podcast>()
    val podcast: LiveData<Podcast> = _podcast

    private var answer: LiveData<Result<Podcast>>? = null
    private val podcastObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> postValues(loading = false, error = "", podcast = it.data)
            is Result.Loading -> postValues(loading = true, error = "", podcast = null)
            is Result.Error -> postValues(loading = false, error = Utils.getString(context, it.error), podcast = null)
        }
    }

    private var answerUpdate: LiveData<Result<Podcast>>? = null
    private val podcastUpdateObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> postValues(loading = false, error = "", podcast = it.data)
            is Result.Loading -> {}
            is Result.Error -> postValues(loading = false, error = Utils.getString(context, it.error), podcast = null)
        }
    }

    private var answerSubs: LiveData<Result<String>>? = null
    private val subsObserver = Observer<Result<String>> {
        when (it) {
            is Result.Success -> getPodcast(it.data)
            is Result.Loading -> postValues(loading = true, error = "", podcast = null)
            is Result.Error -> postValues(loading = false, error = Utils.getString(context, it.error), podcast = null)
        }
    }

    fun getPodcast(id: String) {
        postValues(loading = false, error = "", podcast = null)
        answer?.removeObserver(podcastObserver)
        answer = podcastRepository.getPodcast(id)
        answer?.observeForever(podcastObserver)
    }

    fun updatePodcast(id: String) {
        postValues(loading = false, error = "", podcast = null)
        answerUpdate?.removeObserver(podcastUpdateObserver)
        answerUpdate = podcastRepository.getPodcast(id)
        answerUpdate?.observeForever(podcastUpdateObserver)
    }

    fun subscribePodcast(id: String) {
        postValues(loading = false, error = "", podcast = null)
        answerSubs?.removeObserver(subsObserver)
        answerSubs = podcastRepository.subscribePodcast(id)
        answerSubs?.observeForever(subsObserver)
    }

    fun unsubscribePodcast(id: String) {
        postValues(loading = false, error = "", podcast = null)
        answerSubs?.removeObserver(subsObserver)
        answerSubs = podcastRepository.unsubscribePodcast(id)
        answerSubs?.observeForever(subsObserver)
    }

    override fun onCleared() {
        super.onCleared()
        answer?.removeObserver(podcastObserver)
        answerSubs?.removeObserver(subsObserver)
    }

    private fun postValues(loading: Boolean, error: String, podcast: Podcast?) {
        super.protectedLoading.postValue(loading)
        protectedError.postValue(error)
        podcast?.let{ _podcast.postValue(it) }
    }
}