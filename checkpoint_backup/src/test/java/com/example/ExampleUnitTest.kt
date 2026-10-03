package com.example

import com.example.data.local.CuratedWordStudies
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEuaggelionCuratedStudy() {
        val study = CuratedWordStudies.getCuratedStudy("euaggelion", "New Testament")
        assertNotNull(study)
        assertEquals("euaggelion", study?.word)
        assertEquals("εὐαγγέλιον", study?.originalWord)
        assertEquals("G2098", study?.strongsNumber)
        assertEquals("New Testament", study?.testament)
        assertTrue(study?.historicalBackground?.contains("Priene") == true)
        assertTrue(study?.legalDimension?.contains("Lex Maiestas") == true)
    }

    @Test
    fun testEuaggelionAliases() {
        val gospelStudy = CuratedWordStudies.getCuratedStudy("gospel")
        assertNotNull(gospelStudy)
        assertEquals("G2098", gospelStudy?.strongsNumber)

        val euangelionStudy = CuratedWordStudies.getCuratedStudy("euangelion")
        assertNotNull(euangelionStudy)
        assertEquals("εὐαγγέλιον", euangelionStudy?.originalWord)
    }

    @Test
    fun testCuratedChatResponse() {
        val study = CuratedWordStudies.getCuratedStudy("euaggelion")!!
        val historyReply = CuratedWordStudies.getCuratedChatResponse(study, "What is the historical background of Priene?")
        assertTrue(historyReply.contains("historical", ignoreCase = true))

        val legalReply = CuratedWordStudies.getCuratedChatResponse(study, "Explain the legal dimension under Roman law")
        assertTrue(legalReply.contains("legal", ignoreCase = true))
    }

    @Test
    fun testCuratedDatabaseHasEssentialWords() {
        val all = CuratedWordStudies.getAllCuratedStudies()
        assertTrue(all.any { it.word == "euaggelion" })
        assertTrue(all.any { it.word == "grace" })
        assertTrue(all.any { it.word == "covenant" })
        assertTrue(all.any { it.word == "shalom" })
        assertTrue(all.any { it.word == "agape" })
    }

    @Test
    fun testAppSettingDataModel() {
        val setting = com.example.data.local.AppSetting("api_key_Gemini", "AIzaSyTestKey123")
        assertEquals("api_key_Gemini", setting.key)
        assertEquals("AIzaSyTestKey123", setting.value)
    }

    @Test
    fun testSourceTrustVerifierAuthenticPrimarySource() {
        val source = com.example.data.verification.SourceTrustVerifier.evaluateSource(
            url = "https://inscriptions.packhum.org/text/264",
            rawTitle = "Priene Calendar Inscription (OGIS 458)"
        )
        assertTrue(source.isAuthentic)
        assertEquals(com.example.data.verification.TrustTier.PRIMARY_ARCHAEOLOGICAL, source.trustTier)
        assertTrue(source.trustScore >= 95)
    }

    @Test
    fun testSourceTrustVerifierAcademicPress() {
        val source = com.example.data.verification.SourceTrustVerifier.evaluateSource(
            url = "https://www.cambridge.org/core/books/roman-adoption",
            rawTitle = "Cambridge University Press: Roman Law of Adoption"
        )
        assertTrue(source.isAuthentic)
        assertEquals(com.example.data.verification.TrustTier.ACADEMIC_PEER_REVIEWED, source.trustTier)
        assertTrue(source.trustScore >= 90)
    }

    @Test
    fun testSourceTrustVerifierFiltersUntrustedSources() {
        val badSource = com.example.data.verification.SourceTrustVerifier.evaluateSource(
            url = "https://reddit.com/r/theology/discussion-on-adoption",
            rawTitle = "Reddit Community Discussion Forum"
        )
        assertFalse(badSource.isAuthentic)
        assertEquals(com.example.data.verification.TrustTier.UNVERIFIED_FILTERED, badSource.trustTier)
        assertTrue(badSource.trustScore < 50)
    }

    @Test
    fun testAuditAndFilterSourcesRemovesUntrusted() {
        val good1 = com.example.data.verification.SourceTrustVerifier.evaluateSource("https://epigraphy.info/cil", "Corpus Inscriptionum Latinarum")
        val good2 = com.example.data.verification.SourceTrustVerifier.evaluateSource("https://stepbible.org/bdag", "BDAG Greek Lexicon")
        val bad = com.example.data.verification.SourceTrustVerifier.evaluateSource("https://quora.com/what-is-grace", "Quora opinion thread")

        val (filteredSources, metadata) = com.example.data.verification.SourceTrustVerifier.auditAndFilterSources(
            rawSources = listOf(good1, good2, bad),
            strictFilter = true
        )

        assertEquals(2, filteredSources.size)
        assertEquals(1, metadata.filteredOutSourcesCount)
        assertTrue(filteredSources.none { it.url.contains("quora") })
        assertTrue(metadata.overallTrustScore >= 90)
    }
}
