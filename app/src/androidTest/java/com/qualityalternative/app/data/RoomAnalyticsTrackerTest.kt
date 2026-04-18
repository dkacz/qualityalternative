package com.qualityalternative.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAnalyticsTrackerTest {
    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun recordDurably_persistsExpiredDelayAnalyticsWithContext() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()

        try {
            val tracker = RoomAnalyticsTracker(
                dao = database.analyticsEventDao(),
                scope = backgroundScope,
            )
            tracker.observeReady().first { it }

            tracker.recordDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.RETURN_AFTER_DELAY_ENDED,
                    timestampMillis = 2_000L,
                    interventionId = "intervention-1",
                    targetAppPackage = "com.example.target",
                    primaryContentId = "primary-1",
                    backupContentIds = listOf("backup-1", "backup-2"),
                    contentId = "primary-1",
                    metadata = mapOf(
                        "delayId" to "delay-1",
                        "delayReturnOrigin" to "after_delay_expired",
                        "delayStartedAtMillis" to "1000",
                        "delayEndedAtMillis" to "1900",
                    ),
                ),
            )
            advanceUntilIdle()

            val event = tracker.observeEvents().first { it.size == 1 }.single()

            assertEquals(AnalyticsEventType.RETURN_AFTER_DELAY_ENDED, event.type)
            assertEquals("intervention-1", event.interventionId)
            assertEquals("primary-1", event.primaryContentId)
            assertEquals(listOf("backup-1", "backup-2"), event.backupContentIds)
            assertEquals("after_delay_expired", event.metadata["delayReturnOrigin"])
            assertTrue(event.metadata["delayId"] == "delay-1")
        } finally {
            database.close()
        }
    }
}
