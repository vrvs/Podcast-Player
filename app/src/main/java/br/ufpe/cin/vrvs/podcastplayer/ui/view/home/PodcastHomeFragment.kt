package br.ufpe.cin.vrvs.podcastplayer.ui.view.home

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.ui.contract.home.PodcastHomeContract
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.error.ErrorComponent
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.podcast.PodcastListComponent
import br.ufpe.cin.vrvs.podcastplayer.ui.presenter.home.PodcastHomePresenter
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.loading.LoadingComponent
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PodcastHomeFragment : Fragment(R.layout.fragment_podcast_home), PodcastHomeContract.View {

    override lateinit var presenter: PodcastHomeContract.Presenter
    private lateinit var list: PodcastListComponent
    private lateinit var error: ErrorComponent
    private lateinit var loading: LoadingComponent
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var searchButton: FloatingActionButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        list = view.findViewById(R.id.subscribed_list)
        refresh = view.findViewById(R.id.swipe_refresh)
        error = view.findViewById(R.id.error_screen)
        loading = view.findViewById(R.id.loading_screen)
        searchButton = view.findViewById(R.id.floating_button)

        presenter = PodcastHomePresenter(this)

        refresh.setOnRefreshListener {
            refresh.isRefreshing = false
            presenter.refreshSubscribedPodcasts()
        }
        list.itemClicked.observe(viewLifecycleOwner, Observer {
            val action = PodcastHomeFragmentDirections.actionPodcastHomeFragmentToPodcastDetailsFragment(it)
            findNavController().navigate(action)
        })
        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> presenter.refreshSubscribedPodcasts()
                ErrorComponent.Button.CLOSE -> showView(showMain = true, showLoading = false, showError = false)
            }
        })
        searchButton.setOnClickListener {
            val action = PodcastHomeFragmentDirections.actionPodcastHomeFragmentToPodcastSearchFragment()
            findNavController().navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe(context)
        presenter.refreshSubscribedPodcasts()
    }

    override fun onStop() {
        super.onStop()
        presenter.unsubscribe()
    }

    override fun showSubscribedPodcasts(podcasts: List<Podcast>) {
        showView(showMain = true, showLoading = false, showError = false)
        list.changeDataSet(podcasts)
    }

    override fun showError(error: String) {
        showView(showMain = false, showLoading = false, showError = true)
        this.error.errorText(error)
    }

    override fun showLoading() {
        showView(showMain = false, showLoading = true, showError = false)
    }

    private fun showView(showMain: Boolean, showLoading: Boolean, showError: Boolean) {
        showView(list, showMain)
        showView(loading, showLoading)
        showView(error, showError)
    }

    private fun showView(view: View, showView: Boolean) {
        view.visibility = if (showView) VISIBLE else GONE
    }
}