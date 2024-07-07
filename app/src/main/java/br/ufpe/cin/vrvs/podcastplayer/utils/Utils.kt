package br.ufpe.cin.vrvs.podcastplayer.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.icu.util.TimeUnit
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import br.ufpe.cin.vrvs.podcastplayer.data.model.ErrorModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

object Utils {
    fun processUrl(url: String): String {
        if ("https" in url)
            return url
        return url.replace("http", "https")
    }

    fun setTint(resources: Resources, drawable: Drawable, @ColorRes color: Int) {
        DrawableCompat.setTint(
            DrawableCompat.wrap(drawable),
            ResourcesCompat.getColor(
                resources,
                color,
                null
            )
        )
    }

    fun toPrettyDate(timestamp: Long): String {
        val df = SimpleDateFormat("MMMM dd, yyyy")
        return df.format(Date().apply {  time = timestamp*1000L})
    }

    fun toPrettyDuration(ms: Long): String {
        val h = MILLISECONDS.toHours(ms)
        val m = MILLISECONDS.toMinutes(ms)%60
        val s = MILLISECONDS.toSeconds(ms)%60
        return when {
            h>0 -> {
                "$h h $m m $s s"
            }
            m > 0 -> {
                "$m m $s s"
            }
            else -> {
                "$s s"
            }
        }
    }

    fun getString(context: Context, errorModel: ErrorModel) =
        errorModel.description ?: context.getString(errorModel.descriptionRes)

    inline fun <T1: Any, T2: Any, T3: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3)->R?): R? {
        return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
    }
}