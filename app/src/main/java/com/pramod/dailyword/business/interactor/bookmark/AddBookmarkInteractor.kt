package com.pramod.dailyword.business.interactor.bookmark

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.utils.safeCacheCall
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Bookmark
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddBookmarkInteractor @Inject constructor(
    private val bookmarkCacheDataSource: BookmarkCacheDataSource
) {
    fun addBookmark(bookmark: Bookmark): Flow<Resource<Long?>> {
        return flow {
            emit(Resource.loading())
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                bookmarkCacheDataSource.insert(bookmark)
            }
            val resource: Resource<Long?> = if (cacheResult.error == null) {
                Resource.success(cacheResult.data)
            } else {
                Resource.error(cacheResult.error, null)
            }
            emit(resource)
        }
    }
}