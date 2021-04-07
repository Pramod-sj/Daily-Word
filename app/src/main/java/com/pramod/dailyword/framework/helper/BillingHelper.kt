package com.pramod.dailyword.framework.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.*
import com.google.gson.Gson

class BillingHelper private constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onBillingInitialized: (() -> Unit)? = null,
    private val onProductPurchased: ((productId: String, purchase: Purchase?) -> Unit)? = null,
    private val onBillingError: ((String) -> Unit)? = null
) : LifecycleObserver {

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                //start connection
                startConnection()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                onBillingError?.invoke("User cancelled the purchase flow")
            } else {
                onBillingError?.invoke(billingResult.debugMessage)
            }
        }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        Log.i(TAG, ":init ")
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private fun startConnection() {
        Log.i(TAG, "startConnection: " + billingClient.isReady)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(
                        TAG,
                        "onBillingSetupFinished: BillingResponseCode.OK: " + billingClient.isReady
                    )
                    onBillingInitialized?.invoke()
                    fetchAllPurchase()
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            override fun onBillingServiceDisconnected() {

            }

        })
    }

    /**
     * This method only helps to purchase products of inapp type
     * @param activity
     * @param itemId
     */
    fun purchase(activity: Activity, itemId: String) {
        if (billingClient.isReady) {
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(arrayListOf(itemId))
                .build()
            billingClient.querySkuDetailsAsync(
                skuDetailsParams
            ) { p0, p1 ->
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (!p1.isNullOrEmpty()) {
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(p1[0])
                            .build()
                        billingClient.launchBillingFlow(
                            activity,
                            billingFlowParams
                        )
                    }
                } else {
                    onBillingError?.invoke(p0.debugMessage)
                }
            }
        } else {
            Log.i(TAG, "purchase: billingClient is not connected!")
            startConnection()
        }
    }

    /**
     * fetch owned product
     */
    private fun fetchAllPurchase() {
        Log.i(TAG, "fetchAllPurchase: ")
        val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "fetchAllPurchase: ok")
            result.purchasesList?.forEach {


                Log.i(TAG, "fetchAllPurchase: " + Gson().toJson(it))
            }
        } else {
            Log.i(TAG, "fetchAllPurchase: " + result.billingResult.debugMessage)
        }
    }

    fun isPurchased(itemId: String): Boolean {
        return false
    }


    private fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
                onProductPurchased?.invoke(purchase.orderId, purchase)
            } else {

            }
        }
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    private fun endConnection() {
        Log.i(TAG, "endConnection: ")
        billingClient.endConnection()
    }

    companion object {
        val TAG = BillingHelper::class.java.simpleName


        fun newInstance(
            context: Context, lifecycleOwner: LifecycleOwner,
            onBillingInitialized: (() -> Unit)? = null,
            onProductPurchased: ((productId: String, purchase: Purchase?) -> Unit)? = null,
            onBillingError: ((String) -> Unit)? = null
        ): BillingHelper {
            val billingHelper =
                BillingHelper(
                    context,
                    lifecycleOwner,
                    onBillingInitialized,
                    onProductPurchased,
                    onBillingError
                )
            Log.i(TAG, "newInstance: start connection")
            //immediately start connection
            billingHelper.startConnection()
            return billingHelper
        }
    }

}