package com.qualityalternative.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
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
            assertEquals("Quality Alternative Editorial", item.sourceLabel)
            assertEquals(ContentRightsClass.RENDERABLE, item.rights.rightsClass)
            assertEquals(ContentRenderMode.IN_APP_READER, item.rights.renderMode)
            assertEquals("Quality Alternative first-party placeholder", item.rights.licenseName)
            assertEquals("Quality Alternative", item.rights.attribution)
            assertFalse(item.rights.rightsReviewedAt.isNullOrBlank())
            assertTrue(item.usesRepositoryBody())
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
