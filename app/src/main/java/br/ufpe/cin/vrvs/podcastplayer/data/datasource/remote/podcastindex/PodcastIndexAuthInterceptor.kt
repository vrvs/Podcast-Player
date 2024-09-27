package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex

import android.content.Context
import br.ufpe.cin.vrvs.podcastplayer.R
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PodcastIndexAuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()
        val keys = getKeys()
        req = req.newBuilder()
            .header("X-Auth-Date", keys.xAuthDate)
            .header("X-Auth-Key", keys.xAuthKey)
            .header("Authorization", keys.authorization)
            .header("User-Agent", keys.userAgent)
            .build()
        return chain.proceed(req)
    }

    private fun getKeys() : Keys {
        val apiKey = context.getString(R.string.api_key)
        val apiSecret = context.getString(R.string.api_secret)
        val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        val now = Date()
        calendar.time = now
        val secondsSinceEpoch: Long = calendar.timeInMillis / 1000L
        val apiHeaderTime = "" + secondsSinceEpoch
        val data4Hash = apiKey + apiSecret + apiHeaderTime
        val hashString: String? = sha1(data4Hash)

        return Keys(apiHeaderTime, apiKey, hashString ?: "")
    }

    private fun sha1(clearString: String): String? {
        return try {
            val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-1")
            messageDigest.update(clearString.toByteArray(charset("UTF-8")))
            byteArrayToString(messageDigest.digest())
        } catch (ignored: java.lang.Exception) {
            ignored.printStackTrace()
            null
        }
    }

    private fun byteArrayToString(bytes: ByteArray): String? {
        val buffer = java.lang.StringBuilder()
        for (b in bytes) {
            buffer.append(java.lang.String.format(Locale.getDefault(), "%02x", b))
        }
        return buffer.toString()
    }

    private data class Keys(
        val xAuthDate: String,
        val xAuthKey: String,
        val authorization: String,
        val userAgent: String = "SuperPodcastPlayer/1.4"
    )
}