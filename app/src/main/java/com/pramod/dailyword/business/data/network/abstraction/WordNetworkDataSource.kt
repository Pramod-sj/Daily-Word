package com.pramod.dailyword.business.data.network.abstraction

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse

interface WordNetworkDataSource {
    suspend fun getWordOfTheDay(): ApiResponse<Word>

    suspend fun getWords(
        startFrom: String? = null,
        limit: Int
    ): ApiResponse<List<Word>>

    suspend fun getRandomWord(): ApiResponse<Word>

}