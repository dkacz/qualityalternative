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
        assertEquals("Slice 9.1 should add 24 candidate rows plus the header.", 25, backlogLines.size)
        assertTrue(schema.contains("\"targetNewCandidateCount\": 100"))
        assertTrue(schema.contains("\"renderable\": 42"))
        assertTrue(schema.contains("\"linkOnly\": 58"))
    }

    @Test
    fun slice91BacklogPreservesSourcingOnlyMixAndRightsPolicy() {
        val rows = readCandidateBacklogRows()
        val existingTitles = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[2] }
            .toSet()

        assertEquals(24, rows.size)
        assertTrue(rows.all { row -> row["vertical_slice"] == "9.1" })
        assertTrue(rows.all { row -> row["candidate_type"] == "shared_editorial_candidate" })
        assertTrue(rows.none { row -> row["candidate_status"] == "already_integrated" })
        assertTrue(rows.none { row -> row["candidate_title"].orEmpty() in existingTitles })
        assertTrue(rows.all { row -> row["pro_review_status"] == "not_submitted" })
        assertTrue(rows.all { row -> row["legal_review_needed"] == "yes" })
        assertTrue(rows.all { row ->
            row["verification_label"] == "manually_verified_candidate" ||
                row["canonical_url_verified_at"].isNullOrBlank()
        })
        assertTrue(rows.all { row ->
            row["verification_label"] == "manually_verified_candidate" ||
                row["canonical_url_verified_by"].isNullOrBlank()
        })
        assertTrue(rows.all { row ->
            row["replacement_moment"] == "ATTENTION_RESET" ||
                row["replacement_moment"] == "PRACTICAL_AGENCY"
        })

        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        assertEquals(9, renderableRows.size)
        assertEquals(15, linkOnlyRows.size)
        assertTrue(renderableRows.all { row -> row["render_mode_candidate"] == "IN_APP_READER" })
        assertTrue(renderableRows.all { row -> row["candidate_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["renderable_rights_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["android_reader_viability"] == "needs_excerpt_selection" })
        assertTrue(renderableRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "false" })
        assertTrue(renderableRows.all { row -> row["next_action"].orEmpty().startsWith("manual rights") })

        assertTrue(linkOnlyRows.all { row -> row["render_mode_candidate"] == "EXTERNAL_HANDOFF" })
        assertTrue(linkOnlyRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "true" })
        assertTrue(linkOnlyRows.all { row -> row["renderable_rights_status"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> row["android_reader_viability"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> !row["canonical_url"].isNullOrBlank() })
        assertTrue(linkOnlyRows.all { row ->
            row["modification_or_excerpt_note"] == "Do not scrape cache rehost excerpt or summarize at runtime"
        })

        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()
        assertEquals(10, sourceCapCounts["Aeon/Psyche"])
        assertEquals(9, sourceCapCounts["Project Gutenberg"])
        assertEquals(3, sourceCapCounts["Nautilus"])
        assertEquals(2, sourceCapCounts["SEP"])
        assertTrue(sourceCapCounts.filterKeys { key -> key != "Project Gutenberg" }.values.all { count -> count <= 10 })
    }

    @Test
    fun slice91BacklogEncodesKnownRiskSpotChecks() {
        val rows = readCandidateBacklogRows().associateBy { row -> row["candidate_id"] }

        val repose = rows.getValue("s9-1-r07-call-power-through-repose")
        assertEquals("medium", repose["medical_health_claim_risk"])
        assertEquals("dated_health_mental_health_language", repose["sensitivity_flags"])
        assertEquals("low", repose["first_batch_priority"])
        assertTrue(repose["next_action"].orEmpty().contains("excluding medical nervous-system"))

        val modernMedia = rows.getValue("s9-1-l07-nautilus-modern-media-free-will")
        assertEquals("medium", modernMedia["political_current_events_risk"])
        assertEquals("attention_economy_democracy_political_framing", modernMedia["sensitivity_flags"])
        assertEquals("low", modernMedia["first_batch_priority"])

        val spaceCase = rows.getValue("s9-1-l09-nautilus-space-case")
        assertEquals("mostly_evergreen", spaceCase["durability"])
        assertTrue(spaceCase["source_reference_note"].orEmpty().contains("pandemic-era opening"))

        val temptation = rows.getValue("s9-1-l13-psyche-resist-temptations")
        assertEquals("medium", temptation["medical_health_claim_risk"])
        assertEquals("clinical_impulsivity_addiction_mentions", temptation["sensitivity_flags"])
        assertTrue(temptation["card_description_draft"].orEmpty().contains("non-clinical"))
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

    private fun readCandidateBacklogRows(): List<Map<String, String>> {
        val lines = File(docsRoot, "content_candidate_backlog.csv").readLines()
        val header = parseCsvLine(lines.first())
        return lines.drop(1).map { line ->
            header.zip(parseCsvLine(line)).toMap()
        }
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
