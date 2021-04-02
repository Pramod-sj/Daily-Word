package com.pramod.dailyword.framework.prefmanagers

import android.content.Context

/**
 * This pref class is used to store next remote key to be fetch from server for WordPagingRemoteMediator.kt
 */
class RemoteKeyPrefManager(base: Context) : BasePreferenceManager(PREF_NAME, base) {

    fun setNextRemoteKey(nextRemoteKey: String?) {
        editor.putString(KEY_NEXT_REMOTE_KEY, nextRemoteKey).apply()
    }

    fun getNextRemoteKey(): String? {
        return sPrefManager.getString(KEY_NEXT_REMOTE_KEY, null)
    }

    companion object {
        const val PREF_NAME = "paging_remote_key_pref"
        const val KEY_NEXT_REMOTE_KEY = "next_remote_key"
    }
}