package br.ufpe.cin.vrvs.podcastplayer.app

import android.app.Application
import br.ufpe.cin.vrvs.podcastplayer.di.apiModule
import br.ufpe.cin.vrvs.podcastplayer.di.databaseModule
import br.ufpe.cin.vrvs.podcastplayer.di.preferencesModule
import br.ufpe.cin.vrvs.podcastplayer.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PodcastPlayerApplication : Application() {

    private val modules = listOf(
        apiModule,
        databaseModule,
        preferencesModule,
        repositoryModule
    )

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin{
            androidLogger()
            androidContext(this@PodcastPlayerApplication)
            modules(modules)
        }
    }
}