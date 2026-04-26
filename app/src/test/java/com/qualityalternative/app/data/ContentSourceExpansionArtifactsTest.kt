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
        assertEquals("Slices 9.1 through 9.5 should add 100 candidate rows plus the header.", 101, backlogLines.size)
        assertTrue(schema.contains("\"targetNewCandidateCount\": 100"))
        assertTrue(schema.contains("\"renderable\": 42"))
        assertTrue(schema.contains("\"linkOnly\": 58"))
    }

    @Test
    fun slice91BacklogPreservesSourcingOnlyMixAndRightsPolicy() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.1" }
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
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.1" }
            .associateBy { row -> row["candidate_id"] }

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
    fun slice92BacklogPreservesEmbodiedCalmMixAndPolicy() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.2" }
        val existingTitles = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[2] }
            .toSet()

        assertEquals(20, rows.size)
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
            row["replacement_moment"] == "BODY_RESET" ||
                row["replacement_moment"] == "CALM_PHILOSOPHY"
        })
        assertTrue(rows.none { row -> row["source_family_cap_group"] == "Aeon/Psyche" })

        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        assertEquals(8, renderableRows.size)
        assertEquals(12, linkOnlyRows.size)
        assertTrue(renderableRows.all { row -> row["render_mode_candidate"] == "IN_APP_READER" })
        assertTrue(renderableRows.all { row -> row["candidate_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["android_reader_viability"] == "needs_excerpt_selection" })
        assertTrue(renderableRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "false" })
        assertTrue(linkOnlyRows.all { row -> row["render_mode_candidate"] == "EXTERNAL_HANDOFF" })
        assertTrue(linkOnlyRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "true" })
        assertTrue(linkOnlyRows.all { row -> row["android_reader_viability"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> !row["canonical_url"].isNullOrBlank() })
        val baselineSourceUrls = existingInventorySourceUrls()
        assertTrue(rows.none { row -> row["canonical_url"].orEmpty() in baselineSourceUrls })

        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()
        assertEquals(8, sourceCapCounts["Project Gutenberg"])
        assertEquals(4, sourceCapCounts["SAPIENS"])
        assertEquals(3, sourceCapCounts["IEP"])
        assertEquals(3, sourceCapCounts["Museum/Public Institution"])
        assertEquals(2, sourceCapCounts["SEP"])
    }

    @Test
    fun slice92BacklogEncodesSensitiveBodyAndCalmSpotChecks() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.2" }
            .associateBy { row -> row["candidate_id"] }

        val bookOfTea = rows.getValue("s9-2-r04-okakura-book-of-tea")
        assertEquals("medium", bookOfTea["religious_spiritual_framing_risk"])
        assertEquals("medium", bookOfTea["cultural_context_risk"])
        assertEquals("taoism_zennism_cultural_context", bookOfTea["sensitivity_flags"])

        val boethius = rows.getValue("s9-2-r06-boethius-consolation")
        assertEquals("medium", boethius["political_current_events_risk"])
        assertEquals("medium", boethius["religious_spiritual_framing_risk"])
        assertEquals("imprisonment_execution_religious_context", boethius["sensitivity_flags"])

        val confucius = rows.getValue("s9-2-r05-confucius-analects-calm")
        assertEquals("medium", confucius["religious_spiritual_framing_risk"])
        assertEquals("medium", confucius["cultural_context_risk"])
        assertEquals("confucian_classic_translation_cultural_context", confucius["sensitivity_flags"])

        val bodilyAwareness = rows.getValue("s9-2-l01-sep-bodily-awareness")
        assertEquals("medium", bodilyAwareness["medical_health_claim_risk"])
        assertEquals("body_sensation_pain_interoception_discussion", bodilyAwareness["sensitivity_flags"])

        val coffeeRituals = rows.getValue("s9-2-l07-sapiens-coffee-rituals")
        assertEquals("mostly_evergreen", coffeeRituals["durability"])
        assertEquals("medium", coffeeRituals["cultural_context_risk"])
        assertEquals("pandemic_context_ritual_cultural_framing", coffeeRituals["sensitivity_flags"])

        val exerciseHistory = rows.getValue("s9-2-l09-sapiens-exercise-history")
        assertEquals("medium", exerciseHistory["medical_health_claim_risk"])
        assertEquals("mostly_evergreen", exerciseHistory["durability"])
        assertEquals("pandemic_context_exercise_health_claims", exerciseHistory["sensitivity_flags"])
    }

    @Test
    fun slice93BacklogPreservesWonderScienceMixAndPolicy() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.3" }
        val existingTitles = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[2] }
            .toSet()

        assertEquals(26, rows.size)
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
        assertEquals(14, rows.count { row -> row["replacement_moment"] == "WONDER_CURIOSITY" })
        assertEquals(12, rows.count { row -> row["replacement_moment"] == "SCIENCE_CURIOSITY" })

        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        assertEquals(11, renderableRows.size)
        assertEquals(15, linkOnlyRows.size)
        assertTrue(renderableRows.all { row -> row["render_mode_candidate"] == "IN_APP_READER" })
        assertTrue(renderableRows.all { row -> row["candidate_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["renderable_rights_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["android_reader_viability"] == "needs_excerpt_selection" })
        assertTrue(renderableRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "false" })
        assertTrue(linkOnlyRows.all { row -> row["render_mode_candidate"] == "EXTERNAL_HANDOFF" })
        assertTrue(linkOnlyRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "true" })
        assertTrue(linkOnlyRows.all { row -> row["renderable_rights_status"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> row["android_reader_viability"] == "not_applicable" })

        val baselineSourceUrls = existingInventorySourceUrls()
        assertTrue(rows.none { row -> row["canonical_url"].orEmpty() in baselineSourceUrls })

        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()
        assertEquals(11, sourceCapCounts["Project Gutenberg"])
        assertEquals(5, sourceCapCounts["Quanta"])
        assertEquals(3, sourceCapCounts["NASA"])
        assertEquals(2, sourceCapCounts["NOAA"])
        assertEquals(2, sourceCapCounts["OWID"])
        assertEquals(2, sourceCapCounts["Nautilus"])
        assertEquals(1, sourceCapCounts["Museum/Public Institution"])
    }

    @Test
    fun slice93BacklogFlagsChartImageAndDurabilityRisks() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.3" }
            .associateBy { row -> row["candidate_id"] }

        val owidEnergy = rows.getValue("s9-3-l11-owid-energy")
        assertEquals("chart_dependent_external_only", owidEnergy["image_chart_dependency"])
        assertEquals("medium", owidEnergy["political_current_events_risk"])
        assertEquals("true", owidEnergy["must_not_scrape_cache_or_summarize"])

        val owidGreenhouse = rows.getValue("s9-3-l12-owid-greenhouse-gases")
        assertEquals("chart_dependent_external_only", owidGreenhouse["image_chart_dependency"])
        assertEquals("medium", owidGreenhouse["political_current_events_risk"])
        assertEquals("true", owidGreenhouse["must_not_scrape_cache_or_summarize"])

        val carbonCycle = rows.getValue("s9-3-l06-nasa-carbon-cycle-seawifs")
        assertEquals("image_and_data_context_required", carbonCycle["image_chart_dependency"])
        assertEquals("image_credit_review_needed", carbonCycle["third_party_asset_risk"])

        val storyHeavens = rows.getValue("s9-3-r06-ball-story-heavens")
        assertEquals("image_dependent_sections_avoid", storyHeavens["image_chart_dependency"])
        assertEquals("diagram_image_review_needed", storyHeavens["third_party_asset_risk"])

        val spider = rows.getValue("s9-3-r02-fabre-life-spider")
        assertEquals("image_dependent_sections_avoid", spider["image_chart_dependency"])
        assertEquals("diagram_image_review_needed", spider["third_party_asset_risk"])

        val coal = rows.getValue("s9-3-r07-martin-piece-coal")
        assertEquals("medium", coal["political_current_events_risk"])
        assertEquals("dated_energy_industrial_language_review", coal["sensitivity_flags"])

        val universeShape = rows.getValue("s9-3-l04-quanta-universe-shape")
        assertEquals("mostly_evergreen", universeShape["durability"])
        assertEquals("recent_research_durability_review", universeShape["sensitivity_flags"])

        val vertebrateIntelligence = rows.getValue("s9-3-l05-quanta-vertebrate-intelligence")
        assertEquals("mostly_evergreen", vertebrateIntelligence["durability"])
        assertEquals("recent_research_durability_review", vertebrateIntelligence["sensitivity_flags"])

        listOf(
            "s9-3-l01-quanta-life-complexity",
            "s9-3-l02-quanta-quantumness",
            "s9-3-l03-quanta-uncertainty-measurements",
            "s9-3-l04-quanta-universe-shape",
        ).forEach { candidateId ->
            assertEquals(
                "open_page_spot_check_needs_manual_verification",
                rows.getValue(candidateId)["url_verification_method"],
            )
        }
    }

    @Test
    fun slice94BacklogPreservesLongViewHistoryMixAndPolicy() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.4" }
        val existingTitles = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[2] }
            .toSet()

        assertEquals(22, rows.size)
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
        assertEquals(10, rows.count { row -> row["replacement_moment"] == "LONG_VIEW" })
        assertEquals(12, rows.count { row -> row["replacement_moment"] == "HISTORY_CULTURE" })

        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        assertEquals(8, renderableRows.size)
        assertEquals(14, linkOnlyRows.size)
        assertTrue(renderableRows.all { row -> row["source_family_cap_group"] == "Project Gutenberg" })
        assertTrue(renderableRows.all { row -> row["render_mode_candidate"] == "IN_APP_READER" })
        assertTrue(renderableRows.all { row -> row["candidate_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["renderable_rights_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["android_reader_viability"] == "needs_excerpt_selection" })
        assertTrue(renderableRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "false" })
        assertTrue(linkOnlyRows.all { row -> row["render_mode_candidate"] == "EXTERNAL_HANDOFF" })
        assertTrue(linkOnlyRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "true" })
        assertTrue(linkOnlyRows.all { row -> row["renderable_rights_status"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> row["android_reader_viability"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> !row["canonical_url"].isNullOrBlank() })

        val baselineSourceUrls = existingInventorySourceUrls()
        assertTrue(rows.none { row -> row["canonical_url"].orEmpty() in baselineSourceUrls })

        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()
        assertEquals(8, sourceCapCounts["Project Gutenberg"])
        assertEquals(4, sourceCapCounts["Long Now"])
        assertEquals(4, sourceCapCounts["SAPIENS"])
        assertEquals(3, sourceCapCounts["Museum/Public Institution"])
        assertEquals(2, sourceCapCounts["JSTOR Daily"])
        assertEquals(1, sourceCapCounts["Nautilus"])
    }

    @Test
    fun slice94BacklogFlagsLongViewAndCulturalSensitivityRisks() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.4" }
            .associateBy { row -> row["candidate_id"] }

        val frontier = rows.getValue("s9-4-r04-turner-frontier-history")
        assertEquals("high", frontier["cultural_context_risk"])
        assertEquals("frontier_settler_colonial_indigenous_framing", frontier["sensitivity_flags"])

        val dubois = rows.getValue("s9-4-r06-dubois-souls-black-folk")
        assertEquals("high", dubois["cultural_context_risk"])
        assertTrue(dubois["edition_or_translation_note"].orEmpty().contains("2034"))

        val equiano = rows.getValue("s9-4-r07-equiano-interesting-narrative")
        assertEquals("slavery_violence_abolition_context", equiano["sensitivity_flags"])
        assertEquals("medium", equiano["religious_spiritual_framing_risk"])

        val hearn = rows.getValue("s9-4-r08-hearn-glimpses-japan")
        assertEquals("orientalist_period_travel_cultural_context", hearn["sensitivity_flags"])
        assertEquals("high", hearn["cultural_context_risk"])

        val materialRiskLevels = setOf("medium", "high")
        val sensitiveLinkOnlyRows = rows.values.filter { row ->
            row["render_mode_candidate"] == "EXTERNAL_HANDOFF" &&
                (
                    row["cultural_context_risk"] in materialRiskLevels ||
                        row["religious_spiritual_framing_risk"] in materialRiskLevels
                    )
        }
        assertEquals(10, sensitiveLinkOnlyRows.size)
        assertTrue(sensitiveLinkOnlyRows.all { row -> !row["sensitivity_flags"].isNullOrBlank() })

        listOf(
            "s9-4-l01-longnow-orrery-interval",
            "s9-4-l02-longnow-reframing-education",
            "s9-4-l03-longnow-time-machine-museums",
            "s9-4-l09-met-tang-internationalism",
            "s9-4-l10-met-roman-asia-trade",
            "s9-4-l12-jstor-inventing-silk-roads",
            "s9-4-l14-nautilus-walden-deep-time",
        ).forEach { candidateId ->
            assertEquals(
                "open_page_spot_check_needs_manual_verification",
                rows.getValue(candidateId)["url_verification_method"],
            )
        }
    }

    @Test
    fun slice95BacklogCompletesCreativityPlayAndFinalRightsMix() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.5" }
        val existingTitles = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[2] }
            .toSet()

        assertEquals(8, rows.size)
        assertTrue(rows.all { row -> row["candidate_type"] == "shared_editorial_candidate" })
        assertTrue(rows.none { row -> row["candidate_status"] == "already_integrated" })
        assertTrue(rows.none { row -> row["candidate_title"].orEmpty() in existingTitles })
        assertTrue(rows.all { row -> row["pro_review_status"] == "not_submitted" })
        assertTrue(rows.all { row -> row["legal_review_needed"] == "yes" })
        assertTrue(rows.all { row -> row["replacement_moment"] == "CREATIVITY_PLAY" })
        assertTrue(rows.all { row -> row["primary_topic"] == "CREATIVITY" })
        assertTrue(rows.all { row ->
            row["verification_label"] == "manually_verified_candidate" ||
                row["canonical_url_verified_at"].isNullOrBlank()
        })
        assertTrue(rows.all { row ->
            row["verification_label"] == "manually_verified_candidate" ||
                row["canonical_url_verified_by"].isNullOrBlank()
        })

        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        assertEquals(6, renderableRows.size)
        assertEquals(2, linkOnlyRows.size)
        assertTrue(renderableRows.all { row -> row["source_family_cap_group"] == "Project Gutenberg" })
        assertTrue(renderableRows.all { row -> row["render_mode_candidate"] == "IN_APP_READER" })
        assertTrue(renderableRows.all { row -> row["candidate_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["renderable_rights_status"] == "rights_pending" })
        assertTrue(renderableRows.all { row -> row["android_reader_viability"] == "needs_excerpt_selection" })
        assertTrue(renderableRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "false" })
        assertTrue(linkOnlyRows.all { row -> row["render_mode_candidate"] == "EXTERNAL_HANDOFF" })
        assertTrue(linkOnlyRows.all { row -> row["must_not_scrape_cache_or_summarize"] == "true" })
        assertTrue(linkOnlyRows.all { row -> row["renderable_rights_status"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> row["android_reader_viability"] == "not_applicable" })
        assertTrue(linkOnlyRows.all { row -> !row["canonical_url"].isNullOrBlank() })

        val baselineSourceUrls = existingInventorySourceUrls()
        assertTrue(rows.none { row -> row["canonical_url"].orEmpty() in baselineSourceUrls })

        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()
        assertEquals(6, sourceCapCounts["Project Gutenberg"])
        assertEquals(1, sourceCapCounts["Quanta"])
        assertEquals(1, sourceCapCounts["Museum/Public Institution"])
    }

    @Test
    fun slice95BacklogFlagsCreativityAssetAndContextRisks() {
        val rows = readCandidateBacklogRows()
            .filter { row -> row["vertical_slice"] == "9.5" }
            .associateBy { row -> row["candidate_id"] }

        val dudeney = rows.getValue("s9-5-r01-dudeney-amusements-math")
        assertEquals("diagram_review_needed", dudeney["third_party_asset_risk"])
        assertEquals("diagram_dependent_sections_avoid", dudeney["image_chart_dependency"])

        val lear = rows.getValue("s9-5-r02-lear-book-nonsense")
        assertEquals("image_credit_review_needed", lear["third_party_asset_risk"])
        assertEquals("childrens_literature_nonsense_tone_review", lear["sensitivity_flags"])

        val fletcher = rows.getValue("s9-5-r05-fletcher-wood-block-printing")
        assertEquals("image_dependent_sections_avoid", fletcher["image_chart_dependency"])
        assertEquals("medium", fletcher["cultural_context_risk"])

        val quanta = rows.getValue("s9-5-l01-quanta-local-global-graph")
        assertEquals("diagram_context_external_only", quanta["image_chart_dependency"])
        assertEquals("open_page_spot_check_needs_manual_verification", quanta["url_verification_method"])

        val met = rows.getValue("s9-5-l02-met-design-1900-1925")
        assertEquals("Museum/Public Institution", met["source_family_cap_group"])
        assertEquals("medium", met["cultural_context_risk"])
        assertEquals("open_page_spot_check_needs_manual_verification", met["url_verification_method"])
    }

    @Test
    fun sprint9CandidatePoolKeepsModernSourceCapsAndNoApprovedRows() {
        val rows = readCandidateBacklogRows()
        val sourceCapCounts = rows.groupingBy { row -> row["source_family_cap_group"].orEmpty() }.eachCount()

        assertEquals(100, rows.size)
        assertEquals(42, rows.count { row -> row["rights_class_candidate"] == "RENDERABLE" })
        assertEquals(58, rows.count { row -> row["rights_class_candidate"] == "LINK_ONLY" })
        assertTrue(rows.none { row -> row["candidate_status"] == "approved_for_future_integration" })
        assertTrue(sourceCapCounts.filterKeys { key -> key != "Project Gutenberg" }.values.all { count -> count <= 10 })
        assertEquals(42, sourceCapCounts["Project Gutenberg"])
        assertEquals(10, sourceCapCounts["Aeon/Psyche"])
        assertEquals(6, sourceCapCounts["Quanta"])
        assertEquals(6, sourceCapCounts["Nautilus"])
        assertEquals(3, sourceCapCounts["NASA"])
        assertEquals(2, sourceCapCounts["NOAA"])
        assertEquals(2, sourceCapCounts["OWID"])
        assertEquals(8, sourceCapCounts["Museum/Public Institution"])
        assertEquals(8, sourceCapCounts["SAPIENS"])
        assertEquals(4, sourceCapCounts["Long Now"])
        assertEquals(2, sourceCapCounts["JSTOR Daily"])
    }

    @Test
    fun sprint9CandidatePoolDoesNotReuseBaselineSourceUrls() {
        val rows = readCandidateBacklogRows()
        val baselineSourceUrls = existingInventorySourceUrls()

        assertTrue(rows.none { row -> row["canonical_url"].orEmpty() in baselineSourceUrls })
        assertTrue(rows.any { row -> row["candidate_id"] == "s9-1-r03-betts-mind-education" })
        assertTrue(rows.any { row -> row["candidate_id"] == "s9-1-r04-smiles-character-agency" })
        assertTrue(rows.none { row -> row["canonical_url"] == "https://www.gutenberg.org/ebooks/16287" })
        assertTrue(rows.none { row -> row["canonical_url"] == "https://www.gutenberg.org/ebooks/34901" })
    }

    @Test
    fun sprint9CandidatePoolIsIntegratedIntoStarterPacks() {
        val rows = readCandidateBacklogRows()
        val starterPackAsset = File("src/main/assets/editorial/starter_packs.json").readText()
        val sprint9PackIds = setOf(
            "attention_practical_agency_v1",
            "embodied_calm_v1",
            "wonder_science_v1",
            "long_view_history_v1",
            "creativity_play_v1",
        )
        val renderableRows = rows.filter { row -> row["rights_class_candidate"] == "RENDERABLE" }
        val linkOnlyRows = rows.filter { row -> row["rights_class_candidate"] == "LINK_ONLY" }

        sprint9PackIds.forEach { packId ->
            assertTrue("Missing Sprint 9 pack $packId", starterPackAsset.contains("\"id\": \"$packId\""))
        }
        assertEquals(100, rows.count { row ->
            starterPackAsset.contains("\"id\": \"${row["candidate_id"]}\"")
        })
        assertEquals(42, renderableRows.size)
        assertEquals(58, linkOnlyRows.size)
        assertEquals(42, Regex("\"bodyAssetPath\"\\s*:\\s*\"editorial/items/s9_").findAll(starterPackAsset).count())

        renderableRows.forEach { row ->
            val itemObject = starterItemObject(starterPackAsset, row.getValue("candidate_id"))
            val expectedBodyPath = "editorial/items/${row.getValue("candidate_id").replace("-", "_")}.md"
            val bodyFile = File("src/main/assets/$expectedBodyPath")
            assertTrue(itemObject.contains("\"rightsClass\": \"RENDERABLE\""))
            assertTrue(itemObject.contains("\"renderMode\": \"IN_APP_READER\""))
            assertTrue(itemObject.contains("\"bodyAssetPath\": \"$expectedBodyPath\""))
            assertTrue("${row["candidate_id"]} body asset missing", bodyFile.isFile)
            assertTrue(
                "${row["candidate_id"]} body asset is too thin",
                bodyFile.readText().split(Regex("\\s+")).count(String::isNotBlank) >= 500,
            )
            val bodyText = bodyFile.readText()
            val opening = bodyText.trimStart().take(1_800)
            val frontMatter = Regex(
                pattern = "\\b(this preface|preface|contents|translator|translation history|present translation|frontispiece|proof-sheets?|text used|publication history|newly made edition|purpose of the author|this is mainly a book|by recasting these lectures|the reader will perhaps excuse|appeared six years ago|for myself, long a propagandist|for our edition|madame:|sold also by|booksellers?|subscribers?|copies|my little book|chapter-by-chapter|republication|the books which have been written|reader may, perhaps|these volumes|little work now before the reader|fig\\.\\s*1|journey which this little book|every book is|these pages|following heads|dedication|bibliography of|notices published|papers?,?\\s+with figures|it is proposed in\\s+\"[^\"]+\"\\s+to give|title-page|chiefly translated|free translation|in the present edition)\\b|^\\d+\\.\\s|^\\*\\s+as\\s+.*\\bbibliography\\b",
                option = RegexOption.IGNORE_CASE,
            )
            assertFalse("${row["candidate_id"]} reader asset opens with source frontmatter: $opening", frontMatter.containsMatchIn(opening))
            val requiredAnchors = mapOf(
                "s9-3-r03-fabre-life-fly" to Regex("\\b(flies|greenbottles?|luciliae)\\b", RegexOption.IGNORE_CASE),
            )
            requiredAnchors[row.getValue("candidate_id")]?.let { anchor ->
                assertTrue("${row["candidate_id"]} body asset does not match its card subject", anchor.containsMatchIn(bodyText))
            }
        }

        linkOnlyRows.forEach { row ->
            val itemObject = starterItemObject(starterPackAsset, row.getValue("candidate_id"))
            assertTrue(itemObject.contains("\"rightsClass\": \"LINK_ONLY\""))
            assertTrue(itemObject.contains("\"renderMode\": \"EXTERNAL_HANDOFF\""))
            assertTrue(itemObject.contains("\"externalUrl\": \"${row["canonical_url"]}\""))
            assertFalse(itemObject.contains("\"bodyAssetPath\""))
        }
    }

    @Test
    fun sprint9FinalReleaseApprovalReconcilesIntegratedRows() {
        val approvalRows = File(docsRoot, "final_release_approval_20260426.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
        val sprintDoc = File("../docs/SPRINT_9_CONTENT_SOURCE_EXPANSION.md").readText()
        val proPacket = File(docsRoot, "pro_review_packet.md").readText()
        val rightsRegister = File(docsRoot, "rights_risk_register.md").readText()

        assertEquals(100, approvalRows.size)
        assertEquals(42, approvalRows.count { row -> row[3] == "RENDERABLE" && row[10] == "approved_in_app_reader" })
        assertEquals(58, approvalRows.count { row -> row[3] == "LINK_ONLY" && row[10] == "approved_link_only_handoff" })
        assertTrue(approvalRows.all { row -> row[8] == "2026-04-26" })
        assertTrue(approvalRows.all { row -> row[9] == "2026-04-26" })
        assertTrue(approvalRows.all { row -> row[12].contains("Supersedes pre-integration candidate backlog status") })
        assertTrue(sprintDoc.contains("final_release_approval_20260426.csv"))
        assertTrue(proPacket.contains("final_release_approval_20260426.csv"))
        assertTrue(rightsRegister.contains("final_release_approval_20260426.csv"))
    }

    @Test
    fun sprint9DocumentsDurationDistributionDeviation() {
        val rows = readCandidateBacklogRows()
        val durationMinutes = rows.mapNotNull { row -> row["estimated_duration_minutes"]?.toIntOrNull() }
        val short = durationMinutes.count { minutes -> minutes in 3..5 }
        val medium = durationMinutes.count { minutes -> minutes in 6..10 }
        val long = durationMinutes.count { minutes -> minutes in 11..20 }
        val proPacket = File(docsRoot, "pro_review_packet.md").readText()
        val sprintDoc = File("../docs/SPRINT_9_CONTENT_SOURCE_EXPANSION.md").readText()

        assertEquals(100, durationMinutes.size)
        assertEquals(6, short)
        assertEquals(63, medium)
        assertEquals(31, long)
        assertTrue(proPacket.contains("6 candidates at 3-5 minutes; 63 at 6-10 minutes; 31 at 11-20 minutes"))
        assertTrue(proPacket.contains("distribution deviation"))
        assertTrue(sprintDoc.contains("Documented duration-distribution deviation"))
    }

    @Test
    fun existingInventoryAuditPreservesPreSprint9Baseline() {
        val auditRows = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
        val starterPackAsset = File("src/main/assets/editorial/starter_packs.json").readText()
        val starterItemCount = Regex("\"durationMinutes\"\\s*:").findAll(starterPackAsset).count()
        val starterSourceUrls = Regex("\"sourceUrl\"\\s*:\\s*\"([^\"]+)\"")
            .findAll(starterPackAsset)
            .map { match -> match.groupValues[1] }
            .toSet()

        assertEquals(45, auditRows.size)
        assertEquals(145, starterItemCount)
        assertEquals(25, auditRows.count { row -> row[4] == "RENDERABLE" && row[5] == "IN_APP_READER" })
        assertEquals(20, auditRows.count { row -> row[4] == "LINK_ONLY" && row[5] == "EXTERNAL_HANDOFF" })
        assertTrue(auditRows.all { row -> row[11] == "already_integrated" })
        assertTrue(auditRows.all { row -> row[12] == "false" })
        assertTrue(existingInventorySourceUrls().all { sourceUrl -> sourceUrl in starterSourceUrls })
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
        assertTrue(caps.contains("`JSTOR Daily`"))
        assertTrue(caps.contains("Link-only rows from modern publications must remain `EXTERNAL_HANDOFF`"))
        assertFalse(caps.contains("infinite feed", ignoreCase = true))
    }

    @Test
    fun sprint9IntegrationDocsPreserveReviewAndRuntimeBoundaries() {
        val rows = readCandidateBacklogRows()
        val proPacket = File(docsRoot, "pro_review_packet.md").readText()
        val rightsRegister = File(docsRoot, "rights_risk_register.md").readText()
        val cutLog = File(docsRoot, "candidate_cut_log.md").readText()
        val packPlan = File(docsRoot, "pack_cluster_plan.md").readText()

        assertTrue(proPacket.contains("Sprint 9 app integration completed"))
        assertTrue(proPacket.contains("New Sprint 9 candidates | 100"))
        assertTrue(proPacket.contains("Renderable candidates | 42"))
        assertTrue(proPacket.contains("Link-only candidates | 58"))
        assertTrue(proPacket.contains("Integrated Sprint 9 starter-pack items | 100"))
        assertTrue(rightsRegister.contains("Renderable Project Gutenberg candidates | 42"))
        assertTrue(rightsRegister.contains("Link-only modern or unclear-rights candidates | 58"))
        assertTrue(rightsRegister.contains("No candidate body text from modern link-only pages goes into app assets"))
        assertTrue(rightsRegister.contains("42 Sprint 9 Markdown body assets are app-local Project Gutenberg text excerpts"))
        assertEquals(4, rows.count { row -> row["cultural_context_risk"] == "high" })
        assertEquals(25, rows.count { row -> row["cultural_context_risk"] == "medium" })
        assertEquals(21, rows.count { row -> row["political_current_events_risk"] == "medium" })
        assertEquals(11, rows.count { row -> row["religious_spiritual_framing_risk"] == "medium" })
        assertEquals(6, rows.count { row -> row["medical_health_claim_risk"] == "medium" })
        assertTrue(rightsRegister.contains("Counts below are medium-only rows"))
        assertTrue(rightsRegister.contains("Cultural-context risk | 25"))
        assertTrue(rightsRegister.contains("Political/current-events risk | 21"))
        assertTrue(rightsRegister.contains("Religious/spiritual framing risk | 11"))
        assertTrue(rightsRegister.contains("Medical/health-claim risk | 6"))
        assertTrue(cutLog.contains("Integrated into `starter_packs.json` | 100"))
        assertTrue(cutLog.contains("Renderable body assets shipped | 42"))
        assertTrue(cutLog.contains("Link-only external handoff items shipped | 58"))
        assertTrue(packPlan.contains("Creativity and Play | 10+"))
        assertTrue(packPlan.contains("include no more than 4 Project Gutenberg rows"))
        assertTrue(packPlan.contains("Link-only rows remain external handoff metadata only"))
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

    private fun existingInventorySourceUrls(): Set<String> {
        val starterPackAsset = File("src/main/assets/editorial/starter_packs.json").readText()
        val auditItemIds = File(docsRoot, "existing_inventory_audit.csv").readLines()
            .drop(1)
            .map(::parseCsvLine)
            .map { row -> row[0] }
        return auditItemIds.mapNotNull { itemId ->
            Regex("\"sourceUrl\"\\s*:\\s*\"([^\"]+)\"")
                .find(starterItemObject(starterPackAsset, itemId))
                ?.groupValues
                ?.get(1)
        }.toSet()
    }

    private fun starterItemObject(starterPackAsset: String, itemId: String): String {
        val marker = "\"id\": \"$itemId\""
        val markerIndex = starterPackAsset.indexOf(marker)
        assertTrue("Missing starter-pack item $itemId", markerIndex >= 0)
        val objectStart = starterPackAsset.lastIndexOf("{", markerIndex)
        assertTrue("Missing object start for $itemId", objectStart >= 0)
        var depth = 0
        for (index in objectStart until starterPackAsset.length) {
            when (starterPackAsset[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return starterPackAsset.substring(objectStart, index + 1)
                    }
                }
            }
        }
        error("Missing object end for $itemId")
    }
}
