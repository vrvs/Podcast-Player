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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.databinding.FragmentPodcastDetailsBinding
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
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcast.PodcastDetailsViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class PodcastDetailsFragment : Fragment(R.layout.fragment_podcast_details) {

    private val pdViewModel: PodcastDetailsViewModel by viewModel()
    val args: PodcastDetailsFragmentArgs by navArgs()
    private var mBinding: FragmentPodcastDetailsBinding? = null
    private lateinit var list: EpisodeListComponent
    private lateinit var error: ErrorComponent
    private lateinit var imageComponent: ImageComponent
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
                        pdViewModel.updatePodcast(it)
                    }
                }
            }
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
        imageComponent = view.findViewById(R.id.image_cover)
        list = view.findViewById(R.id.episodes_list)
        error = view.findViewById(R.id.error_screen)
        downloadManager = context?.getDownloadManager()

        mBinding = FragmentPodcastDetailsBinding.bind(view).apply {
            viewModel = pdViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        pdViewModel.podcast.observe(viewLifecycleOwner, Observer {
            imageComponent.render(it.imageUrl)
            list.changeDataSet(it.getEpisodesSorted())
        })
        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> pdViewModel.getPodcast(args.id)
                ErrorComponent.Button.CLOSE -> findNavController().popBackStack()
            }
        })
        pdViewModel.error.observe(viewLifecycleOwner, Observer {
            error.errorText(it)
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

        pdViewModel.getPodcast(args.id)
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(context, PodcastPlayerService::class.java)
            isBound = context?.bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE) ?: false
        }
        podcastPlayerService?.loadListener(listener)
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
        mBinding = null
        downloadManager = null
        broadcastReceiversMaps.values.forEach {
            context?.unregisterReceiver(it)
        }
        super.onDestroy()
    }

    private fun updateInterface(id: String) {
        pdViewModel.updatePodcast(id)
    }
}