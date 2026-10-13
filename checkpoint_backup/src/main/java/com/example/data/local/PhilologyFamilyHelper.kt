package com.example.data.local

import java.util.Locale

data class MorphologicalRow(
    val partOfSpeech: String,
    val originalTerm: String,
    val transliteration: String,
    val englishTranslation: String
)

data class PhilologicalProfile(
    val rootLemma: String,
    val originalLanguage: String,
    val morphologicalRows: List<MorphologicalRow>,
    val translationDisconnect: String
)

object PhilologyFamilyHelper {

    fun getPhilologicalProfile(
        word: String,
        originalWord: String = "",
        transliteration: String = "",
        testament: String = "New Testament",
        customRootLemma: String = "",
        customTable: String = "",
        customDisconnect: String = ""
    ): PhilologicalProfile {
        // If custom AI data is provided, parse table if possible
        if (customRootLemma.isNotBlank() && customDisconnect.isNotBlank()) {
            val parsedRows = parseMarkdownTable(customTable)
            if (parsedRows.isNotEmpty()) {
                return PhilologicalProfile(
                    rootLemma = customRootLemma,
                    originalLanguage = if (testament.contains("New", ignoreCase = true)) "Koine Greek" else "Biblical Hebrew",
                    morphologicalRows = parsedRows,
                    translationDisconnect = customDisconnect
                )
            }
        }

        val clean = word.lowercase(Locale.ROOT).trim()

        return when {
            clean.contains("right") || clean.contains("just") || clean.contains("dike") || clean.contains("tsedak") || clean.contains("tsadaq") -> {
                if (testament.contains("Old", ignoreCase = true)) {
                    PhilologicalProfile(
                        rootLemma = "צָדַק (tsadaq) — Root: To be just, vindicated, covenant-aligned",
                        originalLanguage = "Biblical Hebrew",
                        morphologicalRows = listOf(
                            MorphologicalRow("Noun (Fem)", "צְדָקָה", "tsədaqah", "Righteousness / Relational covenant justice"),
                            MorphologicalRow("Noun (Masc)", "צֶדֶק", "tsedeq", "Rightness / Legal equity / Standard of truth"),
                            MorphologicalRow("Verb (Qal)", "צָדַק", "tsadaq", "To be justified / To be proved in the right"),
                            MorphologicalRow("Verb (Hiphil)", "הִצְדִּיק", "hitsdiq", "To declare righteous / Vindicate the oppressed"),
                            MorphologicalRow("Adjective", "צַדִּיק", "tsaddiq", "Righteous / Just / Upright person")
                        ),
                        translationDisconnect = "THE TRANSLATION DISCONNECT: In English, 'Righteousness' sounds like private, individualistic moral purity, while 'Justification' sounds like a cold, forensic legal technicality. English readers treat them as two entirely separate doctrines. However, in Biblical Hebrew ('tsadaq') and Koine Greek ('dikē'), they are identical grammatical inflections of the exact same root family! To be 'justified' in Scripture does not mean escaping punishment through a detached loophole; it means being actively brought into right relational harmony with God's covenant family. The Western translation divide has severed personal righteousness from judicial vindication."
                    )
                } else {
                    PhilologicalProfile(
                        rootLemma = "δίκη (dikē) — Root: Covenant order, custom, judicial vindication",
                        originalLanguage = "Koine Greek",
                        morphologicalRows = listOf(
                            MorphologicalRow("Noun", "δικαιοσύνη", "dikaiosynē", "Righteousness / Covenant faithfulness"),
                            MorphologicalRow("Verb", "δικαιόω", "dikaioō", "Justify / Vindicate as righteous / Acquit"),
                            MorphologicalRow("Adjective", "δίκαιος", "dikaios", "Righteous / Just / Upright"),
                            MorphologicalRow("Noun (Decree)", "δικαίωμα", "dikaiōma", "Righteous decree / Legal regulation"),
                            MorphologicalRow("Adverb", "δικαίως", "dikaiōs", "Righteously / Justly in accordance with law")
                        ),
                        translationDisconnect = "THE TRANSLATION DISCONNECT: English translations systematically split the single Greek root 'dikē' into two distinct vocabularies: Latinate words ('Justice', 'Justify', 'Justification') and Germanic words ('Righteous', 'Righteousness'). This causes modern English readers to imagine that Paul is talking about two different theological worlds. In the original Greek mindset of Paul, 'dikaiosynē' and 'dikaioō' are the exact same concept: God's covenant justice putting the world to rights and vindicating His loyal people."
                    )
                }
            }

            clean.contains("holy") || clean.contains("sanctif") || clean.contains("kadosh") || clean.contains("hagios") -> {
                if (testament.contains("Old", ignoreCase = true)) {
                    PhilologicalProfile(
                        rootLemma = "קָדַשׁ (qadash) — Root: To set apart, dedicate to the divine realm",
                        originalLanguage = "Biblical Hebrew",
                        morphologicalRows = listOf(
                            MorphologicalRow("Adjective", "קָדוֹשׁ", "qadosh", "Holy / Sacred / Radiant transcendent otherness"),
                            MorphologicalRow("Noun (Sacred)", "קֹדֶשׁ", "qodesh", "Holiness / The Sanctuary / Set-apart place"),
                            MorphologicalRow("Verb (Piel)", "קִדֵּשׁ", "qiddesh", "To sanctify / Consecrate / Pronounce holy"),
                            MorphologicalRow("Noun (Action)", "קִדּוּשׁ", "kiddush", "Sanctification / Blessing of dedication")
                        ),
                        translationDisconnect = "THE TRANSLATION DISCONNECT: Western readers inherited Germanic words ('Holy') and Latinate words ('Sanctify', 'Sanctification'). This leads many believers to think 'holiness' is an intimidating list of moral rules to avoid, while 'sanctification' is a progressive theological process. In Biblical Hebrew, both come from 'qadash'—the burning, radiant presence of the Creator entering ordinary reality and dedicating people, spaces, and objects to sacred divine purpose."
                    )
                } else {
                    PhilologicalProfile(
                        rootLemma = "ἅγιος (hagios) — Root: Devoted to the gods, awe-inspiring, separated",
                        originalLanguage = "Koine Greek",
                        morphologicalRows = listOf(
                            MorphologicalRow("Adjective", "ἅγιος", "hagios", "Holy / Consecrated / Set apart for God"),
                            MorphologicalRow("Noun (Plural)", "ἅγιοι", "hagioi", "The Saints / The Consecrated Holy Ones"),
                            MorphologicalRow("Noun (State)", "ἁγιασμός", "hagiasmos", "Sanctification / Holiness of character"),
                            MorphologicalRow("Verb", "ἁγιάζω", "hagiazo", "To sanctify / Purify / Consecrate")
                        ),
                        translationDisconnect = "THE TRANSLATION DISCONNECT: English Bibles translate 'hagioi' as 'saints' (which modern readers equate with stained-glass medieval statues or moral superheroes) and 'hagios' as 'holy', while using 'sanctify' for the verb. In the New Testament, every ordinary believer is directly addressed as a 'saint' (hagios) because all are co-consecrated into God's holy temple. The translation divide turns an immediate covenant identity into an unattainable moral elite status."
                    )
                }
            }

            clean.contains("faith") || clean.contains("belie") || clean.contains("trust") || clean.contains("pistis") -> {
                PhilologicalProfile(
                    rootLemma = "πείθω (peithō) — Root: To persuade, trust, yield allegiance",
                    originalLanguage = "Koine Greek",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun", "πίστις", "pistis", "Faith / Sworn allegiance / Fidelity / Trust"),
                        MorphologicalRow("Verb", "πιστεύω", "pisteuō", "To believe / Entrust oneself / Swear allegiance"),
                        MorphologicalRow("Adjective", "πιστός", "pistos", "Faithful / Trustworthy / Loyal covenant partner"),
                        MorphologicalRow("Adverb", "πιστῶς", "pistōs", "Faithfully / With steadfast reliability")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: English grammar contains a glaring linguistic void: there is no verb form for the noun 'Faith' (we cannot say 'I faithe in Christ'). Consequently, English translators had to borrow the Germanic word 'Believe' for the verb 'pisteuō'. This created a devastating theological disconnect: modern Christians often view 'believing' as intellectual assent to doctrinal propositions ('I agree that God exists'), whereas in first-century Greco-Roman culture, 'pistis' and 'pisteuō' meant active, sworn allegiance and public fidelity to a sovereign King."
                )
            }

            clean.contains("grace") || clean.contains("favor") || clean.contains("charis") -> {
                PhilologicalProfile(
                    rootLemma = "χαίρω (chairō) — Root: To rejoice, experience benevolent gladness",
                    originalLanguage = "Koine Greek",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun", "χάρις", "charis", "Grace / Benefaction / Patronal goodwill / Beauty"),
                        MorphologicalRow("Verb", "χαρίζομαι", "charizomai", "To bestow freely / Forgive graciously / Give honor"),
                        MorphologicalRow("Noun (Gift)", "χάρισμα", "charisma", "Grace-gift / Endowment of divine favor"),
                        MorphologicalRow("Adjective", "χαρίεις", "charieis", "Gracious / Pleasing / Full of radiant favor")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: Modern Western theological thought treats 'Grace' as abstract, unearned theological benevolence, and 'Forgiveness' as clearing a guilt tally. But in first-century Roman patronage, 'charizomai' (verb) is the active granting of patronal favor that erases crippling social debt and binds the client in lifelong reciprocal loyalty to the patron. Grace was never passive license; it was a life-altering covenantal benefactor relationship."
                )
            }

            clean.contains("gospel") || clean.contains("euaggel") -> {
                PhilologicalProfile(
                    rootLemma = "ἄγγελος (aggelos) — Root: Messenger, herald of imperial news",
                    originalLanguage = "Koine Greek",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun (News)", "εὐαγγέλιον", "euangelion", "Gospel / Imperial coronation victory proclamation"),
                        MorphologicalRow("Verb", "εὐαγγελίζω", "euangelizō", "To proclaim glad tidings / Herald royal victory"),
                        MorphologicalRow("Noun (Herald)", "εὐαγγελιστής", "euangelistēs", "Evangelist / Imperial herald of the King"),
                        MorphologicalRow("Noun (Base)", "ἄγγελος", "aggelos", "Messenger / Envoy sent with official decree")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: English blends Old English 'god-spell' (good story) with Greek borrowings ('Evangelist', 'Evangelism'). Modern readers assume the 'Gospel' is a private religious formula about going to heaven after death. However, in the first-century Roman world, 'euaggelion' was the official political vocabulary used for Caesar's military victories and ascension to the imperial throne (e.g. Priene Inscription, 9 BC). Proclaiming Jesus' euaggelion was an explosive political announcement that the crucified Nazarene—not Caesar—is the true Emperor and Sovereign of the world."
                )
            }

            clean.contains("covenant") || clean.contains("berit") -> {
                PhilologicalProfile(
                    rootLemma = "בָּרָה (barah) — Root: To pledge, bind, cut sacrificial meat",
                    originalLanguage = "Biblical Hebrew",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun", "בְּרִית", "berit", "Covenant / Sacred life-and-death treaty / Binding oath"),
                        MorphologicalRow("Verb phrase", "כָּרַת בְּרִית", "karat berit", "To cut a covenant / Seal by slaughtering sacrifice"),
                        MorphologicalRow("Noun (Greek)", "διαθήκη", "diathēkē", "Covenant testament / Unilateral divine dispensation")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: Western minds confuse 'covenant' with commercial contracts. A contract is a temporary, conditional agreement founded on mutual distrust ('if you default, I walk away'). In Ancient Near Eastern Hebraic culture, a 'berit' was an irreversible, sacrificial kinship bond sealed in blood, where the parties pledged their very lives. God's covenants do not break when we falter; they are held together by His blood-sworn faithfulness."
                )
            }

            clean.contains("love") || clean.contains("agape") -> {
                PhilologicalProfile(
                    rootLemma = "ἀγαπάω (agapaō) — Root: To value, cherish with sacrificial commitment",
                    originalLanguage = "Koine Greek",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun", "ἀγάπη", "agapē", "Sacrificial covenant love / Willful devotion"),
                        MorphologicalRow("Verb", "ἀγαπάω", "agapaō", "To actively love / Cherish sacrificially"),
                        MorphologicalRow("Adjective", "ἀγαπητός", "agapētos", "Beloved / Treasured uniquely of the Father")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: English uses the single word 'Love' for pizza, family, romance, and God. This flattens the rich Greek vocabulary (agape, philia, storge, eros). 'Agape' is not an involuntary emotional feeling; it is a willful, unilateral covenant decision to act for the ultimate good of another, regardless of the cost to oneself."
                )
            }

            clean.contains("peace") || clean.contains("shalom") -> {
                PhilologicalProfile(
                    rootLemma = "שָׁלַם (shalam) — Root: To make safe, restore, pay debts, render whole",
                    originalLanguage = "Biblical Hebrew",
                    morphologicalRows = listOf(
                        MorphologicalRow("Noun", "שָׁלוֹם", "shalom", "Peace / Flourishing / Restored wholeness / Soundness"),
                        MorphologicalRow("Verb (Piel)", "שִׁלֵּם", "shillem", "To pay in full / Make restitution / Fulfill a vow"),
                        MorphologicalRow("Adjective", "שָׁלֵם", "shalem", "Whole / Undivided heart / Full stones for altar")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: In English, 'Peace' is merely the negative absence of warfare or noise ('peace and quiet'). But in Hebraic thought, 'shalom' is derived from 'shalam' (to restore what was broken, pay debts, make whole). Shalom is the active, structural presence of comprehensive well-being, economic equity, and relational harmony under God's blessing."
                )
            }

            else -> {
                // Generic derived philological profile
                val orig = originalWord.ifBlank { word }
                val trans = transliteration.ifBlank { word }
                PhilologicalProfile(
                    rootLemma = "$orig ($trans) — Philological Root Lemma",
                    originalLanguage = if (testament.contains("New", ignoreCase = true)) "Koine Greek" else "Biblical Hebrew",
                    morphologicalRows = listOf(
                        MorphologicalRow("Root Lemma", orig, trans, "Primary semantic anchor in biblical corpus"),
                        MorphologicalRow("Nominal Form", orig, trans, "Primary substantive noun usage in context"),
                        MorphologicalRow("Verbal Form", "$orig (verbal)", "$trans-verb", "Dynamic active expression of root in ancient syntax")
                    ),
                    translationDisconnect = "THE TRANSLATION DISCONNECT: Western theological terminology often isolates biblical concepts into abstract doctrinal categories. In ancient Hebrew and Greek thought, linguistic roots represent unified relational and practical realities. Tracing the word back to its root lemma restores the interconnected matrix of faith, action, and covenant fidelity."
                )
            }
        }
    }

    fun buildMarkdownTable(rows: List<MorphologicalRow>): String {
        if (rows.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append("| Part of Speech | Original Term | Transliteration | English Translation |\n")
        sb.append("| :--- | :--- | :--- | :--- |\n")
        for (row in rows) {
            sb.append("| ${row.partOfSpeech} | ${row.originalTerm} | ${row.transliteration} | ${row.englishTranslation} |\n")
        }
        return sb.toString().trimEnd()
    }

    fun parseMarkdownTable(markdown: String): List<MorphologicalRow> {
        if (markdown.isBlank()) return emptyList()
        val lines = markdown.lines().filter { it.contains("|") }
        if (lines.size < 3) return emptyList()

        val dataRows = mutableListOf<MorphologicalRow>()
        // Skip header and separator lines
        for (i in 2 until lines.size) {
            val parts = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.size >= 4) {
                dataRows.add(
                    MorphologicalRow(
                        partOfSpeech = parts[0],
                        originalTerm = parts[1],
                        transliteration = parts[2],
                        englishTranslation = parts[3]
                    )
                )
            } else if (parts.size >= 3) {
                dataRows.add(
                    MorphologicalRow(
                        partOfSpeech = parts[0],
                        originalTerm = parts[1],
                        transliteration = parts[1],
                        englishTranslation = parts[2]
                    )
                )
            }
        }
        return dataRows
    }
}
