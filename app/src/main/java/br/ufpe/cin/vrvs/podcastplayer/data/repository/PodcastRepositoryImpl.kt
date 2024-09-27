package br.ufpe.cin.vrvs.podcastplayer.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.PodcastDatabase
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.EpisodePersistedDownloaded
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.EpisodePersistedPlaying
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.fromEpisode
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.fromPodcast
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.toEpisode
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.table.toPodcast
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference.PodcastSharedPreferences
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.PodcastIndexApi
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.ApiErrorResponse
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.toEpisode
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response.toPodcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.data.model.ErrorModel
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import retrofit2.await
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.Executors

@KoinApiExtension
class PodcastRepositoryImpl : PodcastRepository, KoinComponent {

    private val podcastDatabase: PodcastDatabase by inject()
    private val podcastIndexApi: PodcastIndexApi by inject()
    private val podcastSharedPreferences: PodcastSharedPreferences by inject()

    private val scope = CoroutineScope(Dispatchers.IO + NonCancellable)
    private val playedContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun getPlayedPodcast(): LiveData<Result<Pair<String?, Long?>>> {
        val podcastPlayedSong: MutableLiveData<Result<Pair<String?, Long?>>>  = MutableLiveData()
        scope.launch(playedContext) {
            podcastPlayedSong.postValue(Result.Loading)
            try {
                val pair = Pair(podcastSharedPreferences.podcastEpisodeId, podcastSharedPreferences.podcastTime)
                podcastPlayedSong.postValue(Result.Success(pair))
            } catch (e: Exception) {
                podcastPlayedSong.postValue(Result.Error(mapException(e)))
            }
        }
        return podcastPlayedSong
    }

    override fun updatePlayedPodcast(id: String, time: Long) {
        scope.launch(playedContext) {
            podcastSharedPreferences.podcastEpisodeId = id
            podcastSharedPreferences.podcastTime = time
        }
    }

    override fun updatePlayingPodcast(id: String, playing: Boolean) {
        scope.launch(playedContext) {
            podcastDatabase.podcastDao().updatePlaying(EpisodePersistedPlaying(id, playing))
        }
    }

    override fun clearPlayedPodcast() {
        scope.launch(playedContext) {
            podcastSharedPreferences.clearPodcastEpisodeId()
            podcastSharedPreferences.clearPodcastTime()
        }
    }


    override fun getPodcastFeed(): LiveData<Result<List<Podcast>>> {
        val podcastsFeed: MutableLiveData<Result<List<Podcast>>>  = MutableLiveData()
        scope.launch {
            podcastsFeed.postValue(Result.Loading)
            try {
                val result = podcastIndexApi.getRecentFeed(30).await()
                podcastsFeed.postValue(Result.Success(result.feeds.map {
                    Podcast.toPodcast(it)
                }))
            } catch (e: Exception) {
                podcastsFeed.postValue(Result.Error(mapException(e)))
            }
        }
        return podcastsFeed
    }

    override fun searchPodcast(query: String): LiveData<Result<List<Podcast>>> {
        val podcastsFeed: MutableLiveData<Result<List<Podcast>>>  = MutableLiveData()
        scope.launch {
            podcastsFeed.postValue(Result.Loading)
            try {
                val result = podcastIndexApi.searchPodcasts(query).await()
                podcastsFeed.postValue(Result.Success(result.feeds.map {
                    Podcast.toPodcast(it)
                }))
            } catch (e: Exception) {
                podcastsFeed.postValue(Result.Error(mapException(e)))
            }
        }
        return podcastsFeed
    }

    override fun getSubscribedPodcast(): LiveData<Result<List<Podcast>>> {
        val podcastsSubscribed: MutableLiveData<Result<List<Podcast>>> = MutableLiveData()
        scope.launch {
            podcastsSubscribed.postValue(Result.Loading)
            try {
                val result = podcastDatabase.podcastDao().getPodcasts()
                podcastsSubscribed.postValue(Result.Success(result.filter {
                    it.subscribed
                }.map {
                    Podcast.toPodcast(it)
                }))
            } catch (e: Exception) {
                podcastsSubscribed.postValue(Result.Error(mapException(e)))
            }
        }
        return podcastsSubscribed
    }

    override fun getPodcast(id: String): LiveData<Result<Podcast>> {
        val podcast: MutableLiveData<Result<Podcast>> = MutableLiveData()
        scope.launch {
            podcast.postValue(Result.Loading)
            try {
                getPodcastAwait(id, podcast)
            } catch (e: Exception) {
                podcast.postValue(Result.Error(mapException(e)))
            }
        }
        return podcast
    }

    override fun getEpisode(id: String): LiveData<Result<Episode>> {
        val episode: MutableLiveData<Result<Episode>> = MutableLiveData()
        scope.launch {
            episode.postValue(Result.Loading)
            try {
                episode.postValue(Result.Success(getEpisodeAwait(id)))
            } catch (e: Exception) {
                episode.postValue(Result.Error(mapException(e)))
            }
        }
        return episode
    }

    override fun updateDownloadedEpisode(episodeId: String, podcastId: String, downloadId: Long?, path: String): LiveData<Result<Boolean>> {
        val updated: MutableLiveData<Result<Boolean>> = MutableLiveData()
        scope.launch {
            updated.postValue(Result.Loading)
            try {
                val db = podcastDatabase.podcastDao()
                when {
                    db.hasEpisode(episodeId) -> {
                        db.updateDownloaded(EpisodePersistedDownloaded(episodeId,downloadId, path))
                    }
                    db.hasPodcast(podcastId) -> {
                        getPodcastAwait(podcastId)
                        db.updateDownloaded(EpisodePersistedDownloaded(episodeId,downloadId, path))
                    }
                    else -> {
                        subscribePodcastAwait(podcastId)
                        db.updateDownloaded(EpisodePersistedDownloaded(episodeId,downloadId, path))
                    }
                }
                if (downloadId == null) {
                    deleteFile(path)
                }
            } catch (e: Exception) {
                updated.postValue(Result.Error(mapException(e)))
            }
        }
        return updated
    }

    override fun subscribePodcast(id: String): LiveData<Result<String>> {
        val subscribed: MutableLiveData<Result<String>> = MutableLiveData()
        scope.launch {
            subscribed.postValue(Result.Loading)
            try {
                subscribed.postValue(Result.Success(subscribePodcastAwait(id)))
            } catch (e: Exception) {
                subscribed.postValue(Result.Error(mapException(e)))
            }
        }
        return subscribed
    }

    override fun unsubscribePodcast(id: String): LiveData<Result<String>> {
        val unsubscribed: MutableLiveData<Result<String>> = MutableLiveData()
        scope.launch {
            unsubscribed.postValue(Result.Loading)
            try {
                unsubscribed.postValue(Result.Success(unsubscribePodcastAwait(id)))
            } catch (e: Exception) {
                unsubscribed.postValue(Result.Error(mapException(e)))
            }
        }
        return unsubscribed
    }

    private suspend fun unsubscribePodcastAwait(id: String): String {
        val db = podcastDatabase.podcastDao()
        db.clearPodcast(id)
        val episodes = db.getPodcastEpisodes(id)
        db.clearPodcastEpisodes(id)
        episodes.filter {
            it.path != ""
        }.forEach {
            deleteFile(it.path)
        }
        return id
    }

    private fun deleteFile(path: String) {
        val file = File(path).canonicalFile
        if (file.exists()) {
            file.delete()
        }
    }

    private suspend fun subscribePodcastAwait(id: String): String {
        val podcast = getPodcastAwait(id)
        val db = podcastDatabase.podcastDao()
        if (!db.hasPodcast(id)) {
            podcast.subscribed = true
            db.insertPodcast(Podcast.fromPodcast(podcast))
            podcast.episodes.forEach {
                db.insertEpisode(Episode.fromEpisode(it))
            }
        }
        return id
    }

    private suspend fun getPodcastAwait(id: String, liveData: MutableLiveData<Result<Podcast>>? = null): Podcast {
        val db = podcastDatabase.podcastDao()
        val answer: Podcast
        if (db.hasPodcast(id)) {
            val podcastResult = db.getPodcast(id)
            var episodeResult = db.getPodcastEpisodes(id)
            answer = Podcast.toPodcast(podcastResult)
            answer.episodes = episodeResult.map {
                Episode.toEpisode(it)
            }
            liveData?.postValue(Result.Success(answer))
            try {
                val episodeResultApi = podcastIndexApi.getEpisodes(id).await()
                val newEpisodes = episodeResultApi.episodes.filter { episodeRemote ->
                    !answer.episodes.map { episodePersisted ->
                        episodePersisted.id
                    }.contains(episodeRemote.id.toString())
                }
                if (newEpisodes.isNotEmpty()) {
                    newEpisodes.map {
                        db.insertEpisode(Episode.fromEpisode(Episode.toEpisode(it)))
                    }
                }
                episodeResult = db.getPodcastEpisodes(id)
                answer.episodes = episodeResult.map {
                    Episode.toEpisode(it)
                }
                liveData?.postValue(Result.Success(answer))
            } catch (e: Exception) {
                // Do nothing - use local values
            }
        } else {
            val podcastResult = podcastIndexApi.getPodcast(id.toInt()).await()
            val episodeResult = podcastIndexApi.getEpisodes(id).await()
            answer = Podcast.toPodcast(podcastResult.feed)
            answer.episodes = episodeResult.episodes.map {
                Episode.toEpisode(it)
            }
            liveData?.postValue(Result.Success(answer))
        }
        return answer
    }

    private suspend fun getEpisodeAwait(id: String): Episode {
        val db = podcastDatabase.podcastDao()
        val answer: Episode
        answer = if (db.hasEpisode(id)) {
            val episodeResult = db.getEpisode(id)
            Episode.toEpisode(episodeResult)
        } else {
            val episodeResult = podcastIndexApi.getEpisode(id.toInt()).await()
            Episode.toEpisode(episodeResult.episode)
        }
        return answer
    }
    
    private fun mapException(e: Exception): ErrorModel {
        when (e) {
            is HttpException -> {
                e.response()?.errorBody()?.string()?.let {
                    val ans = Gson().fromJson<ApiErrorResponse>(it, ApiErrorResponse::class.javaObjectType)
                    return ErrorModel(ans?.description)
                }
            }
            is SocketTimeoutException, is ConnectException, is UnknownHostException-> {
                return ErrorModel(descriptionRes = R.string.connection_error)
            }
        }
        return ErrorModel()
    }
}