package com.pramod.dailyword.framework.helper.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.google.gson.Gson
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.Security
import com.pramod.dailyword.framework.prefmanagers.BasePreferenceManager
import kotlinx.coroutines.launch
import java.io.IOException


class BillingHelper constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val skus: List<String>,
) : BasePreferenceManager(PREF_BILLING, context),
    LifecycleObserver,
    BillingClientStateListener,
    PurchasesUpdatedListener {

    //cache skus with details
    private val skuDetails = mutableMapOf<String, SkuDetails>()

    //cache purchases
    private val purchases = mutableMapOf<String, Purchase>()

    private var billingListener: BillingListener? = null

    fun setBillingListener(billingListener: BillingListener) {
        this.billingListener = billingListener;
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
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }


    fun isInAppPurchaseSupported(): Boolean =
        billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR)
            .responseCode != BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED

    /**
     * This method only helps to purchase products of inapp type
     * @param activity
     * @param sku
     */
    suspend fun buy(activity: Activity, sku: String) {
        if (isInitializedAndReady()) {
            when {
                isPurchased(sku) -> {
                    if (isPurchasePending(sku)) {
                        billingListener?.onBillingError("Please wait your purchase is under process, check on this after some time.")
                    } else {
                        Log.i(TAG, "buy: already purchased")
                        billingListener?.onBillingError("You have already donated this item, Thank you so much :)")
                    }
                }
                else -> {
                    sku.toSkuDetails(INAPP)?.let {
                        billingClient.launchBillingFlow(
                            activity, BillingFlowParams.newBuilder()
                                .setSkuDetails(it)
                                .build()
                        )
                    }
                }
            }
        }

    }

    /**
     * fetch owned product
     */
    private suspend fun getAllPurchase(): List<Purchase> {
        if (purchases.isEmpty()) {
            billingClient.queryPurchasesAsync(INAPP).purchasesList.forEach {
                purchases[it.purchaseToken] = it
            }
        }
        return ArrayList(purchases.values)
    }


    suspend fun isPurchasePending(sku: String): Boolean {
        return getAllPurchase().find {
            sku == it.skus.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PENDING
    }

    suspend fun isPurchasedSucceed(sku: String): Boolean {
        return getAllPurchase().find {
            sku == it.skus.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    suspend fun isPurchased(sku: String): Boolean {
        return getAllPurchase().find {
            sku == it.skus.firstOrNull()
        } != null
    }

    suspend fun isPurchasedAndAcknowledge(sku: String): Boolean {
        return getAllPurchase().find { purchaseHistoryRecord ->
            purchaseHistoryRecord.skus.find { purchasedSku -> purchasedSku == sku } != null
        }?.purchaseToken?.let {
            val billingResult = billingClient.consumePurchase(
                ConsumeParams.newBuilder().setPurchaseToken(it).build()
            )

            billingResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK
        } ?: false
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
        val acknowledgePurchaseResult =
            billingClient.acknowledgePurchase(acknowledgePurchaseParams)
        return acknowledgePurchaseResult.responseCode == BillingClient.BillingResponseCode.OK
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

    }

    private fun isValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64Key = BuildConfig.GOOGLE_IN_APP_RSA_KEY
            Security.verifyPurchase(base64Key, signedData, signature);
        } catch (e: IOException) {
            false
        }
    }


    override fun onBillingServiceDisconnected() {
        Log.i(TAG, "onBillingServiceDisconnected: ")
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        Log.i(TAG, "onBillingSetupFinished: ")
        if (p0.responseCode.isBillingResultOk()) {
            billingListener?.onBillingInitialized()
            lifecycleOwner.lifecycleScope.launch {
                val skuDetailsList = querySkus()
                skuDetailsList?.let {
                    billingListener?.onSkuDetailsAvailable(it)
                }
                getAllPurchase()?.let { purchases ->
                    processPurchase(purchases, true)
                }
            }
            // The BillingClient is ready. You can query purchases here.
        }
    }

    //fetch skus and cache in skuDetails map
    private suspend fun querySkus(): List<SkuDetails>? {
        if (!isInitializedAndReady()) {
            Log.i(TAG, "querySkus: Not initialized or read")
            return null
        }
        val skuDetailsList = skus.toSkuDetailsList(INAPP)
        skuDetailsList?.forEach {
            skuDetails[it.sku] = it
        }
        return skuDetailsList
    }


    private suspend fun queryPurchase() {
        billingClient.queryPurchasesAsync(INAPP)
    }

    private suspend fun processPurchase(
        purchases: List<Purchase>,
        isRestored: Boolean
    ) {

        for (purchase in purchases) {

            //storing purchase in cache
            this.purchases[purchase.purchaseToken] = purchase

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (!isValidSignature(purchase.originalJson, purchase.signature)) {
                        // Invalid purchase
                        // show error to user
                        Log.i(TAG, "processPurchase: invalid purchase")
                        continue
                    }
                    // else purchase is valid
                    //if item is purchased and not acknowledged
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        val result =
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                        if (result.responseCode.isBillingResultOk()) {
                            //if purchase is acknowledged Grant entitlement to the user
                            if (isRestored) {
                                billingListener?.onPurchasedRestored(purchase.skus.first())
                            } else {
                                billingListener?.onPurchased(purchase.skus.first())
                            }
                        }
                    } else {
                        // Grant entitlement to the user on item purchase
                        if (isRestored) {
                            billingListener?.onPurchasedRestored(purchase.skus.first())
                        } else {
                            billingListener?.onPurchased(purchase.skus.first())
                        }
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    Log.e(
                        TAG,
                        "processPurchase: ${Gson().toJson(purchase.skus)}: Purchase State: pending: ${purchase.purchaseState}"
                    )
                    billingListener?.onPurchasePending(purchase.skus.first())
                }
                else -> {
                    Log.e(TAG, "processPurchase: Purchase State: ${purchase.purchaseState}")
                }
            }

        }
    }

    //convert sku to sku details
    private suspend fun String.toSkuDetails(skuType: String): SkuDetails? {
        return skuDetails[this] ?: billingClient.querySkuDetails(
            SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(listOf(this)).build()
        ).skuDetailsList?.firstOrNull()
    }

    //convert list of sku to list of sku details
    private suspend fun List<String>.toSkuDetailsList(skuType: String): List<SkuDetails>? {
        return billingClient.querySkuDetails(
            SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(this).build()
        ).skuDetailsList
    }


    private fun Int.isBillingResultOk(): Boolean = this == BillingClient.BillingResponseCode.OK

    //billing client is Initialized and Ready to use
    private fun isInitializedAndReady() =
        ::billingClient.isInitialized && billingClient.isReady

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        lifecycleOwner.lifecycleScope.launch {
            // To be implemented in a later section.
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.i(TAG, "onPurchasesUpdated: OK")
                    purchases?.let {
                        processPurchase(purchases, false)
                    }
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Log.i(TAG, "onPurchasesUpdated: ITEM_ALREADY_OWNED")
                    purchases?.let {
                        processPurchase(purchases, true)
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.i(TAG, "onPurchasesUpdated: USER_CANCELED")
                    billingListener?.onBillingError("Purchase wasn't made, you can try again now or later. Thank you :)")
                }
                else -> billingListener?.onBillingError(billingResult.debugMessage)
            }


        }
    }

}