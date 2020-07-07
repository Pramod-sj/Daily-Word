package com.pramod.dailyword.db

import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.pramod.dailyword.db.Resource.Companion.loading
import com.pramod.dailyword.db.Resource.Companion.success
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.Status
import com.pramod.dailyword.db.remote.NetworkResponse
import com.pramod.dailyword.db.remote.handleApiFailure
import com.pramod.dailyword.db.remote.handleApiSuccess
import com.pramod.dailyword.db.remote.handleNetworkException
import kotlinx.coroutines.*

abstract class NetworkBoundedResource<RequestType, ResultType> {
    private val result: MediatorLiveData<Resource<ResultType?>> = MediatorLiveData()

    companion object {
        val TAG = NetworkBoundedResource::class.java.simpleName
    }


    init {
        @Suppress("LeakingThis")
        val localLiveData = loadLocalData()
        result.addSource(localLiveData) { data ->
            result.removeSource(localLiveData)
            if (shouldFetch(data)) {
                fetchFromNetwork(localLiveData)
            } else {
                result.addSource(localLiveData) { offlineData ->
                    result.value = success(offlineData)
                }
            }
        }
    }

    private fun fetchFromNetwork(localLiveData: LiveData<ResultType?>) {
        result.addSource(localLiveData) {
            result.value = loading(it)
        }
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val callResponse = callRequest()
                result.removeSource(localLiveData)
                if (callResponse != null) {
                    if (callResponse.code == "200") {
                        saveCallResult(callResponse.data)
                        result.addSource(localLiveData) {
                            result.value = handleApiSuccess(it)
                        }
                    } else {
                        result.addSource(localLiveData) {
                            result.value = handleApiFailure(it, callResponse.message)
                        }
                    }
                } else {
                    result.addSource(localLiveData) {
                        result.value = handleApiFailure(it, "No response from server")
                    }
                }
            } catch (e: Exception) {
                result.removeSource(localLiveData)
                result.addSource(localLiveData) {
                    result.value = handleNetworkException(it, e)
                }
            }
        }
    }


    @WorkerThread
    protected abstract suspend fun saveCallResult(@NonNull item: RequestType?)

    @MainThread
    abstract fun loadLocalData(): LiveData<ResultType?>

    abstract suspend fun callRequest(): ApiResponse<RequestType?>?

    @MainThread
    abstract fun shouldFetch(data: ResultType?): Boolean


    fun asLiveData(): LiveData<Resource<ResultType?>> = result

}