package com.pramod.dailyword.business.data.network

import android.util.Log
import com.pramod.dailyword.business.data.network.utils.handleApiException
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import kotlinx.coroutines.flow.*

abstract class NetworkBoundResource<RequestType, ResponseType> {

    companion object {
        val TAG = NetworkBoundResource::class.java.simpleName
    }

    fun asFlow(): Flow<Resource<ResponseType?>> = flow {
        val data = fetchFromCache().first()

        val flow: Flow<Resource<ResponseType?>> = if (shouldFetchFromNetwork(data)) {

            Log.i(TAG, "asFlow: fetching from network")
            emit(Resource.loading(data))

            try {
                val apiResponse = fetchFromNetwork()
                if (apiResponse.code == "200") {
                    val resData = apiResponse.data
                    if (resData != null) {
                        Log.i(TAG, "asFlow: saving into cached")
                        saveIntoCache(resData)
                    }
                    Log.i(TAG, "asFlow: fetching from cached")
                    fetchFromCache().map { Resource.success(it) }
                } else {
                    fetchFromCache().map {
                        Resource.error(
                            Throwable(apiResponse.message),
                            it
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(
                    TAG,
                    "asFlow: network call failed ${e.message} fetching from cache"
                )
                fetchFromCache().map { Resource.error(handleApiException(e), it) }
            }
        } else {
            Log.i(TAG, "asFlow: fetching from cache")
            fetchFromCache().map { Resource.success(it) }
        }

        emitAll(flow)
    }


    abstract suspend fun fetchFromCache(): Flow<ResponseType?>

    abstract fun shouldFetchFromNetwork(data: ResponseType?): Boolean

    abstract suspend fun saveIntoCache(data: RequestType)

    abstract suspend fun fetchFromNetwork(): ApiResponse<RequestType>

}

