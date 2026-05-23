package com.pramod.dailyword.framework.datasource.network.model

import com.google.gson.annotations.SerializedName

data class WordNE(

    @SerializedName("GID")
    val gid: String?,

    @SerializedName("WORD")
    val word: String?,

    @SerializedName("PRONOUNCE")
    val pronounce: String?,

    @SerializedName("PRONOUNCE_AUDIO_URL")
    val pronounceAudio: String?,

    @SerializedName("DEFINATION")
    val meanings: List<String>?,

    @SerializedName("DID_YOU_KNOW")
    val didYouKnow: String?,

    @SerializedName("ATTRIBUTE")
    val attribute: String?,

    @SerializedName("EXAMPLE")
    val examples: List<String>?,

    @SerializedName("DATE")
    val date: String?,

    @SerializedName("SYNONYMS")
    val synonyms: List<String>?,

    @SerializedName("ANTONYMS")
    val antonyms: List<String>?,

    @SerializedName("OTHER_WORDS")
    val otherWords: List<String>?,

    @SerializedName("Etymology")
    val etymology: String? = null,

    @SerializedName("FirstKnownUse")
    val firstKnownUse: String? = null,

    @SerializedName("TimeTraveler")
    val timeTraveler: String? = null
)