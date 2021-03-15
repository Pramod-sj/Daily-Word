package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.content.ContextWrapper

/**
 * This pref class is used to store next remote key to be fetch from server for WordPagingRemoteMediator.kt
 */
class RemoteKeyPrefManager(base: Context) : ContextWrapper(base) {
    private val sPref = base.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    fun setNextRemoteKey(nextRemoteKey: String?) {
        sPref.edit().putString(KEY_NEXT_REMOTE_KEY, nextRemoteKey).apply()
    }

    fun getNextRemoteKey(): String? {
        return sPref.getString(KEY_NEXT_REMOTE_KEY, null)
    }

    companion object {
        const val PREF_NAME = "paging_remote_key_pref"
        const val KEY_NEXT_REMOTE_KEY = "next_remote_key"
    }
}