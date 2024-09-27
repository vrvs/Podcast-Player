package br.ufpe.cin.vrvs.podcastplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import br.ufpe.cin.vrvs.podcastplayer.services.player.PodcastPlayerService.Companion.PODCAST_ID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val podcastId = intent.extras?.getString(PODCAST_ID)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (podcastId != null) {
            val action = NavGraphDirections.actionGlobalPodcastDetailsFragment(podcastId)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_graph_container) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(action)
        }
    }
}