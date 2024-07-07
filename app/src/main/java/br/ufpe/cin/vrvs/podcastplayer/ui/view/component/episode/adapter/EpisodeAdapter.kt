package br.ufpe.cin.vrvs.podcastplayer.ui.view.component.episode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.getDuration
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.isDownloaded
import br.ufpe.cin.vrvs.podcastplayer.services.download.DownloadUtils.isInProgress
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.image.ImageButtonComponent
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.image.SquareRoundedImageComponent

internal class EpisodeAdapter(val context: Context) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    var dataSet: List<Episode> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val _itemClicked = MutableLiveData<String>()
    val itemClicked: LiveData<String> = _itemClicked
    private val _buttonClicked = MutableLiveData<Pair<Episode, ImageButtonComponent.Type>>()
    val buttonClicked: LiveData<Pair<Episode, ImageButtonComponent.Type>> = _buttonClicked

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView by lazy { view.findViewById<TextView>(R.id.title) }
        val date: TextView by lazy { view.findViewById<TextView>(R.id.date) }
        val squareRoundedImageComponent: SquareRoundedImageComponent by lazy { view.findViewById<SquareRoundedImageComponent>(R.id.image_component) }
        val description: TextView by lazy { view.findViewById<TextView>(R.id.description) }
        val duration: TextView by lazy { view.findViewById<TextView>(R.id.duration) }
        val playPause: ImageButtonComponent by lazy { view.findViewById<ImageButtonComponent>(R.id.play_pause) }
        val downloadStopDelete: ImageButtonComponent by lazy { view.findViewById<ImageButtonComponent>(R.id.download_stop_delete) }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(
                    R.layout.episode_component,
                    viewGroup,
                    false)
        )

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]
        viewHolder.apply {
            title.text = data.title
            date.text = Utils.toPrettyDate(data.datePublished)
            squareRoundedImageComponent.render(data.imageUrl)
            description.text = data.description

            if (data.isDownloaded(context)) {
                playPause.setOnClickListener {
                    _buttonClicked.postValue(Pair(data, playPause.state))
                    playPause.nextState()
                }
                duration.text = Utils.toPrettyDuration(data.getDuration(context))
                downloadStopDelete.state = ImageButtonComponent.Type.DOWNLOADED
                playPause.isEnabled = true
            } else {
                if (data.isInProgress(context)) {
                    downloadStopDelete.state = ImageButtonComponent.Type.STOP
                } else {
                    downloadStopDelete.state = ImageButtonComponent.Type.DOWNLOAD
                }
                playPause.isEnabled = false
            }
            playPause.state = if (data.playing) {
                ImageButtonComponent.Type.PAUSE
            } else {
                ImageButtonComponent.Type.PLAY
            }

            downloadStopDelete.setOnClickListener {
                _buttonClicked.postValue(Pair(data, downloadStopDelete.state))
                downloadStopDelete.nextState()
            }

            // clicked item action
            itemView.setOnClickListener {
                _itemClicked.postValue(data.id)
            }
        }
    }

    override fun getItemCount() = dataSet.size
}