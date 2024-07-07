package br.ufpe.cin.vrvs.podcastplayer.ui.view.component.loading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.ufpe.cin.vrvs.podcastplayer.R

class LoadingComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.loading_component, this, true)
    }
}