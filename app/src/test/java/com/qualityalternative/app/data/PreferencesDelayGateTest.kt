package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    private fun testDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
