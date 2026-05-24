package com.pramod.dailyword.network

data class ApiResponse<T>(
    var code: String,
    var message: String,
    var data: T?,
)