package com.pramod.dailyword.framework.ui.worddetails

/**
 * Created by Pramod on 29,December,2021
 */
interface WordDetailAdapterItemClickListener {

    fun navigateToWeb(url: String)

    fun onChipsCardInfoClick(title: String, infoHint: String)

    fun onChipsCardViewMoreClick(title: String, list: List<String>)

    fun onChipsCardChipClick(url: String)

}