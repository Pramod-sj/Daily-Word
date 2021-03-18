package com.pramod.dailyword.di

import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.SeenCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.interactor.*
import com.pramod.dailyword.business.interactor.bookmark.AddBookmarkInteractor
import com.pramod.dailyword.business.interactor.bookmark.GetAllBookmarks
import com.pramod.dailyword.business.interactor.bookmark.RemoveBookmarkInteractor
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideMarkWordAsSeenInteractor(
        seenCacheDataSource: SeenCacheDataSource
    ): MarkWordAsSeenInteractor {
        return MarkWordAsSeenInteractor(seenCacheDataSource)
    }

    @ExperimentalPagingApi
    @Provides
    fun provideGetWordListInteractor(
        wordPaginationRemoteMediator: WordPaginationRemoteMediator,
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
    ): GetWordListInteractor {
        return GetWordListInteractor(wordPaginationRemoteMediator, bookmarkedWordCacheDataSource)
    }


    @Provides
    @Singleton
    fun provideMarkBookmarkedWordAsSeenInteractor(
        bookmarkCacheDataSource: BookmarkCacheDataSource
    ): MarkBookmarkedWordAsSeenInteractor {
        return MarkBookmarkedWordAsSeenInteractor(bookmarkCacheDataSource)
    }

    @Provides
    @Singleton
    fun provideGetAllBookmarks(
        bookmarkCacheDataSource: BookmarkCacheDataSource
    ): GetAllBookmarks {
        return GetAllBookmarks(bookmarkCacheDataSource)
    }

    @Provides
    @Singleton
    fun provideToggleBookmark(
        bookmarkCacheDataSource: BookmarkCacheDataSource,
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    ): ToggleBookmarkInteractor {
        return ToggleBookmarkInteractor(bookmarkCacheDataSource, bookmarkedWordCacheDataSource)
    }

}