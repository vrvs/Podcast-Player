package br.ufpe.cin.vrvs.podcastplayer.services.download

import android.app.DownloadManager
import android.app.DownloadManager.STATUS_FAILED
import android.app.DownloadManager.STATUS_PAUSED
import android.app.DownloadManager.STATUS_PENDING
import android.app.DownloadManager.STATUS_RUNNING
import android.app.DownloadManager.STATUS_SUCCESSFUL
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment.DIRECTORY_PODCASTS
import android.webkit.MimeTypeMap
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File


object DownloadUtils: KoinComponent {

    private val podcastRepository: PodcastRepository by inject()

    fun DownloadManager.cancel(
        podcastId: String,
        episodeId: String,
        downloadId: Long) {
        this.remove(downloadId)
        podcastRepository.updateDownloadedEpisode(episodeId, podcastId, null)
    }

    fun Context.startPodcastDownload(
        url: String,
        audioType: String,
        episodeTitle: String,
        episodeId: String,
        podcastId: String): Long? {
        val fileName = getEpisodeFileName(episodeId, audioType)
        val file = this.getPodcastFile(fileName)
        val request = getDownloadRequest(Utils.processUrl(url), file, episodeTitle)
        val result = this.enqueueDownload(request)
        savePathDownload(podcastId, episodeId, result, file.canonicalPath)
        return result
    }

    fun Context.getDownloadManager(): DownloadManager? =
        this.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?

    fun getDownloadCompleteBroadcastReceiver(downloadId: Long?, block: () -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    block()
                }
            }
        }
    }

    fun Episode.isDownloaded(context: Context): Boolean {
        this.downloadId?.let {
            return context.getDownloadManager()?.isDownloadSucceed(it, this.id, this.podcastId) == true && isFileExists(this.path)
        }
        return false
    }

    fun Episode.isInProgress(context: Context): Boolean {
        this.downloadId?.let {
            context.getDownloadManager()?.let{ dm ->
                return dm.isDownloadInProgress(it, this.id, this.podcastId)
            }
        }
        return false
    }

    fun Episode.getDuration(context: Context): Long {
        val uri = Uri.parse(this.path)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toLong() ?: 0
    }

    private fun DownloadManager.isDownloadSucceed(downloadId: Long, episodeId: String, podcastId: String): Boolean {
        val cursor: Cursor = this.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status == STATUS_SUCCESSFUL) {
                return true
            } else if (status == STATUS_FAILED) {
                podcastRepository.updateDownloadedEpisode(episodeId, podcastId, null, "")
            }
        }
        return false
    }

    private fun DownloadManager.isDownloadInProgress(downloadId: Long, episodeId: String, podcastId: String): Boolean {
        val cursor: Cursor = this.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status in setOf(STATUS_PENDING, STATUS_RUNNING, STATUS_PAUSED)) {
                return true
            } else if (status == STATUS_FAILED) {
                podcastRepository.updateDownloadedEpisode(episodeId, podcastId, null, "")
            }
        }
        return false
    }

    private fun isFileExists(path: String) = File(path).exists()

    private fun getDownloadRequest(url: String, file: File, episodeTitle: String): DownloadManager.Request =
        DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setTitle(episodeTitle)
            .setDescription("Downloading")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

    private fun getEpisodeFileName(episodeId: String, audioType: String): String =
        "Episode $episodeId.${MimeTypeMap.getSingleton().getExtensionFromMimeType(audioType).orEmpty().ifBlank {audioType.replace('/', '.')}}"

    private fun Context.enqueueDownload(request: DownloadManager.Request): Long? {
        val downloadManager = this.getDownloadManager()
        return downloadManager?.enqueue(request)
    }

    private fun savePathDownload(
        podcastId: String,
        episodeId: String,
        downloadId: Long?,
        path: String) {
        podcastRepository.updateDownloadedEpisode(episodeId, podcastId, downloadId, path)
    }

    private fun Context.getPodcastFile(fileName: String) =
        File(this.getExternalFilesDir(DIRECTORY_PODCASTS), fileName)
}