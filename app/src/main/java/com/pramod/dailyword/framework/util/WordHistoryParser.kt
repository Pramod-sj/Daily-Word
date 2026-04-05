package com.pramod.dailyword.framework.util

import com.pramod.dailyword.business.domain.model.WordHistoryEntry

object WordHistoryParser {
    /**
     * Parses the raw string from the backend into a list of entries.
     * Example input: "Adjective::derivative of ad hoc||Adverb::borrowed from..."
     */
    fun parse(raw: String?): List<WordHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()

        return raw.split("||").map { entry ->
            val parts = entry.split("::", limit = 2)
            if (parts.size == 2) {
                WordHistoryEntry(
                    partOfSpeech = parts[0].trim().takeIf { it.isNotBlank() },
                    text = parts[1].trim()
                )
            } else {
                WordHistoryEntry(
                    partOfSpeech = null,
                    text = parts[0].trim()
                )
            }
        }
    }
}
