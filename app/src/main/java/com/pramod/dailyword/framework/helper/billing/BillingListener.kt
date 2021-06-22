package com.pramod.dailyword.framework.helper.billing

import com.android.billingclient.api.SkuDetails

interface BillingListener {

    fun onBillingInitialized()

    fun onBillingError(message: String)

    fun onSkuDetailsAvailable(skuDetailsList: List<SkuDetails>)

    fun onPurchased(sku: String)

    fun onPurchasedRestored(sku: String)

    fun onPurchasePending(sku: String)

}