package com.pramod.dailyword.db.model

import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.pramod.dailyword.R
import java.io.Serializable

@Keep
@Entity
class WordOfTheDay : Serializable {
    /*
    @SerializedName("GID")
    @ColumnInfo(name = "id")
    var id: Long? = null*/


    @PrimaryKey
    @NonNull
    @SerializedName("WORD")
    @ColumnInfo(name = "word")
    var word: String? = null

    @SerializedName("PRONOUNCE")
    @ColumnInfo(name = "pronounce")
    var pronounce: String? = null

    @SerializedName("PRONOUNCE_AUDIO_URL")
    @ColumnInfo(name = "pronounceAudio")
    var pronounceAudio: String? = null

    @SerializedName("DEFINATION")
    @ColumnInfo(name = "meanings")
    var meanings: List<String>? = null

    @SerializedName("DID_YOU_KNOW")
    @ColumnInfo(name = "didYouKnow")
    var didYouKnow: String? = null

    @SerializedName("ATTRIBUTE")
    @ColumnInfo(name = "attribute")
    var attribute: String? = null

    @SerializedName("EXAMPLE")
    @ColumnInfo(name = "examples")
    var examples: List<String>? = null

    @SerializedName("DATE")
    @ColumnInfo(name = "date")
    var date: String? = null

    @ColumnInfo(name = "dateTimeInMillis")
    var dateTimeInMillis: Long? = null

    @ColumnInfo(name = "isSeen")
    var isSeen: Boolean = false

    @ColumnInfo(name = "seenAtTimeInMillis")
    var seenAtTimeInMillis: Long? = null

    @ColumnInfo(name = "wordColor")
    var wordColor: Int = -1

    @ColumnInfo(name = "wordDesaturatedColor")
    var wordDesaturatedColor: Int = -1

    var bookmarkedAt: Long = 0

    constructor(date: String?) {
        this.date = date
    }

    @Ignore
    fun isBookmarked(): Boolean = bookmarkedAt != 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordOfTheDay

        //if (id != other.id) return false
        if (word != other.word) return false
        if (pronounce != other.pronounce) return false
        if (pronounceAudio != other.pronounceAudio) return false
        if (meanings != other.meanings) return false
        if (attribute != other.attribute) return false
        if (examples != other.examples) return false
        if (date != other.date) return false
        if (dateTimeInMillis != other.dateTimeInMillis) return false
        if (isSeen != other.isSeen) return false
        if (didYouKnow != other.didYouKnow) return false
        if (seenAtTimeInMillis != other.seenAtTimeInMillis) return false
        if (bookmarkedAt != other.bookmarkedAt) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}