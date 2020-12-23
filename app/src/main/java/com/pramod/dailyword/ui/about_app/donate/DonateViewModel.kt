package com.pramod.dailyword.ui.about_app.donate

import android.app.Application
import com.pramod.dailyword.R
import com.pramod.dailyword.ui.BaseViewModel

class DonateViewModel(application: Application) : BaseViewModel(application) {

    val donateItemList = arrayListOf(
        DonateItem("cookie_", R.drawable.ic_cookie, "Buy me cookies", 20, R.color.orange_400),
        DonateItem(
            "coffee_",
            R.drawable.ic_coffee_outline,
            "Buy me a cup of coffee",
            50,
            R.color.pink_400
        ),
        DonateItem(
            "gift_",
            R.drawable.ic_baseline_card_giftcard_24,
            "Buy me a gift",
            150,
            R.color.green_400
        ),
        DonateItem(
            "server_",
            R.drawable.ic_server,
            "Buy server and keep this app alive",
            200,
            R.color.blue_400
        ),
        DonateItem(
            "meal_",
            R.drawable.ic_round_meal_24,
            "Buy meal for me",
            350,
            R.color.deepOrange_400
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