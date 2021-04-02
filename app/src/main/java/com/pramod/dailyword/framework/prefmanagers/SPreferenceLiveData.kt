package com.pramod.dailyword.framework.prefmanagers

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SPreferenceLiveData<T>(
    val sPref: SharedPreferences,
    private val key: String,
    private val defValue: T
) :
    LiveData<T>() {

    init {
        value = getValueFromPreference(key, defValue)
    }

    abstract fun getValueFromPreference(key: String, defValue: T): T

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, s: String ->
            if (s == key) {
                value = getValueFromPreference(s, defValue)
            }
        }

    override fun onActive() {
        super.onActive()
        value = getValueFromPreference(key, defValue)
        sPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sPref.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}

class SPrefIntLiveData(sPref: SharedPreferences, key: String, defValue: Int) :
    SPreferenceLiveData<Int>(sPref, key, defValue) {
    override fun getValueFromPreference(key: String, defValue: Int): Int =
        sPref.getInt(key, defValue)

}

class SPrefBooleanLiveData(sPref: SharedPreferences, key: String, defValue: Boolean) :
    SPreferenceLiveData<Boolean>(sPref, key, defValue) {
    override fun getValueFromPreference(key: String, defValue: Boolean): Boolean =
        sPref.getBoolean(key, defValue)
}

class SPrefStringLiveData(sPref: SharedPreferences, key: String, defValue: String?) :
    SPreferenceLiveData<String?>(sPref, key, defValue) {
    override fun getValueFromPreference(key: String, defValue: String?): String? =
        sPref.getString(key, defValue)
}