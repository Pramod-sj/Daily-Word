package com.pramod.dailyword.framework.datasource.network.model

data class IPInfoNE(
    val status: String?,
    val country: String?,
    var countryCode: String?,
    var zip: String?,
    var city: String?,
    var regionName: String?,
    var region: String?
)