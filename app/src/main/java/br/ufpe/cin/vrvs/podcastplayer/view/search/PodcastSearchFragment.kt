package br.ufpe.cin.vrvs.podcastplayer.view.search

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.data.model.Podcast
import br.ufpe.cin.vrvs.podcastplayer.data.model.Result
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.view.component.error.ErrorComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.podcast.PodcastListComponent
import br.ufpe.cin.vrvs.podcastplayer.view.component.loading.LoadingComponent
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject


class PodcastSearchFragment : Fragment(R.layout.fragment_podcast_search) {

    private val podcastRepository: PodcastRepository by inject()
    private lateinit var list: PodcastListComponent
    private lateinit var error: ErrorComponent
    private lateinit var loading: LoadingComponent
    private lateinit var searchText: TextInputEditText

    private val podcastObserver = Observer<Result<List<Podcast>>> {
        when (it) {
            is Result.Success -> showPodcasts(it.data)
            is Result.Loading -> showLoading()
            is Result.Error -> context?. let { c -> showError(Utils.getString(c, it.error))  }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        list = view.findViewById(R.id.subscribed_list)
        searchText = view.findViewById(R.id.search_text)
        error = view.findViewById(R.id.error_screen)
        loading = view.findViewById(R.id.loading_screen)

        list.itemClicked.observe(viewLifecycleOwner, Observer {
            val action = PodcastSearchFragmentDirections.actionPodcastSearchFragmentToPodcastDetailsFragment(it)
            findNavController().navigate(action)
        })
        searchText.setOnEditorActionListener { v, actionId, _ ->
            hideKeyboard(v)
            searchText.clearFocus()
            if (actionId == IME_ACTION_SEARCH) {
                podcastRepository.searchPodcast(v.text.toString()).observe(viewLifecycleOwner, podcastObserver)
                true
            }
            false
        }
        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> searchText.text?.let {
                    podcastRepository.searchPodcast(it.toString()).observe(viewLifecycleOwner, podcastObserver)
                }
                ErrorComponent.Button.CLOSE -> error.visibility = GONE
            }
        })
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        searchText.text.let {
            if (it.isNullOrBlank()) {
                podcastRepository.getPodcastFeed().observe(viewLifecycleOwner, podcastObserver)
            } else {
                podcastRepository.searchPodcast(it.toString()).observe(viewLifecycleOwner, podcastObserver)
            }
        }
    }

    private fun showPodcasts(podcasts: List<Podcast>) {
        showView(showMain = true, showLoading = false, showError = false)
        list.changeDataSet(podcasts)
    }

    private fun showError(error: String) {
        showView(showMain = false, showLoading = false, showError = true)
        this.error.errorText(error)
    }

    private fun showLoading() {
        showView(showMain = false, showLoading = true, showError = false)
    }

    private fun showView(showMain: Boolean, showLoading: Boolean, showError: Boolean) {
        showView(list, showMain)
        showView(loading, showLoading)
        showView(error, showError)
    }

    private fun showView(view: View, showView: Boolean) {
        view.visibility = if (showView) View.VISIBLE else GONE
    }
}