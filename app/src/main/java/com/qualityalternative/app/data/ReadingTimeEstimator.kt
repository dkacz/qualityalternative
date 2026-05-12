package com.qualityalternative.app.data

import kotlin.math.ceil

enum class ReadingTimeEstimateSource {
    EXTRACTED_TEXT,
    PDF_DEFAULT,
    FALLBACK_DEFAULT,
}

data class ReadingTimeEstimate(
    val minutes: Int,
    val wordCount: Int? = null,
    val source: ReadingTimeEstimateSource,
)

object ReadingTimeEstimator {
    private const val WORDS_PER_MINUTE = 225.0
    const val MIN_SESSION_MINUTES = 3
    const val MAX_SESSION_MINUTES = 20
    const val MAX_DOCUMENT_MINUTES = 720
    const val DEFAULT_LINK_MINUTES = 8
    const val DEFAULT_PDF_MINUTES = 10
    const val DEFAULT_DOCUMENT_MINUTES = 10

    fun estimateFromText(text: String): ReadingTimeEstimate {
        val words = countWords(text)
        val rawMinutes = ceil(words / WORDS_PER_MINUTE).toInt()
        return ReadingTimeEstimate(
            minutes = rawMinutes.coerceIn(MIN_SESSION_MINUTES, MAX_DOCUMENT_MINUTES),
            wordCount = words,
            source = ReadingTimeEstimateSource.EXTRACTED_TEXT,
        )
    }

    fun pdfDefault(): ReadingTimeEstimate {
        return ReadingTimeEstimate(
            minutes = DEFAULT_PDF_MINUTES,
            source = ReadingTimeEstimateSource.PDF_DEFAULT,
        )
    }

    fun fallbackDefault(): ReadingTimeEstimate {
        return ReadingTimeEstimate(
            minutes = DEFAULT_DOCUMENT_MINUTES,
            source = ReadingTimeEstimateSource.FALLBACK_DEFAULT,
        )
    }

    private fun countWords(text: String): Int {
        return Regex("""[\p{L}\p{N}][\p{L}\p{N}'-]*""")
            .findAll(text)
            .count()
    }
}
