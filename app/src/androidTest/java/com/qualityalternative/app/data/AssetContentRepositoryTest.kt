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
                "let-the-pleasure-wait",
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
    fun sharedRenderableInventoryCarriesCompleteDecisionAndRightsMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val renderableItems = repository.inventory()
            .filter { item -> item.sourceType == ContentSourceType.EDITORIAL && item.usesRepositoryBody() }

        assertTrue(renderableItems.isNotEmpty())
        renderableItems.forEach { item ->
            assertEquals("${item.id} rights class", ContentRightsClass.RENDERABLE, item.rights.rightsClass)
            assertEquals("${item.id} render mode", ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertFalse("${item.id} bodyAssetPath", item.bodyAssetPath.isNullOrBlank())
            assertEquals("${item.id} externalUrl", null, item.externalUrl)
            assertFalse("${item.id} source label", item.sourceLabel.isNullOrBlank())
            assertFalse("${item.id} sourceUrl", item.rights.sourceUrl.isNullOrBlank())
            assertFalse("${item.id} licenseUrl", item.rights.licenseUrl.isNullOrBlank())
            assertFalse("${item.id} licenseName", item.rights.licenseName.isNullOrBlank())
            assertFalse("${item.id} attribution", item.rights.attribution.isNullOrBlank())
            assertFalse("${item.id} rightsReviewedAt", item.rights.rightsReviewedAt.isNullOrBlank())
            assertFalse("${item.id} whyThisNow", item.whyThisNow.isNullOrBlank())
            assertFalse("${item.id} content body", repository.contentBody(item).isBlank())
        }
    }

    @Test
    fun sharedRenderableInventoryDoesNotShipNearDuplicateBodyAssets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)

        val renderableBodies = repository.inventory()
            .filter { item -> item.sourceType == ContentSourceType.EDITORIAL && item.usesRepositoryBody() }
            .map { item ->
                val words = normalizedWords(repository.contentBody(item))
                AssetBodyFingerprint(
                    itemId = item.id,
                    words = words,
                    shingles = shingles(words),
                )
            }

        renderableBodies.forEachIndexed { index, left ->
            renderableBodies.drop(index + 1).forEach { right ->
                val similarity = jaccard(left.shingles, right.shingles)
                val smallerCoverage = commonShingleCoverageOfSmaller(left.shingles, right.shingles)
                val longestOverlap = longestContiguousOverlap(left.words, right.words)
                assertTrue(
                    "${left.itemId} and ${right.itemId} appear to reuse the same excerpt body: " +
                        "jaccard=$similarity, smallerCoverage=$smallerCoverage, longestOverlap=$longestOverlap",
                    similarity < 0.35 && smallerCoverage < 0.35 && longestOverlap < 40,
                )
            }
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
    fun sprint9PacksShipIntegratedContentWithReaderBodiesAndExternalHandoffs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetContentRepository(context)
        val sprint9PackCounts = mapOf(
            "attention_practical_agency_v1" to 24,
            "embodied_calm_v1" to 20,
            "wonder_science_v1" to 26,
            "long_view_history_v1" to 22,
            "creativity_play_v1" to 8,
        )

        val sprint9Packs = repository.starterPacks()
            .filter { pack -> pack.id in sprint9PackCounts.keys }
        val sprint9Items = sprint9Packs.flatMap { pack -> pack.items }
        val renderable = sprint9Items.filter { item -> item.rights.rightsClass == ContentRightsClass.RENDERABLE }
        val linkOnly = sprint9Items.filter { item -> item.rights.rightsClass == ContentRightsClass.LINK_ONLY }

        assertEquals(sprint9PackCounts.keys, sprint9Packs.map { pack -> pack.id }.toSet())
        sprint9Packs.forEach { pack ->
            assertEquals(pack.id, sprint9PackCounts.getValue(pack.id), pack.items.size)
        }
        assertEquals(100, sprint9Items.size)
        assertEquals(42, renderable.size)
        assertEquals(58, linkOnly.size)
        assertTrue(sprint9Items.all { item -> item.id.startsWith("s9-") })
        assertTrue(sprint9Items.all { item -> item.rights.rightsReviewedAt == "2026-04-26" })

        renderable.forEach { item ->
            val body = repository.contentBody(item)
            assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertFalse(item.bodyAssetPath.isNullOrBlank())
            assertEquals(null, item.externalUrl)
            assertTrue("${item.id} body is too thin", body.split(Regex("\\s+")).count(String::isNotBlank) >= 500)
            assertFalse("${item.id} leaked Project Gutenberg boilerplate", body.contains("Project Gutenberg", ignoreCase = true))
            assertFalse("${item.id} leaked producer boilerplate", body.contains("Produced by", ignoreCase = true))
        }

        linkOnly.forEach { item ->
            assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, item.rights.renderMode)
            assertEquals(null, item.bodyAssetPath)
            assertFalse(item.externalUrl.isNullOrBlank())
            assertEquals(item.externalUrl, item.rights.sourceUrl)
            assertEquals(item.description, repository.contentBody(item))
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

    private data class AssetBodyFingerprint(
        val itemId: String,
        val words: List<String>,
        val shingles: Set<String>,
    )

    private fun normalizedWords(body: String): List<String> =
        body
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { word -> word.length > 2 }

    private fun shingles(words: List<String>): Set<String> {
        if (words.size < 8) {
            return setOf(words.joinToString(" "))
        }
        return words.windowed(8)
            .map { shingle -> shingle.joinToString(" ") }
            .toSet()
    }

    private fun jaccard(left: Set<String>, right: Set<String>): Double {
        if (left.isEmpty() && right.isEmpty()) {
            return 1.0
        }
        return left.intersect(right).size.toDouble() / left.union(right).size.toDouble()
    }

    private fun commonShingleCoverageOfSmaller(left: Set<String>, right: Set<String>): Double {
        val smaller = minOf(left.size, right.size)
        if (smaller == 0) {
            return 0.0
        }
        return left.intersect(right).size.toDouble() / smaller
    }

    private fun longestContiguousOverlap(left: List<String>, right: List<String>): Int {
        if (left.isEmpty() || right.isEmpty()) {
            return 0
        }
        val previous = IntArray(right.size + 1)
        val current = IntArray(right.size + 1)
        var best = 0

        for (leftIndex in 1..left.size) {
            for (rightIndex in 1..right.size) {
                current[rightIndex] = if (left[leftIndex - 1] == right[rightIndex - 1]) {
                    previous[rightIndex - 1] + 1
                } else {
                    0
                }
                best = maxOf(best, current[rightIndex])
            }
            java.util.Arrays.fill(previous, 0)
            current.copyInto(previous)
        }

        return best
    }
}
