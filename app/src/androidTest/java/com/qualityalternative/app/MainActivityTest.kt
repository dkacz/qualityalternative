package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import kotlinx.coroutines.runBlocking
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null
    private val interventionContinueProgressScreenshotDirName =
        "intervention-continue-progress-${System.currentTimeMillis()}"

    @Before
    fun resetAppState() {
        resetPersistentState()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()
    }

    @Test
    fun onboardingCompletesAndPersistsAfterColdRelaunch() {
        launchApp()
        completeOnboardingIfNeeded()
        waitForHome()
        assertHomeHeroIsDisplayed()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchApp()

        waitForHome()
        assertHomeHeroIsDisplayed()
    }

    @Test
    fun systemInterceptionIntentShowsLiveInterventionForFixtureTarget() {
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithText("You reached for Fixture Feed One").assertIsDisplayed()
        composeRule.onNodeWithText("A BRIEF DETOUR, IF YOU'D LIKE ONE")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-backup-action-0")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithTag("intervention-backup-action-1")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Read this")
        }
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Pause 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterventionDelayActionIsClickableWithoutScrolling() {
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithText("Pause 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        waitForHome()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("FIXTURE FEED ONE PAUSED", substring = true))
        composeRule.onNodeWithText("min alternative", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterventionOpenAnywayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            scenario?.state == androidx.lifecycle.Lifecycle.State.DESTROYED
        }
    }

    @Test
    fun systemInterventionBackupActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithTag("intervention-backup-action-0")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-screen") || hasTag("external-handoff-screen")
        }
    }

    @Test
    fun homeShowsReadinessAndCompactLibrarySummary() {
        launchOnboardedApp()

        assertHomeHeroIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("SETUP"))
        composeRule.onNodeWithText("SETUP")
            .assertIsDisplayed()
        composeRule.onNodeWithText("INTERCEPTING")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("YOUR LIBRARY"))
        composeRule.onNodeWithText("YOUR LIBRARY")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Editorial picks"))
        composeRule.onNodeWithText("Editorial picks")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Your added links"))
        composeRule.onNodeWithText("Your added links")
            .assertIsDisplayed()
        assertFalse(hasNode("Local analytics ledger"))
        assertFalse(hasNode("Local preferences"))
        assertFalse(hasNode("Recent replacement history"))
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link").assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Preview intervention"))
        composeRule.onAllNodesWithText("Preview intervention")
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(1, nodes.size) }
    }

    @Test
    fun onboardingShowsSprint9TopicChips() {
        launchApp()

        composeRule.onNodeWithText("Begin").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Which apps pull at you?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("What would you rather read?") }

        composeRule.onNodeWithTag("onboarding-topic-ATTENTION").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-PRACTICAL").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-BODY").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-NATURE").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-HISTORY_CULTURE").assertIsDisplayed()
    }

    @Test
    fun sprint9ContentIsReachableAfterNormalOnboarding() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("tab-library", useUnmergedTree = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("How We Think"))
        composeRule.onNodeWithText("How We Think").assertIsDisplayed()
    }

    @Test
    fun addLinkKeepsSaveDisabledForInvalidUrl() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithText("Add content.", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("add-link-url").performTextInput("quality://bad")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Use a normal web link starting with http or https.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForMissingTopic() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithText("Choose at least one topic so the app can rank this link.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForBlankUrl() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Add the link you want to save.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkSavesValidLinkAndReturnsLibrary() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
        composeRule.onNodeWithTag("add-link-save")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Ready when you are") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Saved essay"))
        composeRule.onNodeWithText("Saved essay").assertIsDisplayed()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Your link · example.com", substring = true))
        composeRule.onNodeWithText("Your link · example.com", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun libraryManageModeDeletesSavedLinkAndKeepsEditorialReadOnly() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/delete-me")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Delete me")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Ready when you are") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }

        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Only your saved links and files can be deleted.") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-editorial-note-s9-1-r01-dewey-how-we-think"))
        composeRule.onNodeWithTag("library-editorial-note-s9-1-r01-dewey-how-we-think")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Delete me"))
        composeRule.onNodeWithText("Delete me")
            .assertIsDisplayed()
        val selectTag = "library-select-user-link:918123c9245605b90800494db814f2b6282ee47d915b479427b52aa7fe1b9805"
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag(selectTag))
        composeRule.onNodeWithTag(selectTag)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-delete-selected"))
        composeRule.onNodeWithTag("library-delete-selected")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Delete me") }
        assertFalse(hasNode("Delete me"))
    }

    @Test
    fun addLinkPriorityAtAddAppearsInPriorityLibraryFilter() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/priority-at-add")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Priority at add")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-priority")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Priority")
            .performClick()
        composeRule.onNodeWithText("Priority at add")
            .assertIsDisplayed()
    }

    @Test
    fun themeSettingSwitchesToDarkMode() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Dark")
            .assertIsDisplayed()
    }

    @Test
    fun readerFeedbackAndProgressUseFiniteReplacementCopy() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-screen")
        }
        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.onAllNodesWithText("figure · editorial image", ignoreCase = true)
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(0, nodes.size) }
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Two quick questions.")
        }
        composeRule.onNodeWithTag("feedback-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Was this a good fit?").assertIsDisplayed()
        composeRule.onNodeWithText("Did it help you skip the scroll?").assertIsDisplayed()
        composeRule.onNodeWithText("Great fit").performClick()
        composeRule.onNodeWithText("Yes").performClick()
        composeRule.onNodeWithTag("feedback-log")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("progress-card") || hasNode("Progress")
        }
        composeRule.onNodeWithTag("progress-card").assertIsDisplayed()
        composeRule.onNodeWithText("day converted").assertIsDisplayed()
    }

    @Test
    fun completedContentIsHiddenThenCanBeReactivatedFromLibrary() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val completedContentId = currentContentId()
        val completedTitle = currentContentTitle()
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("feedback-screen") && hasCompletedProgressFor(completedContentId)
        }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            completedContentId !in currentRecommendationContentIds()
        }

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText(completedTitle))
        composeRule.onNodeWithTag("library-completed-status-$completedContentId")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Completed · hidden from suggestions")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("completed-activation-$completedContentId")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Completed · active in suggestions")
        }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            completedContentId in currentRecommendationContentIds()
        }
    }

    @Test
    fun settingsOpenAnywayUnlockSuppressesRepeatedSystemIntervention() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("open-anyway-unlock-120"))
        composeRule.onNodeWithTag("open-anyway-unlock-120")
            .assertIsDisplayed()
            .performClick()

        seedFixtureSelection()
        relaunchFixtureSystemIntervention()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            scenario?.state == androidx.lifecycle.Lifecycle.State.DESTROYED
        }
        scenario = null

        assertTrue(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
                nowMillis = System.currentTimeMillis() + 119 * 60_000L,
            ),
        )

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        assertFalse(hasNode("You reached for Fixture Feed One"))
    }

    @Test
    fun unfinishedReadingAppearsOnHomeAndLibraryAndCanContinueWithoutIntervention() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val initiallyOpenedContentId = currentContentId()
        assertFalse(hasUnfinishedProgressFor(initiallyOpenedContentId))

        scenario?.onActivity { activity ->
            activity.mainViewModel.openHome()
        }
        waitForHome()
        assertFalse(hasTag("home-continue-card"))

        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.waitUntil(timeoutMillis = 10_000) { hasUnfinishedProgressFor(contentId) }
        val savedPercent = savedProgressPercentFor(contentId)
        val savedParagraphIndex = savedProgressParagraphIndexFor(contentId)
        assertTrue(savedPercent in 1..99)
        assertTrue(savedParagraphIndex > 0)

        scenario?.onActivity { activity -> activity.mainViewModel.openHome() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        composeRule.onNodeWithTag("home-continue-card").assertIsDisplayed()
        composeRule.onNodeWithText("$savedPercent% read", substring = true).assertIsDisplayed()

        composeRule.onNodeWithTag("home-continue-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent%") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > 0 }
        }
        val restoredVisibleParagraphIndices = visibleReaderParagraphIndices()
        val minimumRestoredParagraphIndex = (savedParagraphIndex - 2).coerceAtLeast(1)
        assertTrue(
            "Expected continued Reader to restore near saved paragraph $savedParagraphIndex, found $restoredVisibleParagraphIndices",
            restoredVisibleParagraphIndices.any { index ->
                index in minimumRestoredParagraphIndex..savedParagraphIndex
            },
        )
        assertFalse(restoredVisibleParagraphIndices.contains(0))

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent% read") }
        composeRule.onNodeWithText("$savedPercent% read", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("library-open-$contentId")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("feedback-screen") && hasCompletedProgressFor(contentId)
        }
        assertTrue(hasCompletedManualReaderEventFor(contentId))
    }

    @Test
    fun systemInterventionShowsContinueProgressRemainingTimeAndScrollableOtherOptions() {
        launchOnboardedApp()
        seedFixtureSelection()
        val seededItems = seedScrollableUnfinishedDocumentOptions(progressPercent = 42)
        assertTrue("Expected enough seeded options for a scrollable backup list", seededItems.size >= 4)

        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithTag("intervention-primary-progress").assertIsDisplayed()
        val recommendationSet = currentRecommendationSet()
        val primaryLabel = continueProgressLabel(item = recommendationSet.primary, progressPercent = 42)
        assertTrue(
            "Expected at least one visible progress label for $primaryLabel",
            composeRule.onAllNodesWithText(primaryLabel, substring = true).fetchSemanticsNodes().isNotEmpty(),
        )
        captureInterventionContinueProgressScreenshot("01_intervention_continue_progress_light")

        assertTrue(
            "Expected more than two backup options, got ${recommendationSet.backups.size}",
            recommendationSet.backups.size > 2,
        )
        composeRule.onNodeWithTag("intervention-backup-list").assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-backup-list")
            .performScrollToNode(hasTestTag("intervention-backup-action-2"))
        composeRule.onNodeWithTag("intervention-backup-action-2").assertIsDisplayed()

        val seededIds = seededItems.mapTo(mutableSetOf(), ContentItem::id)
        val backup = recommendationSet.backups[2]
        assertTrue("Expected the scrolled backup option to be unfinished", backup.id in seededIds)
        composeRule.onNodeWithContentDescription(
            "${backup.title}, ${continueProgressLabel(item = backup, progressPercent = 42)}",
        ).assertIsDisplayed()
        captureInterventionContinueProgressScreenshot("02_intervention_other_options_scrolled_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitForIdle()
        captureInterventionContinueProgressScreenshot("03_intervention_continue_progress_dark")
    }

    @Test
    fun meditationAlternativeOpensThreeMinuteTimer() {
        launchMeditationFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("3-minute reset")
        }
        if (hasNodeContaining("Start timer")) {
            composeRule.onNodeWithText("Start timer", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("3-minute reset")
                .assertIsDisplayed()
                .performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        composeRule.onNodeWithTag("meditation-timer-screen").assertIsDisplayed()
        composeRule.onNodeWithText("3:00").assertIsDisplayed()
        composeRule.onNodeWithTag("meditation-complete").assertIsNotEnabled()
        composeRule.onNodeWithText("End early").assertIsDisplayed()
    }

    @Test
    fun completedMeditationIsHiddenThenCanBeReactivatedFromLibrary() {
        launchMeditationFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("3-minute reset")
        }
        if (hasNodeContaining("Start timer")) {
            composeRule.onNodeWithText("Start timer", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("3-minute reset")
                .assertIsDisplayed()
                .performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        assertEquals(MEDITATION_TIMER_CONTENT_ID, currentContentId())
        scenario?.onActivity { activity ->
            activity.mainViewModel.finishMeditationReset(nowMillis = 10_000L)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }

        relaunchFixtureSystemInterventionWithoutWaitingForIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        assertFalse(MEDITATION_TIMER_CONTENT_ID in currentRecommendationContentIds())

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-completed-status-$MEDITATION_TIMER_CONTENT_ID"))
        composeRule.onNodeWithTag("library-completed-status-$MEDITATION_TIMER_CONTENT_ID")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Completed · hidden from suggestions")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("completed-activation-$MEDITATION_TIMER_CONTENT_ID")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Completed · active in suggestions")
        }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            MEDITATION_TIMER_CONTENT_ID in currentRecommendationContentIds()
        }
    }

    @Test
    fun libraryStartedMeditationCompletionIsHiddenFromFutureInterventions() {
        launchOnboardedApp()

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-open-$MEDITATION_TIMER_CONTENT_ID"))
        composeRule.onNodeWithTag("library-open-$MEDITATION_TIMER_CONTENT_ID")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        scenario?.onActivity { activity ->
            activity.mainViewModel.finishMeditationReset(nowMillis = 10_000L)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }

        seedFixtureSelection()
        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            MEDITATION_TIMER_CONTENT_ID !in currentRecommendationContentIds()
        }
    }

    @Test
    fun sharedLinkOnlyAlternativeOpensExternalHandoffScreen() {
        launchLinkOnlyFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Open link")
        }
        if (hasNodeContaining("Open link")) {
            composeRule.onNodeWithText("Open link", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("The Big Here and Long Now")
                .assertIsDisplayed()
                .performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("external-handoff-screen")
        }
        composeRule.onNodeWithTag("external-handoff-screen").assertIsDisplayed()
        composeRule.onNodeWithText("https://", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("external-link-open")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    private fun hasNode(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun hasNodeContaining(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text, substring = true).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun hasTag(tag: String): Boolean {
        return runCatching {
            composeRule.onNodeWithTag(tag).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun hasContentDescriptionNode(description: String): Boolean {
        return runCatching {
            composeRule.onNode(hasContentDescription(description)).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun visibleReaderParagraphIndices(): List<Int> {
        return (0..80).filter { index ->
            hasContentDescriptionNode("reader-first-visible-paragraph-$index")
        }
    }

    private fun currentContentId(): String {
        var contentId = ""
        scenario?.onActivity { activity ->
            contentId = activity.mainViewModel.uiState.currentContent?.id.orEmpty()
        }
        assertTrue(contentId.isNotBlank())
        return contentId
    }

    private fun currentContentTitle(): String {
        var title = ""
        scenario?.onActivity { activity ->
            title = activity.mainViewModel.uiState.currentContent?.title.orEmpty()
        }
        assertTrue(title.isNotBlank())
        return title
    }

    private fun currentRecommendationContentIds(): Set<String> {
        var contentIds = emptySet<String>()
        scenario?.onActivity { activity ->
            val recommendationSet = activity.mainViewModel.uiState.currentRecommendationSet
            contentIds = listOfNotNull(recommendationSet?.primary)
                .plus(recommendationSet?.backups.orEmpty())
                .mapTo(mutableSetOf(), ContentItem::id)
        }
        return contentIds
    }

    private fun hasUnfinishedProgressFor(contentId: String): Boolean {
        var hasProgress = false
        scenario?.onActivity { activity ->
            hasProgress = activity.mainViewModel.uiState.readingProgress.any { progress ->
                progress.contentId == contentId && progress.progressPercent in 1..99 && progress.completedAtMillis == null
            }
        }
        return hasProgress
    }

    private fun savedProgressPercentFor(contentId: String): Int {
        var percent = 0
        scenario?.onActivity { activity ->
            percent = activity.mainViewModel.uiState.readingProgress
                .firstOrNull { progress -> progress.contentId == contentId && progress.completedAtMillis == null }
                ?.progressPercent
                ?: 0
        }
        return percent
    }

    private fun savedProgressParagraphIndexFor(contentId: String): Int {
        var index = -1
        scenario?.onActivity { activity ->
            index = activity.mainViewModel.uiState.readingProgress
                .firstOrNull { progress -> progress.contentId == contentId && progress.completedAtMillis == null }
                ?.lastVisibleParagraphIndex
                ?: -1
        }
        return index
    }

    private fun hasCompletedProgressFor(contentId: String): Boolean {
        var hasCompleted = false
        scenario?.onActivity { activity ->
            hasCompleted = activity.mainViewModel.uiState.readingProgress.any { progress ->
                progress.contentId == contentId && progress.progressPercent == 100 && progress.completedAtMillis != null
            }
        }
        return hasCompleted
    }

    private fun hasCompletedManualReaderEventFor(contentId: String): Boolean {
        var hasEvent = false
        scenario?.onActivity { activity ->
            hasEvent = activity.mainViewModel.uiState.events.any { event ->
                event.type == AnalyticsEventType.READER_COMPLETED &&
                    event.contentId == contentId &&
                    event.sessionId == null
            }
        }
        return hasEvent
    }

    private fun openAddLinkFromHome() {
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link")
            .performClick()
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You're set up for quieter reading today.") || hasNode("Interception needs one more step.")
        }
    }

    private fun launchApp(intent: Intent? = null) {
        val launchIntent = intent ?: Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MainActivity::class.java,
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        scenario = ActivityScenario.launch(
            launchIntent,
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun launchOnboardedApp() {
        launchApp()
        completeOnboardingIfNeeded()
        waitForHome()
    }

    private fun completeOnboardingIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Turn an impulse") ||
                hasNode("You're set up for quieter reading today.") ||
                hasNode("Interception needs one more step.")
        }
        if (!hasNodeContaining("Turn an impulse")) {
            return
        }
        composeRule.onNodeWithText("Begin").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Which apps pull at you?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("What would you rather read?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("How long should a session feel?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Two small permissions.") }
        composeRule.onNodeWithText("Grant & finish").performClick()
    }

    private fun launchFixtureSystemIntervention() {
        launchOnboardedApp()
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()
    }

    private fun relaunchFixtureSystemIntervention() {
        relaunchFixtureSystemInterventionWithoutWaitingForIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You reached for Fixture Feed One")
        }
    }

    private fun relaunchFixtureSystemInterventionWithoutWaitingForIntervention() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )
    }

    private fun launchMeditationFixtureSystemIntervention() {
        launchOnboardedApp()
        seedMeditationFixtureSelection()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You reached for Fixture Feed One")
        }
    }

    private fun launchLinkOnlyFixtureSystemIntervention() {
        launchOnboardedApp()
        seedLinkOnlyFixtureSelection()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You reached for Fixture Feed One")
        }
    }

    private fun seedScrollableUnfinishedDocumentOptions(progressPercent: Int): List<ContentItem> = runBlocking {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        val durations = listOf(7, 6, 5, 4, 3, 3)
        durations.mapIndexed { index, minutes ->
            val result = app.appContainer.userDocumentRepository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://qa-test/continue-option-$index",
                    displayName = "continue-option-$index.md",
                    mimeType = "text/markdown",
                    title = "Continue option ${index + 1}",
                    durationMinutes = minutes,
                    topicTags = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
                ),
                nowMillis = 10_000L + index,
            )
            assertTrue("Expected unfinished option document to be saved", result is AddUserDocumentResult.Added)
            (result as AddUserDocumentResult.Added).item.also { item ->
                app.appContainer.readingProgressRepository.saveProgress(
                    ReadingProgress(
                        contentId = item.id,
                        progressPercent = progressPercent,
                        lastVisibleParagraphIndex = 4 + index,
                        paragraphCount = 12,
                        updatedAtMillis = 20_000L + index,
                    ),
                )
            }
        }
    }

    private fun currentRecommendationSet(): com.qualityalternative.app.domain.model.RecommendationSet {
        var recommendationSet: com.qualityalternative.app.domain.model.RecommendationSet? = null
        scenario?.onActivity { activity ->
            recommendationSet = activity.mainViewModel.uiState.currentRecommendationSet
        }
        return requireNotNull(recommendationSet)
    }

    private fun continueProgressLabel(item: ContentItem, progressPercent: Int): String {
        val remainingPercent = (100 - progressPercent.coerceIn(1, 99)).coerceAtLeast(1)
        val remainingMinutes = ((item.durationMinutes * remainingPercent) + 99) / 100
        return "$progressPercent% read · $remainingMinutes min left"
    }

    private fun captureInterventionContinueProgressScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$interventionContinueProgressScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun assertHomeHeroIsDisplayed() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You're set up for quieter reading today.") || hasNode("Interception needs one more step.")
        }
    }

    private fun seedFixtureSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
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

    private fun seedMeditationFixtureSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PSYCHOLOGY, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("meditation-only-test-pack"),
            ),
        )
    }

    private fun seedLinkOnlyFixtureSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.ESSAYS, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("link-only-modern-v1"),
            ),
        )
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }
}
