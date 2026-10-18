# Word Context 📜 — Master System Blueprint & Reconstruction Specification

> **A Definitive Technical, Architectural, and Domain Specification for Reconstructing the Word Context Historical Biblical Exegesis & Apologetics Application**

---

## 📑 Table of Contents
1. [Executive Product Vision & Historical Mission](#1-executive-product-vision--historical-mission)
2. [Master Architecture & File Tree](#2-master-architecture--file-tree)
3. [Build System, Gradle Configuration & Version Catalog](#3-build-system-gradle-configuration--version-catalog)
4. [AndroidManifest, Permissions & Platform Metadata](#4-androidmanifest-permissions--platform-metadata)
5. [Design System & Jetpack Compose Theming Specification](#5-design-system--jetpack-compose-theming-specification)
6. [Domain Models & Room Database Architecture (v7)](#6-domain-models--room-database-architecture-v7)
7. [Offline Curated Exegesis & Concordance Knowledge Bases](#7-offline-curated-exegesis--concordance-knowledge-bases)
8. [Historical Apologetics Architecture & Defense Engine](#8-historical-apologetics-architecture--defense-engine)
9. [Anti-Hallucination & Epigraphical Verification Engine](#9-anti-hallucination--epigraphical-verification-engine)
10. [Networking, Multi-Provider Architecture & AI Pipeline](#10-networking-multi-provider-architecture--ai-pipeline)
11. [Repository Layer & Offline-First Data Orchestration](#11-repository-layer--offline-first-data-orchestration)
12. [ViewModel & Reactive State Management](#12-viewmodel--reactive-state-management)
13. [UI Components & Interactive Subsystems](#13-ui-components--interactive-subsystems)
14. [Primary Screens & 5-Tab Navigation Architecture](#14-primary-screens--5-tab-navigation-architecture)
15. [Sharing, Copying & Export System Specification](#15-sharing-copying--export-system-specification)
16. [Step-by-Step Reconstruction Manual for Autonomous AI](#16-step-by-step-reconstruction-manual-for-autonomous-ai)
17. [Verification, Testing & Developer Maintenance Contract](#17-verification-testing--developer-maintenance-contract)

---

## 1. Executive Product Vision & Historical Mission

### 1.1 The Core Problem: Modern Linguistic Drift & Skeptical Anachronism
Every modern Bible reader reads ancient Scripture through centuries of European cultural filters, post-Enlightenment rationalism, and Victorian moral abstractions. Common theological terms have suffered profound semantic drift:
* **"Righteousness"** is misconstrued as solitary moral purity or self-righteous piety, when in the 1st century it signified covenantal faithfulness, judicial equity, and relational fidelity in civic Roman and Second Temple legal environments.
* **"Peace"** (*Eirene* / *Shalom*) is viewed as an internal emotional tranquility, contrasting sharply with the imperial *Pax Romana* (peace through imperial conquest and subjugation) and biblical *Shalom* (communal wholeness, systemic justice, and social flourishing).
* **"Faith"** (*Pistis* / *Emunah*) has been reduced to passive intellectual assent ("believing without evidence"), when in antiquity it demanded active political and covenantal allegiance, loyalty, and fidelity to a sovereign King.
* **"Grace"** (*Charis*) is frequently taught as unconditional benevolence without obligation, whereas in the Greco-Roman patron-client framework, *charis* was a gracious unearned gift that established a lifelong mutual bond of loyalty between Benefactor and Client.

Furthermore, **modern internet skepticism and biblical objections** routinely stem from **historical anachronism**: imposing 21st-century Western expectations onto ancient Near Eastern, Second Temple Jewish, and Greco-Roman texts, leading to claims that passages are "contradictory," "borrowed from pagan myths," or "morally barbaric."

### 1.2 The Solution: "Word Context"
**Word Context** is a production-grade, offline-first Android application designed to immerse scholars, pastors, students, and readers into the original **1st-century Near Eastern, Second Temple Jewish, and Greco-Roman** linguistic, cultural, and legal setting, while providing an integrated, rigorously vetted **Historical Apologetics Engine** grounded in archaeology, epigraphy, textual criticism, and patristic defenses.

### 1.3 Key Architectural Capabilities
* **Offline-First Exegesis:** Comprehensive curated knowledge base of seminal New Testament Greek and Old Testament Hebrew words that functions 100% offline without network access.
* **Historical Apologetics Hub:** Curated and AI-synthesized defense dossiers answering major skeptical challenges (Christ's deity, archaeological proof of Pilate and Erastus, pagan myth refutations, textual transmission reliability, Council of Nicaea, genealogical harmony, ancient Near Eastern war rhetoric, and early patristic defenses).
* **100% Offline Strong's Concordance:** Exhaustive integrated Strong's Greek (NT) and Hebrew (OT) dictionary with search, morphology, and transliteration.
* **Patristic Witness Archive (100–450 AD):** Primary historical quotations from Apostolic Fathers, Ante-Nicene Apologists, and Nicene Church Fathers, verifying how native ancient Greek and Latin speakers interpreted these terms before medieval scholastic shifts.
* **Morphological Family Matrix:** Exposing original linguistic roots and word families that English translations splinter into unrelated words (e.g., Greek *dike*, *dikaios*, *dikaioo*, *dikaiosyne*).
* **Linguistic Anomaly Engine:** Proactively flagging Contronyms (words meaning their own opposite), Homonyms, and critical translation disconnects.
* **Anti-Hallucination Grounding Pipeline:** Dual verification system inspecting external citations, filtering out unverified blogs and forums, and enforcing a 90%+ academic and epigraphical trust threshold.
* **Comprehensive Sharing & Copying:** One-tap clipboard copy and native Android Share Sheet intents for AI responses, apologetics defense briefs, full dialogue transcripts, church father citations, and multi-format sermon/academic study sheets.

---

## 2. Master Architecture & File Tree

The project strictly follows **Clean Architecture** paired with **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**.

```
com.example/
├── data/
│   ├── api/
│   │   ├── GeminiApiService.kt            # Retrofit REST interface for Gemini API generateContent
│   │   ├── GeminiModels.kt                # Moshi request & response schemas
│   │   └── RetrofitClient.kt              # OkHttpClient (60s timeouts, logging interceptor)
│   ├── apologetics/
│   │   ├── HistoricalApologeticsModels.kt # Dossiers, Ancient Citations, Archaeology, Manuscripts, Patristics
│   │   └── CuratedApologeticsLibrary.kt   # 8 Master curated peer-reviewed defense dossiers
│   ├── local/
│   │   ├── ApologeticsDao.kt              # Room DAO for apologetics persistence, bookmarks, and notes
│   │   ├── AppDatabase.kt                 # Room database (v7) with TypeConverters & migrations
│   │   ├── AppSetting.kt                  # AppSetting Room Entity & AppSettingDao
│   │   ├── Converters.kt                  # Moshi-backed Room TypeConverters for List<String>
│   │   ├── CuratedWordStudies.kt          # Exhaustive offline seed word studies (NT Greek & OT Hebrew)
│   │   ├── LinguisticAnomalyHelper.kt     # Contronym & Homonym catalog and comparison tables
│   │   ├── PatristicWitnessData.kt        # Early Church Fathers corpus (Apostolic, Ante-Nicene, Nicene)
│   │   ├── PhilologyFamilyHelper.kt       # Morphological root matrix & word family generator
│   │   ├── StrongsConcordanceData.kt      # Offline Strong's Concordance database (Greek & Hebrew)
│   │   └── WordStudyDao.kt                # Room DAO queries, Flow observables & transactions
│   ├── model/
│   │   ├── StrongsEntry.kt                # Concordance lexical model
│   │   └── WordStudy.kt                   # Core Room Entity & Moshi JSON data class
│   ├── repository/
│   │   ├── HistoricalApologeticsRepository.kt # Apologetics persistence & vetted AI defense generator
│   │   └── WordStudyRepository.kt         # Single source of truth, cache-first coordination, AI prompts
│   └── verification/
│       └── SourceTrustVerifier.kt         # Domain trust scoring, tier classification & web filters
├── ui/
│   ├── components/
│   │   ├── ExegeticalNotesSection.kt      # Homiletical note-taking with interactive tag pills
│   │   ├── LinguisticDisconnectAlertCard.kt # Homonym/Contronym visual warning banner
│   │   ├── PatristicWitnessCard.kt        # Church Fathers quotes, epoch chips, copy/share actions
│   │   └── StudyExportSheet.kt            # 3-format export generator (Sermon, Paper, Quick Card)
│   ├── screens/
│   │   ├── ApologeticsDetailSheet.kt      # Full modal sheet for apologetic briefs, archaeology & notes
│   │   ├── ApologeticsScreen.kt           # Apologetics Hub with category filters, search, and cards
│   │   ├── AskApologeticsDialog.kt        # Interactive modal for asking AI any skeptical objection
│   │   ├── LegalDisclosuresDialog.kt      # Hermeneutical methodology & anti-hallucination disclaimers
│   │   ├── LexiconScreen.kt               # Strong's Concordance browser with NT/OT tabs
│   │   ├── NotebookScreen.kt              # User's saved studies, favorites, tags & personal notes
│   │   ├── OnboardingDialog.kt            # First-launch orientation to 1st-century exegesis
│   │   ├── PaywallScreen.kt               # Developer mode & Pro subscription simulation
│   │   └── WordContextApp.kt              # App shell, Scaffold, 5-Tab Navigation, TopAppBar, Ask AI dialog
│   ├── theme/
│   │   ├── Color.kt                       # Midnight Sanctum & Scholar's Desk tokens
│   │   ├── Theme.kt                       # Material 3 theme wrapper supporting manual/dynamic modes
│   │   └── Type.kt                        # Ancient serif, modern sans & monospace typography
│   ├── ChatMessage.kt                     # Chat message model with sources & verification metadata
│   ├── SearchUiState.kt                   # Sealed interface: Idle, Loading, Success, Error
│   └── WordStudyViewModel.kt              # StateFlow management, dual persistence, TTS, AI chat & apologetics
└── MainActivity.kt                        # ComponentActivity, enableEdgeToEdge(), entry point
```

---

## 3. Build System, Gradle Configuration & Version Catalog

### 3.1 Version Catalog (`gradle/libs.versions.toml`)
```toml
[versions]
agp = "9.1.1"
kotlin = "2.2.10"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
composeBom = "2026.03.00"
room = "2.8.4"
ksp = "2.2.10-2.0.2"
moshi = "1.15.2"
retrofit = "2.13.0"
okhttp = "4.12.0"
coroutines = "1.10.2"
roborazzi = "1.46.1"
robolectric = "4.16.1"
secretsPlugin = "2.0.1"
googleServices = "4.4.4"
firebaseBom = "34.12.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }
converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
roborazzi = { group = "io.github.takahirom.roborazzi", name = "roborazzi", version.ref = "roborazzi" }
roborazzi-compose = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose", version.ref = "roborazzi" }
roborazzi-junit-rule = { group = "io.github.takahirom.roborazzi", name = "roborazzi-junit-rule", version.ref = "roborazzi" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
androidx-core = { group = "androidx.test", name = "core", version = "1.7.0" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
firebase-appcheck-recaptcha = { group = "com.google.firebase", name = "firebase-appcheck-recaptcha" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-devtools-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
secrets = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version.ref = "secretsPlugin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

### 3.2 Root Settings (`settings.gradle.kts`)
```kotlin
rootProject.name = "Word Context"
include(":app")
```

### 3.3 App-Level Build Script (`app/build.gradle.kts`)
```kotlin
android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.wordcontext.pkyvqr"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}
```

---

## 4. AndroidManifest, Permissions & Platform Metadata

### 4.1 AndroidManifest (`app/src/main/AndroidManifest.xml`)
The manifest declares edge-to-edge support, theme assignments, and necessary permissions:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Network permissions for Gemini, Claude, Groq & Google Search Grounding -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WordContext">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:theme="@style/Theme.WordContext">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 4.2 Application Strings & AI Studio Metadata
* **`app/src/main/res/values/strings.xml`**:
  ```xml
  <resources>
      <string name="app_name">Word Context</string>
  </resources>
  ```
* **`metadata.json`**:
  ```json
  {
    "name": "Word Context",
    "description": "Historical, Second Temple Jewish, and Greco-Roman biblical exegesis engine & apologetics defense toolkit.",
    "requestFramePermissions": [],
    "majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
  }
  ```

---

## 5. Design System & Jetpack Compose Theming Specification

### 5.1 Design Philosophy: "Scholar's Desk" & "Midnight Sanctum"
The visual identity steers deliberately clear of sterile, generic SaaS styles. It channels the tactile atmosphere of a rare manuscript reading room, classical codex study, and archaeological archive:
* **Parchment, Vellum & Warm Terracotta:** Warm cream and soft sepia backgrounds in Light Mode (`Scholar's Desk`).
* **Midnight Papyrus & Obsidian Gold:** Deep obsidian, aged bronze, and warm amber gold in Dark Mode (`Midnight Sanctum`).
* **Linguistic Color Coding:**
  * Greek NT Lexical Tokens: Classical Aegean Blue / Cobalt (`#1976D2` / `#64B5F6`).
  * Hebrew OT Lexical Tokens: Ancient Temple Crimson / Ochre (`#C2185B` / `#F06292`).
  * Archaeological Inscription Accents: Patina Emerald / Bronze (`#2E7D32` / `#81C784`).
  * Linguistic Anomaly / Contronym Alerts: Amber Ochre Alert (`#FF8F00` / `#FFA000`).

### 5.2 Color Tokens (`ui/theme/Color.kt`)
```kotlin
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Palette: "Scholar's Desk"
val ParchmentLight = Color(0xFFF9F7F2)
val SurfaceParchment = Color(0xFFFFFFFF)
val PrimaryBronze = Color(0xFF5D4037)
val SecondaryOchre = Color(0xFF8D6E63)
val TertiaryLapis = Color(0xFF1E3A8A)
val AncientGold = Color(0xFFD4AF37)
val TextCharcoal = Color(0xFF1F1F1F)
val TextMutedBrown = Color(0xFF5C554E)

// Dark Palette: "Midnight Sanctum"
val ObsidianDark = Color(0xFF121214)
val SurfaceObsidian = Color(0xFF1E1E22)
val PrimaryGold = Color(0xFFDFC07A)
val SecondarySand = Color(0xFFB0A89F)
val TertiaryCyan = Color(0xFF90CAF9)
val TextIvory = Color(0xFFF0EFEB)
val TextMutedGray = Color(0xFFA6A29A)
```

### 5.3 Typography (`ui/theme/Type.kt`)
Typography uses serif fonts for Scripture headers, Greek/Hebrew lemmata, and Church Father quotations, paired with crisp sans-serif for UI chrome and monospace for morphological Strong's codes:
* `displayMedium`: 28.sp Serif, Bold (Scripture & Word Headers).
* `titleLarge`: 22.sp Serif, Medium (Dossier Titles & Exegetical Theorems).
* `bodyLarge`: 16.sp Sans, Normal (Commentaries, Defenses, Primary Source Translations).
* `labelSmall`: 11.sp Monospace (Strong's references, morphology tags, manuscript sigla).

---

## 6. Domain Models & Room Database Architecture (v7)

### 6.1 Core Room Entities
The database (`AppDatabase`) operates at schema **version 7** with `fallbackToDestructiveMigration(true)`:

1. **`WordStudy` (`word_studies`)**:
   Contains all 30 exegetical and morphological fields, including `rootLemma`, `morphologicalFamilyTable`, `hasLinguisticAnomaly`, and `userNotes`.

2. **`AppSetting` (`app_settings`)**:
   Key-value store for API keys (`api_key_Gemini`, `api_key_Claude`, `api_key_Groq`, `api_key_Grok`) and user preference toggles (`strict_source_verification`, `use_search_grounding`).

3. **`ApologeticsEntity` (`apologetics_entries`)**:
   Persists saved and user-generated apologetics dossiers:
   ```kotlin
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
   ```

### 6.2 Apologetics DAO: `ApologeticsDao.kt`
```kotlin
@Dao
interface ApologeticsDao {
    @Query("SELECT * FROM apologetics_entries ORDER BY timestamp DESC")
    fun getAllDossiers(): Flow<List<ApologeticsEntity>>

    @Query("SELECT * FROM apologetics_entries WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedDossiers(): Flow<List<ApologeticsEntity>>

    @Query("SELECT * FROM apologetics_entries WHERE id = :id LIMIT 1")
    suspend fun getDossierById(id: String): ApologeticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ApologeticsEntity): Long

    @Query("UPDATE apologetics_entries SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE apologetics_entries SET userPersonalNotes = :notes WHERE id = :id")
    suspend fun updatePersonalNotes(id: String, notes: String)

    @Query("DELETE FROM apologetics_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
```

---

## 7. Offline Curated Exegesis & Concordance Knowledge Bases

To guarantee zero dependency on cloud connectivity for core theological study, Word Context bundles an extensive, scholarly compiled offline knowledge repository.

### 7.1 Curated Exegetical Corpus (`CuratedWordStudies.kt`)
The seed repository features in-depth exegetical entries for seminal biblical terms across both testaments:
* **`Agape` (G26 / ἀγάπη):** Refutes romantic/sentimental reductions; demonstrates its 1st-century Greco-Roman reality as covenantal self-giving action and allegiance, independent of reciprocal benefit.
* **`Shalom` (H7965 / שָׁלוֹם):** Contrasts modern internal serenity with ancient Semitic *shalam* (wholeness, restitution, cosmic relational harmony, and restorative justice).
* **`Pistis` (G4102 / πίστις):** Refutes passive mental assent ("blind faith"); establishes *pistis* as the Greco-Roman term for sworn civic loyalty, political allegiance, and covenantal trustworthiness to the Emperor/Kyrios.
* **`Charis` (G5485 / χάρις):** Reconstructs the ancient Greco-Roman Patron-Client dynamic, where *charis* is the unmerited royal favor of the Benefactor (*patronus*) calling forth lifelong client loyalty and gratitude.
* **`Sarx` (G4561 / σάρξ):** Clarifies Paul's dual use between physical biological creatureliness and the fallen, idolatrous human socio-religious power structure.
* **`Chesed` (H2617 / חֶסֶד):** Unpacks ancient Near Eastern covenantal devotion, stubborn loyalty, and steadfast treaty obligation.
* **`Logos` (G3056 / λόγος):** Bridges Heraclitus/Stoic cosmic rationality with the Jewish *Memra* (Targumic divine agency and dynamic creative Word of Yahweh).

### 7.2 Offline Strong's Concordance (`StrongsConcordanceData.kt`)
* Bundles comprehensive lexical records for New Testament Greek (`G1`–`G5624`) and Old Testament Hebrew (`H1`–`H8674`).
* Supports prefix filtering (`G` vs `H`), transliteration queries, English gloss matching, and morphological root exploration.

### 7.3 Patristic Witness Archive (`PatristicWitnessData.kt`)
* Categorized into three historical epochs:
  1. **Apostolic Fathers (c. 95–150 AD):** Clement of Rome, Ignatius of Antioch, Polycarp, *Didache*, Hermas.
  2. **Ante-Nicene Apologists (c. 150–325 AD):** Justin Martyr, Irenaeus of Lyons, Tertullian, Clement of Alexandria, Origen, Cyprian.
  3. **Nicene & Post-Nicene Fathers (c. 325–450 AD):** Athanasius of Alexandria, Basil of Caesarea, Gregory of Nazianzus, John Chrysostom, Jerome, Augustine of Hippo.
* Every entry contains the Latin/Greek work title, chapter/section, date of composition, and verbatim translated quote showing original patristic linguistic usage.

### 7.4 Philological Family Matrix & Linguistic Anomaly Helper
* **`PhilologyFamilyHelper.kt`:** Re-unifies English Bible translations by building morphological root tables (e.g., showing how *Righteousness*, *Justification*, *Justice*, and *Righteous* all derive from Greek *DIK-*).
* **`LinguisticAnomalyHelper.kt`:** Automatically inspects words for contronymic shifts (e.g., Hebrew *Barak* meaning both "to bless" and euphemistically "to curse"), homonyms, and translation traps.

---

## 8. Historical Apologetics Architecture & Defense Engine

### 8.1 The Apologetics Philosophy: Antimodernism & Anti-Anachronism
Most modern skepticism against Scripture commits the fallacy of **presentism**: demanding that ancient Near Eastern, Jewish, or Greco-Roman texts adhere to 21st-century Western literary, scientific, or cultural idioms.
The **Historical Apologetics Engine** refutes skepticism by reconstructing the authentic historical horizon:
1. **Primary Epigraphical Priority:** Physical inscriptions (Pilate Stone, Erastus Pavement, Mesha Stele, Merneptah Stele) take precedence over 19th-century speculative literary hypotheses.
2. **Ancient Historiographical Standards:** Distinguishing ancient hyperbolic war rhetoric (standard Near Eastern idioms of victory) from modern genocide.
3. **Manuscript Superiority:** Demonstrating that the New Testament manuscript tradition (over 5,800 Greek copies, earliest fragments within 30-40 years) exceeds all classical secular Greco-Roman literature (Plato, Caesar, Tacitus) by orders of magnitude.
4. **Patristic Dialogue Continuity:** Mobilizing the earliest Church Fathers (Justin Martyr, Irenaeus, Tertullian, Origen) who answered the exact same philosophical objections from pagan philosophers (Celsus, Porphyry) in the 2nd and 3rd centuries.

### 8.2 Apologetics Domain Models (`HistoricalApologeticsModels.kt`)
* `ApologeticsCategory`: Enum covering `HISTORICAL_JESUS`, `ARCHAEOLOGY_ARTIFACTS`, `MANUSCRIPT_RELIABILITY`, `ALLEGED_CONTRADICTIONS`, `PAGAN_MYTH_REFUTATION`, `CANON_AND_COUNCILS`, `MORAL_OLD_TESTAMENT`, `PATRISTIC_APOLOGISTS`, `THEODICY_AND_SUFFERING`, `JUDGMENT_AND_HELL`.
* `AncientHistoricalCitation`: Author, Source Work, Date, Translated Excerpt, and Scholarly Context (e.g., Tacitus *Annals* 15.44, Josephus *Antiquities* 18.3.3, Pliny *Epistles* 10.96).
* `ArchaeologicalDiscovery`: Artifact Name, Discovery Location/Year, Repository Museum, Inscription Summary, and Academic Significance.
* `ManuscriptEvidenceItem`: Identifier (P52, P46, P66, Codex Sinaiticus), Approximate Date, Biblical Passage, Repository, and Comparative Advantage vs Secular Classics.
* `PatristicApologeticQuote`: Church Father, Era, Treatise Cited, Argument against Pagan Critic, and Excerpt.
* `ScholarCitation`: Scholar Name, University/Institution, Published Work, and Verified Domain.
* `ApologeticsDossier`: Master model assembling the full defense brief with Quick Debate Bullets.

### 8.3 The 10 Master Curated Dossiers (`CuratedApologeticsLibrary.kt`)
1. **Did Jesus Ever Claim to Be God?** (`apo_jesus_divinity`):
   - Refuting claims of late evolution or invention at Nicaea.
   - Core Evidence: Mark 14:61–64 (Daniel 7:13 clouds + Psalm 110:1 divine throne); Caiaphas tearing his robes for blasphemy; Philippians 2:5–11 pre-Pauline hymn (c. 38–40 AD); Pliny the Younger (112 AD); Alexamenos Graffito (c. 150–200 AD).
2. **Archaeological Confirmations: Pilate, Erastus & Caiaphas** (`apo_archaeology_pilate_erastus`):
   - Refuting claims that Gospel figures are fictitious romance characters.
   - Core Evidence: The 1961 Caesarea Pilate Stone (*Praefectus Iudaeae*); 1929 Corinth Erastus Pavement (*Aedilis* = *Oikonomos* in Rom 16:23); 1990 Jerusalem Caiaphas Family Ossuary (*Yehosef bar Qayafa*).
3. **The 'Copycat Jesus' Fallacy: Mithras, Horus & Mystery Cults** (`apo_pagan_myth_refutation`):
   - Dismantling *Zeitgeist* internet claims of pagan plagiarism.
   - Core Evidence: Primary Persian/Roman texts proving Mithras was born from a rock (*petrogenes*), never died, had no disciples; Egyptian Pyramid Texts proving Horus was conceived post-mortem by Isis with Osiris's reconstructed corpse (no virgin birth, no crucifixion); late 1st-century arrival of Roman Mithraism.
4. **Textual Integrity: The Unrivaled New Testament Manuscript Record** (`apo_manuscript_reliability`):
   - Refuting the "telephone game" myth.
   - Core Evidence: Over 5,800 Greek manuscripts; P52 (c. 125 AD, 30-year gap) vs 900+ year gap for Caesar and Plato; 99% of variants being spelling/word order; Dead Sea Scrolls 1QIsa confirming 1,000-year Old Testament copying stability.
5. **The Council of Nicaea: Debunking Dan Brown & Constantine Myths** (`apo_council_nicaea_canon`):
   - Did Constantine choose the Bible or invent Jesus' deity in 325 AD?
   - Core Evidence: Official council canons (Arian controversy, Easter date); 316 to 2 vote (99.4% landslide); Muratorian Fragment (c. 170 AD) and Irenaeus (c. 180 AD) confirming the fourfold Gospel 150 years prior to Nicaea.
6. **The Genealogies of Jesus: Levirate Law & Maternal Succession** (`apo_genealogies_matthew_luke`):
   - Resolving Matthew 1 vs Luke 3.
   - Core Evidence: Matthew traces the legal royal line through Solomon to Joseph; Luke traces the biological line through Nathan to Mary (Jerusalem Talmud *Chagigah* 77b identifies "Miriam daughter of Heli"); Mosaic inheritance law for daughters (Numbers 27); avoiding the blood-curse on Jeconiah (Jeremiah 22:30).
7. **Canaanite Conquest & Ancient Near Eastern War Rhetoric** (`apo_canaanite_warfare_ethics`):
   - Did God command genocide?
   - Core Evidence: Standard Near Eastern military hyperbole ("destroyed all that breathed" on Mesha Stele 840 BC and Merneptah Stele 1208 BC); Scripture proving survival (Joshua 15 and Judges 1 show Canaanites living in the same cities); Deuteronomy 7:3 forbidding intermarriage (proving coexistence expected); judgment for 400 years of child sacrifice to Moloch.
8. **Early Church vs Pagan Critics: Origen Refutes Celsus** (`apo_celsus_origen_defense`):
   - Did early Christianity avoid intellectual scrutiny?
   - Core Evidence: Celsus's *The True Word* (177 AD) raising modern objections; Origen's 8-volume *Contra Celsum* (248 AD); the psychology of apostolic martyrdom; Christians rescuing discarded Roman infants from trash heaps and transforming Roman society.
9. **The Problem of Evil & Suffering: Why Do Bad Things Happen to Good People?** (`apo_problem_of_evil_suffering`):
   - Resolving the dilemma between divine goodness and human agony.
   - Core Evidence: Alvin Plantinga's Free Will Defense proving moral agency requires the possibility of evil; Irenaeus's soul-making theodicy (adversity trains virtue); evil as privation of good (*privatio boni* in Augustine); biblical reality of a fractured creation (Romans 8); the Cross demonstrating God entering human agony; Giv'at ha-Mivtar crucifixion archaeology.
10. **The Nature of Hell & Judgment: Does an All-Loving God Send People to Hell?** (`apo_hell_divine_justice_love`):
    - Deconstructing medieval Dante folklore to reveal biblical Gehenna, moral autonomy, and holiness.
    - Core Evidence: 1st-century context of *Ge-Hinnom* as emblem of tragedy and wasted life; C.S. Lewis's principle that the doors of hell are locked from the inside ("Thy will be done"); Romans 1:24 judicial "giving over"; Miroslav Volf on divine wrath as holy love opposing injustice; Ketef Hinnom silver scrolls (c. 600 BC); Papyrus 72 (2 Pet 3:9, God not wishing that any perish).

### 8.4 Anti-Hallucination AI Defense Generator (`HistoricalApologeticsRepository.kt`)
When users ask an apologetics question or enter a skeptical objection:
1. **Low-Temperature Prompting (`temperature = 0.2`):** Directs the model into strict academic and historical mode.
2. **Strict Negative Constraints:** Explicitly forbids the hallucination of non-existent ancient inscriptions, fake church father treatises, or fictitious papyri.
3. **Structured JSON Output:** Returns clean schema matching `ApologeticsDossier` (title, shortSummary, category, skepticalClaim, historicalThesis, detailedDefense, keyScriptures, quickDebateBullets).
4. **Keyword-Ranked Semantic Curated Fallback:** If offline or without API keys, matches against `CuratedApologeticsLibrary` using semantic keywords and exact phrase weighting, guaranteeing reliable, vetted responses even without cloud connectivity.

---

## 9. Anti-Hallucination & Epigraphical Verification Engine

### 9.1 The Need for Rigorous Epigraphical Verification
General-purpose LLMs frequently invent citations, misattribute ancient texts, or quote popular apologetics myths that lack scholarly backing. Word Context implements a strict **Source Verification Engine** (`SourceTrustVerifier.kt`) to ensure that every historical citation meets rigorous academic standards.

### 9.2 Trust Tiers & Whitelisting Algorithm
Every web source, archaeological report, or journal citation is classified into one of four trust tiers based on academic pedigree:
```kotlin
enum class TrustTier(val label: String, val minScore: Int) {
    PEER_REVIEWED_ACADEMIC("Peer-Reviewed Academic", 95),
    PRIMARY_SOURCE_ARCHIVE("Primary Source Archive", 90),
    SCHOLARLY_REFERENCE("Scholarly Reference", 80),
    GENERAL_WEB_UNVERIFIED("Unverified Web Source", 0)
}
```
* **Tier 1 (95–100%):** `.edu` university domains, Cambridge Core, Oxford Academic (`oup.com`), Brill (`brill.com`), JSTOR (`jstor.org`), Harvard University Press, Society of Biblical Literature (`sbl-site.org`).
* **Tier 2 (90–95%):** Primary text repositories: Perseus Digital Library (`tufts.edu`), Packard Humanities Institute (`phi.org`), Center for the Study of New Testament Manuscripts (`csntm.org`), Dead Sea Scrolls Digital Library, Papyrus Portal (`papyri.info`).
* **Tier 3 (80–89%):** Standard scholarly encyclopedias: *Encyclopaedia Britannica*, BibleOdyssey, Jewish Virtual Library.
* **Tier 4 (<80%):** Unmoderated forums, personal apologetics blogs, and commercial content farms. In "Strict Verification Mode", Tier 4 sources are flagged with warning badges or filtered out.

---

## 10. Networking, Multi-Provider Architecture & AI Pipeline

### 10.1 Multi-Provider Flexibility
Users are not locked into a single AI backend. The Settings tab allows configuring and swapping between four leading LLM providers:
1. **Google Gemini (`gemini-3.8-flash` / `gemini-3.1-pro-preview` / `gemini-flash-latest`):** High-speed, high-depth scholarly reasoning with native Google Search Grounding support.
2. **Anthropic Claude (`claude-3-5-sonnet`):** Premium academic exegetical and patristic reasoning.
3. **Groq (`llama-3.3-70b-versatile`):** Ultra-low latency open-weights inference (sub-500ms responses).
4. **xAI Grok (`grok-2`):** Broad contextual understanding and alternative perspective synthesis.

### 10.2 Retrofit & OkHttp Architecture
* `RetrofitClient.kt`: Configures an `OkHttpClient` with 60-second connect/read/write timeouts, HTTP logging interceptor, and `MoshiConverterFactory`.
* `GeminiApiService.kt`: Defines the REST endpoint for Gemini:
  ```kotlin
  @POST("v1beta/models/{model}:generateContent")
  suspend fun generateContent(
      @Path("model") model: String,
      @Query("key") apiKey: String,
      @Body request: GenerateContentRequest
  ): Response<GenerateContentResponse>
  ```

### 10.3 Live Google Search Grounding
To verify recent archaeological digs (e.g., Mount Ebal curse tablet or Jerusalem excavations), Word Context activates Gemini's native search grounding tool:
```json
{
  "tools": [
    { "googleSearch": {} }
  ]
}
```
The response includes `groundingMetadata` containing search queries and `groundingChunks` with live web URIs that are automatically parsed and graded by `SourceTrustVerifier`.

---

## 11. Repository Layer & Offline-First Data Orchestration

### 11.1 Cache-First Exegesis Pipeline (`WordStudyRepository.kt`)
When a user searches for a biblical word or passage:
1. **Local Room Check:** Queries `word_studies` table. If cached, emits immediately via Kotlin `Flow`.
2. **Curated Seed Check:** If not in Room, searches `CuratedWordStudies.kt`. If found, saves to Room and emits.
3. **AI Exegetical Synthesis:** If entirely new, constructs a 30-field strict JSON prompt directing the active model to act as a 1st-century Near Eastern epigrapher.
4. **Moshi Serialization & Room Caching:** Parses the JSON response, validates fields, saves to `AppDatabase`, and emits `SearchUiState.Success`.

### 11.2 Apologetics Orchestration (`HistoricalApologeticsRepository.kt`)
* Coordinates between `ApologeticsDao` and AI generation.
* Low-temperature prompting (`temperature = 0.2`) ensures factual stability.
* Emits reactive `Flow<List<ApologeticsEntity>>` for automatic UI updates when bookmarks or notes change.

---

## 12. ViewModel & Reactive State Management

`WordStudyViewModel.kt` serves as the central state coordinator across all five app tabs:

### 12.1 Key StateFlows
* `searchUiState: StateFlow<SearchUiState>`: Drives the Study Hub UI (`Idle`, `Loading`, `Success`, `Error`).
* `currentWordStudy: StateFlow<WordStudy?>`: The actively inspected exegetical study.
* `savedWordStudies: StateFlow<List<WordStudy>>`: All user-favorited studies.
* `savedApologetics: StateFlow<List<ApologeticsEntity>>`: All persisted apologetics dossiers.
* `bookmarkedApologetics: StateFlow<List<ApologeticsEntity>>`: Favorited apologetic briefs.
* `apologeticsSearchQuery: StateFlow<String>`: Real-time query filter in the Apologetics tab.
* `selectedApologeticsCategory: StateFlow<ApologeticsCategory?>`: Active category chip filter.
* `activeProvider / geminiKey / claudeKey / groqKey / grokKey`: Live provider credentials.

### 12.2 Native Koine Greek & Biblical Hebrew TTS
Integrated with `android.speech.tts.TextToSpeech`. Correctly sets `Locale.GREEK` or phonetic fallbacks to pronounce ancient lemmata aloud, enhancing scholarly pronunciation.

---

## 13. UI Components & Interactive Subsystems

### 13.1 `PatristicWitnessCard.kt`
Renders quotes from Early Church Fathers with an epoch indicator chip (Apostolic / Ante-Nicene / Nicene), ancient source title, and one-tap copy/share buttons.

### 13.2 `LinguisticDisconnectAlertCard.kt`
Highlights when an English translation obscures a biblical Contronym or false friend, preventing exegetical fallacies.

### 13.3 `ExegeticalNotesSection.kt`
In-app rich notebook enabling scholars and pastors to write sermon notes, attach category tags, and auto-persist to Room.

### 13.4 `StudyExportSheet.kt`
Bottom sheet offering three structured export formats:
1. **Homiletical Sermon Outline:** Hook, Historical Context, Word Study, Exegesis, Modern Application.
2. **Academic Research Paper Excerpt:** Formal citations, Greek/Hebrew lemmata, morphology tables.
3. **Quick Study Flashcard:** Definition, 1st-century context, core takeaway.

---

## 14. Primary Screens & 5-Tab Navigation Architecture

The app shell in `WordContextApp.kt` implements a **5-Tab Navigation Bar**:
1. **Tab 0: Study Hub (`Icons.Outlined.Search`, "Study Hub"):** Word study search, historical exegesis breakdown, interactive linguistic anomaly banner, morphological matrix, and Google Search Grounded Ask AI.
2. **Tab 1: Apologetics (`Icons.Outlined.Shield`, "Apologetics"):**
   - Category filter chips (All, Historical Jesus, Archaeology, Manuscripts, Harmonies, Pagan Myths, Canon, Ethics, Patristics).
   - Real-time search across titles, objections, and archaeological discoveries.
   - Sub-filters: "All Defense Briefs" vs "Saved & Bookmarked".
   - "Ask AI" button launching `AskApologeticsDialog.kt`.
   - Rich cards with skeptical claim quote banners, thesis snippets, and evidence tags.
   - Full-screen `ApologeticsDetailSheet.kt` with personal defense notes editor.
3. **Tab 2: Lexicon (`Icons.Outlined.MenuBook`, "Lexicon"):** Offline Strong's Concordance browser with NT Greek and OT Hebrew tabs.
4. **Tab 3: Notebook (`Icons.Outlined.EditNote`, "Notebook"):** Saved exegetical studies, bookmarks, history, and custom tags.
5. **Tab 4: Settings (`Icons.Outlined.Settings`, "Settings"):** Multi-provider API keys (Gemini, Claude, Groq, Grok), Scholar's Desk vs Midnight Sanctum theme switcher, search grounding toggle, and strict verification options.

---

## 15. Sharing, Copying & Export System Specification

### 15.1 Apologetics Defense Brief Export
`buildFullApologeticsExportText(dossier: ApologeticsDossier)` generates a comprehensive text brief containing:
* Header banner
* Title & Category
* Skeptical Claim / Popular Critique
* Core Historical Thesis & Rebuttal
* Exhaustive Historical & Exegetical Breakdown
* Key Scriptures
* Archaeological & Epigraphical Confirmations (Artifact, Location, Inscription, Significance)
* Primary Ancient Historical Citations (Tacitus, Josephus, Pliny, etc.)
* Quick Debate Takeaways (Bullet points)
* Exported via clipboard copy or Android `Intent.ACTION_SEND` chooser for study groups, campus evangelism, or debates.

---

## 16. Step-by-Step Reconstruction Manual for Autonomous AI

An AI system reading this blueprint can construct the entire application from scratch by following this exact 13-step sequence:

1. **Step 1: Configure Build & Dependency Matrix** (`gradle/libs.versions.toml`, `settings.gradle.kts`, `app/build.gradle.kts`).
2. **Step 2: Configure Android Manifest & App Name** (`AndroidManifest.xml`, `strings.xml`, `metadata.json`).
3. **Step 3: Implement Theme Tokens** (`ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`).
4. **Step 4: Implement Domain Models & Room DB Layer (v7)**:
   - `WordStudy.kt`, `AppSetting.kt`, `HistoricalApologeticsModels.kt`.
   - `Converters.kt`, `WordStudyDao.kt`, `ApologeticsDao.kt`, `AppDatabase.kt`.
5. **Step 5: Implement Seed Knowledge Bases**:
   - `CuratedWordStudies.kt`, `StrongsConcordanceData.kt`, `PatristicWitnessData.kt`.
   - `PhilologyFamilyHelper.kt`, `LinguisticAnomalyHelper.kt`.
   - `CuratedApologeticsLibrary.kt` (10 master dossiers: Divinity, Nicaea, Pagan Myths, Pilate Stone, Eyewitness Gospels, Resurrection Minimal Facts, Canaanite Warfare, Patristic Critics, Problem of Evil & Suffering, and Hell & Divine Justice).
6. **Step 6: Implement Verification & AI Client** (`SourceTrustVerifier.kt`, `GeminiApiService.kt`, `GeminiModels.kt`).
7. **Step 7: Implement Repositories**:
   - `WordStudyRepository.kt` (cache-first exegesis & chat).
   - `HistoricalApologeticsRepository.kt` (apologetics persistence & low-temp vetted defense generation).
8. **Step 8: Implement ViewModel & Reactive State** (`WordStudyViewModel.kt` managing 5-tab flows).
9. **Step 9: Implement UI Components**:
   - `PatristicWitnessCard.kt`, `LinguisticDisconnectAlertCard.kt`, `ExegeticalNotesSection.kt`, `StudyExportSheet.kt`.
10. **Step 10: Implement Core Screens**:
    - `LexiconScreen.kt`, `NotebookScreen.kt`, `PaywallScreen.kt`, `OnboardingDialog.kt`, `LegalDisclosuresDialog.kt`.
    - `ApologeticsScreen.kt`, `ApologeticsDetailSheet.kt`, `AskApologeticsDialog.kt`.
11. **Step 11: Implement App Shell & 5-Tab Navigation** (`WordContextApp.kt`).
12. **Step 12: Implement Entry Point** (`MainActivity.kt`).
13. **Step 13: Test & Compile**:
    - `compile_applet` (or `gradle compileDebugSources`).
    - `gradle :app:testDebugUnitTest`.

### 16.2 Cross-Platform Progressive Web App (PWA) Architectural Mapping

For an AI agent or engineer tasked with building **Word Context** as a **Progressive Web App (PWA)**, the architecture, domain models, algorithms, and prompts translate directly into modern web primitives:

| Android Architecture Component | PWA / Web Stack Equivalent | Implementation Blueprint |
| :--- | :--- | :--- |
| **UI Framework** | **React 19 / Vite + TypeScript** or **Next.js** | Single Page Application with client-side tab routing. |
| **Design System & Styling** | **Tailwind CSS + Lucide React** | Use CSS variables or Tailwind classes matching Section 5 tokens (`#080C14`, `#131D2E`, `#38BDF8`, `#FBF8F2`, `#FAF7F0`). |
| **Local Database (Room v7)** | **IndexedDB via `Dexie.js`** | Client-side persistent relational storage for word studies, apologetics dossiers, and user settings. |
| **Seed Knowledge Bases** | **Bundled JSON / Static TypeScript Objects** | Bundle `curated_studies.json`, `strongs_concordance.json`, `patristic_witnesses.json`, and `curated_apologetics.json` for 100% offline zero-network execution. |
| **Offline Engine & Installability** | **Service Worker (`sw.js` / Workbox) + `manifest.webmanifest`** | Cache-first caching strategy for app shell, fonts, and seed databases; installable to home screen / desktop. |
| **Pronunciation / Audio** | **Web Speech API (`speechSynthesis`)** | `window.speechSynthesis.speak(new SpeechSynthesisUtterance(text))` with Greek (`el-GR`) / Hebrew (`he-IL`) language fallback. |
| **Sharing & Clipboard** | **Web Share API & Clipboard API** | `navigator.share({ title, text })` with fallback to `navigator.clipboard.writeText(text)`. |
| **AI Networking & Generation** | **Standard `fetch()` or `@google/genai`** | Direct calls to `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=...` using exact JSON schemas in Section 10. |
| **State Management** | **Zustand** or **React Context / hooks** | Unidirectional data store replicating `WordStudyViewModel` (search state, apologetics filter state, theme toggle, chat history). |

#### 16.2.1 IndexedDB Schema (Dexie.js TypeScript Definition)
```typescript
import Dexie, { type Table } from 'dexie';

export interface WordStudy {
  id?: number;
  word: string;
  testament: 'New Testament' | 'Old Testament';
  originalWord: string;
  transliteration: string;
  strongsNumber: string;
  literalMeaning: string;
  historicalContext: string;
  culturalNuance: string;
  theologicalSignificance: string;
  modernApplication: string;
  scriptureReferences: string[];
  isFavorite: boolean;
  userNotes: string;
  userTags: string[];
  isUserGenerated: boolean;
  timestamp: number;
}

export interface ApologeticsEntity {
  id: string;
  title: string;
  shortSummary: string;
  categoryName: string;
  skepticalClaim: string;
  historicalThesis: string;
  detailedDefense: string;
  isBookmarked: boolean;
  userPersonalNotes: string;
  isUserGenerated: boolean;
  timestamp: number;
}

export interface AppSetting {
  key: string;
  value: string;
}

export class WordContextDB extends Dexie {
  wordStudies!: Table<WordStudy, number>;
  apologetics!: Table<ApologeticsEntity, string>;
  settings!: Table<AppSetting, string>;

  constructor() {
    super('WordContextDB');
    this.version(1).stores({
      wordStudies: '++id, word, originalWord, strongsNumber, testament, isFavorite, timestamp',
      apologetics: 'id, title, categoryName, isBookmarked, isUserGenerated, timestamp',
      settings: 'key'
    });
  }
}

export const db = new WordContextDB();
```

#### 16.2.2 PWA Web Manifest (`public/manifest.webmanifest`)
```json
{
  "name": "Word Context — Historical Bible Exegesis & Apologetics",
  "short_name": "WordContext",
  "description": "Greek and Hebrew word context unfolding original 1st-century cultural nuance and peer-reviewed historical apologetics.",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#080C14",
  "theme_color": "#080C14",
  "icons": [
    { "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icon-512.png", "sizes": "512x512", "type": "image/png" }
  ]
}
```

#### 16.2.3 Prompt to Instruct Another AI to Build the PWA
To instruct any autonomous coding AI (e.g. Claude Code, Cursor, Copilot, or another agent) to build the complete PWA from this README, pass this prompt:
> *"Read the technical specification in `README.md`. Build a complete, production-grade Progressive Web App (PWA) using Vite + React 19 + TypeScript + Tailwind CSS + Lucide Icons + Dexie.js (IndexedDB). Implement the exact 5-tab layout (Study Hub, Apologetics, Lexicon, Notebook, Settings), both Midnight Sanctum and Scholar's Desk themes, the offline seed knowledge bases (including all 10 curated apologetics dossiers and Strong's Concordance), the anti-hallucination domain trust scoring engine, and the Gemini API client with the exact JSON generation prompts in Section 10. Ensure the app is fully installable with service worker offline caching."*

---

## 17. Verification, Testing & Developer Maintenance Contract

### 17.1 Verification Commands
```bash
# 1. Compile all Kotlin sources
gradle compileDebugSources

# 2. Run local JVM & Robolectric unit tests
gradle :app:testDebugUnitTest
```

### 17.2 Autonomous Maintenance Contract
> **MANDATORY DIRECTIVE FOR ALL CONTRIBUTING AGENTS & ENGINEERS:**
> Whenever any feature, database column, screen, component, or AI prompt is introduced, altered, or refactored, **YOU MUST UPDATE THIS `README.md` FILE IMMEDIATELY**.
> This blueprint must remain perpetually in sync with the codebase so that any future engineer or autonomous AI agent can understand, maintain, or rebuild the entire application without ambiguity.
