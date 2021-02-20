package com.pramod.dailyword.business.data.network.utils

import com.pramod.dailyword.business.data.network.Resource


abstract class ApiResponseHandler<ApiResponse, Data>(
    private val response: ApiResult<ApiResponse?>
) {

    suspend fun getResult(): Resource<Data?> {

        return when (response) {

            is ApiResult.GenericError -> {
                Resource.error(throwable = Throwable(response.message))
            }

            is ApiResult.NetworkError -> {
                Resource.error(throwable = NetworkError())
            }

            is ApiResult.Success -> {
                if (response.data == null) {
                    Resource.error(throwable = Throwable("No response from api"))
                } else {
                    handleSuccess(resultObj = response.data)
                }
            }

        }
    }

    abstract suspend fun handleSuccess(resultObj: ApiResponse): Resource<Data?>

}