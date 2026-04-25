package com.qualityalternative.app.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSourceExpansionArtifactsTest {
    private val docsRoot = File("../docs/content-sourcing")

    @Test
    fun candidateBacklogHeaderMatchesSchemaFieldOrder() {
        val schema = File(docsRoot, "content_candidate_backlog.schema.json").readText()
        val schemaFields = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
            .findAll(schema)
            .map { match -> match.groupValues[1] }
            .toList()
        val backlogLines = File(docsRoot, "content_candidate_backlog.csv").readLines()
        val header = parseCsvLine(backlogLines.first())

        assertTrue("Schema should define candidate backlog fields.", schemaFields.isNotEmpty())
        assertEquals(schemaFields, header)
        assertEquals("Slice 9.0 should not add new candidate rows yet.", 1, backlogLines.size)
        assertTrue(schema.contains("\"targetNewCandidateCount\": 100"))
        assertTrue(schema.contains("\"renderable\": 42"))
        assertTrue(schema.contains("\"linkOnly\": 58"))
    }

    @Test
    fun existingInventoryAuditMatchesCurrentStarterPackShape() {
        val auditRows = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
        val starterPackAsset = File("src/main/assets/editorial/starter_packs.json").readText()
        val starterItemCount = Regex("\"durationMinutes\"\\s*:").findAll(starterPackAsset).count()

        assertEquals(starterItemCount, auditRows.size)
        assertEquals(45, auditRows.size)
        assertEquals(25, auditRows.count { row -> row[4] == "RENDERABLE" && row[5] == "IN_APP_READER" })
        assertEquals(20, auditRows.count { row -> row[4] == "LINK_ONLY" && row[5] == "EXTERNAL_HANDOFF" })
        assertTrue(auditRows.all { row -> row[11] == "already_integrated" })
        assertTrue(auditRows.all { row -> row[12] == "false" })
        val longNowRows = auditRows.filter { row -> row[3] == "Long Now" }
        assertEquals(3, longNowRows.size)
        assertTrue(longNowRows.all { row -> row[10] == "LONG_VIEW" })
    }

    @Test
    fun taxonomyRejectsUserFacingOtherAndAddsMissingReplacementTopics() {
        val taxonomy = File(docsRoot, "topic_taxonomy_decision.md").readText()

        assertTrue(taxonomy.contains("Do not add `OTHER` as a user-facing topic"))
        assertTrue(taxonomy.contains("`ATTENTION` | Add"))
        assertTrue(taxonomy.contains("`PRACTICAL` | Add"))
        assertTrue(taxonomy.contains("`BODY` | Add"))
        assertTrue(taxonomy.contains("`NATURE` | Add"))
        assertTrue(taxonomy.contains("`HISTORY_CULTURE` | Add"))
        assertTrue(taxonomy.contains("Move `ESSAYS` and `POETRY` out of primary topics"))
    }

    @Test
    fun sourceCapsPreserveFiniteNonFeedInventoryShape() {
        val caps = File(docsRoot, "source_family_caps.md").readText()

        assertTrue(caps.contains("One modern source family in the full 100-row pool | 10 candidates"))
        assertTrue(caps.contains("One source family in a future 10-item pack | 4 candidates"))
        assertTrue(caps.contains("Link-only rows from modern publications must remain `EXTERNAL_HANDOFF`"))
        assertFalse(caps.contains("infinite feed", ignoreCase = true))
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && insideQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index += 1
                }
                char == '"' -> insideQuotes = !insideQuotes
                char == ',' && !insideQuotes -> {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index += 1
        }
        values += current.toString()
        return values
    }
}
