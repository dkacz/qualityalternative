package com.qualityalternative.app

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.fixture.FixtureDistractorOneActivity
import com.qualityalternative.app.interception.FixtureTargetRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccessibilityInterceptionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun resetAppState() {
        resetPersistentState()
        device.pressHome()
        instrumentation.waitForIdleSync()
    }

    @After
    fun tearDown() {
        device.pressHome()
        instrumentation.waitForIdleSync()
        resetPersistentState()
    }

    @Test
    fun fixtureForegroundLaunchesInterventionThroughCrossAppHarness() {
        seedFixtureSelection()

        targetContext.startActivity(
            Intent(targetContext, FixtureDistractorOneActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(FixtureDistractorOneActivity.EXTRA_TRIGGER_INTERCEPTION, true)
            },
        )
        instrumentation.waitForIdleSync()

        assertTrue(
            device.wait(
                Until.hasObject(By.text("Pause before Fixture Feed One")),
                10_000L,
            ),
        )
    }

    private fun seedFixtureSelection() = runBlocking {
        val repository = (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy"),
            ),
        )
    }

    private fun resetPersistentState() = runBlocking {
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }
}
