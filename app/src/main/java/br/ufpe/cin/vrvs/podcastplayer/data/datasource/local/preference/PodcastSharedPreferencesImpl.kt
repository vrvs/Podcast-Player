package br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class PodcastSharedPreferencesImpl(context: Context) : PodcastSharedPreferences {

    companion object {
        private const val PODCAST_SHARED_PREFERENCES = "br.ufpe.cin.vrvs.podcastplayer.data.datasource.local.preference.PodcastSharedPreferences"
        private const val PODCAST_ID = "PodcastId"
        private const val PODCAST_TIME = "PodcastTime"
    }

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(PODCAST_SHARED_PREFERENCES, MODE_PRIVATE)
    }

    override var podcastEpisodeId: String?
        get() = sharedPreferences.getStringHelper(PODCAST_ID)
        set(newValue) = sharedPreferences.putStringHelper(PODCAST_ID, newValue)
    override fun clearPodcastEpisodeId() = sharedPreferences.clear(PODCAST_ID)

    override var podcastTime: Long?
        get() = sharedPreferences.getLongHelper(PODCAST_TIME)
        set(newValue) = sharedPreferences.putLongHelper(PODCAST_TIME, newValue)
    override fun clearPodcastTime() = sharedPreferences.clear(PODCAST_TIME)


    // Helper functions

    private fun SharedPreferences.putLongHelper(key: String?, value: Long?) {
        if (key.isNullOrEmpty() || value == null) return
        edit().putLong(key, value).apply()
    }

    private fun SharedPreferences.getLongHelper(key: String?): Long? {
        return if (key.isNullOrEmpty() || contains(key).not()) null
        else getLong(key, -1)
    }

    private fun SharedPreferences.putStringHelper(key: String?, value: String?) {
        if (key.isNullOrEmpty() || value.isNullOrEmpty()) return
        edit().putString(key, value).apply()
    }

    private fun SharedPreferences.getStringHelper(key: String?): String? {
        return if (key.isNullOrEmpty() || contains(key).not()) null
        else getString(key, null)
    }

    private fun SharedPreferences.clear(key: String?) {
        if (key.isNullOrEmpty() || contains(key).not()) return
        else edit().remove(key).apply()
    }
}