package com.example.data.local

data class StrongsEntry(
    val strongsNumber: String,
    val word: String,
    val originalScript: String,
    val transliteration: String,
    val testament: String, // "New Testament" (Greek) or "Old Testament" (Hebrew)
    val partOfSpeech: String,
    val literalMeaning: String,
    val ancientAudienceUnderstanding: String,
    val keyVerses: List<String>,
    val rootEtymology: String
)

object StrongsConcordanceData {

    val entries: List<StrongsEntry> = listOf(
        // GREEK (New Testament)
        StrongsEntry(
            strongsNumber = "G26",
            word = "Agape",
            originalScript = "ἀγάπη",
            transliteration = "agapē",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Self-sacrificing covenant love; deliberate active benevolence",
            ancientAudienceUnderstanding = "Not an emotional affection or passion (eros/philia), but a solemn covenantal commitment to the highest good of another regardless of cost or reciprocity.",
            keyVerses = listOf("John 3:16", "1 Corinthians 13:4-8", "1 John 4:8"),
            rootEtymology = "Derived from agapao (G25), to welcome, entertain, or prefer above all else."
        ),
        StrongsEntry(
            strongsNumber = "G5485",
            word = "Charis",
            originalScript = "χάρις",
            transliteration = "charis",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Unmerited favor; reciprocal benevolence in Greco-Roman patronage",
            ancientAudienceUnderstanding = "In the 1st-century Roman patronage system, charis was the benevolent gift bestowed by an elite patron upon a client, which created an enduring relationship of reciprocal loyalty (pistis).",
            keyVerses = listOf("Ephesians 2:8-9", "Romans 5:1-2", "2 Corinthians 12:9"),
            rootEtymology = "From chairo (G5463), to rejoice or be glad; the joyful wellspring of favor."
        ),
        StrongsEntry(
            strongsNumber = "G4102",
            word = "Pistis",
            originalScript = "πίστις",
            transliteration = "pistis",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Faith, active allegiance, fiduciary fidelity",
            ancientAudienceUnderstanding = "Not merely intellectual assent to doctrinal propositions, but wholehearted covenant loyalty and pledged obedience, reflecting Roman fides—the bedrock of political and personal treaties.",
            keyVerses = listOf("Romans 1:17", "Hebrews 11:1", "Galatians 2:16"),
            rootEtymology = "From peitho (G3982), to persuade, listen to, or yield allegiance."
        ),
        StrongsEntry(
            strongsNumber = "G1343",
            word = "Dikaiosyne",
            originalScript = "δικαιοσύνη",
            transliteration = "dikaiosynē",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Righteousness, covenant rectitude, legal vindication",
            ancientAudienceUnderstanding = "In Greco-Roman legal civic terminology, dikaiosyne meant living in accordance with judicial and cosmic justice; Paul unites it with the Hebrew concept of covenant fidelity.",
            keyVerses = listOf("Romans 3:21-22", "Matthew 6:33", "2 Corinthians 5:21"),
            rootEtymology = "From dike (G1349), custom, rule, judicial verdict, right order."
        ),
        StrongsEntry(
            strongsNumber = "G5206",
            word = "Huiothesia",
            originalScript = "υἱοθεσία",
            transliteration = "huiothesia",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Adoption as adult sons; transfer of legal patrimony",
            ancientAudienceUnderstanding = "Under Roman civil law, huiothesia severed patria potestas (the former father's authority), cancelled all previous debts, and granted legal rights to the adoptive father's estate.",
            keyVerses = listOf("Romans 8:15", "Galatians 4:5", "Ephesians 1:5"),
            rootEtymology = "Compound of huios (son) and thesis (legal placement/positioning)."
        ),
        StrongsEntry(
            strongsNumber = "G629",
            word = "Apolytrosis",
            originalScript = "ἀπολύτρωσις",
            transliteration = "apolytrosis",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Redemption, emancipation through payment of ransom",
            ancientAudienceUnderstanding = "Commercial legal procedure in Greco-Roman slave markets where a third party deposited a ransom price (lytron) at a temple treasury to purchase a slave's perpetual freedom.",
            keyVerses = listOf("Romans 3:24", "Ephesians 1:7", "Colossians 1:14"),
            rootEtymology = "From apo (away from) and lytron (ransom/release price)."
        ),
        StrongsEntry(
            strongsNumber = "G1577",
            word = "Ekklesia",
            originalScript = "ἐκκλησία",
            transliteration = "ekklesia",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Summoned assembly; democratic civic council",
            ancientAudienceUnderstanding = "In Greek poleis (city-states), the ekklesia was the governing body of full citizens summoned by the town herald to deliberate and govern city affairs—not a passive congregation.",
            keyVerses = listOf("Matthew 16:18", "Acts 2:47", "1 Corinthians 12:28"),
            rootEtymology = "From ek (out of) and kaleo (to summon or call)."
        ),
        StrongsEntry(
            strongsNumber = "G3056",
            word = "Logos",
            originalScript = "λόγος",
            transliteration = "logos",
            testament = "New Testament",
            partOfSpeech = "Noun, Masculine",
            literalMeaning = "The Word, divine cosmic reason, revelatory speech",
            ancientAudienceUnderstanding = "In Stoic philosophy, the cosmic principle animating the universe; in Jewish wisdom and Targums, the 'Memra' (divine communicative presence of Yahweh).",
            keyVerses = listOf("John 1:1-14", "Hebrews 4:12", "Revelation 19:13"),
            rootEtymology = "From lego (G3004), to speak, gather, or reckon."
        ),
        StrongsEntry(
            strongsNumber = "G2842",
            word = "Koinonia",
            originalScript = "κοινωνία",
            transliteration = "koinōnia",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Joint partnership, shared enterprise, communal participation",
            ancientAudienceUnderstanding = "A commercial business partnership in Roman law (societas) where partners pool capital, share liabilities, and mutually own enterprise outcomes.",
            keyVerses = listOf("Acts 2:42", "1 John 1:3", "Philippians 1:5"),
            rootEtymology = "From koinos (G2839), common, shared, held in partnership."
        ),
        StrongsEntry(
            strongsNumber = "G3875",
            word = "Parakletos",
            originalScript = "παράκλητος",
            transliteration = "paraklētos",
            testament = "New Testament",
            partOfSpeech = "Noun, Masculine",
            literalMeaning = "Advocate, legal counsel, one summoned alongside",
            ancientAudienceUnderstanding = "In Greek and Roman jurisprudence, a trusted patron, character witness, or legal assistant summoned to stand beside a defendant in a tribunal.",
            keyVerses = listOf("John 14:16", "John 15:26", "1 John 2:1"),
            rootEtymology = "From para (beside) and kaleo (called to one's side)."
        ),
        StrongsEntry(
            strongsNumber = "G3341",
            word = "Metanoia",
            originalScript = "μετάνοια",
            transliteration = "metanoia",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Transformative change of mind; complete reversal of life orientation",
            ancientAudienceUnderstanding = "Far deeper than emotional remorse (metamelomai); a fundamental realignment of one's perceptual worldview, ethical allegiance, and supreme allegiance.",
            keyVerses = listOf("Mark 1:15", "Acts 2:38", "Romans 2:4"),
            rootEtymology = "From meta (after/change) and noieo (to perceive/think)."
        ),
        StrongsEntry(
            strongsNumber = "G2222",
            word = "Zoe",
            originalScript = "ζωή",
            transliteration = "zōē",
            testament = "New Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Divine uncreated life; spiritual vitality",
            ancientAudienceUnderstanding = "Contrasted with bios (biological animal existence); zoe is the eternal, incorruptible life intrinsic to the Creator Himself.",
            keyVerses = listOf("John 10:10", "John 14:6", "1 John 5:12"),
            rootEtymology = "From zao (G2198), to live, breathe, flourish in divine fullness."
        ),

        // HEBREW (Old Testament)
        StrongsEntry(
            strongsNumber = "H2617",
            word = "Hesed",
            originalScript = "חֶסֶד",
            transliteration = "chesed",
            testament = "Old Testament",
            partOfSpeech = "Noun, Masculine",
            literalMeaning = "Covenant lovingkindness; steadfast loyal devotion",
            ancientAudienceUnderstanding = "The obligating, unwavering faithfulness of a covenant partner. It is not whimsical sentiment, but sworn allegiance anchored in ancient Near Eastern covenant treaties.",
            keyVerses = listOf("Psalm 136:1", "Micah 6:8", "Exodus 34:6-7"),
            rootEtymology = "Primary root expressing steadfast, reciprocal covenant commitment."
        ),
        StrongsEntry(
            strongsNumber = "H7965",
            word = "Shalom",
            originalScript = "שָׁלוֹם",
            transliteration = "shalōm",
            testament = "Old Testament",
            partOfSpeech = "Noun, Masculine",
            literalMeaning = "Holistic wholeness, complete well-being, societal harmony",
            ancientAudienceUnderstanding = "Not mere absence of military conflict; shalom describes cosmic and communal completeness where every element of life—health, justice, relationships—is restored to original divine order.",
            keyVerses = listOf("Numbers 6:24-26", "Isaiah 9:6", "Jeremiah 29:11"),
            rootEtymology = "From shalam (H7999), to make safe, complete, restore, or repay."
        ),
        StrongsEntry(
            strongsNumber = "H1285",
            word = "Berit",
            originalScript = "בְּרִית",
            transliteration = "berît",
            testament = "Old Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Covenant, solemn binding treaty sealed by oath",
            ancientAudienceUnderstanding = "Modeled on Hittite and ancient Near Eastern suzerainty treaties: solemn life-and-death bonds established by 'cutting' (karat berit) sacrificial animals as witness to sworn consequences.",
            keyVerses = listOf("Genesis 15:18", "Exodus 24:7-8", "Jeremiah 31:31"),
            rootEtymology = "Likely related to cutting (karat) or binding (barah) oaths."
        ),
        StrongsEntry(
            strongsNumber = "H6918",
            word = "Kadosh",
            originalScript = "קָדוֹשׁ",
            transliteration = "qādôsh",
            testament = "Old Testament",
            partOfSpeech = "Adjective",
            literalMeaning = "Sacred, wholly set apart, utterly distinct",
            ancientAudienceUnderstanding = "Transcendent otherness. In ancient Israelite sanctuary theology, Yahweh is in a supreme category of His own; holiness emanates outward like clean fire requiring ritual purifications.",
            keyVerses = listOf("Isaiah 6:3", "Leviticus 19:2", "1 Samuel 2:2"),
            rootEtymology = "From qadash (H6942), to cut, separate, demarcate from the common realm."
        ),
        StrongsEntry(
            strongsNumber = "H1288",
            word = "Barak",
            originalScript = "בָּרַךְ",
            transliteration = "bārak",
            testament = "Old Testament",
            partOfSpeech = "Verb",
            literalMeaning = "To bless, kneel, adore (and euphemistic contronym: to curse)",
            ancientAudienceUnderstanding = "Ancient contronym. Primarily to bow the knee in veneration and bestow divine flourishing/fruitfulness; used ironically in court legal contexts (Job 1:5, 2:9) to mean renounce/curse.",
            keyVerses = listOf("Genesis 12:2-3", "Psalm 103:1", "Job 1:21"),
            rootEtymology = "Primitive root denoting bending the knee (berek) in reverence."
        ),
        StrongsEntry(
            strongsNumber = "H6666",
            word = "Tsedaqah",
            originalScript = "צְדָקָה",
            transliteration = "tṣedāqāh",
            testament = "Old Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Righteousness, communal equity, covenant rectitude",
            ancientAudienceUnderstanding = "Relational justice: upholding the rights of the poor, widow, orphan, and foreigner within the covenant community as required by Yahweh's character.",
            keyVerses = listOf("Genesis 15:6", "Amos 5:24", "Isaiah 1:17"),
            rootEtymology = "From tsadaq (H6663), to be just, right, straight, legally acquitted."
        ),
        StrongsEntry(
            strongsNumber = "H1350",
            word = "Goel",
            originalScript = "גֹּאֵל",
            transliteration = "gō'ēl",
            testament = "Old Testament",
            partOfSpeech = "Participle, Masculine",
            literalMeaning = "Kinsman-redeemer; family legal defender",
            ancientAudienceUnderstanding = "The nearest male relative legally obligated under Toraitic law to buy back family land, redeem relatives sold into debt slavery, and avenge unjust blood.",
            keyVerses = listOf("Leviticus 25:25", "Ruth 3:12-13", "Job 19:25"),
            rootEtymology = "From ga'al (H1350), to redeem, repurchase, defend family rights."
        ),
        StrongsEntry(
            strongsNumber = "H8085",
            word = "Shema",
            originalScript = "שָׁמַע",
            transliteration = "shāma'",
            testament = "Old Testament",
            partOfSpeech = "Verb",
            literalMeaning = "To hear, listen attentively, respond with active obedience",
            ancientAudienceUnderstanding = "In Hebrew psychology, hearing and obeying are inseparable. There is no separate Hebrew verb for 'obey'; to truly hear God's voice is to immediately do what is commanded.",
            keyVerses = listOf("Deuteronomy 6:4-5", "Exodus 19:5", "1 Samuel 15:22"),
            rootEtymology = "Primitive root denoting listening with the intent of wholehearted compliance."
        ),
        StrongsEntry(
            strongsNumber = "H7307",
            word = "Ruach",
            originalScript = "רוּחַ",
            transliteration = "rûaḥ",
            testament = "Old Testament",
            partOfSpeech = "Noun, Feminine/Masculine",
            literalMeaning = "Breath, wind, animating Spirit of God",
            ancientAudienceUnderstanding = "The invisible, dynamic, life-giving power of Yahweh. Just as the desert wind reshapes landscapes, the Ruach of God hovered over chaos to create cosmic order and empowered prophetic leaders.",
            keyVerses = listOf("Genesis 1:2", "Ezekiel 37:9-14", "Psalm 104:30"),
            rootEtymology = "Primitive root representing dynamic air motion and divine breath."
        ),
        StrongsEntry(
            strongsNumber = "H8451",
            word = "Torah",
            originalScript = "תּוֹרָה",
            transliteration = "tôrâ",
            testament = "Old Testament",
            partOfSpeech = "Noun, Feminine",
            literalMeaning = "Instruction, guidance, fatherly covenant direction",
            ancientAudienceUnderstanding = "Often translated narrowly as 'Law' in English, but to ancient Israelites it was the warm, instructional guidance of a loving Father aiming arrows straight toward life and covenant flourishing.",
            keyVerses = listOf("Psalm 1:2", "Psalm 119:97", "Proverbs 3:1"),
            rootEtymology = "From yarah (H3384), to shoot an arrow or point out the correct path."
        )
    )

    fun search(query: String, testamentFilter: String = "All"): List<StrongsEntry> {
        val q = query.trim().lowercase()
        return entries.filter { entry ->
            val matchesFilter = when (testamentFilter) {
                "Greek (NT)" -> entry.testament.contains("New", ignoreCase = true)
                "Hebrew (OT)" -> entry.testament.contains("Old", ignoreCase = true)
                "Church Fathers" -> PatristicWitnessData.getPatristicProfile(entry.word) != null
                else -> true
            }
            val matchesQuery = if (q.isBlank()) true else {
                entry.word.lowercase().contains(q) ||
                entry.strongsNumber.lowercase().contains(q) ||
                entry.originalScript.contains(q) ||
                entry.transliteration.lowercase().contains(q) ||
                entry.literalMeaning.lowercase().contains(q) ||
                entry.ancientAudienceUnderstanding.lowercase().contains(q)
            }
            matchesFilter && matchesQuery
        }
    }
}
