package com.pramod.dailyword.framework.datasource.network.model

import com.google.gson.annotations.SerializedName

data class WordNE(

    @SerializedName("GID")
    var gid: String?,

    @SerializedName("WORD")
    var word: String?,

    @SerializedName("PRONOUNCE")
    var pronounce: String?,

    @SerializedName("PRONOUNCE_AUDIO_URL")
    var pronounceAudio: String?,

    @SerializedName("DEFINATION")
    var meanings: List<String>?,

    @SerializedName("DID_YOU_KNOW")
    var didYouKnow: String?,

    @SerializedName("ATTRIBUTE")
    var attribute: String?,

    @SerializedName("EXAMPLE")
    var examples: List<String>?,

    @SerializedName("DATE")
    var date: String?,

    @SerializedName("SYNONYMS")
    var synonyms: List<String>?,

    @SerializedName("ANTONYMS")
    var antonyms: List<String>?,

    @SerializedName("OTHER_WORDS")
    var otherWords: List<String>?,
)