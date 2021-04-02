package com.pramod.dailyword.business.domain.model

data class IPInfo (
    val status: String?,
    val country: String?,
    var countryCode: String?,
    var zip: String?,
    var city: String?,
    var regionName: String?,
    var region: String?
)