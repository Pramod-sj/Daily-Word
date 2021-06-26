package com.pramod.dailyword.business.data.network.impl

import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordNetworkDataSourceImpl @Inject constructor(private val wordNetworkService: WordNetworkService) :
    WordNetworkDataSource {
    override suspend fun getWordOfTheDay(): ApiResponse<Word> {
        return wordNetworkService.getWordOfTheDay()
    }

    override suspend fun getWords(startFrom: String?, limit: Int): ApiResponse<List<Word>> {
        return wordNetworkService.getWords(startFrom, limit)
    }

    override suspend fun getWordsPaging(
        search: String,
        pageNo: Int,
        pageSize: Int
    ): ApiResponse<List<Word>> {
        return wordNetworkService.getWordsPaging(search, pageNo, pageSize)
    }

    override suspend fun getRandomWord(): ApiResponse<Word> {
        return wordNetworkService.getRandomWord()
    }
}