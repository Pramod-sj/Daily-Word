package com.pramod.dailyword.ui.about_app.donate

import android.app.Application
import com.pramod.dailyword.R
import com.pramod.dailyword.ui.BaseViewModel

class DonateViewModel(application: Application) : BaseViewModel(application) {

    val donateItemList = arrayListOf(
        DonateItem("cookie_", R.drawable.ic_cookie, "Cookie", 20),
        DonateItem("coffee_", R.drawable.ic_coffee_outline, "Coffee", 50),
        DonateItem("gift_", R.drawable.ic_baseline_card_giftcard_24, "Gift", 150),
        DonateItem("server_", R.drawable.ic_server, "Server", 200)
    )
}


data class DonateItem(
    val itemPurchaseId: String,
    val drawableId: Int,
    val title: String,
    val amount: Int
)