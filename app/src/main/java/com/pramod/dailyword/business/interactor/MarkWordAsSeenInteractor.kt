package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.SeenCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.cache.utils.safeCacheCall
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.domain.model.Seen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class MarkWordAsSeenInteractor(
    private val seenCacheDataSource: SeenCacheDataSource
) {
    fun markAsSeen(word: String): Flow<Resource<Long?>> {
        return flow {
            emit(Resource.loading())
            val cacheResult = safeCacheCall(Dispatchers.IO) {
                val seen = Seen(
                    word = word,
                    seenAt = Calendar.getInstance().timeInMillis
                )
                seenCacheDataSource.add(seen)
            }
            val resource: Resource<Long?> = if (cacheResult.error == null) {
                Resource.success(cacheResult.data)
            } else Resource.error(cacheResult.error, null)
            emit(resource)
        }
    }

}