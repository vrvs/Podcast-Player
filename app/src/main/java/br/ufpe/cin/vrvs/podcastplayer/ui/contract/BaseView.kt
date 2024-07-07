package br.ufpe.cin.vrvs.podcastplayer.ui.contract

interface BaseView<T> {
    var presenter: T
    fun showError(error: String)
    fun showLoading()
}