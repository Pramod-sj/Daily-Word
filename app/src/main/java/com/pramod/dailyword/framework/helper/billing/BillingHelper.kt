package com.pramod.dailyword.framework.helper.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.common.collect.ImmutableList
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.Security
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val productDetails = mutableMapOf<String, ProductDetails>()

    //cache purchases
    private val purchases = mutableMapOf<String, Purchase>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        ).build()

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
        Timber.i("close: ")
        fetchSkuDetailsJob?.cancel()
        purchaseUpdateJob?.cancel()
        removeListeners()
        billingClient.endConnection()
    }


    private fun isInAppPurchaseSupported(): Boolean =
        billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_MESSAGING)
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
                        Timber.i("buy: already purchased")
                        emitPurchaseError("You have already donated this item, Thank you so much ❤")
                    }
                }

                else -> {
                    sku.toProductDetails(INAPP)?.let {
                        billingClient.launchBillingFlow(
                            activity, BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    ImmutableList.of(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(it)
                                            .build()
                                    )
                                )
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
            val result = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(INAPP)
                    .build()
            )

            result.purchasesList
                .filter { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                }
                .forEach { purchase ->
                    purchases[purchase.purchaseToken] = purchase
                }
        }
        return ArrayList(purchases.values)
    }

    private suspend fun isPurchasePending(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.products.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PENDING
    }

    private suspend fun isPurchasedSucceed(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.products.firstOrNull()
        }?.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    suspend fun isPurchased(sku: String): Boolean {
        return queryPurchases().find {
            sku == it.products.firstOrNull()
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
        Timber.i("onBillingServiceDisconnected: ")
        emitBillingClientError("Billing service is disconnected!")
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        Timber.i("onBillingSetupFinished: ")
        if (p0.responseCode.isBillingResultOk()) {
            emitBillingInitialized()
            fetchSkuDetailsJob = CoroutineScope(Dispatchers.Main).launch {
                val skuDetailsList = queryProductDetails()
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
    private suspend fun queryProductDetails(): List<ProductDetails>? {
        if (!isInitializedAndReady()) {
            Timber.i("querySkus: Not initialized or read")
            return null
        }
        val skuDetailsList = skus.toSkuDetailsList(INAPP)
        skuDetailsList?.forEach {
            productDetails[it.productId] = it
        }
        return skuDetailsList
    }

    //convert sku to sku details
    private suspend fun String.toProductDetails(skuType: String): ProductDetails? {
        return productDetails[this] ?: billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(this)
                            .setProductType(skuType)
                            .build()
                    )
                ).build()
        ).productDetailsList?.firstOrNull()
    }

    //convert list of sku to list of sku details
    private suspend fun List<String>.toSkuDetailsList(skuType: String): List<ProductDetails>? {
        return billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(skuType)
                        .build()
                }).build()
        ).productDetailsList
    }


    private fun Int.isBillingResultOk(): Boolean = this == BillingClient.BillingResponseCode.OK

    //billing client is Initialized and Ready to use
    private fun isInitializedAndReady() = billingClient.isReady

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        purchaseUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            // To be implemented in a later section.
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Timber.i("onPurchasesUpdated: OK")
                    purchases?.let {
                        processPurchase(
                            purchases,
                            isRestored = false,
                            isInitialPurchaseProcess = false
                        )
                    }
                }

                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Timber.i("onPurchasesUpdated: ITEM_ALREADY_OWNED")
                    purchases?.let {
                        processPurchase(
                            purchases,
                            isRestored = true,
                            isInitialPurchaseProcess = false
                        )
                    }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Timber.i("onPurchasesUpdated: USER_CANCELED")
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
                        Timber.i("processPurchase: invalid purchase")
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
                            purchased(purchase.products.first(), isRestored)
                        }
                    } else {
                        // Grant entitlement to the user on item purchase
                        purchased(purchase.products.first(), isRestored)
                    }
                }

                Purchase.PurchaseState.PENDING -> {
                    purchasePending(purchase.products.first())
                }

                else -> {
                    Timber.e("processPurchase: Purchase State: ${purchase.purchaseState}")
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
            ) { p0, p1 -> Timber.i("onConsumeResponse: ") }
        }
    }

    companion object {
        private val TAG = BillingHelper::class.java.simpleName
    }

}

