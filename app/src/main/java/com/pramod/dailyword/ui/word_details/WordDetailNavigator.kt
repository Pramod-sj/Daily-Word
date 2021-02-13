package com.pramod.dailyword.ui.word_details

interface WordDetailNavigator {
    fun navigateToShowSynonymsList(list: List<String>?)
    fun navigateToShowAntonymsList(list: List<String>?)
    fun navigateToWeb(url: String)
    fun navigateToShowThesaurusInfo(title: String, desc: String)
}