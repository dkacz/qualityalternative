package com.qualityalternative.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.usesRepositoryBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetContentRepositoryTest {
    @Test
    fun starterPackItemsCarryExplicitRenderableRightsMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val items = repository.inventory()

        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            assertEquals(ContentSourceType.EDITORIAL, item.sourceType)
            assertEquals(null, item.externalUrl)
            assertFalse(item.bodyAssetPath.isNullOrBlank())
            assertEquals(ContentRightsClass.RENDERABLE, item.rights.rightsClass)
            assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertFalse(item.sourceLabel.isNullOrBlank())
            assertFalse(item.rights.licenseName.isNullOrBlank())
            assertFalse(item.rights.attribution.isNullOrBlank())
            assertFalse(item.rights.rightsReviewedAt.isNullOrBlank())
            assertTrue(item.usesRepositoryBody())
        }
    }

    @Test
    fun qualityAlternativePlaceholderItemsRemainClearlyLabeled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val placeholders = repository.inventory()
            .filter { item -> item.rights.licenseName == "Quality Alternative first-party placeholder" }

        assertTrue(placeholders.isNotEmpty())
        placeholders.forEach { item ->
            assertEquals("Quality Alternative Editorial", item.sourceLabel)
            assertEquals(null, item.rights.licenseUrl)
            assertEquals(null, item.rights.sourceUrl)
            assertEquals("Quality Alternative", item.rights.attribution)
        }
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
        items.forEach { item ->
            assertEquals("Public-domain source text via Project Gutenberg", item.sourceLabel)
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
