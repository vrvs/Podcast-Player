package br.ufpe.cin.vrvs.podcastplayer.data.model

import androidx.annotation.StringRes
import br.ufpe.cin.vrvs.podcastplayer.R
import java.lang.Exception

data class ErrorModel(
    val description: String? = null,
    @StringRes val descriptionRes: Int = R.string.error_generic_text
) : Exception()