package com.pramod.todaysword.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        val type = TypeToken.getParameterized(List::class.java, String::class.java).type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(value: List<String>?): String? {
        return Gson().toJson(value)
    }
}