# Word Context 📜 — Master System Blueprint & Reconstruction Specification

> **A Definitive Technical, Architectural, and Domain Specification for Reconstructing the Word Context Historical Biblical Exegesis Application**

---

## 📑 Table of Contents
1. [Executive Product Vision & Historical Mission](#1-executive-product-vision--historical-mission)
2. [Master Architecture & File Tree](#2-master-architecture--file-tree)
3. [Build System, Gradle Configuration & Version Catalog](#3-build-system-gradle-configuration--version-catalog)
4. [AndroidManifest, Permissions & Platform Metadata](#4-androidmanifest-permissions--platform-metadata)
5. [Design System & Jetpack Compose Theming Specification](#5-design-system--jetpack-compose-theming-specification)
6. [Domain Models & Room Database Architecture](#6-domain-models--room-database-architecture)
7. [Offline Curated Knowledge Bases & Epigraphic Corpus](#7-offline-curated-knowledge-bases--epigraphic-corpus)
8. [Anti-Hallucination & Epigraphical Verification Engine](#8-anti-hallucination--epigraphical-verification-engine)
9. [Networking, Multi-Provider Architecture & AI Pipeline](#9-networking-multi-provider-architecture--ai-pipeline)
10. [Repository Layer & Offline-First Data Orchestration](#10-repository-layer--offline-first-data-orchestration)
11. [ViewModel & Reactive State Management](#11-viewmodel--reactive-state-management)
12. [UI Components & Interactive Subsystems](#12-ui-components--interactive-subsystems)
13. [Primary Screens & Navigation Architecture](#13-primary-screens--navigation-architecture)
14. [Sharing, Copying & Export System Specification](#14-sharing-copying--export-system-specification)
15. [Step-by-Step Reconstruction Manual for Autonomous AI](#15-step-by-step-reconstruction-manual-for-autonomous-ai)
16. [Verification, Testing & Developer Maintenance Contract](#16-verification-testing--developer-maintenance-contract)

---

## 1. Executive Product Vision & Historical Mission

### 1.1 The Core Problem: Modern Linguistic Drift
Every modern Bible reader reads ancient Scripture through centuries of European cultural filters, post-Enlightenment rationalism, and Victorian moral abstractions. Common theological terms have suffered profound semantic drift:
* **"Righteousness"** is misconstrued as solitary moral purity or self-righteous piety, when in the 1st century it signified covenantal faithfulness, judicial equity, and relational fidelity in civic Roman and Second Temple legal environments.
* **"Peace"** (*Eirene* / *Shalom*) is viewed as an internal emotional tranquility, contrasting sharply with the imperial *Pax Romana* (peace through imperial conquest and subjugation) and biblical *Shalom* (communal wholeness, systemic justice, and social flourishing).
* **"Faith"** (*Pistis* / *Emunah*) has been reduced to passive intellectual assent ("believing without evidence"), when in antiquity it demanded active political and covenantal allegiance, loyalty, and fidelity to a sovereign King.
* **"Grace"** (*Charis*) is frequently taught as unconditional benevolence without obligation, whereas in the Greco-Roman patron-client framework, *charis* was a gracious unearned gift that established a lifelong mutual bond of loyalty between Benefactor and Client.

### 1.2 The Solution: "Word Context"
**Word Context** is a production-grade, offline-first Android application designed to immerse scholars, pastors, students, and readers into the original **1st-century Near Eastern, Second Temple Jewish, and Greco-Roman** linguistic, cultural, and legal setting.

### 1.3 Key Architectural Capabilities
* **Offline-First Exegesis:** Comprehensive curated knowledge base of seminal New Testament Greek and Old Testament Hebrew words that functions 100% offline without network access.
* **100% Offline Strong's Concordance:** Exhaustive integrated Strong's Greek (NT) and Hebrew (OT) dictionary with search, morphology, and transliteration.
* **Patristic Witness Archive (100–450 AD):** Primary historical quotations from Apostolic Fathers, Ante-Nicene Apologists, and Nicene Church Fathers, verifying how native ancient Greek and Latin speakers interpreted these terms before medieval scholastic shifts.
* **Morphological Family Matrix:** Exposing original linguistic roots and word families that English translations splinter into unrelated words (e.g., Greek *dike*, *dikaios*, *dikaioo*, *dikaiosyne*).
* **Linguistic Anomaly Engine:** Proactively flagging Contronyms (words meaning their own opposite), Homonyms, and critical translation disconnects.
* **Anti-Hallucination Grounding Pipeline:** Dual verification system inspecting external citations, filtering out unverified blogs and forums, and enforcing a 90%+ academic and epigraphical trust threshold.
* **Comprehensive Sharing & Copying:** One-tap clipboard copy and native Android Share Sheet intents for AI responses, full dialogue transcripts, church father citations, and multi-format sermon/academic study sheets.

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
│   ├── local/
│   │   ├── AppDatabase.kt                 # Room database (v6) with TypeConverters & migrations
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
│   │   ├── LegalDisclosuresDialog.kt      # Hermeneutical methodology & anti-hallucination disclaimers
│   │   ├── LexiconScreen.kt               # Strong's Concordance browser with NT/OT tabs
│   │   ├── NotebookScreen.kt              # User's saved studies, favorites, tags & personal notes
│   │   ├── OnboardingDialog.kt            # First-launch orientation to 1st-century exegesis
│   │   ├── PaywallScreen.kt               # Developer mode & Pro subscription simulation
│   │   └── WordContextApp.kt              # App shell, Scaffold, BottomNav, TopAppBar, Ask AI dialog
│   ├── theme/
│   │   ├── Color.kt                       # Midnight Sanctum & Scholar's Desk tokens
│   │   ├── Theme.kt                       # Material 3 theme wrapper supporting manual/dynamic modes
│   │   └── Type.kt                        # Ancient serif, modern sans & monospace typography
│   ├── ChatMessage.kt                     # Chat message model with sources & verification metadata
│   ├── SearchUiState.kt                   # Sealed interface: Idle, Loading, Success, Error
│   └── WordStudyViewModel.kt              # StateFlow management, dual persistence, TTS, AI chat
└── MainActivity.kt                        # ComponentActivity, enableEdgeToEdge(), entry point
```

---

## 3. Build System, Gradle Configuration & Version Catalog

### 3.1 Version Catalog (`gradle/libs.versions.toml`)
The project utilizes Gradle's modern Version Catalog with Kotlin 2.2.10 and Compose Compiler plugin:

```toml
[versions]
agp = "9.1.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.8.7"
lifecycleViewmodelCompose = "2.8.7"
lifecycleRuntimeCompose = "2.8.7"
activityCompose = "1.10.1"
kotlin = "2.2.10"
composeBom = "2024.09.00"
googleDevtoolsKsp = "2.3.5"
navigationCompose = "2.8.9"
roomRuntime = "2.7.0"
roomKtx = "2.7.0"
roomCompiler = "2.7.0"
kotlinxCoroutinesTest = "1.10.2"
core = "1.6.1"
runner = "1.6.2"
retrofit = "2.12.0"
converterMoshi = "2.12.0"
kotlinxCoroutinesAndroid = "1.10.2"
kotlinxCoroutinesCore = "1.10.2"
loggingInterceptor = "4.10.0"
okhttp = "4.10.0"
moshiKotlin = "1.15.2"
moshiKotlinCodegen = "1.15.2"
robolectric = "4.16.1"
roborazzi = "1.59.0"
firebaseBom = "34.15.0"
secretsGradlePlugin = "2.0.1"
googleServices = "4.5.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
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
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomRuntime" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomKtx" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomCompiler" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "converterMoshi" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "loggingInterceptor" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshiKotlin" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshiKotlinCodegen" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlinxCoroutinesCore" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutinesAndroid" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
roborazzi = { group = "io.github.takahirom.roborazzi", name = "roborazzi", version.ref = "roborazzi" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
firebase-appcheck-recaptcha = { group = "com.google.firebase", name = "firebase-appcheck-recaptcha" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-devtools-ksp = { id = "com.google.devtools.ksp", version.ref = "googleDevtoolsKsp" }
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
secrets = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version.ref = "secretsGradlePlugin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

### 3.2 Root Settings (`settings.gradle.kts`)
```kotlin
pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Word Context"
include(":app")
```

### 3.3 App-Level Build Script (`app/build.gradle.kts`)
```kotlin
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

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

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  testOptions { 
    unitTests { isIncludeAndroidResources = true } 
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices {
  missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)

  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
```

---

## 4. AndroidManifest, Permissions & Platform Metadata

### 4.1 AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 4.2 Platform Metadata (`metadata.json`)
```json
{
  "name": "Word Context",
  "description": "Revealing the original first-century historical, cultural, and legal meaning of Bible words as understood by their original audience.",
  "requestFramePermissions": [],
  "majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
}
```

---

## 5. Design System & Jetpack Compose Theming Specification

### 5.1 The Two Canonical Theme Modes (`AppThemeMode`)
The application supports seamless toggling between two scholarly visual paradigms:
1. **Midnight Sanctum (`MIDNIGHT` - Default):** Dark obsidian theme optimized for low-glare study, night reading, and vivid accent contrasts.
2. **Scholar's Desk (`SCHOLAR_DESK`):** Warm parchment, leather, and iron-gall ink theme evoking ancient libraries, manuscript scriptoriums, and daylight study.

### 5.2 Color Tokens (`Color.kt`)
```kotlin
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Midnight Sanctum (Dark Theme Tokens) ---
val SanctumVoidBlack = Color(0xFF070A12)        // Root Background
val SanctumSurfaceDark = Color(0xFF0D1117)      // Card / Container Surface
val SanctumSurfaceElevated = Color(0xFF161B22)  // Dialog / Header Surface
val SanctumBorder = Color(0xFF21262D)           // Subtle Divider / Border
val SanctumBorderHighlight = Color(0xFF384353)  // Active Element Border
val CelestialCyan = Color(0xFF38BDF8)           // Primary Brand Accent
val AncientCodexGold = Color(0xFFC9A84C)        // Secondary Accent & Strong's Badges
val ImperialPurple = Color(0xFFA855F7)          // Theological Depth Accent
val TextHighEmphasisDark = Color(0xFFF0F6FC)    // Primary Text (Parchment White)
val TextMediumEmphasisDark = Color(0xFF8B949E)  // Secondary Text (Slate Grey)

// --- Scholar's Desk (Light Theme Tokens) ---
val ScholarParchmentBg = Color(0xFFFBF8F2)      // Root Manuscript Vellum
val ScholarCardSurface = Color(0xFFFFFFFF)      // Pure White Card Surface
val ScholarContainerSurface = Color(0xFFF4ECE1) // Warm Alabaster Container
val ScholarBorder = Color(0xFFE2D9CC)           // Manuscript Border
val OxfordBlue = Color(0xFF1E3A8A)              // Primary Academic Accent
val SaddleLeatherAmber = Color(0xFFB45309)      // Secondary Rich Amber Accent
val IronGallInkText = Color(0xFF1C1917)         // Primary Text (Deep Ink)
val ClassicGraphiteText = Color(0xFF64748B)     // Secondary Text (Muted Graphite)
```

### 5.3 Typography Tokens (`Type.kt`)
Features ancient serif fonts for Greek/Hebrew lemmata and scripture citations, clean sans-serif for UI navigation, and monospaced typography for Strong's numbers:
```kotlin
val AncientScriptStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    letterSpacing = 0.5.sp
)

val TransliterationStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    fontStyle = FontStyle.Italic
)

val StrongsBadgeStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp
)
```

---

## 6. Domain Models & Room Database Architecture

### 6.1 Core Room Entity: `WordStudy.kt`
```kotlin
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "word_studies")
@JsonClass(generateAdapter = true)
data class WordStudy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,                              // e.g., "Righteousness"
    val testament: String,                         // "New Testament (Greek)" or "Old Testament (Hebrew)"
    val originalWord: String,                      // Ancient script: "δικαιοσύνη" or "צְדָקָה"
    val transliteration: String,                   // Phonetic guide: "dikaiosyne"
    val strongsNumber: String,                     // "G1343", "H6666"
    val literalMeaning: String,                    // Raw etymological root
    val thenMeaning: String,                       // 1st-Century legal, cultural or covenantal reality
    val nowMeaning: String,                        // Modern Western individualistic misunderstanding
    val historicalBackground: String,              // Ancient historical, civic, or covenantal backdrop
    val culturalContext: String,                   // Near Eastern / Greco-Roman social customs
    val legalDimension: String,                    // Roman imperial law or Torah judicial framework
    val theologicalWeight: String,                 // Impact on major scriptural themes
    val keyScriptures: List<String>,               // Primary passages ["Romans 1:17", "Matthew 6:33"]
    val closingInsight: String,                    // Pastoral / scholarly synthesis
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val searchGroundingSources: List<String> = emptyList(), // Audited external citation URLs
    val rootLemma: String = "",                    // Primary linguistic root (e.g., "dike")
    val morphologicalFamilyTable: String = "",     // Markdown table of related noun/verb/adjective forms
    val translationDisconnect: String = "",        // Explicit explanation of where English fails
    val hasLinguisticAnomaly: Boolean = false,     // True if Contronym, Homonym, etc.
    val anomalyType: String = "",                  // "HOMONYM", "CONTRONYM", "SPLIT_TRANSLATION"
    val anomalyRootWord: String = "",
    val anomalyStrongsNumber: String = "",
    val anomalyPrimaryMeaning: String = "",
    val anomalyAlternateMeaning: String = "",
    val anomalyComparisonTable: String = "",
    val anomalyWowFactor: String = "",
    val userNotes: String = "",                    // Personal homiletical notes
    val userTags: List<String> = emptyList()       // Categorical tags ["#Romans", "#Sermon"]
)
```

### 6.2 Settings Persistence: `AppSetting.kt`
Used for multi-provider API keys and user preferences:
```kotlin
package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String
)

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettings(): List<AppSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: List<AppSetting>)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)

    @Query("DELETE FROM app_settings")
    suspend fun clearAllSettings()
}
```

### 6.3 Database Access: `WordStudyDao.kt`
```kotlin
package com.example.data.local

import androidx.room.*
import com.example.data.model.WordStudy
import kotlinx.coroutines.flow.Flow

@Dao
interface WordStudyDao {
    @Query("SELECT * FROM word_studies ORDER BY timestamp DESC")
    fun getAllWordStudies(): Flow<List<WordStudy>>

    @Query("SELECT * FROM word_studies WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWordStudies(): Flow<List<WordStudy>>

    @Query("SELECT * FROM word_studies WHERE LENGTH(userNotes) > 0 ORDER BY timestamp DESC")
    fun getWordStudiesWithNotes(): Flow<List<WordStudy>>

    @Query("SELECT * FROM word_studies WHERE (LOWER(word) = LOWER(:word) OR LOWER(transliteration) = LOWER(:word) OR originalWord = :word) AND LOWER(testament) = LOWER(:testament) LIMIT 1")
    suspend fun getWordStudyByWordAndTestament(word: String, testament: String): WordStudy?

    @Query("SELECT * FROM word_studies WHERE LOWER(word) = LOWER(:word) OR LOWER(transliteration) = LOWER(:word) OR originalWord = :word LIMIT 1")
    suspend fun getWordStudyAnyTestament(word: String): WordStudy?

    @Query("SELECT COUNT(*) FROM word_studies")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordStudy(wordStudy: WordStudy): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(wordStudies: List<WordStudy>): List<Long>

    @Update
    suspend fun updateWordStudy(wordStudy: WordStudy)

    @Query("UPDATE word_studies SET userNotes = :notes, userTags = :tags WHERE id = :id")
    suspend fun updateUserNotes(id: Long, notes: String, tags: List<String>)

    @Delete
    suspend fun deleteWordStudy(wordStudy: WordStudy)

    @Query("DELETE FROM word_studies WHERE id = :id")
    suspend fun deleteWordStudyById(id: Long)
}
```

### 6.4 Room Converters: `Converters.kt`
Serializes `List<String>` to/from JSON via Moshi for Room SQLite compatibility.

---

## 7. Offline Curated Knowledge Bases & Epigraphic Corpus

### 7.1 Curated Word Studies (`CuratedWordStudies.kt`)
Ships with comprehensive seed studies populated automatically on first run:
* **Greek NT:** `Righteousness` (*dikaiosyne* - G1343), `Peace` (*eirene* - G1515), `Covenant` (*diatheke* - G1242), `Holy` (*hagios* - G40), `Faith` (*pistis* - G4102), `Grace` (*charis* - G5485), `Love` (*agape* - G26), `Gospel` (*euaggelion* - G2098), `Adoption` (*huiothesia* - G5206), `Redemption` (*apolytrosis* - G629), `Church` (*ekklesia* - G1577), `Fellowship` (*koinonia* - G2842), `The Word` (*logos* - G3056).
* **Hebrew OT:** `Righteousness` (*tsedakah* - H6666), `Peace` (*shalom* - H7965), `Covenant` (*berit* - H1285), `Holy` (*kadosh* - H6918), `Faithfulness` (*emunah* - H530), `Grace/Favor` (*chen* - H2580), `Lovingkindness` (*chesed* - H2617), `Kinsman Redeemer` (*goel* - H1350), `Hear/Obey` (*shema* - H8085).

### 7.2 Strong's Concordance Engine (`StrongsConcordanceData.kt`)
Provides a standalone lexical lookup dictionary containing entries across Greek and Hebrew:
* `code`: String (e.g., "G1343", "H6666")
* `lemma`: String (Ancient script)
* `transliteration`: String
* `pronunciation`: String
* `definition`: Exhaustive concordance entry
* `derivation`: Etymological roots (e.g., "From G1342")
* `partOfSpeech`: Grammatical role
* `kjvCount`: Occurrences in King James translation

### 7.3 Early Church Fathers Corpus (`PatristicWitnessData.kt`)
Structures primary witness quotations into three eras:
1. **Apostolic Fathers (c. 70–150 AD):** Clement of Rome (*1 Clement*), Ignatius of Antioch (*Epistle to the Trallians*, *Epistle to the Romans*), Polycarp of Smyrna (*Epistle to the Philippians*), *Didache*.
2. **Ante-Nicene Apologists (c. 150–325 AD):** Justin Martyr (*First & Second Apologies*), Irenaeus of Lyons (*Adversus Haereses*), Tertullian (*Apologeticus*), Origen of Alexandria (*De Principiis*).
3. **Nicene & Post-Nicene Fathers (c. 325–450 AD):** Athanasius of Alexandria (*De Incarnatione*), John Chrysostom (*Homilies on Romans*), Augustine of Hippo (*De Spiritu et Littera*), Basil of Caesarea (*De Spiritu Sancto*).

### 7.4 Linguistic Anomaly Engine (`LinguisticAnomalyHelper.kt`)
Identifies Contronyms (words that have contradictory meanings depending on context) and Homonyms:
* **Hebrew *Barakh* (H1288):** Primarily means "to bless", but by ancient euphemistic legal usage also means "to curse" (Job 1:5, 1 Kings 21:10).
* **Hebrew *Hesed* (H2617):** Primarily means "unfailing covenant love/kindness", but can also mean "disgrace/reproach" (Leviticus 20:17, Proverbs 14:34).
* **Greek *Pharmakeia* (G5331):** Original dual meaning: medicine/healing drugs vs. sorcery/poisoning.
* **Greek *Zelos* (G2205):** Zealous devotion vs. destructive factional jealousy.

---

## 8. Anti-Hallucination & Epigraphical Verification Engine

### 8.1 The Trust Tier Taxonomy (`SourceTrustVerifier.kt`)
Every external source citation is audited and assigned a trust tier:
* **Tier 1: Primary Archaeological / Epigraphic (Score: 98–100%, Emerald `#10B981`):** Inscriptions, papyri, classical digital archives (`perseus.tufts.edu`, `papyri.info`, `epigraphy.info`, `inscriptions.packhum.org`, `britishmuseum.org`, `baslibrary.org`, `trismegistos.org`).
* **Tier 2: Academic Peer-Reviewed Press (Score: 95–98%, Sky Blue `#38BDF8`):** University presses and scholarly journals (`cambridge.org`, `oxfordbibliographies.com`, `oup.com`, `jstor.org`, `brill.com`, `degruyter.com`, `harvard.edu`, `yale.edu`, `mohrsiebeck.com`).
* **Tier 3: Scholarly Lexicons (Score: 92–95%, Gold `#C9A84C`):** Standard academic lexical tools (`stepbible.org`, `blueletterbible.org`, `ccel.org`, `sefaria.org`, `earlychristianwritings.com`).
* **Tier 4: General Educational (Score: 85–90%, Slate `#94A3B8`):** Reputable historical encyclopedias (`worldhistory.org`, `britannica.com`, `bibleodyssey.org`).
* **Tier 5: Excluded (Score: 0%, Red `#EF4444`):** Unverified blogs, forums, commercial clickbait, Reddit, Quora, Medium. Auto-filtered to ensure zero hallucination.

### 8.2 Audit Pipeline Logic
`SourceTrustVerifier.auditAndFilterSources(sources, strictFilter)`:
1. Strips any URLs matching the blacklist or commercial forum patterns.
2. Evaluates the domain against trusted registries.
3. Calculates an overall consensus trust score (averaging Tier 1–3 sources).
4. Emits `VerificationMetadata` providing the audit summary, primary sources count, and methodology explanation for user inspection.

---

## 9. Networking, Multi-Provider Architecture & AI Pipeline

### 9.1 Retrofit Client & Gemini API (`GeminiApiService.kt`)
```kotlin
interface GeminiApiService {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}
```

### 9.2 The Master Philologist System Prompt
```
You are an expert First-Century Biblical Philologist, Historian of Antiquity, and Classical Epigrapher.
Your mission is to reveal the exact first-century historical, cultural, legal, and linguistic meaning of the requested biblical term as understood by its original Near Eastern, Second Temple Jewish, or Greco-Roman audience.

CRITICAL DISCIPLINARY RULES:
1. ANTIMODERNISM: Contrast the original reality with modern Western/English misconceptions.
2. EPIGRAPHY & LAW: Cite specific Roman civic statutes, Near Eastern treaty structures, or papyrological records.
3. WORD FAMILY: Supply the complete morphological word family table (nouns, verbs, adjectives).
4. SCRIPTURAL PASSAGES: Provide key scriptures illustrating the original first-century meaning.
5. STRICT OBJECTIVITY: Avoid denominational bias; adhere to peer-reviewed historical-grammatical consensus.

OUTPUT FORMAT: Strict valid JSON conforming to the WordStudy schema.
```

### 9.3 Multi-Provider Key Support
The application supports multiple LLM providers configured in Settings:
* **Gemini** (Google AI Studio - Default, injected via `BuildConfig.GEMINI_API_KEY`)
* **Claude** (Anthropic)
* **Groq** (Fast Llama-3 inference)
* **Grok** (xAI)

---

## 10. Repository Layer & Offline-First Data Orchestration

### 10.1 `WordStudyRepository.kt` Core Mechanics
1. **Cache-First Lookup:** First queries `WordStudyDao.getWordStudyByWordAndTestament(word, testament)`. If found, returns the cached record immediately.
2. **Curated Fallback:** If not in DB, searches `CuratedWordStudies.getCuratedWordStudy(word, testament)`. If found, inserts it into SQLite and returns it.
3. **AI Generation:** If neither exists and an API key is available, calls Gemini API with the Philologist prompt. Parses response into `WordStudy`, enhances it with `PhilologyFamilyHelper` and `LinguisticAnomalyHelper`, saves to SQLite, and returns it.
4. **Offline Resilience:** If network fails or no key is provided, constructs a synthesized historical study using Strong's Concordance and lexical root algorithms.

---

## 11. ViewModel & Reactive State Management

### 11.1 Dual Persistence Strategy
All critical configuration flags and API keys are dual-persisted:
* **SharedPreferences (`word_study_prefs`):** For instantaneous synchronous retrieval upon app boot.
* **Room SQLite (`app_settings`):** For long-term durable multi-table backup across updates and device migrations.

### 11.2 StateFlow Reactive Streams
* `searchUiState`: Sealed interface (`Idle`, `Loading`, `Success(WordStudy)`, `Error(String)`).
* `viewingStudy`: Currently opened `WordStudy` displayed in the detail view.
* `history`, `favorites`, `studiesWithNotes`: Room Flows converted to `StateFlow` via `.stateIn()`.
* `themeMode`: `MIDNIGHT` vs `SCHOLAR_DESK`.
* `isProUser`: Boolean state for Pro features / developer mode.
* `chatMessages`: List of `ChatMessage` objects in the Ask AI overlay dialog.
* `useSearchGrounding`: Boolean toggle for live academic grounding.
* `strictVerification`: Boolean toggle enforcing 90%+ academic domain filter.

---

## 12. UI Components & Interactive Subsystems

### 12.1 `PatristicWitnessCard.kt`
* Expandable Material 3 Card displaying Early Church Fathers profiles.
* Epoch selector chips: **Apostolic Fathers (70–150 AD)**, **Ante-Nicene (150–325 AD)**, and **Nicene (325–450 AD)**.
* Verbatim quotation box with primary work citations (e.g., *Against Heresies, Book III.18*).
* **Copy Quote Button:** Copies formatted quote with full historical citation to clipboard.
* **Share Quote Button:** Launches Android Share Sheet with citation text.

### 12.2 `LinguisticDisconnectAlertCard.kt`
* High-visibility contextual card displayed when `study.hasLinguisticAnomaly == true`.
* Displays **Contronym / Homonym** badge, root word, and comparative tabular contrast.
* Explains where English translations cause serious theological or conceptual disconnects.

### 12.3 `ExegeticalNotesSection.kt`
* Rich note-taking component for pastors and scholars.
* Outlined text field with debounce auto-save into `WordStudy.userNotes`.
* Interactive tag pill row (e.g., `#Romans`, `#SermonPrep`, `#Thesis`).
* Add/remove custom tags stored directly in the Room database.

### 12.4 `StudyExportSheet.kt`
* Modal bottom sheet offering three export formats:
  1. **Sermon Outline:** 3-point homiletical preaching outline with pastoral application.
  2. **Academic Exegesis Paper:** Full scholarly Markdown brief with lexical apparatus and citations.
  3. **Pocket Study Card:** Compact bulleted summary for small groups and messaging apps.
* Actions: **Copy to Clipboard** and **Share via Android Share Sheet**.

---

## 13. Primary Screens & Navigation Architecture

### 13.1 `WordContextApp.kt` (Shell & Hub Screen)
* **Scaffold Layout:** Handles `WindowInsets` edge-to-edge.
* **TopAppBar:**
  * App Branding & Icon.
  * **Theme Toggle Lamp Button:** Cycles between Midnight Sanctum & Scholar's Desk.
  * **Ask AI Dialog Quick Launcher:** Opens full-screen interactive philologist chat.
  * **Pro Mode Indicator:** Toggles developer/unlocked mode.
* **Bottom Navigation Bar (4 Destinations):**
  * `0: Hub (Study Engine)` — Search bar, hero cards, curated word carousel, word study detail.
  * `1: Lexicon (Strong's)` — Comprehensive offline Strong's Concordance viewer.
  * `2: Notebook` — Saved studies, favorites, tag filter chips, user notes.
  * `3: Settings` — API keys, providers, search grounding toggles, methodology audits.

### 13.2 `LexiconScreen.kt`
* Tabbed navigation between **New Testament (Greek)** and **Old Testament (Hebrew)**.
* Live filter search bar searching by Strong's number (e.g., "G1343"), ancient lemma, or English transliteration.
* Displays part of speech, KJV occurrences, root derivation, and exhaustive definition.
* "Study in Context" button that routes the selected term directly into the Study Hub.

### 13.3 `NotebookScreen.kt`
* Dual tabs: **Favorites** and **Study Notes**.
* Dynamic tag filter bar allowing filtering by custom user tags.
* Delete, export, and quick-open actions on all saved studies.

---

## 14. Sharing, Copying & Export System Specification

### 14.1 Individual AI Response Sharing
Every AI response message rendered in the `WordStudyChatView` contains:
* **Header Quick Actions:** Accessible Copy (`Icons.Outlined.ContentCopy`) and Share (`Icons.Outlined.Share`) icon buttons.
* **Bottom Action Row:** Prominent labeled action pills:
  * `[📋 Copy Response]` — Copies response to clipboard and shows confirmation Toast.
  * `[↗ Share Response]` — Generates a formatted share payload and opens the native Android Share Sheet:

```kotlin
val shareBody = buildString {
    appendLine("📜 Biblical Philologist Response")
    if (study != null) {
        appendLine("Subject: ${study.word} (${study.originalWord} - ${study.transliteration})")
    }
    appendLine("──────────────────────────────")
    appendLine(message.text)
    if (message.isGrounded && message.sources.isNotEmpty()) {
        appendLine("\n─── Audited Academic Sources ───")
        message.sources.take(3).forEach { s ->
            appendLine("• ${s.title}")
        }
    }
    appendLine("\nShared from Word Context • Historical Exegesis Engine")
}

val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_SUBJECT, if (study != null) "Word Study: ${study.word}" else "Biblical Philology Response")
    putExtra(Intent.EXTRA_TEXT, shareBody)
    type = "text/plain"
}
context.startActivity(Intent.createChooser(sendIntent, "Share AI Response via"))
```

### 14.2 Full Dialogue Transcript Sharing
Located in the top bar of `WordStudyChatView`:
* Collates all user questions and AI philologist responses into an academic Q&A dialogue transcript.
* Includes timestamps, subject word, and academic citations.
* Dispatches standard `Intent.ACTION_SEND` chooser.

### 14.3 Patristic Quote Sharing
Located in `PatristicWitnessCard.kt`:
* Allows one-tap sharing or copying of direct quotations from the Church Fathers, including Father name, century, work cited, and theological significance.

---

## 15. Step-by-Step Reconstruction Manual for Autonomous AI

An AI system reading this blueprint can construct the entire application from scratch by following this exact 12-step sequence:

### Step 1: Configure Build & Dependency Matrix
1. Create `gradle/libs.versions.toml` with the exact versions and libraries listed in Section 3.1.
2. Create `settings.gradle.kts` setting `rootProject.name = "Word Context"` and including `:app`.
3. Create `app/build.gradle.kts` applying plugins (`android.application`, `kotlin.compose`, `google.devtools.ksp`, `secrets`, `google.services`), configuring Java 11, `buildFeatures { compose = true; buildConfig = true }`, and adding the Room, Retrofit, Moshi, and Compose dependencies.

### Step 2: Configure Android Manifest & Assets
1. Write `app/src/main/AndroidManifest.xml` with `INTERNET` permission and launcher activity.
2. In `app/src/main/res/values/strings.xml`, declare `<string name="app_name">Word Context</string>`.
3. Configure `metadata.json` with platform title and `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.

### Step 3: Implement Theme Tokens
1. Create `ui/theme/Color.kt` containing Midnight Sanctum and Scholar's Desk palettes.
2. Create `ui/theme/Type.kt` containing Ancient Serif, Sans-Serif, and Monospace styles.
3. Create `ui/theme/Theme.kt` with `WordContextTheme` composable accepting `AppThemeMode`.

### Step 4: Implement Domain Models & Room SQLite Layer
1. Create `data/model/WordStudy.kt` as an `@Entity(tableName = "word_studies")` with all 30 fields.
2. Create `data/local/AppSetting.kt` with `@Entity(tableName = "app_settings")` and `AppSettingDao`.
3. Create `data/local/Converters.kt` with Moshi TypeConverters for `List<String>`.
4. Create `data/local/WordStudyDao.kt` with all query, insert, update, and Flow observation methods.
5. Create `data/local/AppDatabase.kt` abstract Room database class (version 6, `fallbackToDestructiveMigration(true)`).

### Step 5: Implement Seed Data & Knowledge Bases
1. Create `data/local/CuratedWordStudies.kt` providing seed studies for 13 NT Greek and 9 OT Hebrew terms.
2. Create `data/local/StrongsConcordanceData.kt` implementing the offline Strong's lexicon entries.
3. Create `data/local/PatristicWitnessData.kt` containing Church Fathers quotes grouped by epoch.
4. Create `data/local/PhilologyFamilyHelper.kt` generating morphological root tables.
5. Create `data/local/LinguisticAnomalyHelper.kt` identifying contronyms, homonyms, and translation disconnects.

### Step 6: Implement Verification & AI Client
1. Create `data/verification/SourceTrustVerifier.kt` with domain registries, `TrustTier` enum, and scoring logic.
2. Create `data/api/GeminiModels.kt` defining Moshi-annotated `GenerateContentRequest` and `GenerateContentResponse`.
3. Create `data/api/GeminiApiService.kt` and `RetrofitClient` with 60-second timeouts.

### Step 7: Implement Repository Layer
1. Create `data/repository/WordStudyRepository.kt` combining cache-first DB queries, curated fallbacks, and Gemini API calls with the Philologist prompt.

### Step 8: Implement ViewModel & State
1. Create `ui/ChatMessage.kt` and `ui/SearchUiState.kt`.
2. Create `ui/WordStudyViewModel.kt` managing `SearchUiState`, `themeMode`, `chatMessages`, and dual persistence across SharedPreferences and Room.

### Step 9: Implement Reusable UI Components
1. Create `ui/components/PatristicWitnessCard.kt` with epoch tabs, quote display, and copy/share buttons.
2. Create `ui/components/LinguisticDisconnectAlertCard.kt` displaying anomaly warnings.
3. Create `ui/components/ExegeticalNotesSection.kt` with note editor and tag pills.
4. Create `ui/components/StudyExportSheet.kt` with Sermon, Paper, and Card export formats.

### Step 10: Implement Core Screens
1. Create `ui/screens/LexiconScreen.kt` for browsing Strong's Concordance.
2. Create `ui/screens/NotebookScreen.kt` for saved favorites and notes.
3. Create `ui/screens/PaywallScreen.kt`, `OnboardingDialog.kt`, and `LegalDisclosuresDialog.kt`.
4. Create `ui/screens/WordContextApp.kt` assembling the TopAppBar, BottomNav, Study Hub, and full-screen Ask AI dialog.

### Step 11: Implement Entry Point
1. Create `MainActivity.kt` with `enableEdgeToEdge()`, ViewModel initialization, and `WordContextApp` rendering inside `WordContextTheme`.

### Step 12: Test & Compile
1. Run `compile_applet` (or `gradle compileDebugSources`) to verify complete compilation.
2. Run `gradle :app:testDebugUnitTest` to execute local JVM and Robolectric tests.

---

## 16. Verification, Testing & Developer Maintenance Contract

### 16.1 Verification Commands
```bash
# 1. Compile all Kotlin sources
gradle compileDebugSources

# 2. Run local JVM & Robolectric unit tests
gradle :app:testDebugUnitTest
```

### 16.2 Autonomous Maintenance Contract
> **MANDATORY DIRECTIVE FOR ALL CONTRIBUTING AGENTS & ENGINEERS:**
> Whenever any feature, database column, screen, component, or AI prompt is introduced, altered, or refactored, **YOU MUST UPDATE THIS `README.md` FILE IMMEDIATELY**.
> This blueprint must remain perpetually in sync with the codebase so that any future engineer or autonomous AI agent can understand, maintain, or rebuild the entire application without ambiguity.
