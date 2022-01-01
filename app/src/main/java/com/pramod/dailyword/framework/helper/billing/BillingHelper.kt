package com.pramod.dailyword.framework.helper.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.Security
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import kotlin.collections.set


class BillingHelper constructor(
    context: Context,
    private val skus: List<String>,
) : BillingListenerHandler(),
    LifecycleObserver,
    BillingClientStateListener,
    PurchasesUpdatedListener {

    //cache skus with details
    private val skuDetails = mutableMapOf<String, SkuDetails>()

    //cache purchases
    private val purchases = mutableMapOf<String, Purchase>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    //GlobalScope coroutine job fetching sku details and processing puchase
    private var fetchSkuDetailsJob: Job? = null

    //GlobalScope coroutine job for purchase update
    private var purchaseUpdateJob: Job? = null

    init {

        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }

    }

    fun close() {
        Timber.i( "close: ")
        fetchSkuDetailsJob?.cancel()
        purchaseUpdateJob?.cancel()
        removeListeners()
        billingClient.endConnection()
    }


    private fun isInAppPurchaseSupported(): Boolean =
        billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR)
            .responseCode != BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED

    /**
     * This method only helps to purchase products of inapp type
     * @param activity
     * @param sku
     */
    override suspend fun buy(activity: Activity, sku: String) {
        if (isInitializedAndReady()) {
            when {
                !isInAppPurchaseSupported() -> {
                    emitPurchaseError("In-app purchase service not available, you need to update google play service")
                }

                isPurchased(sku) -> {
                    if (isPurchasePending(sku)) {
                        emitPurchaseError("Please wait your purchase is under process, check on this after some time.")
                    } else {
                        Timber.i( "buy: already purchased")
                        emitPurchaseError("You have already donated this item, Thank you so much ❤")
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
    suspend fun queryPurchases(): List<Purchase> {
        if (purchases.isEmpty()) {
            billingClient.queryPurchasesAsync(INAPP).purchasesList.forEach {
                purchases[it.purchaseToken] = it
            }
        }
        return ArrayList(purchases.values)
    }

    private suspend fun isPurchasePending(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.skus.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PENDING
    }

    private suspend fun isPurchasedSucceed(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.skus.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    suspend fun isPurchased(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.skus.firstOrNull()
        } != null
    }

    private fun isValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64Key = BuildConfig.GOOGLE_IN_APP_RSA_KEY
            Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    override fun onBillingServiceDisconnected() {
        Timber.i( "onBillingServiceDisconnected: ")
        emitBillingClientError("Billing service is disconnected!")
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        Timber.i( "onBillingSetupFinished: ")
        if (p0.responseCode.isBillingResultOk()) {
            emitBillingInitialized()
            fetchSkuDetailsJob = GlobalScope.launch(Dispatchers.Main) {
                val skuDetailsList = querySkus()
                skuDetailsList?.let {
                    emitSkuDetails(it)
                }
                processPurchase(
                    queryPurchases(),
                    isRestored = true,
                    isInitialPurchaseProcess = true
                )
            }
            // The BillingClient is ready. You can query purchases here.
        }
    }

    //fetch skus and cache in skuDetails map
    private suspend fun querySkus(): List<SkuDetails>? {
        if (!isInitializedAndReady()) {
            Timber.i( "querySkus: Not initialized or read")
            return null
        }
        val skuDetailsList = skus.toSkuDetailsList(INAPP)
        skuDetailsList?.forEach {
            skuDetails[it.sku] = it
        }
        return skuDetailsList
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
    private fun isInitializedAndReady() = billingClient.isReady

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        purchaseUpdateJob = GlobalScope.launch(Dispatchers.Main) {
            // To be implemented in a later section.
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Timber.i( "onPurchasesUpdated: OK")
                    purchases?.let {
                        processPurchase(
                            purchases,
                            isRestored = false,
                            isInitialPurchaseProcess = false
                        )
                    }
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Timber.i( "onPurchasesUpdated: ITEM_ALREADY_OWNED")
                    purchases?.let {
                        processPurchase(
                            purchases,
                            isRestored = true,
                            isInitialPurchaseProcess = false
                        )
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Timber.i( "onPurchasesUpdated: USER_CANCELED")
                    emitPurchaseError("Purchase wasn't made, you can try again now or later. Thank you :)")
                }
                else -> emitPurchaseError(billingResult.debugMessage)
            }


        }
    }

    //purchase should be processed and acknowledge
    private suspend fun processPurchase(
        purchases: List<Purchase>,
        isRestored: Boolean,
        isInitialPurchaseProcess: Boolean
    ) {

        for (purchase in purchases) {

            //storing purchase in cache
            this.purchases[purchase.purchaseToken] = purchase

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (!isValidSignature(purchase.originalJson, purchase.signature)) {
                        // Invalid purchase
                        // show error to user
                        Timber.i( "processPurchase: invalid purchase")
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
                            purchased(purchase.skus.first(), isRestored)
                        }
                    } else {
                        // Grant entitlement to the user on item purchase
                        purchased(purchase.skus.first(), isRestored)
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    purchasePending(purchase.skus.first())
                }
                else -> {
                    Timber.e( "processPurchase: Purchase State: ${purchase.purchaseState}")
                }
            }

        }

        if (isInitialPurchaseProcess) {
            emitInitialPurchaseProcess()
        }

    }

    suspend fun consumeAllPurchase() {
        queryPurchases().forEach {
            billingClient.consumeAsync(
                ConsumeParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()
            ) { p0, p1 -> Timber.i( "onConsumeResponse: ") }
        }
    }

    companion object {
        private val TAG = BillingHelper::class.java.simpleName
    }

}

