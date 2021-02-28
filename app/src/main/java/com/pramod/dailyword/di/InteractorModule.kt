package com.pramod.dailyword.di

import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.interactor.*
import com.pramod.dailyword.business.interactor.bookmark.AddBookmarkInteractor
import com.pramod.dailyword.business.interactor.bookmark.RemoveBookmarkInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
object InteractorModule {

    @Provides
    fun provideGetRandomWordInteractor(
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
        wordCacheDataSource: WordCacheDataSource,
        wordNetworkDataSource: WordNetworkDataSource
    ): GetRandomWordInteractor {
        return GetRandomWordInteractor(
            bookmarkedWordCacheDataSource,
            wordCacheDataSource,
            wordNetworkDataSource
        )
    }

    @Provides
    fun provideGetRecapWordsInteractor(
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
        wordCacheDataSource: WordCacheDataSource,
        wordNetworkDataSource: WordNetworkDataSource
    ): GetRecapWordsInteractor {
        return GetRecapWordsInteractor(
            bookmarkedWordCacheDataSource, wordCacheDataSource, wordNetworkDataSource
        )
    }

    @Provides
    fun provideGetWordDetailsByDateInteractor(
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
        wordCacheDataSource: WordCacheDataSource,
        wordNetworkDataSource: WordNetworkDataSource
    ): GetWordDetailsByDateInteractor {
        return GetWordDetailsByDateInteractor(
            bookmarkedWordCacheDataSource, wordCacheDataSource, wordNetworkDataSource
        )
    }

    @Provides
    fun provideGetWordsInteractor(
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
        wordCacheDataSource: WordCacheDataSource,
        wordNetworkDataSource: WordNetworkDataSource
    ): GetWordsInteractor {
        return GetWordsInteractor(
            bookmarkedWordCacheDataSource, wordCacheDataSource, wordNetworkDataSource
        )
    }

    @ExperimentalPagingApi
    @Provides
    fun provideGetBookmarkedWordsInteractor(
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
    ): GetBookmarkedWordList {
        return GetBookmarkedWordList(
            bookmarkedWordCacheDataSource
        )
    }

    @Provides
    fun provideAddBookmarkInteractor(bookmarkCacheDataSource: BookmarkCacheDataSource): AddBookmarkInteractor {
        return AddBookmarkInteractor(bookmarkCacheDataSource)
    }

    @Provides
    fun provideRemoveBookmarkInteractor(bookmarkCacheDataSource: BookmarkCacheDataSource): RemoveBookmarkInteractor {
        return RemoveBookmarkInteractor(bookmarkCacheDataSource)
    }

    @Provides
    fun provideMarkWordAsSeenInteractor(wordCacheDataSource: WordCacheDataSource): MarkWordAsSeenInteractor {
        return MarkWordAsSeenInteractor(wordCacheDataSource)
    }

    @ExperimentalPagingApi
    @Provides
    fun provideGetWordListInteractor(
        wordPaginationRemoteMediator: WordPaginationRemoteMediator,
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
    ): GetWordListInteractor {
        return GetWordListInteractor(wordPaginationRemoteMediator,bookmarkedWordCacheDataSource)
    }


}