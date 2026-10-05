package com.example.data.local

import com.example.data.model.WordStudy
import java.util.Locale

enum class AnomalyType(val displayName: String, val badgeLabel: String, val description: String) {
    HOMONYM(
        displayName = "Homonym",
        badgeLabel = "HOMONYM DETECTED",
        description = "Identical original language spelling with completely unrelated root definitions"
    ),
    CONTRONYM(
        displayName = "Contronym",
        badgeLabel = "CONTRONYM DETECTED",
        description = "Single word/root that can mean its own exact polar opposite (Janus-word)"
    ),
    SPLIT_TRANSLATION(
        displayName = "Split-Translation",
        badgeLabel = "SPLIT-TRANSLATION DETECTED",
        description = "One unified original root family split into entirely separate English theological words"
    )
}

data class ComparisonRow(
    val dimension: String,
    val contextMeaning: String,
    val alternateMeaning: String,
    val scripturalNote: String = ""
)

data class LinguisticAnomalyInfo(
    val anomalyType: AnomalyType,
    val rootWord: String,
    val strongsNumber: String,
    val primaryMeaning: String,
    val alternateMeaning: String,
    val comparisonRows: List<ComparisonRow>,
    val comparisonMarkdownTable: String,
    val wowFactor: String
)

object LinguisticAnomalyHelper {

    fun parseMarkdownComparisonTable(markdown: String): List<ComparisonRow> {
        val lines = markdown.lines()
            .map { it.trim() }
            .filter { it.startsWith("|") && it.endsWith("|") && !it.contains("---") }

        if (lines.size <= 1) return emptyList()

        // Skip header row
        val dataRows = lines.drop(1)
        return dataRows.mapNotNull { line ->
            val cols = line.split("|")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (cols.size >= 3) {
                ComparisonRow(
                    dimension = cols[0],
                    contextMeaning = cols[1],
                    alternateMeaning = cols[2],
                    scripturalNote = if (cols.size > 3) cols[3] else ""
                )
            } else if (cols.size == 2) {
                ComparisonRow(
                    dimension = "Meaning",
                    contextMeaning = cols[0],
                    alternateMeaning = cols[1]
                )
            } else null
        }
    }

    fun resolveAnomaly(study: WordStudy): LinguisticAnomalyInfo? {
        // 1. If study already carries AI-generated anomaly data, verify and return it
        if (study.hasLinguisticAnomaly || study.anomalyType.isNotBlank()) {
            val type = when (study.anomalyType.uppercase(Locale.ROOT)) {
                "HOMONYM" -> AnomalyType.HOMONYM
                "CONTRONYM" -> AnomalyType.CONTRONYM
                else -> AnomalyType.SPLIT_TRANSLATION
            }

            val parsedRows = if (study.anomalyComparisonTable.isNotBlank()) {
                parseMarkdownComparisonTable(study.anomalyComparisonTable)
            } else emptyList()

            val fallbackRows = if (parsedRows.isEmpty()) {
                listOf(
                    ComparisonRow("Core Meaning", study.anomalyPrimaryMeaning.ifBlank { study.literalMeaning }, study.anomalyAlternateMeaning),
                    ComparisonRow("Biblical Usage", "Primary translation in this context", "Hidden alternate / opposite nuance")
                )
            } else parsedRows

            return LinguisticAnomalyInfo(
                anomalyType = type,
                rootWord = study.anomalyRootWord.ifBlank { "${study.transliteration} (${study.originalWord})" },
                strongsNumber = study.anomalyStrongsNumber.ifBlank { study.strongsNumber },
                primaryMeaning = study.anomalyPrimaryMeaning.ifBlank { study.literalMeaning },
                alternateMeaning = study.anomalyAlternateMeaning,
                comparisonRows = fallbackRows,
                comparisonMarkdownTable = study.anomalyComparisonTable,
                wowFactor = study.anomalyWowFactor.ifBlank { study.translationDisconnect }
            )
        }

        // 2. Curated & scholarly knowledge-base scan for known high-impact anomalies
        val clean = study.word.lowercase(Locale.ROOT).trim()
        val orig = study.originalWord
        val translit = study.transliteration.lowercase(Locale.ROOT).trim()
        val isOt = study.testament.contains("Old", ignoreCase = true)
        val isNt = study.testament.contains("New", ignoreCase = true)

        return when {
            // Contronym: Barak (Bless vs. Curse)
            clean.contains("barak") || clean.contains("bless") || orig.contains("בָּרַךְ") || translit.contains("barak") -> {
                if (isOt || clean.contains("barak")) {
                    LinguisticAnomalyInfo(
                        anomalyType = AnomalyType.CONTRONYM,
                        rootWord = "Barak (בָּרַךְ)",
                        strongsNumber = "H1288",
                        primaryMeaning = "To bless, kneel in humble adoration, praise God (Gen 12:2, Ps 103:1)",
                        alternateMeaning = "To curse, blaspheme, renounce God (Job 1:5, 1:11, 2:9, 1 Kgs 21:10)",
                        comparisonRows = listOf(
                            ComparisonRow("Root Meaning", "To bend the knee / Bless God", "To renounce / Curse God (Sacred Euphemism)"),
                            ComparisonRow("Job 2:9 ('Curse God and die')", "Lit. 'Bless (barak) God and die!'", "Euphemistic inversion by ancient scribes"),
                            ComparisonRow("1 Kings 21:10 (Naboth trial)", "Accused of: 'Naboth blessed God'", "Translated as: 'Naboth cursed God and king'"),
                            ComparisonRow("Theological Motive", "Reverential worship before Yahweh", "Aversion to writing a direct curse against God")
                        ),
                        comparisonMarkdownTable = """
                            | Context | Hebrew Lemma (Barak) | Context Meaning | Hidden Contronym / Euphemism |
                            | --- | --- | --- | --- |
                            | Worship (Ps 103:1) | בָּרַךְ (barak) | Bless the LORD | Knee bent in praise |
                            | Job's Trial (Job 2:9) | בָּרַךְ (barak) | 'Bless God & die' | Scribes substituted 'bless' for 'curse' |
                            | Capital Crime (1 Kgs 21:10) | בָּרַךְ (barak) | 'Blessed God & king' | Judicially meant treasonous blasphemy |
                        """.trimIndent(),
                        wowFactor = "Ancient Hebrew scribes considered uttering or writing a direct curse against Yahweh so utterly horrifying that they employed 'barak' as a reverential contronym. When Job's wife tells him to 'Curse God and die' (Job 2:9), the Hebrew text literally reads: 'Bless God and die!' English translations completely hide this sacred linguistic paradox."
                    )
                } else null
            }

            // Homonym: Chesed (Mercy/Loyalty vs. Reproach/Shame)
            clean.contains("chesed") || clean.contains("hesed") || clean.contains("lovingkindness") || orig.contains("חֶסֶד") -> {
                LinguisticAnomalyInfo(
                    anomalyType = AnomalyType.HOMONYM,
                    rootWord = "Chesed (חֶסֶד)",
                    strongsNumber = "H2617 & H2617b",
                    primaryMeaning = "Unfailing covenant loyalty, steadfast love, active mercy (Exod 34:6, Ps 136)",
                    alternateMeaning = "Shame, abominable reproach, incestuous disgrace (Lev 20:17, Prov 14:34)",
                    comparisonRows = listOf(
                        ComparisonRow("Root Spelling", "חֶסֶד (chesed) - Root I", "חֶסֶד (chesed) - Root II"),
                        ComparisonRow("Strong's Entry", "H2617: Covenant lovingkindness", "H2617b: Reproach / Abomination"),
                        ComparisonRow("Key Verse", "Exod 34:6 ('abounding in chesed')", "Lev 20:17 ('it is a chesed / wicked thing')"),
                        ComparisonRow("Proverbs 14:34", "Righteousness exalts a nation", "Sin is a 'chesed' (reproach) to any people")
                    ),
                    comparisonMarkdownTable = """
                        | Dimension | Contextual Meaning (H2617) | Hidden Homonym (H2617b) |
                        | --- | --- | --- |
                        | Core Definition | Covenant steadfast love & mercy | Shameful disgrace & moral reproach |
                        | Scripture Focus | Exodus 34:6; Psalm 136:1 | Leviticus 20:17; Proverbs 14:34 |
                        | Covenant Impact | The highest manifestation of God | The deepest societal violation |
                    """.trimIndent(),
                    wowFactor = "In Biblical Hebrew, the exact same three consonants (ח-ס-ד) represent both the highest moral glory of God (His unbreakable covenant loyalty) and the darkest societal shame (in Leviticus 20:17 and Proverbs 14:34, where sin is called a 'chesed'—a reproach). English versions obscure that true covenant unfaithfulness is not passive indifference; it inverts sacred loyalty into an absolute public reproach."
                )
            }

            // Split-Translation: Dikaiosyne (Righteousness vs. Justification)
            clean.contains("right") || clean.contains("just") || clean.contains("dike") || clean.contains("dikaiosyne") || orig.contains("δικαιοσύνη") -> {
                LinguisticAnomalyInfo(
                    anomalyType = AnomalyType.SPLIT_TRANSLATION,
                    rootWord = if (isOt) "Tsadaq (צָדַק / צְדָקָה)" else "Dikaiosynē / Dikē (δικαιοσύνη / δίκη)",
                    strongsNumber = if (isOt) "H6666 / H6663" else "G1343 / G1344",
                    primaryMeaning = if (isOt) "Righteousness (tsedakah): Relational covenant faithfulness" else "Righteousness (dikaiosynē): Covenant status & moral uprightness",
                    alternateMeaning = if (isOt) "Justification (hitsdiq): Forensic acquittal & vindication" else "Justification (dikaioō): Judicial verdict declaring one in the right",
                    comparisonRows = listOf(
                        ComparisonRow("Noun Form", if (isOt) "צְדָקָה (tsedakah)" else "δικαιοσύνη (dikaiosynē)", "English: 'Righteousness' (personal morality)"),
                        ComparisonRow("Verb Form", if (isOt) "הִצְדִּיק (hitsdiq)" else "δικαιόω (dikaioō)", "English: 'Justify' (courtroom legal acquittal)"),
                        ComparisonRow("Adjective Form", if (isOt) "צַדִּיק (tsaddiq)" else "δίκαιος (dikaios)", "English: 'Just' or 'Righteous'"),
                        ComparisonRow("Root Family", if (isOt) "One unified root: ts-d-q" else "One unified root: dikē", "Split across Germanic and Latinate English vocabularies")
                    ),
                    comparisonMarkdownTable = """
                        | Grammatical Form | Original Language Lemma | English Split Translation | Theological Distortion |
                        | --- | --- | --- | --- |
                        | Noun | δικαιοσύνη (dikaiosynē) | 'Righteousness' | Viewed as private moral character |
                        | Verb | δικαιόω (dikaioō) | 'Justify / Justification' | Viewed as cold forensic transactional loophole |
                        | Adjective | δίκαιος (dikaios) | 'Just' or 'Righteous' | Inconsistent translation severs identity |
                    """.trimIndent(),
                    wowFactor = "English Bibles split the single Greek root 'dikē' into two distinct linguistic worlds: Germanic terms ('Righteousness') and Latinate legal terms ('Justification'). This tricks modern believers into thinking Paul is discussing two separate theological doctrines, when in the Greek mind, personal righteousness and divine justification are the exact same covenant reality!"
                )
            }

            // Split-Translation / Contronym: Hagios / Kadosh (Holy vs. Cultic Prostitute / Saints)
            clean.contains("holy") || clean.contains("sanctif") || clean.contains("kadosh") || clean.contains("hagios") || orig.contains("קָדוֹשׁ") || orig.contains("ἅγιος") -> {
                if (isOt || clean.contains("kadosh")) {
                    LinguisticAnomalyInfo(
                        anomalyType = AnomalyType.CONTRONYM,
                        rootWord = "Qadash / Kadosh (קָדַשׁ / קָדוֹשׁ)",
                        strongsNumber = "H6918 & H6948",
                        primaryMeaning = "Holy, consecrated, set apart for Yahweh's radiant purity (Exod 3:5, Lev 19:2)",
                        alternateMeaning = "Qedeshah: Pagan shrine prostitute / Cultic prostitute dedicated to fertility idols (Gen 38:21, Deut 23:17)",
                        comparisonRows = listOf(
                            ComparisonRow("Sacred Root", "קָדַשׁ (qadash)", "To be set apart / Dedicated exclusively to the divine realm"),
                            ComparisonRow("Positive Aspect", "קָדוֹשׁ (kadosh)", "Holy God, holy people consecrated to Yahweh"),
                            ComparisonRow("Pagan Contronym", "קְדֵשָׁה (qedeshah)", "Female cult prostitute dedicated to Canaanite deities"),
                            ComparisonRow("Deut 23:17", "Sanctuary holiness commanded", "No daughter of Israel shall be a 'qedeshah'")
                        ),
                        comparisonMarkdownTable = """
                            | Form | Hebrew Word | Meaning | Context |
                            | --- | --- | --- | --- |
                            | Adjective | קָדוֹשׁ (kadosh) | Holy, sacred, utterly other | Yahweh's radiant transcendent majesty |
                            | Cultic Noun | קְדֵשָׁה (qedeshah) | Cult / shrine prostitute | Canaanite fertility ritual devotee |
                            | Verb (Piel) | קִדֵּשׁ (qiddesh) | To sanctify, consecrate | Dedicating life to God's purpose |
                        """.trimIndent(),
                        wowFactor = "In Biblical Hebrew, the root 'qadash' does not inherently mean 'moral purity'—it means 'set apart for a deity'. This is why Canaanite cultic shrine prostitutes were literally called 'qedeshah' (consecrated ones)! English Bibles translate this as 'prostitute', masking the staggering truth that true biblical holiness is about WHO you are dedicated to, not merely external ritualism."
                    )
                } else {
                    LinguisticAnomalyInfo(
                        anomalyType = AnomalyType.SPLIT_TRANSLATION,
                        rootWord = "Hagios / Hagiasmos (ἅγιος / ἁγιασμός)",
                        strongsNumber = "G40 & G38",
                        primaryMeaning = "Holy (Adjective: hagios) — Consecrated temple sanctuary status",
                        alternateMeaning = "Saints (Plural Noun: hagioi) & Sanctification (Noun: hagiasmos)",
                        comparisonRows = listOf(
                            ComparisonRow("Adjective", "ἅγιος (hagios)", "Translated 'Holy' (moral requirement)"),
                            ComparisonRow("Plural Noun", "ἅγιοι (hagioi)", "Translated 'Saints' (mistaken as medieval moral elites)"),
                            ComparisonRow("State Noun", "ἁγιασμός (hagiasmos)", "Translated 'Sanctification' (progressive spiritual growth)"),
                            ComparisonRow("Verb", "ἁγιάζω (hagiazo)", "Translated 'Sanctify' (cleansed for sacred use)")
                        ),
                        comparisonMarkdownTable = """
                            | Greek Form | Transliteration | English Translation | Cultural Disconnect |
                            | --- | --- | --- | --- |
                            | ἅγιος | hagios | 'Holy' | Perceived as rigid, sterile rule-following |
                            | ἅγιοι | hagioi | 'Saints' | Confused with canonized historical moral superstars |
                            | ἁγιασμός | hagiasmos | 'Sanctification' | Segregated into a separate systematic doctrine |
                        """.trimIndent(),
                        wowFactor = "English translations systematically disguise that 'holy', 'sanctified', and 'saint' are identical grammatical forms of 'hagios'. In Paul's letters, every ordinary believer is directly addressed as a 'saint' (hagios) because all share the same consecrated temple status in Christ!"
                    )
                }
            }

            // Split-Translation: Shalom (Peace vs. Full Restitution of Debt)
            clean.contains("shalom") || clean.contains("peace") || orig.contains("שָׁלוֹם") -> {
                LinguisticAnomalyInfo(
                    anomalyType = AnomalyType.SPLIT_TRANSLATION,
                    rootWord = "Shalom / Shalam (שָׁלוֹם / שָׁלַם)",
                    strongsNumber = "H7965 & H7999",
                    primaryMeaning = "Peace, harmonious relational tranquility, absence of strife",
                    alternateMeaning = "Shalam: Full economic restitution, repayment of debts, making whole what was broken",
                    comparisonRows = listOf(
                        ComparisonRow("Noun", "שָׁלוֹם (shalom)", "Wholeness, flourishing, welfare, peace"),
                        ComparisonRow("Verb (Piel)", "שִׁלֵּם (shillem)", "To pay debts, make full legal restitution (Exod 22:1)"),
                        ComparisonRow("Legal Reality", "Restitution required for peace", "Shalom cannot exist while debts remain unpaid"),
                        ComparisonRow("Messianic Title", "Sar Shalom (Isa 9:6)", "The Prince who pays the restorative debt")
                    ),
                    comparisonMarkdownTable = """
                        | Form | Hebrew Word | English Translation | Ancient Legal Dimension |
                        | --- | --- | --- | --- |
                        | Noun | שָׁלוֹם (shalom) | 'Peace' | Holistic communal thriving & health |
                        | Verb | שָׁלַם (shalam) | 'To repay / make restitution' | Restoring damaged property to total wholeness |
                    """.trimIndent(),
                    wowFactor = "Modern English treats 'peace' as a quiet emotional state or truce. But in Biblical Hebrew, 'shalom' is grammatically fused to 'shalam'—the legal repayment of debts and economic restitution. There is no true shalom in Scripture until every fracture and debt is actively made whole!"
                )
            }

            else -> null
        }
    }
}
