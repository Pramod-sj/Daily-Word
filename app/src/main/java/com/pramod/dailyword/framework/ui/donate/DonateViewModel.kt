package com.pramod.dailyword.framework.ui.donate

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

val Context.DONATE_ITEM_LIST: List<DonateItem>
    get() {
        return arrayListOf(
            DonateItem(
                "cookie_new_",
                R.drawable.ic_cookie,
                resources.getString(R.string.buy_me_cookie),//"Buy me cookies",
                "₹30",
                R.color.color_cookie
            ),
            DonateItem(
                "coffee_new_",
                R.drawable.ic_coffee_outline,
                resources.getString(R.string.buy_me_coffee),//"Buy me a cup of coffee",
                "₹60",
                R.color.color_coffee
            ),
            DonateItem(
                "snacks_",
                R.drawable.ic_snacks,
                resources.getString(R.string.buy_me_snacks),//"Buy me snacks",
                "₹150",
                R.color.color_snacks
            ),
            DonateItem(
                "movie_",
                R.drawable.ic_round_local_movies_24,
                resources.getString(R.string.buy_me_movie_ticket),//"Buy movie ticket for me",
                "₹200",
                R.color.color_movie
            ),
            DonateItem(
                "meal_",
                R.drawable.ic_round_meal_24,
                resources.getString(R.string.buy_me_meal),//"Buy meal for me",
                "₹350",
                R.color.color_meal
            ),
            DonateItem(
                "server_new_",
                R.drawable.ic_server,
                resources.getString(R.string.buy_me_server),//"Buy server and keep this app alive",
                "₹500",
                R.color.color_server
            ),
            DonateItem(
                "gift_new_",
                R.drawable.ic_baseline_card_giftcard_24,
                resources.getString(R.string.buy_me_gift),//"Buy me a gift",
                "₹750",
                R.color.color_gift
            )
        )
    }


@HiltViewModel
class DonateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _donateItemList =
        MutableLiveData<List<DonateItem>>().apply {
            value = context.applicationContext.DONATE_ITEM_LIST
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