package com.pramod.dailyword.framework.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.Security
import com.pramod.dailyword.framework.prefmanagers.BasePreferenceManager
import kotlinx.coroutines.launch
import java.io.IOException


class BillingHelper private constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onBillingInitialized: (() -> Unit)? = null,
    private val onProductPurchased: ((productId: String, purchase: Purchase?) -> Unit)? = null,
    private val onBillingError: ((String) -> Unit)? = null
) : BasePreferenceManager(PREF_BILLING, context), LifecycleObserver {


    private var onGoingPurchaseProductId: String? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases)
            }
            //if item already purchased then check and reflect changes
            else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                billingClient.queryPurchasesAsync(
                    INAPP
                ) { p0, alreadyPurchases ->
                    if (alreadyPurchases.isNotEmpty()) {
                        handlePurchases(alreadyPurchases);
                    }
                };
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                //start connection
                startConnection()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                onBillingError?.invoke("Purchase wasn't made, you can try again now or later. Thank you :)")
            } else {
                onBillingError?.invoke(billingResult.debugMessage)
            }
        }

    private lateinit var billingClient: BillingClient

    init {
        Log.i(TAG, ":init ")
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.d(TAG, "ON_CREATE")
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            startConnection()
        }
    }


    fun isInAppPurchaseSupported(): Boolean =
        billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR)
            .responseCode != BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED

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
                    lifecycleOwner.lifecycleScope.launch {
                        getAllPurchase()
                    }
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
     * @param sku
     */
    suspend fun purchase(activity: Activity, sku: String) {
        if (billingClient.isReady) {
            this.onGoingPurchaseProductId = sku
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setType(INAPP)
                .setSkusList(arrayListOf(sku))
                .build()
            val skuDetailsResult = billingClient.querySkuDetails(skuDetailsParams);
            if (skuDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                skuDetailsResult.skuDetailsList?.get(0)?.let { skuDetails ->
                    if (isPurchased(sku)) {
                        if (acknowledgePurchaseByProductId(sku)) {
                            onBillingError?.invoke("You have already donated this item, Thank you so much :)")
                        } else {
                            onBillingError?.invoke("Your purchase is still not completed, Please wait...")
                        }
                    } else {
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build()
                        billingClient.launchBillingFlow(
                            activity,
                            billingFlowParams
                        )

                    }
                }
            } else {
                onBillingError?.invoke(skuDetailsResult.billingResult.debugMessage)
            }
        } else {
            Log.i(TAG, "purchase: billingClient is not connected!")
            startConnection()
        }
    }

    /**
     * fetch owned product
     */
    suspend fun getAllPurchase(): List<PurchaseHistoryRecord>? {
        return billingClient.queryPurchaseHistory(INAPP).purchaseHistoryRecordList
    }

    suspend fun isPurchased(sku: String): Boolean {
        return getAllPurchase()?.find { purchaseHistoryRecord ->
            purchaseHistoryRecord.skus.find { purchasedSku -> purchasedSku == sku } != null
        } != null
    }

    suspend fun getProductDetailsList(skuList: List<String>): List<SkuDetails>? {
        return billingClient.querySkuDetails(
            params = SkuDetailsParams.newBuilder()
                .setType(INAPP)
                .setSkusList(skuList)
                .build()
        ).skuDetailsList
    }

    private suspend fun getPurchaseHistoryByProductId(productId: String): PurchaseHistoryRecord? {
        return billingClient.queryPurchaseHistory(INAPP).purchaseHistoryRecordList?.find { purchaseHistoryRecord ->
            purchaseHistoryRecord.skus.find { sku -> sku == productId } != null
        }
    }

    suspend fun acknowledgePurchaseByProductId(productId: String): Boolean {
        return getPurchaseHistoryByProductId(productId)?.let { purchaseHistoryRecord ->
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseHistoryRecord.purchaseToken)
                .build()
            val acknowledgePurchaseResult =
                billingClient.acknowledgePurchase(acknowledgePurchaseParams)
            acknowledgePurchaseResult.responseCode == BillingClient.BillingResponseCode.OK
        } ?: false
    }

    suspend fun acknowledgePurchaseByToken(purchaseToken: String): Boolean {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val acknowledgePurchaseResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
        return acknowledgePurchaseResult.responseCode == BillingClient.BillingResponseCode.OK
    }


    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {

            Log.i(TAG, "handlePurchases: " + purchase.originalJson)

            val isCurrentProductPurchased =
                purchase.skus.find { sku -> onGoingPurchaseProductId == sku } != null

            //if item is purchased
            if (isCurrentProductPurchased && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    // Invalid purchase
                    // show error to user
                    onBillingError?.invoke("Error : Invalid Purchase")
                    return
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(
                        acknowledgePurchaseParams
                    ) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            //if purchase is acknowledged
                            // Grant entitlement to the user. and restart activity
                            // savePurchaseValueToPref(true)
                            onProductPurchased?.invoke(
                                onGoingPurchaseProductId ?: "",
                                purchase
                            )
                        }
                    }
                } else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    /*if (!getPurchaseValueFromPref()) {
                        savePurchaseValueToPref(true)
                        Toast.makeText(
                            ApplicationProvider.getApplicationContext(),
                            "Item Purchased",
                            Toast.LENGTH_SHORT
                        ).show()
                        this.recreate()
                    }*/
                }
            } else if (isCurrentProductPurchased && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                onBillingError?.invoke("Purchase is Pending. Please complete Transaction")
            } else if (isCurrentProductPurchased && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                onBillingError?.invoke("Purchase Status Unknown")
            }
        }
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        Log.i(TAG, "endConnection: ")
        billingClient.endConnection()
    }

    companion object {
        val TAG = BillingHelper::class.java.simpleName

        const val PREF_BILLING = "billing"

        const val KEY_ACKNOWLEDGE_PURCHASE_PRODUCT_ID = "acknowledge_purchase_product_id";

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
            return billingHelper
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64Key = BuildConfig.GOOGLE_IN_APP_RSA_KEY
            Security.verifyPurchase(base64Key, signedData, signature);
        } catch (e: IOException) {
            false
        }
    }


}