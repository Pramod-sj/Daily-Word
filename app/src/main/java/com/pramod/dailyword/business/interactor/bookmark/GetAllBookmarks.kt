package com.pramod.dailyword.business.interactor.bookmark

import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Bookmark
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllBookmarks @Inject constructor(
    private val bookmarkCacheDataSource: BookmarkCacheDataSource
) {

    fun getBookmarks(): Flow<Resource<List<Bookmark>?>> {
        return flow {
            emit(Resource.loading())
            emitAll(
                bookmarkCacheDataSource.getAll().map {
                    Resource.success(it)
                }.asFlow()
            )
        }
    }
}