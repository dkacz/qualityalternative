package com.qualityalternative.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.usesExternalHandoff
import com.qualityalternative.app.domain.model.usesRepositoryBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetContentRepositoryTest {
    @Test
    fun starterPackItemsCarryExplicitRightsAndRenderMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val items = repository.inventory()

        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            assertEquals(ContentSourceType.EDITORIAL, item.sourceType)
            assertFalse(item.sourceLabel.isNullOrBlank())
            assertFalse(item.rights.licenseName.isNullOrBlank())
            assertFalse(item.rights.attribution.isNullOrBlank())
            assertFalse(item.rights.rightsReviewedAt.isNullOrBlank())
            if (item.usesRepositoryBody()) {
                assertFalse(item.bodyAssetPath.isNullOrBlank())
                assertEquals(null, item.externalUrl)
                assertEquals(ContentRightsClass.RENDERABLE, item.rights.rightsClass)
                assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            }
            if (item.usesExternalHandoff()) {
                assertEquals(null, item.bodyAssetPath)
                assertFalse(item.externalUrl.isNullOrBlank())
                assertEquals(ContentRightsClass.LINK_ONLY, item.rights.rightsClass)
                assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, item.rights.renderMode)
            }
        }
    }

    @Test
    fun sharedLinkOnlyItemsUseExternalHandoffWithoutBodyAssets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val pack = repository.starterPacks().first { it.id == "link-only-modern-v1" }
        val item = pack.items.first { it.id == "big-here-long-now" }

        assertEquals("big-here-long-now", item.id)
        assertEquals("Long Now", item.sourceLabel)
        assertEquals(ContentRightsClass.LINK_ONLY, item.rights.rightsClass)
        assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, item.rights.renderMode)
        assertEquals("https://longnow.org/ideas/the-big-here-and-long-now/", item.externalUrl)
        assertEquals(null, item.bodyAssetPath)
        assertTrue(item.usesExternalHandoff())
        assertFalse(item.usesRepositoryBody())
        assertEquals(item.description, repository.contentBody(item))
    }

    @Test
    fun modernLinkOnlyPackContainsCuratedExternalHandoffInventory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val pack = repository.starterPacks().first { it.id == "link-only-modern-v1" }
        val items = pack.items

        assertEquals(20, items.size)
        assertTrue(items.map { item -> item.id }.toSet().size == items.size)
        assertTrue(items.all { item -> item.sourceType == ContentSourceType.EDITORIAL })
        assertTrue(items.all { item -> item.rights.rightsClass == ContentRightsClass.LINK_ONLY })
        assertTrue(items.all { item -> item.rights.renderMode == ContentRenderMode.EXTERNAL_HANDOFF })
        assertTrue(items.all { item -> item.bodyAssetPath == null })
        assertTrue(items.all { item -> !item.externalUrl.isNullOrBlank() })
        assertTrue(items.all { item -> item.externalUrl == item.rights.sourceUrl })
        assertTrue(items.all { item -> !item.whyThisNow.isNullOrBlank() })
        assertTrue(items.all { item -> item.durationMinutes in 6..18 })
        assertTrue(items.all { item -> item.topicTags.isNotEmpty() })
        assertTrue(items.none { item -> item.rights.licenseName.orEmpty().contains("first-party") })
        assertTrue(
            items.groupingBy { item -> item.sourceLabel.orEmpty() }.eachCount().values.all { count -> count <= 4 },
        )
    }

    @Test
    fun legacyStarterPacksNoLongerContainPlaceholderEditorialItems() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val legacyPacks = repository.starterPacks()
            .filter { pack -> pack.id in setOf("philosophy", "science") }
        val legacyItems = legacyPacks.flatMap { pack -> pack.items }

        assertEquals(2, legacyPacks.size)
        assertEquals(7, legacyItems.size)
        assertEquals(
            setOf(
                "care-for-the-soul-first",
                "leave-the-crowd",
                "go-about-a-humans-work",
                "neither-ask-nor-consent",
                "a-candle-opens-natural-philosophy",
                "water-dust-becomes-a-cloud",
                "attention-comes-in-beats",
            ),
            legacyItems.map { item -> item.id }.toSet(),
        )
        legacyItems.forEach { item ->
            assertFalse(item.sourceLabel.orEmpty().contains("Quality Alternative Editorial"))
            assertFalse(item.rights.licenseName.orEmpty().contains("placeholder", ignoreCase = true))
            assertEquals(ContentRightsClass.RENDERABLE, item.rights.rightsClass)
            assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertFalse(item.bodyAssetPath.isNullOrBlank())
            assertEquals(null, item.externalUrl)
            assertFalse(item.whyThisNow.isNullOrBlank())
            assertEquals("https://www.gutenberg.org/policy/license.html", item.rights.licenseUrl)
            assertTrue(item.rights.sourceUrl.orEmpty().startsWith("https://www.gutenberg.org/ebooks/"))
            assertFalse(item.rights.attribution.isNullOrBlank())
            assertEquals("2026-04-22", item.rights.rightsReviewedAt)
        }
    }

    @Test
    fun starterInventoryContainsNoFirstPartyPlaceholderLicenses() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val placeholders = repository.inventory()
            .filter { item ->
                item.rights.licenseName.orEmpty().contains("placeholder", ignoreCase = true) ||
                    item.sourceLabel.orEmpty() == "Quality Alternative Editorial"
            }

        assertTrue(placeholders.isEmpty())
    }

    @Test
    fun attentionClassicsPackCarriesPublicDomainSourceMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val pack = repository.starterPacks().first { it.id == "attention-classics-v1" }
        val items = pack.items

        assertEquals(8, items.size)
        assertTrue(items.count { item -> item.durationMinutes <= 5 } >= 3)
        assertEquals(
            setOf(
                "start-with-what-is-yours",
                "the-morning-test",
                "the-flywheel-of-habit",
                "live-deliberately",
                "walk-before-you-scroll",
                "trust-the-first-honest-thought",
                "the-desert-resets-the-eye",
                "of-studies",
            ),
            items.map { item -> item.id }.toSet(),
        )
        val expectedSourceLabels = mapOf(
            "start-with-what-is-yours" to "Epictetus",
            "the-morning-test" to "Marcus Aurelius",
            "the-flywheel-of-habit" to "William James",
            "live-deliberately" to "Henry David Thoreau",
            "walk-before-you-scroll" to "Henry David Thoreau",
            "trust-the-first-honest-thought" to "Ralph Waldo Emerson",
            "the-desert-resets-the-eye" to "Mary Austin",
            "of-studies" to "Francis Bacon",
        )
        items.forEach { item ->
            assertEquals(expectedSourceLabels[item.id], item.sourceLabel)
            assertFalse(item.sourceLabel.orEmpty().contains("Project Gutenberg"))
            assertTrue(item.rights.licenseName.orEmpty().contains("Public domain text"))
            assertTrue(item.rights.licenseName.orEmpty().contains("source policy"))
            assertEquals("https://www.gutenberg.org/policy/license.html", item.rights.licenseUrl)
            assertTrue(item.rights.sourceUrl.orEmpty().startsWith("https://www.gutenberg.org/"))
            assertFalse(item.rights.attribution.isNullOrBlank())
            assertEquals("2026-04-22", item.rights.rightsReviewedAt)
        }
    }

    @Test
    fun attentionClassicsDurationsMatchShippedMarkdownBodies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val items = repository.starterPacks().first { it.id == "attention-classics-v1" }.items

        items.forEach { item ->
            val words = repository.contentBody(item).split(Regex("\\s+")).count(String::isNotBlank)
            val wordsPerMinute = words / item.durationMinutes

            assertTrue(
                "${item.id} has too few words for ${item.durationMinutes} min: $words words",
                wordsPerMinute >= 70,
            )
            assertTrue(
                "${item.id} has too many words for ${item.durationMinutes} min: $words words",
                wordsPerMinute <= 190,
            )
        }
    }

    @Test
    fun publicDomainExpansionPackCarriesRenderableRightsAndBodies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val pack = repository.starterPacks().first { it.id == "public-domain-expansion-v2" }
        val items = pack.items

        assertEquals(10, items.size)
        assertEquals(
            setOf(
                "a-naturalist-notices-everything",
                "a-doorway-into-learning",
                "choose-your-own-plan",
                "look-at-the-stars",
                "a-place-of-business",
                "the-minds-own-snare",
                "rest-satisfied-with-what-we-have",
                "anger-divides-what-life-joins",
                "earnestness-as-an-island",
                "the-examined-life",
            ),
            items.map { item -> item.id }.toSet(),
        )
        items.forEach { item ->
            assertEquals(ContentSourceType.EDITORIAL, item.sourceType)
            assertEquals(ContentRightsClass.RENDERABLE, item.rights.rightsClass)
            assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertFalse(item.bodyAssetPath.isNullOrBlank())
            assertEquals(null, item.externalUrl)
            assertFalse(item.whyThisNow.isNullOrBlank())
            assertFalse(item.rights.licenseName.isNullOrBlank())
            assertFalse(item.rights.licenseUrl.isNullOrBlank())
            assertFalse(item.rights.sourceUrl.isNullOrBlank())
            assertFalse(item.rights.attribution.isNullOrBlank())
            assertEquals("2026-04-22", item.rights.rightsReviewedAt)
            assertFalse(repository.contentBody(item).isBlank())
        }
    }

    @Test
    fun publicDomainExpansionDurationsMatchShippedMarkdownBodies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val items = repository.starterPacks().first { it.id == "public-domain-expansion-v2" }.items

        items.forEach { item ->
            val words = repository.contentBody(item).split(Regex("\\s+")).count(String::isNotBlank)
            val wordsPerMinute = words / item.durationMinutes

            assertTrue(
                "${item.id} has too few words for ${item.durationMinutes} min: $words words",
                wordsPerMinute >= 70,
            )
            assertTrue(
                "${item.id} has too many words for ${item.durationMinutes} min: $words words",
                wordsPerMinute <= 190,
            )
        }
    }

    @Test
    fun starterPackItemsDoNotClaimExternalPublicationAffiliation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)
        val disallowedPublicationLabels = setOf(
            "Substack",
            "The Atlantic",
            "New Yorker",
            "Wired",
            "Guardian",
            "NYT",
            "Medium",
        )

        val items = repository.inventory()

        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            disallowedPublicationLabels.forEach { publication ->
                assertFalse(
                    "${item.id} should not imply affiliation with $publication",
                    item.sourceLabel.orEmpty().contains(publication, ignoreCase = true),
                )
            }
        }
    }

    @Test
    fun editorialAssetsDoNotContainUnmanifestedMarkdownFiles() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)
        val manifestFiles = repository.inventory()
            .mapNotNull { item -> item.bodyAssetPath?.substringAfterLast("/") }
            .toSet()
        val packagedMarkdownFiles = context.assets.list("editorial/items")
            .orEmpty()
            .filter { fileName -> fileName.endsWith(".md") }
            .toSet()

        assertEquals(manifestFiles, packagedMarkdownFiles)
    }
}
