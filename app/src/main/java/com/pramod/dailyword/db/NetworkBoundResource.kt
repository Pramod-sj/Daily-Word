package com.pramod.dailyword.db

import android.util.Log
import com.pramod.dailyword.db.model.ApiResponse
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
                    fetchFromCache().map { Resource.error(apiResponse.message, it) }
                }

            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                Log.i(
                    TAG,
                    "asFlow: network call failed $throwable fetching from cache"
                )
                val message = when (throwable) {
                    is SocketTimeoutException -> "Timeout! Please check your internet connection!"

                    is IOException -> "You don't have an proper internet connection!"

                    is HttpException -> "Something went wrong! Try again after sometime."

                    is UnknownHostException -> "You don't have an proper internet connection!"

                    is ConnectException -> "Unable to connect to our server"

                    else -> "Something went wrong! Try again after sometime."
                }
                fetchFromCache().map { Resource.error(message, it) }
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

