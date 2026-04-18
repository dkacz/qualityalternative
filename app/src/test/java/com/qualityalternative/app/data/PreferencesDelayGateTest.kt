package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesDelayGateTest {
    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun storedDelayIsActiveThenExpires() = runTest {
        val file = File.createTempFile("delay-gate-test", ".preferences_pb").apply { deleteOnExit() }
        val app = SupportedCatalog.distractingApps.first()
        val dataStore = testDataStore(file)
        val firstGate = PreferencesDelayGate(
            dataStore = dataStore,
            scope = backgroundScope,
        )

        val created = firstGate.storeDelay(targetApp = app, nowMillis = 1_000L)
        assertNotNull(created)
        assertNotNull(firstGate.activeDelay(targetApp = app, nowMillis = 2_000L))
        assertNull(firstGate.activeDelay(targetApp = app, nowMillis = created.endsAtMillis + 1))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun delayProvenanceAndFirstReturnAttemptRoundTripThroughEncoding() = runTest {
        val window = com.qualityalternative.app.domain.model.DelayWindow(
            id = "delay-1",
            targetAppPackage = "com.example.target",
            startsAtMillis = 1_000L,
            endsAtMillis = 2_000L,
            interventionId = "intervention-1",
            interventionShownAtMillis = 900L,
            primaryContentId = "primary-1",
            backupContentIds = listOf("backup-1", "backup-2"),
            firstReturnAttemptAtMillis = 1_500L,
        )

        val encoded = PreferencesDelayGate.encodeWindow(window)
        val decoded = PreferencesDelayGate.decodeWindow(encoded)

        assertEquals(window.interventionId, decoded?.interventionId)
        assertEquals(window.primaryContentId, decoded?.primaryContentId)
        assertEquals(window.backupContentIds, decoded?.backupContentIds)
        assertEquals(window.firstReturnAttemptAtMillis, decoded?.firstReturnAttemptAtMillis)
    }

    private fun testDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
