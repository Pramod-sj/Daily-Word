package com.pramod.dailyword.framework.ui.donate

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.pramod.dailyword.R

enum class DonateItemState {
    NOT_PURCHASED,
    PURCHASE_IN_PROCESS,
    PURCHASED
}

data class DonateItem(
    val itemProductId: String,
    val drawableId: Int,
    val title: String,
    val amount: String,
    val color: Int,
    val donateItemState: DonateItemState = DonateItemState.NOT_PURCHASED
) {
    fun getDonateStateIcon(context: Context): Drawable? {
        return when (donateItemState) {
            DonateItemState.NOT_PURCHASED -> {
                null
            }
            DonateItemState.PURCHASE_IN_PROCESS -> {
                ContextCompat.getDrawable(context, R.drawable.ic_round_access_time_24)
            }
            DonateItemState.PURCHASED -> {
                ContextCompat.getDrawable(context, R.drawable.ic_round_check_24)
            }
        }
    }

    fun isDonateItemPurchased() = donateItemState != DonateItemState.NOT_PURCHASED

}