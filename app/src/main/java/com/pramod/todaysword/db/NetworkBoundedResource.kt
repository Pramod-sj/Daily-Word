package com.pramod.todaysword.db

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import com.pramod.todaysword.db.Resource.Companion.error
import com.pramod.todaysword.db.Resource.Companion.loading
import com.pramod.todaysword.db.Resource.Companion.success
import com.pramod.todaysword.db.model.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class NetworkBoundedResource<RequestType, ResultType>() {
    private val result =
        MediatorLiveData<Resource<ResultType?>>()

    private fun fetchData(dataSource: LiveData<ResultType?>) {
        result.addSource(
            dataSource
        ) { newData: ResultType? ->
            result.setValue(
                loading(newData)
            )
        }
        Log.i(
            TAG,
            "BEFORE FETCHING FIRST SHOW LOCAL DATA"
        )
        val call = callRequest()
        Log.i("URL", call.request().url().toString())
        call.enqueue(object : Callback<ApiResponse<RequestType>?> {
            override fun onResponse(
                call: Call<ApiResponse<RequestType>?>,
                response: Response<ApiResponse<RequestType>?>
            ) {
                result.removeSource(dataSource)
                Log.i(
                    TAG,
                    "REMOVING LOADING OFFLINE SOURCE"
                )
                val apiResponse = response.body()
                Log.i(
                    TAG,
                    Gson().toJson(apiResponse)
                )
                if(apiResponse != null) {
                    if (apiResponse.code == "200") {
                        saveResultAndReInit(apiResponse.data)
                    } else {
                        result.addSource(
                            dataSource
                        ) { newData: ResultType? ->
                            result.setValue(
                                error(
                                    apiResponse?.message,
                                    newData,
                                    Resource.ErrorType.UNKNOWN
                                )
                            )
                        }
                    }
                }
                else{
                    result.addSource(
                        dataSource
                    ) { newData: ResultType? ->
                        result.setValue(
                            error(
                                "No response from server",
                                newData,
                                Resource.ErrorType.UNKNOWN
                            )
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<RequestType>?>,
                t: Throwable
            ) {
                result.removeSource(dataSource)
                if (t is UnknownHostException) {
                    result.addSource(
                        dataSource
                    ) { newData: ResultType? ->
                        result.setValue(
                            error(
                                "You don't have a proper internet connection",
                                newData,
                                Resource.ErrorType.NO_INTERNET
                            )
                        )
                    }

                } else if (t is SocketTimeoutException) {
                    result.addSource(
                        dataSource
                    ) { newData: ResultType? ->
                        result.setValue(
                            error(
                                "Timeout! Please check your internet connection or retry!",
                                newData,
                                Resource.ErrorType.NO_INTERNET
                            )
                        )
                    }

                } else {
                    result.addSource(
                        dataSource
                    ) { newData: ResultType? ->
                        result.setValue(
                            error(
                                t.toString(),
                                newData,
                                Resource.ErrorType.UNKNOWN
                            )
                        )
                    }

                }
                Log.i("ERROR", t.toString())
            }
        })
    }

    @MainThread
    private fun saveResultAndReInit(response: RequestType?) {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg params: Void?): Void? {
                Log.i(
                    TAG,
                    "SAVING FETCHED DATA TO ROOM DB"
                )
                saveCallResult(response)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                Log.i(
                    TAG,
                    "ADAING SHOWING LOCAL DATA WITH NEW DATA"
                )
                result.addSource(
                    loadLocalDb()
                ) { newData: ResultType? ->
                    result.setValue(success(newData))
                }
            }
        }.execute()
    }

    @WorkerThread
    protected abstract fun saveCallResult(@NonNull item: RequestType?)

    @MainThread
    abstract fun loadLocalDb(): LiveData<ResultType?>

    @MainThread
    abstract fun callRequest(): Call<ApiResponse<RequestType>>

    @MainThread
    abstract fun shouldFetch(data: ResultType?): Boolean

    fun asLiveData(): LiveData<Resource<ResultType?>> {
        return result
    }

    companion object {
        val TAG = NetworkBoundedResource::class.java.simpleName
    }

    init {
        val localDb = loadLocalDb()
        result.addSource(localDb) { data: ResultType? ->
            result.removeSource(localDb)
            Log.i(TAG, "OFFLINE SOURCE REMOVED")
            if (shouldFetch(data)) {
                Log.i(TAG, "FETCHING FROM REMOTE DB")
                fetchData(localDb)
            } else {
                result.addSource(
                    localDb
                ) { newData: ResultType? ->
                    result.setValue(success(newData))
                }
            }
        }
    }
}