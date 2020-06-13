package com.pramod.dailyword.db.model

class ApiResponse<T> {
    var code: String? = "0"
    var message: String? = null
    var data: T? = null
}