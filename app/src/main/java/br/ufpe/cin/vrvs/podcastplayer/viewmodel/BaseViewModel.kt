package br.ufpe.cin.vrvs.podcastplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val protectedLoading =  MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = protectedLoading

    protected val protectedError =  MutableLiveData<String>()
    val error: LiveData<String> = protectedError
}