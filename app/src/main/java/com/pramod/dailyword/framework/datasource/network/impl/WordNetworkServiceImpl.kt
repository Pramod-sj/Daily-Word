package com.pramod.dailyword.framework.datasource.network.impl

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.mappers.WordNEMapper
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import com.pramod.dailyword.framework.datasource.network.service.WordApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordNetworkServiceImpl @Inject constructor(
    private val wordApiService: WordApiService,
    private val wordNEMapper: WordNEMapper
) : WordNetworkService {
    override suspend fun getWordOfTheDay(): ApiResponse<Word> {
        val apiResponse = wordApiService.getWordOfTheDay()
        return ApiResponse(
            code = apiResponse.code,
            message = apiResponse.message,
            data = apiResponse.data?.let { wordNEMapper.fromEntity(it) }
        )
    }

    override suspend fun getWords(startFrom: String?, limit: Int): ApiResponse<List<Word>> {
        val apiResponse = wordApiService.getWords(startFrom, limit)
        return ApiResponse(
            code = apiResponse.code,
            message = apiResponse.message,
            data = apiResponse.data?.map {
                wordNEMapper.fromEntity(it)
            }
        )
    }

    override suspend fun getWordsPaging(
        search: String,
        pageNo: Int,
        pageSize: Int
    ): ApiResponse<List<Word>> {
        val apiResponse = wordApiService.getWordsPaging(search, pageNo, pageSize)
        return ApiResponse(
            code = apiResponse.code,
            message = apiResponse.message,
            data = apiResponse.data?.map {
                wordNEMapper.fromEntity(it)
            }
        )
    }

    override suspend fun getRandomWord(): ApiResponse<Word> {
        val apiResponse = wordApiService.getRandomWord()
        return ApiResponse(
            code = apiResponse.code,
            message = apiResponse.message,
            data = apiResponse.data?.let { wordNEMapper.fromEntity(it) }
        )
    }
}