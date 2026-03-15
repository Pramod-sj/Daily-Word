package com.pramod.dialyword.games.featureCard.dismissible

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dialyword.games.featureCard.DismissScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DismissedCardsStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val KEY = "dismissed_records"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _records = MutableStateFlow<List<DismissRecord>>(emptyList())

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY) coroutineScope.launch {
            _records.value = readRecords()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        coroutineScope.launch {
            _records.value = readRecords()
        }
    }

    fun activeRecords(): Flow<List<DismissRecord>> = _records.map { list ->
        val now = System.currentTimeMillis()
        list.filter { record ->
            when (record.scope) {
                DismissScope.TIMED -> record.expiresAt != null && record.expiresAt > now
                else -> true
            }
        }
    }

    suspend fun save(record: DismissRecord) = withContext(Dispatchers.IO) {
        val updated = _records.value
            .filterNot { it.cardId == record.cardId } + record
        prefs.edit { putString(KEY, gson.toJson(updated)) }
        _records.value = updated
    }

    suspend fun prune() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pruned = _records.value.filter { record ->
            when (record.scope) {
                DismissScope.TIMED -> record.expiresAt != null && record.expiresAt > now
                else -> true
            }
        }
        prefs.edit { putString(KEY, gson.toJson(pruned)) }
        _records.value = pruned
    }

    private suspend fun readRecords(): List<DismissRecord> = withContext(Dispatchers.IO) {
        val raw = prefs.getString(KEY, null) ?: return@withContext emptyList()
        val type = object : TypeToken<List<DismissRecord>>() {}.type
        return@withContext try {
            gson.fromJson(raw, type)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }
}