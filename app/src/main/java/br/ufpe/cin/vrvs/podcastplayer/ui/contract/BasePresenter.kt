package br.ufpe.cin.vrvs.podcastplayer.ui.contract

import android.content.Context

interface BasePresenter {
    fun subscribe(context: Context?)
    fun unsubscribe()
}