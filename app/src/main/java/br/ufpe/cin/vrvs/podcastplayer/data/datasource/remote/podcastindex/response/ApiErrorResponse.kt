package br.ufpe.cin.vrvs.podcastplayer.data.datasource.remote.podcastindex.response

import com.google.gson.annotations.SerializedName

class ApiErrorResponse {
    @SerializedName("description")
    var description: String? = ""
}