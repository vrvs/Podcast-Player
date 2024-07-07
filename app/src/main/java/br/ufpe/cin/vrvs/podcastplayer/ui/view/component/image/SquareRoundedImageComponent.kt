package br.ufpe.cin.vrvs.podcastplayer.ui.view.component.image

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import br.ufpe.cin.vrvs.podcastplayer.R

class SquareRoundedImageComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val imageComponent: ImageComponent by lazy { findViewById<ImageComponent>(R.id.icon) }

    init {
        LayoutInflater.from(context).inflate(R.layout.image_component, this, true)
    }

    fun render(url: String) {
        imageComponent.render(url)
    }
}
