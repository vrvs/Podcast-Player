package br.ufpe.cin.vrvs.podcastplayer.view.component.image

import android.content.Context
import android.util.AttributeSet
import br.ufpe.cin.vrvs.podcastplayer.R
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils
import br.ufpe.cin.vrvs.podcastplayer.utils.Utils.safeLet
import com.squareup.picasso.Picasso

class ImageComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private var widthView: Int? = null
    private var heightView: Int? = null
    private var url: String? = null

    fun render(url: String) {
        this.url = url
        loadUrl()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthView = w
        heightView = h
        loadUrl()
    }

    private fun loadUrl() = safeLet(widthView, heightView, url) { w, h, u ->
        Utils.processUrl(u).let {
            if (it.isNotEmpty()) {
                Picasso
                    .get()
                    .load(it)
                    .resize(w, h)
                    .onlyScaleDown()
                    .centerCrop()
                    .error(R.drawable.ic_announcement_white_18dp)
                    .into(this)
            } else {
                Picasso
                    .get()
                    .load(R.drawable.ic_announcement_white_18dp)
                    .into(this)
            }
        }
    }
}
