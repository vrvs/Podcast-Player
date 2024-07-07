package br.ufpe.cin.vrvs.podcastplayer.view.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.databinding.FragmentPodcastHomeBinding
import br.ufpe.cin.vrvs.podcastplayer.view.component.error.ErrorComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.podcast.PodcastListComponent
import br.ufpe.cin.vrvs.podcastplayer.viewmodel.podcasts.subscribed.SubscribedPodcastsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.viewmodel.ext.android.viewModel

class PodcastHomeFragment : Fragment(R.layout.fragment_podcast_home) {

    private val spViewModel: SubscribedPodcastsViewModel by viewModel()
    private var mBinding: FragmentPodcastHomeBinding? = null
    private lateinit var list: PodcastListComponent
    private lateinit var error: ErrorComponent
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
        searchButton = view.findViewById(R.id.floating_button)

        mBinding = FragmentPodcastHomeBinding.bind(view).apply {
            viewModel = spViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        spViewModel.podcasts.observe(viewLifecycleOwner, Observer {
            list.changeDataSet(it)
        })
        refresh.setOnRefreshListener {
            refresh.isRefreshing = false
            spViewModel.refreshSubscribedPodcast()
        }
        list.itemClicked.observe(viewLifecycleOwner, Observer {
            val action = PodcastHomeFragmentDirections.actionPodcastHomeFragmentToPodcastDetailsFragment(it)
            findNavController().navigate(action)
        })
        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> spViewModel.refreshSubscribedPodcast()
                ErrorComponent.Button.CLOSE -> error.visibility = View.GONE
            }
        })
        spViewModel.error.observe(viewLifecycleOwner, Observer {
            error.errorText(it)
        })
        searchButton.setOnClickListener {
            val action = PodcastHomeFragmentDirections.actionPodcastHomeFragmentToPodcastSearchFragment()
            findNavController().navigate(action)
        }
        spViewModel.refreshSubscribedPodcast()
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}