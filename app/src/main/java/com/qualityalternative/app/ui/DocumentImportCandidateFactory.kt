package com.qualityalternative.app.ui

import com.qualityalternative.app.data.DocumentReadingTimeEstimator
import com.qualityalternative.app.data.UserDocumentValidator
import java.io.InputStream

object DocumentImportCandidateFactory {
    fun fromPickedDocument(
        uri: String,
        displayName: String,
        mimeType: String?,
        openInputStream: () -> InputStream? = { null },
    ): DocumentImportCandidate {
        val cleanedName = displayName.trim().ifBlank { "Untitled document" }
        val title = cleanedName.substringBeforeLast('.', cleanedName).trim().ifBlank { cleanedName }
        val format = UserDocumentValidator.detectFormat(displayName = cleanedName, mimeType = mimeType)
        val estimate = DocumentReadingTimeEstimator.estimate(format, openInputStream)
        return DocumentImportCandidate(
            uri = uri.trim(),
            displayName = cleanedName,
            mimeType = mimeType,
            title = title,
            durationMinutes = estimate.minutes.toString(),
            format = format,
            estimateSource = estimate.source,
            estimatedWordCount = estimate.wordCount,
        )
    }
}
