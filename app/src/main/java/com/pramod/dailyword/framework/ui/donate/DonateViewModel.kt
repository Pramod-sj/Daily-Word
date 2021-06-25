package com.pramod.dailyword.framework.ui.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.common.BaseViewModel

val DONATE_ITEM_LIST = arrayListOf(
    DonateItem(
        "cookie_new_",
        R.drawable.ic_cookie,
        "Buy me cookies",
        "₹30.00",
        R.color.color_cookie
    ),
    DonateItem(
        "coffee_new_",
        R.drawable.ic_coffee_outline,
        "Buy me a cup of coffee",
        "₹60.00",
        R.color.color_coffee
    ),
    DonateItem(
        "snacks_",
        R.drawable.ic_snacks,
        "Buy me snacks",
        "₹150.00",
        R.color.color_snacks
    ),
    DonateItem(
        "movie_",
        R.drawable.ic_round_local_movies_24,
        "Buy movie ticket for me",
        "₹200.00",
        R.color.color_movie
    ),
    DonateItem(
        "meal_",
        R.drawable.ic_round_meal_24,
        "Buy meal for me",
        "₹350.00",
        R.color.color_meal
    ),
    DonateItem(
        "server_new_",
        R.drawable.ic_server,
        "Buy server and keep this app alive",
        "₹500.00",
        R.color.color_server
    ),
    DonateItem(
        "gift_new_",
        R.drawable.ic_baseline_card_giftcard_24,
        "Buy me a gift",
        "₹750.00",
        R.color.color_gift
    )
)


class DonateViewModel : BaseViewModel() {

    private val _donateItemList =
        MutableLiveData<List<DonateItem>>().apply {
            value = DONATE_ITEM_LIST
        }

    val donateItemList: LiveData<List<DonateItem>> = _donateItemList

    fun updateDonateItemStatus(sku: String, donateState: DonateItemState) {
        val current = _donateItemList.value?.let { ArrayList(it) } ?: arrayListOf<DonateItem>()

        val immutableCurrent = _donateItemList.value?.let { ArrayList(it) } ?: listOf<DonateItem>()

        for (donateItem in immutableCurrent) {
            if (donateItem.itemProductId == sku) {
                current[immutableCurrent.indexOf(donateItem)] =
                    DonateItem(
                        donateItem.itemProductId,
                        donateItem.drawableId,
                        donateItem.title,
                        donateItem.amount,
                        donateItem.color,
                        donateState
                    )
            }
        }

        _donateItemList.value = current
    }

    fun updateDonateItemPrice(skuDetailsList: List<SkuDetails>) {

        val current = _donateItemList.value?.let { ArrayList(it) } ?: mutableListOf<DonateItem>()

        val immutableCurrent = _donateItemList.value?.let { ArrayList(it) } ?: listOf<DonateItem>()

        for (skuDetails in skuDetailsList) {
            immutableCurrent.find { skuDetails.sku == it.itemProductId }?.let {
                current[immutableCurrent.indexOf(it)] =
                    DonateItem(
                        it.itemProductId,
                        it.drawableId,
                        it.title,
                        skuDetails.price,
                        it.color,
                        it.donateItemState
                    )
            }
        }

        _donateItemList.value = current
    }

}