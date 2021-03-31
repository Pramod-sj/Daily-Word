package com.pramod.dailyword.framework.ui.donate

import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.R

val donateItemList = MutableLiveData<List<DonateItem>>().apply {
    value = arrayListOf(
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

}

data class DonateItem(
    val itemProductId: String,
    val drawableId: Int,
    val title: String,
    val amount: String,
    val color: Int
)