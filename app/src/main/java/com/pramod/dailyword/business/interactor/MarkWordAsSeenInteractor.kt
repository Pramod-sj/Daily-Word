package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.cache.utils.safeCacheCall
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class MarkWordAsSeenInteractor(
    private val wordCacheDataSource: WordCacheDataSource
) {
    fun markAsSeen(word: Word): Flow<Resource<Int?>> {
        return flow {
            emit(Resource.loading())
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val updatedWord = Word(
                    word.word,
                    word.pronounce,
                    word.pronounceAudio,
                    word.meanings,
                    word.didYouKnow,
                    word.attribute,
                    word.examples,
                    word.date,
                    word.dateTimeInMillis,
                    true,
                    Calendar.getInstance().timeInMillis,
                    word.wordColor,
                    word.wordDesaturatedColor,
                    word.synonyms,
                    word.antonyms,
                    word.bookmarkedId,
                    word.bookmarkedAt
                )
                wordCacheDataSource.update(updatedWord)
            }
            val resource: Resource<Int?> = if (cacheResult.error == null) {
                Resource.success(cacheResult.data)
            } else Resource.error(cacheResult.error, null)
            emit(resource)
        }
    }
}