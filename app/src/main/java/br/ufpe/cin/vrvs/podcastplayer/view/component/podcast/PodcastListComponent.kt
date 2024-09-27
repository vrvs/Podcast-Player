package br.ufpe.cin.vrvs.podcastplayer.view.component.podcast

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.view.component.podcast.adapter.PodcastAdapter

class PodcastListComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val list: RecyclerView by lazy { findViewById<RecyclerView>(R.id.list) }

    private val observer: Observer<String>
    private val _itemClicked = MutableLiveData<String>()
    val itemClicked: LiveData<String> = _itemClicked

    init {
        LayoutInflater.from(context).inflate(R.layout.podcast_list_component, this, true)
        list.layoutManager = LinearLayoutManager(context)
        observer = Observer {
            _itemClicked.postValue(it)
        }
        list.adapter = PodcastAdapter(context).also {
            it.itemClicked.observeForever(observer)
        }
    }

    fun changeDataSet(dataSet: List<Podcast>) {
        (list.adapter as PodcastAdapter).dataSet = dataSet
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (list.adapter as PodcastAdapter).itemClicked.removeObserver(observer)
    }
}