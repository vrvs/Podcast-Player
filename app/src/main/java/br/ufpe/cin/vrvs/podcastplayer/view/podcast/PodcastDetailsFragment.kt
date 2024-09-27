package br.ufpe.cin.vrvs.podcastplayer.view.podcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.startPodcastDownload
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.cancel
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.getDownloadCompleteBroadcastReceiver
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.getDownloadManager
import br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService
import br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.Companion.PAUSE_ACTION
import br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.Companion.PLAY_ACTION
import br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.PodcastPlayPauseListener
import br.ufpe.cin.vrvs.podcastplayer.view.component.episode.EpisodeListComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.error.ErrorComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent.Type.DOWNLOAD
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent.Type.DOWNLOADED
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent.Type.PAUSE
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent.Type.PLAY
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent.Type.STOP
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.loading.LoadingComponent
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.ext.android.inject

class PodcastDetailsFragment : Fragment(R.layout.fragment_podcast_details) {

    private val podcastRepository: PodcastRepository by inject()
    val args: PodcastDetailsFragmentArgs by navArgs()
    private lateinit var root: LinearLayout
    private lateinit var list: EpisodeListComponent
    private lateinit var error: ErrorComponent
    private lateinit var loading: LoadingComponent
    private lateinit var imageComponent: ImageComponent
    private lateinit var subscribeButton: FloatingActionButton
    private lateinit var title: TextView
    private lateinit var author: TextView
    private lateinit var description: TextView
    private var downloadManager: DownloadManager? = null
    private val broadcastReceiversMaps = mutableMapOf<Long, BroadcastReceiver>()

    private var podcastPlayerService: PodcastPlayerService? = null
    private var isBound = false
    private lateinit var listener: PodcastPlayPauseListener
    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            podcastPlayerService = null
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as PodcastPlayerService.PodcastBinder
            podcastPlayerService = binder.service
            podcastPlayerService?.loadListener(listener)
        }
    }

    init {
        listener = object : PodcastPlayPauseListener {
            override fun playPausePressed(podcastId: String?) {
                podcastId?.let {
                    if (it == args.id) {
                        podcastRepository.getPodcast(it).observe(viewLifecycleOwner, podcastUpdateObserver)
                    }
                }
            }
        }
    }

    private val podcastObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> showPodcast(it.data)
            is Result.Loading -> showLoading()
            is Result.Error -> context?. let { c -> showError(Utils.getString(c, it.error)) }
        }
    }

    private val podcastUpdateObserver = Observer<Result<Podcast>> {
        when (it) {
            is Result.Success -> showPodcast(it.data)
            is Result.Loading -> {}
            is Result.Error -> context?. let { c -> showError(Utils.getString(c, it.error)) }
        }
    }

    private val subsObserver = Observer<Result<String>> {
        when (it) {
            is Result.Success -> podcastRepository.getPodcast(it.data).observe(viewLifecycleOwner, podcastUpdateObserver)
            is Result.Loading -> showLoading()
            is Result.Error -> context?. let { c -> showError(Utils.getString(c, it.error)) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val musicServiceIntent = Intent(context, PodcastPlayerService::class.java)
        context?.startService(musicServiceIntent)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        root = view.findViewById(R.id.root)
        imageComponent = view.findViewById(R.id.image_cover)
        list = view.findViewById(R.id.episodes_list)
        error = view.findViewById(R.id.error_screen)
        loading = view.findViewById(R.id.loading_screen)
        subscribeButton = view.findViewById(R.id.subscribe_button)
        title = view.findViewById(R.id.title)
        author = view.findViewById(R.id.author)
        description = view.findViewById(R.id.description)
        downloadManager = context?.getDownloadManager()

        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> podcastRepository.getPodcast(args.id).observe(viewLifecycleOwner, podcastObserver)
                ErrorComponent.Button.CLOSE -> findNavController().popBackStack()
            }
        })
        list.buttonClicked.observe(viewLifecycleOwner, Observer {
            val episode = it.first
            when (it.second) {
                PLAY -> {
                    podcastPlayerService?.loadEpisode(episode,  args.id)
                    context?.sendBroadcast(Intent().apply { action = PLAY_ACTION })
                }
                PAUSE -> {
                    context?.sendBroadcast(Intent().apply { action = PAUSE_ACTION })
                }
                DOWNLOAD -> {
                    context?.let { context ->
                        context.startPodcastDownload(
                            episode.audioUrl,
                            episode.audioType,
                            episode.title,
                            episode.id,
                            episode.podcastId
                        )?.let { downloadId ->
                            broadcastReceiversMaps[downloadId] = getDownloadCompleteBroadcastReceiver(downloadId) {
                                updateInterface(episode.podcastId)
                            }
                            context.registerReceiver(
                                broadcastReceiversMaps[downloadId],
                                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                            )
                        }
                    }
                }
                DOWNLOADED, STOP -> {
                    episode.downloadId?.let { downloadId ->
                        downloadManager?.cancel(
                            episode.podcastId,
                            episode.id,
                            downloadId
                        )
                    }
                    updateInterface(episode.podcastId)
                }
                else -> {}
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(context, PodcastPlayerService::class.java)
            isBound = context?.bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE) ?: false
        }
        podcastPlayerService?.loadListener(listener)
        podcastRepository.getPodcast(args.id).observe(viewLifecycleOwner, podcastObserver)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            context?.unbindService(sConn)
            isBound = false
        }
        podcastPlayerService?.loadListener(null)
    }

    override fun onDestroy() {
        downloadManager = null
        broadcastReceiversMaps.values.forEach {
            context?.unregisterReceiver(it)
        }
        super.onDestroy()
    }

    private fun updateInterface(id: String) {
        podcastRepository.getPodcast(id).observe(viewLifecycleOwner, podcastUpdateObserver)
    }

    private fun showPodcast(podcast: Podcast) {
        showView(showMain = true, showLoading = false, showError = false)
        imageComponent.render(podcast.imageUrl)
        subscribeButton.apply {
            backgroundTintList = ResourcesCompat.getColorStateList(context.resources, if (podcast.subscribed) R.color.red else R.color.green,null)
            setImageResource(if (podcast.subscribed) R.drawable.ic_remove_white_24dp else R.drawable.ic_add_white_24dp)
            setOnClickListener {
                if (podcast.subscribed)
                    podcastRepository.unsubscribePodcast(podcast.id).observe(viewLifecycleOwner, subsObserver)
                else
                    podcastRepository.subscribePodcast(podcast.id).observe(viewLifecycleOwner, subsObserver)
            }

        }
        title.text = podcast.title
        author.text = podcast.author
        description.text = podcast.description
        list.changeDataSet(podcast.episodes)
    }

    private fun showError(error: String) {
        showView(showMain = false, showLoading = false, showError = true)
    }

    private fun showLoading() {
        showView(showMain = false, showLoading = true, showError = false)
    }

    private fun showView(showMain: Boolean, showLoading: Boolean, showError: Boolean) {
        showView(root, showMain)
        showView(loading, showLoading)
        showView(error, showError)
    }

    private fun showView(view: View, showView: Boolean) {
        view.visibility = if (showView) View.VISIBLE else View.GONE
    }
}