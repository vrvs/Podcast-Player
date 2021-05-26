package br.ufpe.cin.vrvs.podcastplayer.ui.view.search

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
import br.ufpe.cin.vrvs.podcastplayer.databinding.FragmentPodcastSearchBinding
import br.ufpe.cin.vrvs.podcastplayer.ui.contracts.search.PodcastSearch
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.error.ErrorComponent
import br.ufpe.cin.vrvs.podcastplayer.ui.view.component.podcast.PodcastListComponent
import br.ufpe.cin.vrvs.podcastplayer.ui.viewmodel.search.PodcastSearchViewModel
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.viewmodel.ext.android.viewModel

class PodcastSearchFragment : Fragment(R.layout.fragment_podcast_search) {

    private val spViewModel: PodcastSearchViewModel by viewModel()
    private var mBinding: FragmentPodcastSearchBinding? = null
    private lateinit var list: PodcastListComponent
    private lateinit var error: ErrorComponent
    private lateinit var searchText: TextInputEditText

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        list = view.findViewById(R.id.subscribed_list)
        searchText = view.findViewById(R.id.search_text)
        error = view.findViewById(R.id.error_screen)

        mBinding = FragmentPodcastSearchBinding.bind(view).apply {
            viewModel = spViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        spViewModel.state.observe(viewLifecycleOwner, Observer { state ->
        state.data?.let {
                list.changeDataSet(it)
            }
            state.error?.let {
                error.errorText(it)
            }
        })

        list.itemClicked.observe(viewLifecycleOwner, Observer {
            val action = PodcastSearchFragmentDirections.actionPodcastSearchFragmentToPodcastDetailsFragment(it)
            findNavController().navigate(action)
        })
        searchText.setOnEditorActionListener { v, actionId, _ ->
            hideKeyboard(v)
            searchText.clearFocus()
            if (actionId == IME_ACTION_SEARCH) {
                spViewModel.userIntent.offer(PodcastSearch.Intent.SearchPodcasts(v.text.toString()))
                true
            }
            false
        }
        error.buttonClicked.observe(viewLifecycleOwner, Observer { button ->
            when (button) {
                ErrorComponent.Button.TRY_AGAIN -> searchText.text?.let {
                    spViewModel.userIntent.offer(PodcastSearch.Intent.SearchPodcasts(it.toString()))
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
                spViewModel.userIntent.offer(PodcastSearch.Intent.GetPodcastFeed)
            } else {
                spViewModel.userIntent.offer(PodcastSearch.Intent.SearchPodcasts(it.toString()))
            }
        }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}