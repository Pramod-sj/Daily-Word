package com.pramod.dailyword.framework.datasource.network.model.api

data class ApiResponse<T>(
    var code: String,
    var message: String,
    var data: T?
)