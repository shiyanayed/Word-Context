package com.example.data.apologetics

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Categorization of historical and biblical apologetic defense domains.
 */
enum class ApologeticsCategory(
    val displayName: String,
    val shortSubtitle: String,
    val iconName: String
) {
    HISTORICAL_JESUS(
        displayName = "Historical Jesus & Divinity",
        shortSubtitle = "Self-understanding, claims of deity, trial, and the resurrection",
        iconName = "cross"
    ),
    ARCHAEOLOGY_ARTIFACTS(
        displayName = "Biblical Archaeology",
        shortSubtitle = "Inscriptions, stones, and excavations confirming biblical events & persons",
        iconName = "temple"
    ),
    MANUSCRIPT_RELIABILITY(
        displayName = "Manuscript Reliability",
        shortSubtitle = "Papyri, codices, textual transmission vs classical Greco-Roman texts",
        iconName = "scroll"
    ),
    ALLEGED_CONTRADICTIONS(
        displayName = "Harmonization & Discrepancies",
        shortSubtitle = "Resolving apparent textual anomalies via ancient idioms and customs",
        iconName = "balance"
    ),
    PAGAN_MYTH_REFUTATION(
        displayName = "Pagan Myth Refutations",
        shortSubtitle = "Dismantling claims of borrowed pagan mystery cults (Mithras, Osiris)",
        iconName = "shield_broken"
    ),
    CANON_AND_COUNCILS(
        displayName = "Canon & Councils",
        shortSubtitle = "Debunking myths regarding Constantine, Nicaea, and canon formation",
        iconName = "church"
    ),
    MORAL_OLD_TESTAMENT(
        displayName = "Ancient Near Eastern Ethics",
        shortSubtitle = "War rhetoric, servitude, and ancient law vs modern anachronisms",
        iconName = "scale"
    ),
    PATRISTIC_APOLOGISTS(
        displayName = "Patristic Defenses",
        shortSubtitle = "Early Church Fathers refuting Roman skeptics (Celsus, Porphyry, Lucian)",
        iconName = "book"
    ),
    THEODICY_AND_SUFFERING(
        displayName = "Problem of Evil & Suffering",
        shortSubtitle = "Free will, soul-making, cosmic brokenness, and the cross",
        iconName = "heart_broken"
    ),
    JUDGMENT_AND_HELL(
        displayName = "Hell, Justice & Divine Love",
        shortSubtitle = "Biblical Gehenna, human moral autonomy, and divine holiness",
        iconName = "flame"
    )
}

/**
 * Primary ancient textual citation (Jewish, Roman, Greek, or Christian).
 */
@JsonClass(generateAdapter = true)
data class AncientHistoricalCitation(
    val author: String, // e.g., "Cornelius Tacitus", "Flavius Josephus", "Pliny the Younger"
    val sourceWork: String, // e.g., "Annals 15.44", "Antiquities of the Jews 18.3.3"
    val dateOrEra: String, // e.g., "c. 116 AD", "c. 93–94 AD"
    val quotation: String, // The historical quote in translation
    val contextAndImportance: String // Scholarly significance
)

/**
 * Physical archaeological discovery confirming a biblical datum.
 */
@JsonClass(generateAdapter = true)
data class ArchaeologicalDiscovery(
    val artifactName: String, // e.g., "The Pilate Stone", "The Erastus Inscription"
    val discoveryLocationAndYear: String, // e.g., "Caesarea Maritima, 1961 (Dr. Antonio Frova)"
    val currentMuseumLocation: String, // e.g., "Israel Museum, Jerusalem"
    val inscriptionSummary: String, // The Latin/Greek/Hebrew translation
    val academicSignificance: String // Why this refutes skeptical claims
)

/**
 * Textual manuscript data demonstrating historical preservation.
 */
@JsonClass(generateAdapter = true)
data class ManuscriptEvidenceItem(
    val identifier: String, // e.g., "Papyrus 52 (P52)", "Codex Vaticanus"
    val approximateDate: String, // e.g., "c. 117–138 AD (Hadrianic Period)"
    val biblicalPassage: String, // e.g., "John 18:31–33, 37–38"
    val repository: String, // e.g., "John Rylands Library, Manchester, UK"
    val comparativeAdvantage: String // e.g., "Separated by only 30-40 years from original autograph vs 1000 years for Tacitus"
)

/**
 * Early Church Father apologetic defense.
 */
@JsonClass(generateAdapter = true)
data class PatristicApologeticQuote(
    val fatherName: String, // e.g., "Origen of Alexandria", "Justin Martyr"
    val era: String, // e.g., "c. 185–254 AD"
    val treatiseCited: String, // e.g., "Contra Celsum (Against Celsus), Book II.55"
    val argumentAgainstCritic: String, // How they refuted pagan critics of their time
    val quotationExcerpt: String
)

/**
 * Academic citation verifying historical data against peer-reviewed consensus.
 */
@JsonClass(generateAdapter = true)
data class ScholarCitation(
    val scholarName: String, // e.g., "Dr. F.F. Bruce", "Dr. Richard Bauckham"
    val universityOrInstitution: String, // e.g., "University of Manchester", "University of St Andrews"
    val publishedWork: String, // e.g., "Jesus and the Eyewitnesses (Eerdmans, 2006)"
    val verifiedAcademicDomain: String // e.g., "st-andrews.ac.uk"
)

/**
 * Full master apologetic dossier resolving a major historical/skeptical challenge.
 */
@JsonClass(generateAdapter = true)
data class ApologeticsDossier(
    val id: String,
    val title: String,
    val shortSummary: String,
    val category: ApologeticsCategory,
    val skepticalClaim: String, // The modern objection or misconception
    val historicalThesis: String, // Core scholarly answer
    val detailedDefense: String, // Comprehensive multi-paragraph academic defense
    val keyScriptureReferences: List<String>,
    val ancientHistoricalTexts: List<AncientHistoricalCitation> = emptyList(),
    val archaeologicalEvidence: List<ArchaeologicalDiscovery> = emptyList(),
    val manuscriptEvidence: List<ManuscriptEvidenceItem> = emptyList(),
    val patristicDefenses: List<PatristicApologeticQuote> = emptyList(),
    val peerReviewedScholarship: List<ScholarCitation> = emptyList(),
    val quickDebateBullets: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val userPersonalNotes: String = "",
    val isUserGenerated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Room database entity for saving custom or bookmarked apologetic dossiers and user notes.
 */
@Entity(tableName = "apologetics_entries")
data class ApologeticsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val shortSummary: String,
    val categoryName: String,
    val skepticalClaim: String,
    val historicalThesis: String,
    val detailedDefense: String,
    val keyScripturesJson: String,
    val quickDebateBulletsJson: String,
    val ancientTextsJson: String,
    val archaeologyJson: String,
    val manuscriptsJson: String,
    val patristicsJson: String,
    val scholarshipJson: String,
    val isBookmarked: Boolean = false,
    val userPersonalNotes: String = "",
    val isUserGenerated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
