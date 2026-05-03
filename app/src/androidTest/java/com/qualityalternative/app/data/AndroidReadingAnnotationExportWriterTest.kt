package com.qualityalternative.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFile
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidReadingAnnotationExportWriterTest {
    @Test
    fun fileDirectoryJsonLdExportWritesPerSourceFilesAndIndex() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val outputDir = File(context.cacheDir, "annotation-export-${System.nanoTime()}")
            outputDir.mkdirs()
            try {
                val writer = AndroidReadingAnnotationExportWriter(context)
                val files = listOf(
                    ReadingAnnotationExportFile(
                        contentId = "source-1",
                        sourceTitle = "First Source",
                        fileName = "quality-alternative-first-source-source-1.annotations.jsonld",
                        jsonLd = """{"type":"AnnotationCollection","label":"First"}""",
                    ),
                    ReadingAnnotationExportFile(
                        contentId = "source-2",
                        sourceTitle = "Second Source",
                        fileName = "quality-alternative-second-source-source-2.annotations.jsonld",
                        jsonLd = """{"type":"AnnotationCollection","label":"Second"}""",
                    ),
                )

                writer.writeJsonLdFiles(uri = outputDir.toURI().toString(), files = files)

                val exportedNames = outputDir.listFiles().orEmpty().map(File::getName).sorted()
                assertEquals(
                    listOf(
                        "quality-alternative-annotations.index.json",
                        "quality-alternative-first-source-source-1.annotations.jsonld",
                        "quality-alternative-second-source-source-2.annotations.jsonld",
                    ),
                    exportedNames,
                )
                assertEquals(files[0].jsonLd, File(outputDir, files[0].fileName).readText())
                assertEquals(files[1].jsonLd, File(outputDir, files[1].fileName).readText())
                val index = File(outputDir, "quality-alternative-annotations.index.json").readText()
                assertTrue(index.contains("QualityAlternativeAnnotationExportIndex"))
                assertTrue(index.contains(files[0].fileName))
                assertTrue(index.contains(files[1].fileName))
            } finally {
                outputDir.deleteRecursively()
            }
        }
    }
}
