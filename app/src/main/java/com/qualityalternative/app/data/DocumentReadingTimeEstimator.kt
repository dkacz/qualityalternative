package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import java.io.InputStream

object DocumentReadingTimeEstimator {
    fun estimate(format: ContentFormat?, openInputStream: () -> InputStream?): ReadingTimeEstimate {
        return when (format) {
            ContentFormat.MARKDOWN -> estimateExtracted(openInputStream) { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }

            ContentFormat.EPUB -> estimateExtracted(openInputStream) { input ->
                EpubTextExtractor.extract(input)
            }

            ContentFormat.PDF -> ReadingTimeEstimator.pdfDefault()

            else -> ReadingTimeEstimator.fallbackDefault()
        }
    }

    private fun estimateExtracted(
        openInputStream: () -> InputStream?,
        extractText: (InputStream) -> String,
    ): ReadingTimeEstimate {
        return runCatching {
            openInputStream()?.use { input ->
                ReadingTimeEstimator.estimateFromText(extractText(input))
            } ?: ReadingTimeEstimator.fallbackDefault()
        }.getOrDefault(ReadingTimeEstimator.fallbackDefault())
    }
}
