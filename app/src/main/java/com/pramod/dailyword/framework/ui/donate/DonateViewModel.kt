package com.pramod.dailyword.framework.ui.donate

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DonateViewModel
@ExperimentalPagingApi
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {


    val donateItemList = arrayListOf(
        DonateItem(
            "cookie_new_",
            R.drawable.ic_cookie,
            "Buy me cookies",
            30,
            R.color.color_cookie
        ),
        DonateItem(
            "coffee_new_",
            R.drawable.ic_coffee_outline,
            "Buy me a cup of coffee",
            60,
            R.color.color_coffee
        ),
        DonateItem(
            "snacks_",
            R.drawable.ic_snacks,
            "Buy me snacks",
            150,
            R.color.color_snacks
        ),
        DonateItem(
            "movie_",
            R.drawable.ic_round_local_movies_24,
            "Buy movie ticket for me",
            200,
            R.color.color_movie
        ),
        DonateItem(
            "meal_",
            R.drawable.ic_round_meal_24,
            "Buy meal for me",
            350,
            R.color.color_meal
        ),
        DonateItem(
            "server_new_",
            R.drawable.ic_server,
            "Buy server and keep this app alive",
            500,
            R.color.color_server
        ),
        DonateItem(
            "gift_new_",
            R.drawable.ic_baseline_card_giftcard_24,
            "Buy me a gift",
            750,
            R.color.color_gift
        )
    )
}


data class DonateItem(
    val itemPurchaseId: String,
    val drawableId: Int,
    val title: String,
    val amount: Int,
    val color: Int
)