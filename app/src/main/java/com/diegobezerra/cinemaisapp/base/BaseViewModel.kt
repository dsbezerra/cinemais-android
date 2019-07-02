package com.diegobezerra.cinemaisapp.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.result.Error
import com.diegobezerra.core.result.Error.Network
import com.diegobezerra.core.result.Error.NoConnection
import com.diegobezerra.core.result.Error.Timeout
import com.diegobezerra.core.result.Error.Unknown
import com.diegobezerra.core.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException

open class BaseViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _error = MutableLiveData<Error?>()
    val error: LiveData<Error?>
        get() = _error

    fun <T : Any> execute(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = operation()) {
                is Result.Success -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        onSuccess(result.data)
                    }
                }
                is Result.Error -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        errorHandler(result.exception, onError)
                    }
                }
            }
        }
    }

    private fun errorHandler(t: Throwable, handler: (t: Throwable) -> Unit) {
        var err: Error? = null
        when (t) {
            is HttpException -> {
                if (t.code() == 504) { // Unsatisfiable Request (only-if-cached)
                    err = NoConnection
                } else {
                    err = Network
                }
            }
            is SocketTimeoutException -> {
                err = Timeout
            }
            else -> {
                err = Unknown
            }
        }
        if (err != null) {
            _error.setValueIfNew(err)
        } else {
            handler(t)
        }
        Timber.e(t)
    }

    private fun setLoading(value: Boolean) {
        _loading.setValueIfNew(value)
    }
}