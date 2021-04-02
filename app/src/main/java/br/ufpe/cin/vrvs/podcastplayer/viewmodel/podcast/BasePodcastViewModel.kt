package br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.BaseViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
abstract class BasePodcastViewModel : BaseViewModel(), KoinComponent {

    protected val podcastRepository: PodcastRepository by inject()

    private val _podcasts =  MutableLiveData<List<Podcast>>()
    val podcasts: LiveData<List<Podcast>> = _podcasts

    protected var answer: LiveData<Result<List<Podcast>>>? = null
    protected val podcastObserver = Observer<Result<List<Podcast>>> {
        when (it) {
            is Result.Success -> postValues(loading = false, error = false, podcasts = it.data)
            is Result.Loading -> postValues(loading = true, error = false, podcasts = emptyList())
            is Result.Error -> postValues(loading = false, error = true, podcasts = emptyList())
        }
    }

    override fun onCleared() {
        super.onCleared()
        answer?.removeObserver(podcastObserver)
    }

    protected fun postValues(loading: Boolean, error: Boolean, podcasts: List<Podcast>) {
        super.protectedLoading.postValue(loading)
        protectedError.postValue(error)
        _podcasts.postValue(podcasts)
    }
}