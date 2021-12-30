package com.pramod.dailyword.business.data.network.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.utils.handleApiException
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import com.pramod.dailyword.framework.prefmanagers.RemoteKeyPrefManager
import retrofit2.HttpException

@ExperimentalPagingApi
class WordPaginationRemoteMediator(
    val search: String,
    private val skipInitialRefresh: Boolean = false,
    private val wordNetworkDataSource: WordNetworkDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val remoteKeyPrefManager: RemoteKeyPrefManager
) : RemoteMediator<Int, BookmarkedWordCE>() {

    companion object {
        val TAG = WordPaginationRemoteMediator::class.simpleName
    }

    override suspend fun initialize(): InitializeAction {
        return if (skipInitialRefresh) InitializeAction.SKIP_INITIAL_REFRESH
        else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BookmarkedWordCE>
    ): MediatorResult {
        return try {

            val nextLoadKey: Any = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.APPEND -> {
                    if (remoteKeyPrefManager.isReachedToEnd()) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    (remoteKeyPrefManager.getNextRemoteKey().toString().toIntOrNull() ?: 0)
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            val pageNo = nextLoadKey.toString().toIntOrNull() ?: 0

            Log.i(TAG, "load: page no to be fetched:$pageNo and size:${state.config.pageSize}")

            val resource = wordNetworkDataSource.getWordsPaging(
                search = search,
                pageNo = pageNo,
                pageSize = state.config.pageSize
            )

            if (resource.code == "200") {

                if (loadType == LoadType.REFRESH) {
                    Log.i(TAG, "LOAD TYPE: REFRESH -- DELETE ALL WORDS")
                    remoteKeyPrefManager.setNextRemoteKey(null)
                    if (search.isEmpty()) {
                        wordCacheDataSource.deleteAll()
                    } else {
                        wordCacheDataSource.deleteAllExceptTop(10)
                    }
                }

                resource.data?.let {
                    Log.i(TAG, "INSERT COUNT ${it.size}")
                    wordCacheDataSource.addAll(it)
                }

                val isReachedToEnd = (resource.data?.size ?: 0) < state.config.pageSize

                remoteKeyPrefManager.setReachedToEnd(isReachedToEnd)

                if (!isReachedToEnd) {
                    remoteKeyPrefManager.setNextRemoteKey((pageNo + 1).toString())
                }

                MediatorResult.Success(
                    endOfPaginationReached = isReachedToEnd
                )
            } else {
                MediatorResult.Error(Exception(resource.message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(handleApiException(e))
        }
    }


}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.toString()
    } catch (exception: Exception) {
        "Unknown"
    }
}
