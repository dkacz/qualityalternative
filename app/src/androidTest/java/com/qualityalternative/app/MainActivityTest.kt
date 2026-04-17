package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun launchApp() {
        scenario = ActivityScenario.launch(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MainActivity::class.java,
            ),
        )
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    @Test
    fun onboardingCompletesAndPersistsAfterActivityRecreate() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Let’s set up your replacement loop") || hasNode("Quality Alternative")
        }

        if (hasNode("Let’s set up your replacement loop")) {
            composeRule.onNodeWithText("Complete setup", useUnmergedTree = true)
                .performScrollTo()
                .performClick()

            composeRule.waitUntil(timeoutMillis = 10_000) {
                hasNode("Quality Alternative")
            }
        }

        composeRule.onNodeWithText("Quality Alternative").assertIsDisplayed()

        scenario?.recreate()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative")
        }

        composeRule.onNodeWithText("Quality Alternative").assertIsDisplayed()
    }

    private fun hasNode(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }
}
