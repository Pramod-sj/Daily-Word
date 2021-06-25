package com.pramod.dailyword.framework.helper.billing

import android.app.Activity
import com.android.billingclient.api.SkuDetails

abstract class BillingListenerHandler {

    private val billingListeners = mutableSetOf<BillingListener>()

    private val purchaseListeners = mutableSetOf<PurchaseListener>()


    fun addBillingListener(billingListener: BillingListener) {
        billingListeners.add(billingListener)
    }

    fun removeBillingListener(billingListener: BillingListener) {
        billingListeners.remove(billingListener)
    }

    fun addPurchaseListener(purchaseListener: PurchaseListener) {
        purchaseListeners.add(purchaseListener)
    }

    fun removePurchaseListener(purchaseListener: PurchaseListener) {
        purchaseListeners.remove(purchaseListener)
    }


    fun purchased(sku: String, isRestored: Boolean) {
        purchaseListeners.forEach {
            if (isRestored)
                it.onPurchasedRestored(sku)
            else
                it.onPurchased(sku)
        }
    }

    fun purchasePending(sku: String) {
        purchaseListeners.forEach {
            it.onPurchasePending(sku)
        }
    }

    fun emitInitialPurchaseProcess() {
        billingListeners.forEach { it.onBillingPurchasesProcessed() }
        purchaseListeners.forEach { it.onBillingPurchasesProcessed() }
    }


    fun emitPurchaseError(message: String) {
        purchaseListeners.forEach {
            it.onPurchaseError(message)
        }
    }

    fun emitBillingClientError(message: String) {
        billingListeners.forEach {
            it.onBillingError(message)
        }
        purchaseListeners.forEach {
            it.onBillingError(message)
        }
    }

    fun emitBillingInitialized() {
        billingListeners.forEach {
            it.onBillingInitialized()
        }
        purchaseListeners.forEach {
            it.onBillingInitialized()
        }
    }

    fun emitSkuDetails(skuDetailsList: List<SkuDetails>) {
        billingListeners.forEach {
            it.onBillingSkuDetailsAvailable(skuDetailsList)
        }
        purchaseListeners.forEach {
            it.onBillingSkuDetailsAvailable(skuDetailsList)
        }
    }


    abstract suspend fun buy(activity: Activity, sku: String)

    fun removeListeners() {
        billingListeners.clear()
        purchaseListeners.clear()
    }


}


interface BillingListener {

    //this method is invoked when billing client is ready to use
    fun onBillingInitialized()

    //if there any error in billing client this method will be invoked
    fun onBillingError(message: String)

    //called when list of sku details is available
    fun onBillingSkuDetailsAvailable(skuDetailsList: List<SkuDetails>)

    //called when initial processing for any previous purchase is done
    fun onBillingPurchasesProcessed()

}

interface PurchaseListener : BillingListener {

    fun onPurchased(sku: String)

    fun onPurchasedRestored(sku: String)

    fun onPurchasePending(sku: String)

    fun onPurchaseError(message: String)

}


open class BillingListenerImpl : BillingListener {
    override fun onBillingInitialized() {

    }

    override fun onBillingError(message: String) {

    }

    override fun onBillingSkuDetailsAvailable(skuDetailsList: List<SkuDetails>) {

    }

    override fun onBillingPurchasesProcessed() {

    }

}

open class PurchaseListenerImpl : PurchaseListener {
    override fun onPurchased(sku: String) {

    }

    override fun onPurchasedRestored(sku: String) {

    }

    override fun onPurchasePending(sku: String) {

    }

    override fun onPurchaseError(message: String) {

    }

    override fun onBillingInitialized() {

    }

    override fun onBillingError(message: String) {

    }

    override fun onBillingSkuDetailsAvailable(skuDetailsList: List<SkuDetails>) {

    }

    override fun onBillingPurchasesProcessed() {

    }

}
