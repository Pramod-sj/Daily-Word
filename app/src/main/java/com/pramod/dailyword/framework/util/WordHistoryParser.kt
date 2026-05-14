package com.pramod.dailyword.framework.util

import com.pramod.dailyword.business.domain.model.WordHistoryEntry

object WordHistoryParser {

    private const val ENTRY_SEPARATOR = "||"
    private const val PART_SEPARATOR = "::"

    /**
     * Parses the raw string from the backend into a list of entries.
     * Example input: "Adjective::derivative of ad hoc||Adverb::borrowed from..."
     */
    fun parse(raw: String?): List<WordHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()

        return raw.split(ENTRY_SEPARATOR)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() } // Don't process empty segments
            .map { entry ->
                val parts = entry.split(PART_SEPARATOR, limit = 2)

                if (parts.size == 2) {
                    val pos = parts[0].trim()
                    WordHistoryEntry(
                        // Only assign POS if it actually contains text
                        partOfSpeech = pos.ifBlank { null },
                        text = parts[1].trim()
                    )
                } else {
                    WordHistoryEntry(
                        partOfSpeech = null,
                        text = parts[0].trim()
                    )
                }
            }
            .filter { !it.text.isNullOrBlank() } // Final safety check: skip entries with no content
            .toList()
    }
}