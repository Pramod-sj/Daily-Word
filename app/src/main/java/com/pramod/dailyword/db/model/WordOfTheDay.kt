package com.pramod.dailyword.db.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
class WordOfTheDay : Serializable {
    @PrimaryKey
    @NonNull
    @SerializedName("GID")
    var id: Long? = null

    @SerializedName("WORD")
    var word: String? = null

    @SerializedName("PRONOUNCE")
    var pronounce: String? = null

    @SerializedName("PRONOUNCE_AUDIO_URL")
    var pronounceAudio: String? = null

    @SerializedName("DEFINATION")
    var meanings: List<String>? = null

    @SerializedName("ATTRIBUTE")
    var attribute: String? = null

    @SerializedName("EXAMPLE")
    var examples: List<String>? = null

    @SerializedName("DATE")
    var date: String? = null
    var dateTimeInMillis: Long? = null
    var isSeen: Boolean = false
    var seenAtTimeInMillis: Long? = null
    var wordColor: Int? = -1
    var wordDesaturatedColor: Int? = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordOfTheDay

        if (id != other.id) return false
        if (word != other.word) return false
        if (pronounce != other.pronounce) return false
        if (pronounceAudio != other.pronounceAudio) return false
        if (meanings != other.meanings) return false
        if (attribute != other.attribute) return false
        if (examples != other.examples) return false
        if (date != other.date) return false
        if (dateTimeInMillis != other.dateTimeInMillis) return false
        if (isSeen != other.isSeen) return false
        if (seenAtTimeInMillis != other.seenAtTimeInMillis) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}