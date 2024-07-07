package br.ufpe.cin.vrvs.podcastplayer.ui.view.component.error

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.ufpe.cin.vrvs.podcastplayer.R
import com.google.android.material.button.MaterialButton

class ErrorComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): RelativeLayout(context, attrs, defStyleAttr) {

    enum class Button {
        CLOSE,
        TRY_AGAIN
    }

    private val errorText: TextView by lazy { findViewById<TextView>(R.id.text_info) }
    private val closeButton: ImageButton by lazy { findViewById<ImageButton>(R.id.close_button) }
    private val buttonError: MaterialButton by lazy { findViewById<MaterialButton>(R.id.button_error) }

    private val _buttonClicked = MutableLiveData<Button>()
    val buttonClicked: LiveData<Button> = _buttonClicked

    init {
        LayoutInflater.from(context).inflate(R.layout.error_component, this, true)
        buttonError.setOnClickListener {
            _buttonClicked.postValue(Button.TRY_AGAIN)
        }
        closeButton.setOnClickListener {
            _buttonClicked.postValue(Button.CLOSE)
        }
    }

    fun errorText(text: String) {
        errorText.text = text
    }
}