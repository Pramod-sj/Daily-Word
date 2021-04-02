package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.utils.safeCacheCall
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class MarkBookmarkedWordAsSeenInteractor(
    private val bookmarkCacheDataSource: BookmarkCacheDataSource
) {
    fun markAsSeen(word: String): Flow<Resource<Int?>> {
        return flow {
            emit(Resource.loading())
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val bookmarked = bookmarkCacheDataSource.get(word)
                bookmarked?.let {
                    val seen = Bookmark(
                        bookmarkId = it.bookmarkId,
                        bookmarkedWord = it.bookmarkedWord,
                        bookmarkedAt = it.bookmarkedAt,
                        bookmarkSeenAt = Calendar.getInstance().timeInMillis
                    )
                    bookmarkCacheDataSource.update(seen)
                }
            }
            val resource: Resource<Int?> = if (cacheResult.error == null) {
                Resource.success(cacheResult.data)
            } else Resource.error(cacheResult.error, null)
            emit(resource)
        }
    }

}