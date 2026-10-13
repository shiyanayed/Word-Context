package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WordStudy

enum class ExportFormat(val title: String, val subtitle: String) {
    SERMON_OUTLINE("Sermon Outline", "Homiletical brief with 3 points & application"),
    ACADEMIC_EXEGESIS("Academic Exegesis", "Markdown paper with lexical apparatus & citations"),
    QUICK_CARD("Quick Card", "Concise summary for handouts & messaging")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyExportSheet(
    study: WordStudy,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.SERMON_OUTLINE) }

    val formattedContent = remember(study, selectedFormat) {
        when (selectedFormat) {
            ExportFormat.SERMON_OUTLINE -> generateSermonOutline(study)
            ExportFormat.ACADEMIC_EXEGESIS -> generateAcademicMarkdown(study)
            ExportFormat.QUICK_CARD -> generateQuickSummary(study)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF334155))
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export Biblical Study",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Prepared for \"${study.word.capitalizeWords()}\" (${study.originalWord})",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Format Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.values().forEach { format ->
                    val isSelected = selectedFormat == format
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedFormat = format }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = format.title,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preview Card
            Text(
                text = "PREVIEW (${selectedFormat.subtitle})",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B13))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = formattedContent,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy to Clipboard
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(formattedContent))
                        Toast.makeText(context, "Copied ${selectedFormat.title} to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_copy_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF38BDF8)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Text", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Share Sheet Intent
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, "Word Study: ${study.word.capitalizeWords()} (${study.originalWord})")
                            putExtra(Intent.EXTRA_TEXT, formattedContent)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Word Study via")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_share_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Study", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

private fun generateSermonOutline(study: WordStudy): String {
    val capitalized = study.word.capitalizeWords()
    val scriptures = if (study.keyScriptures.isNotEmpty()) study.keyScriptures.joinToString(", ") else "Scripture Text"

    val noteSection = if (study.userNotes.isNotBlank()) {
        "\nPASTOR'S PERSONAL REFLECTIONS:\n${study.userNotes}\n"
    } else ""

    return """
============================================================
SERMON PREPARATION BRIEF & HOMILETICAL OUTLINE
============================================================
TOPIC: The Ancient Reality of "$capitalized"
SCRIPTURE BASES: $scriptures
ORIGINAL TERM: ${study.originalWord} (${study.transliteration}) | Strong's ${study.strongsNumber}
TESTAMENT FOCUS: ${study.testament}
------------------------------------------------------------

I. THE CENTRAL PROPOSITION
Modern Western culture views "$capitalized" as:
"${study.nowMeaning}"

However, to the original ancient biblical audience, it meant:
"${study.thenMeaning}"

II. THE HISTORICAL & LEGAL BACKDROP
- Historical Setting: ${study.historicalBackground}
- Cultural Framework: ${study.culturalContext}
- Legal / Treaty Dimension: ${study.legalDimension}

III. THREE HOMILETICAL SERMON POINTS
1. RECOVER THE COVENANT ROOTS
   - In its original grammar, "$capitalized" (${study.literalMeaning}) established binding loyalty rather than mere emotional feeling.
   - Cross Reference: ${study.keyScriptures.firstOrNull() ?: "Primary Text"}

2. CONFRONT MODERN MISCONCEPTIONS
   - Why shifting from the first-century meaning weakens our theological understanding.
   - Contrast: What was once legal/covenantal is now reduced to private sentiment.

3. EMBODY THE FIRST-CENTURY CALL
   - The ancient audience did not just contemplate this word—they lived under its legal protection and civic obligation.
   - Practical Application: How the church today lives as an ancient ekklesia/covenant community.
${run {
    val patristic = com.example.data.local.PatristicWitnessData.getVettedHistoricalContext(study.word, study.testament)
    val witness = patristic.primaryWitnesses.firstOrNull()
    if (witness != null) {
        "\nEARLY CHURCH WITNESS (Historical Illustration):\n\"${witness.directQuotation}\"\n— ${witness.fatherName} (${witness.eraAndCentury})\nWork: ${witness.primaryWorkCited}\nSignificance: ${witness.theologicalSignificance}\n"
    } else ""
}}
$noteSection
IV. CLOSING CONVICTION & BENEDICTION
"${study.closingInsight}"

Prepared with Word Context: Historical & Philological Bible Study
============================================================
""".trimIndent()
}

private fun generateAcademicMarkdown(study: WordStudy): String {
    val capitalized = study.word.capitalizeWords()
    val citations = if (study.searchGroundingSources.isNotEmpty()) {
        study.searchGroundingSources.joinToString("\n") { "- $it" }
    } else {
        "- Standard Lexical Authority: BDAG / HALOT / Thayer's Greek-English Lexicon\n- Epigraphical & Historical Corpus: Corpus Inscriptionum Latinarum"
    }

    val anomalyMarkdown = if (study.hasLinguisticAnomaly && study.anomalyPrimaryMeaning.isNotBlank()) {
        """
### Linguistic Anomaly Analysis (${study.anomalyType})
- **Primary Sense**: ${study.anomalyPrimaryMeaning}
- **Hidden / Split Sense**: ${study.anomalyAlternateMeaning}
- **Philological Insight**: ${study.anomalyWowFactor}
"""
    } else ""

    val userNotesMarkdown = if (study.userNotes.isNotBlank()) {
        """
### Scholar's Personal Exegetical Notes
${study.userNotes}

**Tags**: ${study.userTags.joinToString(" ") { "#$it" }}
"""
    } else ""

    return """
# Exegetical Research Paper: $capitalized (${study.originalWord})

**Date**: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(study.timestamp))}  
**Testament Scope**: ${study.testament}  
**Strongs Concordance**: ${study.strongsNumber}  
**Transliteration**: *${study.transliteration}*  
**Root Lemma**: ${study.rootLemma.ifBlank { study.transliteration }}  

---

## 1. Lexical Definition & Morphology
- **Literal Gloss**: ${study.literalMeaning}
- **First-Century Definition**: ${study.thenMeaning}
- **Modern Semantic Shift**: ${study.nowMeaning}

${if (study.morphologicalFamilyTable.isNotBlank()) "### Morphological Family\n" + study.morphologicalFamilyTable else ""}

$anomalyMarkdown

## 2. Socio-Historical & Legal Environment
### Historical Matrix
${study.historicalBackground}

### Cultural Norms & Honor-Shame Dynamics
${study.culturalContext}

### Legal Framework (Roman Law / ANE Treaties)
${study.legalDimension}

## 3. Theological Significance
${study.theologicalWeight}

### Canonical Biblical Attestation
${study.keyScriptures.joinToString("\n") { "- $it" }}

$userNotesMarkdown

## 4. Patristic Reception & Early Church Witness (100–450 AD)
${run {
    val patristic = com.example.data.local.PatristicWitnessData.getVettedHistoricalContext(study.word, study.testament)
    val witnesses = patristic.primaryWitnesses.joinToString("\n\n") { w ->
        """
> "${w.directQuotation}"  
> — **${w.fatherName}** (${w.eraAndCentury})  
> *${w.primaryWorkCited}*  
> **Theological Significance**: ${w.theologicalSignificance}
        """.trimIndent()
    }
    witnesses
}}

## 5. Synthesis & Hermeneutical Horizon
> "${study.closingInsight}"

## 6. Primary Epigraphic & Scholarly Apparatus
$citations

---
*Generated via Word Context Academic Engine*
""".trimIndent()
}

private fun generateQuickSummary(study: WordStudy): String {
    val capitalized = study.word.capitalizeWords()
    return """
📖 BIBLE WORD CONTEXT: $capitalized (${study.originalWord})
Strong's: ${study.strongsNumber} | Testament: ${study.testament}
Literal Meaning: ${study.literalMeaning}

🔍 THEN (Original 1st-Century Meaning):
${study.thenMeaning}

⚠️ NOW (Modern Western Misunderstanding):
${study.nowMeaning}

🏛️ LEGAL & CULTURAL REALITY:
${study.legalDimension}

💡 KEY INSIGHT:
"${study.closingInsight}"

Key Verses: ${study.keyScriptures.joinToString(", ")}
""".trimIndent()
}
