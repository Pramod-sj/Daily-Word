package com.pramod.dailyword.business.interactor.bookmark

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.utils.safeCacheCall
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Bookmark
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleBookmarkInteractor @Inject constructor(
    private val bookmarkCacheDataSource: BookmarkCacheDataSource,
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
) {

    fun toggle(wordName: String): Flow<Resource<ToggleResult?>> {
        return flow {
            emit(Resource.loading())
            val wordResult = safeCacheCall(Dispatchers.IO) {
                bookmarkedWordCacheDataSource.getWordByNameNonLive(wordName)
            }
            if (wordResult.data?.bookmarkedId == null) {
                val result = safeCacheCall(Dispatchers.IO) {
                    bookmarkCacheDataSource.insert(
                        Bookmark(
                            bookmarkId = null,
                            bookmarkedWord = wordResult.data?.word,
                            bookmarkedAt = Calendar.getInstance().timeInMillis,
                            bookmarkSeenAt = null
                        )
                    )
                }
                if (result.error != null) {
                    emit(Resource.error<ToggleResult>(result.error))
                } else {
                    emit(
                        Resource.success(
                            ToggleResult(
                                toggle = true,
                                word = wordName
                            )
                        )
                    )
                }

            } else {
                val result = safeCacheCall(Dispatchers.IO) {
                    bookmarkCacheDataSource.delete(wordName)
                }
                if (result.error != null) {
                    emit(Resource.error<ToggleResult>(result.error))
                } else {
                    emit(
                        Resource.success(
                            ToggleResult(
                                toggle = false,
                                word = wordName
                            )
                        )
                    )
                }
            }

        }
    }

}

data class ToggleResult(
    val toggle: Boolean,
    val word: String
)