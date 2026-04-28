package com.qualityalternative.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingTimeEstimatorTest {
    @Test
    fun estimateFromTextBoundsShortAndVeryLongDocumentsToSessionRange() {
        assertEquals(3, ReadingTimeEstimator.estimateFromText("one two three").minutes)

        val longText = List(10_000) { "word" }.joinToString(" ")
        val estimate = ReadingTimeEstimator.estimateFromText(longText)

        assertEquals(20, estimate.minutes)
        assertEquals(10_000, estimate.wordCount)
        assertEquals(ReadingTimeEstimateSource.EXTRACTED_TEXT, estimate.source)

        val normalText = List(1_125) { "word" }.joinToString(" ")
        assertEquals(5, ReadingTimeEstimator.estimateFromText(normalText).minutes)
    }

    @Test
    fun pdfDefaultUsesInterventionSessionEstimate() {
        val estimate = ReadingTimeEstimator.pdfDefault()

        assertEquals(10, estimate.minutes)
        assertEquals(ReadingTimeEstimateSource.PDF_DEFAULT, estimate.source)
    }
}
