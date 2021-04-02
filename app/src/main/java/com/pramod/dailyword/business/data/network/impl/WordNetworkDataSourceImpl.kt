package com.pramod.dailyword.business.data.network.impl

import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse

class WordNetworkDataSourceImpl(private val wordNetworkService: WordNetworkService) :
    WordNetworkDataSource {
    override suspend fun getWordOfTheDay(): ApiResponse<Word> {
        return wordNetworkService.getWordOfTheDay()
    }

    override suspend fun getWords(startFrom: String?, limit: Int): ApiResponse<List<Word>> {
        return wordNetworkService.getWords(startFrom, limit)
    }

    override suspend fun getRandomWord(): ApiResponse<Word> {
        return wordNetworkService.getRandomWord()
    }
}