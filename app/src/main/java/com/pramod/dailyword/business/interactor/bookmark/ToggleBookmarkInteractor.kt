package com.pramod.dailyword.business.interactor.bookmark

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.business.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class ToggleBookmarkInteractor (
    private val bookmarkCacheDataSource: BookmarkCacheDataSource,
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
) {

    fun toggle(wordName: String): Flow<Resource<Word?>> {
        return flow {
            emit(Resource.loading())
            val word = bookmarkedWordCacheDataSource.getWordByNameNonLive(wordName)
            if (word?.bookmarkedId == null) {
                bookmarkCacheDataSource.insert(
                    Bookmark(
                        bookmarkId = null,
                        bookmarkedWord = word?.word,
                        bookmarkedAt = Calendar.getInstance().timeInMillis
                    )
                )
            } else {
                bookmarkCacheDataSource.delete(wordName)
            }
        }
    }

}