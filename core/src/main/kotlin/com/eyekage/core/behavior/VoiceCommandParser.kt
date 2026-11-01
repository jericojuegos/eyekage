package com.eyekage.core.behavior

import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.VoiceCommand
import java.util.Locale

/**
 * Pure Kotlin parser that maps spoken string phrases to strongly-typed [VoiceCommand] intents.
 *
 * Normalizes input strings by trimming, lowercasing, and stripping punctuation,
 * then checks against registered synonym catalogs. Returns null on unrecognized inputs.
 */
object VoiceCommandParser {

    private val punctuationRegex = Regex("[^a-z0-9\\s]")
    private val whitespaceRegex = Regex("\\s+")

    fun parse(rawText: String?): VoiceCommand? {
        if (rawText.isNullOrBlank()) return null
        val normalized = normalize(rawText)
        if (normalized.isEmpty()) return null

        return when {
            // Awakening reaction
            matchesAny(normalized, AWAKEN_SYNONYMS) -> VoiceCommand.TriggerAwakening

            // Sleep / Drowsy state
            matchesAny(normalized, SLEEP_SYNONYMS) -> VoiceCommand.Sleep

            // Blink reaction
            matchesAny(normalized, BLINK_SYNONYMS) -> VoiceCommand.Blink

            // Center gaze
            matchesAny(normalized, CENTER_SYNONYMS) -> VoiceCommand.CenterGaze

            // Crimson Tracker Mode
            matchesAny(normalized, CRIMSON_SYNONYMS) -> VoiceCommand.SwitchMode(EyeModeId.CRIMSON_TRACKER)

            // White Scan Mode
            matchesAny(normalized, WHITE_SCAN_SYNONYMS) -> VoiceCommand.SwitchMode(EyeModeId.WHITE_SCAN)

            // Purple Orbit Mode
            matchesAny(normalized, PURPLE_ORBIT_SYNONYMS) -> VoiceCommand.SwitchMode(EyeModeId.PURPLE_ORBIT)

            // Normal Mode
            matchesAny(normalized, NORMAL_SYNONYMS) -> VoiceCommand.SwitchMode(EyeModeId.NORMAL)

            else -> null
        }
    }

    private fun normalize(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(punctuationRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()
    }

    private fun matchesAny(text: String, synonyms: List<String>): Boolean {
        val words = text.split(" ")
        return synonyms.any { synonym ->
            if (synonym.contains(" ")) {
                text.contains(synonym)
            } else {
                words.contains(synonym)
            }
        }
    }

    private val AWAKEN_SYNONYMS = listOf(
        "awaken",
        "wake up",
        "wake",
        "hello eyekage",
        "eyekage awaken",
        "hey eyekage"
    )

    private val SLEEP_SYNONYMS = listOf(
        "sleep",
        "rest",
        "go to sleep",
        "goodnight",
        "drowsy"
    )

    private val BLINK_SYNONYMS = listOf(
        "blink",
        "wink"
    )

    private val CENTER_SYNONYMS = listOf(
        "center",
        "look forward",
        "look at me",
        "center gaze"
    )

    private val CRIMSON_SYNONYMS = listOf(
        "crimson tracker",
        "crimson",
        "red eyes",
        "sharingan"
    )

    private val WHITE_SCAN_SYNONYMS = listOf(
        "white scan",
        "white",
        "scan mode",
        "byakugan"
    )

    private val PURPLE_ORBIT_SYNONYMS = listOf(
        "purple orbit",
        "purple",
        "orbit mode",
        "rinnegan"
    )

    private val NORMAL_SYNONYMS = listOf(
        "normal eyes",
        "normal",
        "default mode",
        "reset mode"
    )
}
