package br.ufpe.cin.vrvs.podcastplayer.view.component.episode

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Episode
import br.ufpe.cin.vrvs.podcastplayer.view.component.episode.adapter.EpisodeAdapter
import br.ufpe.cin.vrvs.podcastplayer.view.component.image.ImageButtonComponent

class EpisodeListComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val list: RecyclerView by lazy { findViewById<RecyclerView>(R.id.list_episode) }

    private val observerItem: Observer<String>
    private val observerButton: Observer<Pair<Episode, ImageButtonComponent.Type>>

    private val _itemClicked = MutableLiveData<String>()
    val itemClicked: LiveData<String> = _itemClicked
    private val _buttonClicked = MutableLiveData<Pair<Episode, ImageButtonComponent.Type>>()
    val buttonClicked: LiveData<Pair<Episode, ImageButtonComponent.Type>> = _buttonClicked

    init {
        LayoutInflater.from(context).inflate(R.layout.episode_list_component, this, true)
        list.layoutManager = LinearLayoutManager(context)
        observerItem = Observer {
            _itemClicked.postValue(it)
        }
        observerButton = Observer {
            _buttonClicked.postValue(it)
        }
        list.adapter = EpisodeAdapter(context).apply {
            itemClicked.observeForever(observerItem)
            buttonClicked.observeForever(observerButton)
        }
    }

    fun changeDataSet(dataSet: List<Episode>) {
        (list.adapter as EpisodeAdapter).dataSet =  dataSet
    }

    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(changedView, visibility)
        (list.adapter as EpisodeAdapter).notifyDataSetChanged()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (list.adapter as EpisodeAdapter).apply{
            itemClicked.removeObserver(observerItem)
            buttonClicked.removeObserver(observerButton)
        }
    }
}