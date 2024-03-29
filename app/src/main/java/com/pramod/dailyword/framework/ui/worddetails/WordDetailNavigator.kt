package com.pramod.dailyword.framework.ui.worddetails

interface WordDetailNavigator {
    fun navigateToShowSynonymsList(list: List<String>?)
    fun navigateToShowAntonymsList(list: List<String>?)
    fun navigateToWeb(url: String)
    fun navigateToShowThesaurusInfo(title: String, desc: String)
    fun navigateToMerriamWebsterPage(value: String)
}