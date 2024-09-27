package br.ufpe.cin.vrvs.podcastplayer.services.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.media.app.NotificationCompat.MediaStyle
import br.ufpe.cin.vrvs.podcastplayer.MainActivity
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.FileInputStream
import java.lang.Exception


class PodcastPlayerService : LifecycleService() {

    companion object {
        private const val CHANNEL_ID = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
        const val REQUEST_CODE = 1234
        const val PODCAST_ID = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.PODCAST_ID"
        const val TAG = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.TAG"
        const val PLAY_ACTION = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.PLAY_ACTION"
        const val PAUSE_ACTION = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.PAUSE_ACTION"
        const val STOP_ACTION = "br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.STOP_ACTION"
    }

    private val podcastRepository: PodcastRepository by inject()
    private lateinit var mPlayer: MediaPlayer
    private val mBinder: Binder
    private var episode: Episode?
    private var podcastId: String?
    private lateinit var mSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private lateinit var mPlaybackState: PlaybackStateCompat
    private var listener: PodcastPlayPauseListener?
    private var bitmap: Bitmap?

    init {
        mBinder = PodcastBinder()
        episode = null
        podcastId = null
        listener = null
        bitmap = null
    }


    override fun onCreate() {
        super.onCreate()
        mPlayer = MediaPlayer()
        mSession = MediaSessionCompat(this, TAG)
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        mPlayer.setOnCompletionListener {
            stop(true)
        }
        registerReceiver(playReceiver, IntentFilter(PLAY_ACTION))
        registerReceiver(pauseReceiver, IntentFilter(PAUSE_ACTION))
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        intent?.action?.let { action ->
            when (action) {
                PLAY_ACTION -> {
                    playEpisode()
                    listener?.playPausePressed(podcastId)
                }
                PAUSE_ACTION -> {
                    pauseEpisode()
                    listener?.playPausePressed(podcastId)
                }
                STOP_ACTION -> {
                    stop(false)
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    fun loadEpisode(episode: Episode, podcastId: String) {
        this.episode?.id?.let { id ->
            podcastRepository.updatePlayingPodcast(id, false)
        }
        this.episode = episode
        this.podcastId = podcastId
        this.bitmap = null
        GlobalScope.launch(Dispatchers.Main) {
            getLargeIcon()
        }
    }

    fun loadListener(listener: PodcastPlayPauseListener?) {
        this.listener = listener
        podcastRepository.getPlayedPodcast().observe(this@PodcastPlayerService, Observer { result ->
            if (result is Result.Success) {
                result.data.first?.let { id ->
                    podcastRepository.getEpisode(id).observe(this@PodcastPlayerService, Observer { ep ->
                        if (ep is Result.Success && ep.data.playing != mPlayer.isPlaying) {
                            podcastRepository.updatePlayingPodcast(id, mPlayer.isPlaying)
                            podcastId?.let { podcastId ->
                                listener?.playPausePressed(podcastId)
                            }
                        }
                    })
                }
            }
        })
    }

    private fun playEpisode() {
        podcastRepository.getPlayedPodcast().observe(this@PodcastPlayerService, Observer {
            val fis = FileInputStream(episode?.path)
            mPlayer.reset()
            mPlayer.setDataSource(fis.fd)
            mPlayer.prepare()
            if (it is Result.Success || it is Result.Error) {
                if (it is Result.Success && it.data.first == episode?.id) {
                    it.data.second?.let { duration ->
                        mPlayer.seekTo(duration.toInt())
                        it.data.first?.let { id ->
                            podcastRepository.updatePlayedPodcast(id, duration)
                            podcastRepository.updatePlayingPodcast(id, true)
                        }
                    }
                } else {
                    episode?.id?.let { id ->
                        podcastRepository.updatePlayedPodcast(id, 0)
                        podcastRepository.updatePlayingPodcast(id, true)
                    }
                }
                fis.close()
                mPlayer.start()
                setNotification(false)
            }
        })
    }

    private fun pauseEpisode() {
        if (mPlayer.isPlaying) {
            mPlayer.pause()
            episode?.id?.let {
                podcastRepository.updatePlayedPodcast(it, mPlayer.currentPosition.toLong())
                podcastRepository.updatePlayingPodcast(it, false)
            }
            setNotification(true)
        }
    }

    private fun setNotification(isNotPlaying: Boolean) {
        val notificationBuilder = createNotificationBuilder()
        notificationBuilder
            .setLargeIcon(bitmap ?: ResourcesCompat.getDrawable(resources, R.drawable.ic_announcement_white_18dp, null)?.apply {
                Utils.setTint(
                    resources,
                    this,
                    R.color.dark
                )
            }?.toBitmap(45, 45))
            .setContentIntent(createContentIntent())
            .setContentTitle(episode?.title)
            .addAction(
                if (isNotPlaying)
                    NotificationCompat.Action(
                        R.drawable.ic_play_circle_filled_white_24dp,
                        "play",
                        createPlayIntent()
                    )
                else
                    NotificationCompat.Action(
                        R.drawable.ic_pause_circle_filled_white_24dp,
                        "pause",
                        createPauseIntent()
                    )
            ).addAction(
                NotificationCompat.Action(
                    R.drawable.ic_cancel_white_24dp,
                    "stop",
                    createStopIntent()
                )
            )
            .setProgress(mPlayer.duration, mPlayer.currentPosition, false)

        val notification = notificationBuilder.build()
        updateData(if (!isNotPlaying) STATE_PLAYING else STATE_PAUSED)
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
        listener?.playPausePressed(podcastId)
    }

    private fun stop(finished: Boolean) {
        episode?.id?.let {
            podcastRepository.updatePlayedPodcast(it, if (finished) 0 else mPlayer.currentPosition.toLong())
            podcastRepository.updatePlayingPodcast(it, false)
        }
        mPlayer.stop()
        mPlayer.reset()
        notificationManager.cancel(NOTIFICATION_ID)
        listener?.playPausePressed(podcastId)
        stopForeground(true)
    }

    private fun updateData(@PlaybackStateCompat.State state: Int) {
        mSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                mPlayer.seekTo(pos.toInt())
                episode?.id?.let { id ->
                    podcastRepository.updatePlayedPodcast(id, pos)
                    mSession.setMetadata(getMediaMetadata())
                    if (mPlayer.isPlaying) {
                        playEpisode()
                    } else {
                        setNotification(true)
                    }
                }
            }
        })
        mSession.setMetadata(getMediaMetadata())
        mPlaybackState = PlaybackStateCompat.Builder().setState(state, mPlayer.currentPosition.toLong(), 1.0f)
            .setActions(ACTION_PLAY or ACTION_PAUSE or ACTION_SEEK_TO or ACTION_STOP)
            .build()
        mSession.setPlaybackState(mPlaybackState)
    }

    override fun onDestroy() {
        unregisterReceiver(playReceiver)
        unregisterReceiver(pauseReceiver)
        notificationManager.cancel(NOTIFICATION_ID)
        episode?.id?.let {
            podcastRepository.updatePlayedPodcast(it, mPlayer.currentPosition.toLong())
            podcastRepository.updatePlayingPodcast(it, false)
        }
        mPlayer.stop()
        mPlayer.reset()
        mPlayer.release()
        super.onDestroy()
    }

    private fun getLargeIcon() {
        episode?.imageUrl?.let { url ->
            Utils.processUrl(url).let { urlProcessed ->
                if (urlProcessed.isNotEmpty()) {
                    Picasso
                        .get()
                        .load(urlProcessed)
                        .resize(400, 400)
                        .into(
                            object: Target {
                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                                override fun onBitmapFailed(
                                    e: Exception?,
                                    errorDrawable: Drawable?
                                ) {}
                                override fun onBitmapLoaded(
                                    bitmap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    this@PodcastPlayerService.bitmap = bitmap
                                    setNotification(!mPlayer.isPlaying)
                                }
                            }
                        )
                }
            }
        }
    }

    private fun createNotificationBuilder()  =
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        createStopIntent()
                    )
            )
            .setColor(ContextCompat.getColor(this, R.color.dark))
            .setSmallIcon(R.mipmap.ic_podcast_player_round)
            .setDeleteIntent(
                createStopIntent()
            )
            .setNotificationSilent()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID, "Podcast Notification Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun createContentIntent(): PendingIntent? {
        val openUI = Intent(this, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openUI.putExtra(PODCAST_ID, podcastId)
        return PendingIntent.getActivity(
            this, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun createPlayIntent(): PendingIntent? {
        val play = Intent(this, PodcastPlayerService::class.java)
        play.action = PLAY_ACTION
        return PendingIntent.getService(this, 0, play, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createPauseIntent(): PendingIntent? {
        val pause = Intent(this, PodcastPlayerService::class.java)
        pause.action = PAUSE_ACTION
        return PendingIntent.getService(this, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createStopIntent(): PendingIntent? {
        val stop = Intent(this, PodcastPlayerService::class.java)
        stop.action = STOP_ACTION
        return PendingIntent.getService(this, 0, stop, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun getMediaMetadata(): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode?.title ?: "Podcast")
        builder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION, mPlayer.duration.toLong()
        )
        return builder.build()
    }

    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playEpisode()
        }
    }

    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pauseEpisode()
        }
    }

    inner class PodcastBinder : Binder() {
        internal val service: PodcastPlayerService
            get() = this@PodcastPlayerService
    }

    interface PodcastPlayPauseListener {
        fun playPausePressed(podcastId: String?)
    }
}