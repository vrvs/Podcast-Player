package br.ufpe.cin.vrvs.podcastplayer.di

import android.content.Context
import androidx.room.Room
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.database.PodcastDatabase
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference.PodcastSharedPreferences
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference.PodcastSharedPreferencesImpl
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.PodcastIndexApi
import br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.PodcastIndexAuthInterceptor
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepository
import br.ufpe.cin.vrvs.podcastplayer.data.repository.PodcastRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
    single {
        provideHttpLoggingInterceptor()
    }
    single {
        providePodcastIndexAuthInterceptor(get())
    }
    single {
        provideHttpClient(get(), get())
    }
    single {
        provideRetrofit(get())
    }
    single {
        providePodcastIndexApi(get())
    }
}

val databaseModule = module {
    single {
        providePodcastDatabase(get())
    }
}

val preferencesModule = module {
    single {
        PodcastSharedPreferencesImpl(get()) as PodcastSharedPreferences
    }
}

@OptIn(KoinApiExtension::class)
val repositoryModule = module {
    single {
        PodcastRepositoryImpl() as PodcastRepository
    }
}

private fun provideHttpLoggingInterceptor() =
    HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

private fun providePodcastIndexAuthInterceptor(context: Context) =
    PodcastIndexAuthInterceptor(context)

private fun provideHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    podcastIndexAuthInterceptor: PodcastIndexAuthInterceptor
) =
    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(podcastIndexAuthInterceptor)
        .build()

private fun provideRetrofit(okHttpClient: OkHttpClient) =
    Retrofit.Builder()
        .baseUrl(PodcastIndexApi.URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

private fun providePodcastIndexApi(retrofit: Retrofit) =
    retrofit.create(PodcastIndexApi::class.java)

private fun providePodcastDatabase(context: Context) =
    Room.databaseBuilder(context, PodcastDatabase::class.java, "podcast_database")
        .fallbackToDestructiveMigration()
        .build()