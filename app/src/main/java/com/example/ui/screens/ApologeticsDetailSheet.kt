package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.apologetics.ApologeticsDossier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApologeticsDetailSheet(
    dossier: ApologeticsDossier,
    onDismiss: () -> Unit,
    onToggleBookmark: (ApologeticsDossier) -> Unit,
    onSaveNotes: (id: String, notes: String) -> Unit,
    isScholarDesk: Boolean = false
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEditingNotes by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(dossier.userPersonalNotes) }

    val bgSurface = if (isScholarDesk) Color(0xFFFAF7F0) else Color(0xFF0F172A)
    val textPrimary = if (isScholarDesk) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textSecondary = if (isScholarDesk) Color(0xFF64748B) else Color(0xFF94A3B8)
    val cardBg = if (isScholarDesk) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val cardBorder = if (isScholarDesk) Color(0xFFE2D9C8) else Color(0xFF334155)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgSurface,
        dragHandle = null,
        modifier = Modifier.testTag("apologetics_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Top Bar with Dismiss and Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("apologetics_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark toggle
                    IconButton(
                        onClick = { onToggleBookmark(dossier) },
                        modifier = Modifier.testTag("apologetics_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (dossier.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark Dossier",
                            tint = if (dossier.isBookmarked) Color(0xFFF59E0B) else textSecondary
                        )
                    }

                    // Copy Full Defense
                    IconButton(
                        onClick = {
                            val fullText = buildFullApologeticsExportText(dossier)
                            clipboardManager.setText(AnnotatedString(fullText))
                            Toast.makeText(context, "Copied Defense Brief to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("apologetics_copy_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Full Defense",
                            tint = textSecondary
                        )
                    }

                    // Share Native Intent
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Historical Apologetics: ${dossier.title}")
                                putExtra(Intent.EXTRA_TEXT, buildFullApologeticsExportText(dossier))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Apologetics Brief"))
                        },
                        modifier = Modifier.testTag("apologetics_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share Defense",
                            tint = textSecondary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Category Chip & Verified Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = dossier.category.displayName.uppercase(),
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Peer-Reviewed Consensus",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Historical & Archaeological Vetted",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = dossier.title,
                    color = textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Short Summary
                com.example.ui.components.FormattedMarkdownText(
                    text = dossier.shortSummary,
                    color = textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. SKEPTICAL OBJECTION CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skeptical_claim_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.FormatQuote,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SKEPTICAL CLAIM / POPULAR CRITIQUE",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dossier.skepticalClaim,
                            color = if (isScholarDesk) Color(0xFF7F1D1D) else Color(0xFFFECACA),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. HISTORICAL THESIS CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_thesis_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CORE HISTORICAL THESIS & REBUTTAL",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.ui.components.FormattedMarkdownText(
                            text = dossier.historicalThesis,
                            color = if (isScholarDesk) Color(0xFF065F46) else Color(0xFFD1FAE5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. DETAILED EXHAUSTIVE DEFENSE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXHAUSTIVE HISTORICAL & EXEGETICAL BREAKDOWN",
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        com.example.ui.components.FormattedMarkdownText(
                            text = dossier.detailedDefense,
                            color = textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        if (dossier.keyScriptureReferences.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Key Scriptural References:",
                                color = textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dossier.keyScriptureReferences.forEach { ref ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = ref,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. ARCHAEOLOGICAL EVIDENCE (If Available)
                if (dossier.archaeologicalEvidence.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("archaeological_evidence_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ARCHAEOLOGICAL & EPIGRAPHICAL CONFIRMATIONS",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            dossier.archaeologicalEvidence.forEachIndexed { idx, artifact ->
                                if (idx > 0) {
                                    Divider(
                                        color = cardBorder,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                                Text(
                                    text = artifact.artifactName,
                                    color = textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 Excavation: ${artifact.discoveryLocationAndYear}",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "🏛️ Current Repository: ${artifact.currentMuseumLocation}",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.08f))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Inscription / Summary: \"${artifact.inscriptionSummary}\"",
                                        color = if (isScholarDesk) Color(0xFF92400E) else Color(0xFFFDE68A),
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = artifact.academicSignificance,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }

                // 5. ANCIENT HISTORICAL TEXTS & CITATIONS (If Available)
                if (dossier.ancientHistoricalTexts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PRIMARY ANCIENT HISTORICAL SOURCES",
                                    color = Color(0xFFA855F7),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            dossier.ancientHistoricalTexts.forEachIndexed { idx, citation ->
                                if (idx > 0) {
                                    Divider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                                }
                                Text(
                                    text = "${citation.author} — ${citation.sourceWork} (${citation.dateOrEra})",
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFA855F7).copy(alpha = 0.08f))
                                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "\"${citation.quotation}\"",
                                        color = if (isScholarDesk) Color(0xFF581C87) else Color(0xFFE9D5FF),
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = citation.contextAndImportance,
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // 6. QUICK DEBATE & CONVERSATION BULLET POINTS
                if (dossier.quickDebateBullets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_bullets_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Psychology,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "QUICK DEBATE TAKEAWAYS",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Text(
                                    text = "Tap to Copy Bullets",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable {
                                        val bulletsText = dossier.quickDebateBullets.joinToString("\n• ", prefix = "• ")
                                        clipboardManager.setText(AnnotatedString(bulletsText))
                                        Toast.makeText(context, "Copied debate bullet points!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            dossier.quickDebateBullets.forEach { bullet ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = bullet,
                                        color = textPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. USER PERSONAL DEFENSE NOTES
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PERSONAL DEFENSE NOTES",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            if (!isEditingNotes) {
                                Text(
                                    text = "Edit Notes",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { isEditingNotes = true }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingNotes) {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                placeholder = { Text("Add personal thoughts, debate points, or group prep notes...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apologetics_notes_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = cardBorder
                                ),
                                minLines = 3
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { isEditingNotes = false },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        onSaveNotes(dossier.id, notesText)
                                        isEditingNotes = false
                                        Toast.makeText(context, "Notes saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Notes")
                                }
                            }
                        } else {
                            if (notesText.isNotBlank()) {
                                Text(
                                    text = notesText,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            } else {
                                Text(
                                    text = "No notes added yet. Tap 'Edit Notes' to prepare your personal defense strategy for conversations or study groups.",
                                    color = textSecondary.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons Row: Copy and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val fullText = buildFullApologeticsExportText(dossier)
                            clipboardManager.setText(AnnotatedString(fullText))
                            Toast.makeText(context, "Copied full defense brief!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_copy_defense_btn"),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Brief", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Historical Apologetics: ${dossier.title}")
                                putExtra(Intent.EXTRA_TEXT, buildFullApologeticsExportText(dossier))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Apologetics Brief"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_share_defense_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Defense", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun buildFullApologeticsExportText(dossier: ApologeticsDossier): String {
    val sb = StringBuilder()
    sb.appendLine("══════════════════════════════════════════")
    sb.appendLine("📜 HISTORICAL APOLOGETICS DEFENSE BRIEF")
    sb.appendLine("══════════════════════════════════════════\n")
    sb.appendLine("TITLE: ${dossier.title}")
    sb.appendLine("CATEGORY: ${dossier.category.displayName}\n")
    sb.appendLine("⚡ SKEPTICAL CLAIM / CRITIQUE:")
    sb.appendLine("${dossier.skepticalClaim}\n")
    sb.appendLine("🛡️ CORE HISTORICAL THESIS:")
    sb.appendLine("${dossier.historicalThesis}\n")
    sb.appendLine("📖 EXHAUSTIVE HISTORICAL BREAKDOWN:")
    sb.appendLine(dossier.detailedDefense)
    sb.appendLine()

    if (dossier.keyScriptureReferences.isNotEmpty()) {
        sb.appendLine("📌 KEY SCRIPTURES: ${dossier.keyScriptureReferences.joinToString(", ")}\n")
    }

    if (dossier.archaeologicalEvidence.isNotEmpty()) {
        sb.appendLine("🏛️ ARCHAEOLOGICAL CONFIRMATIONS:")
        dossier.archaeologicalEvidence.forEach { art ->
            sb.appendLine("• ${art.artifactName} (${art.discoveryLocationAndYear})")
            sb.appendLine("  Inscription: \"${art.inscriptionSummary}\"")
            sb.appendLine("  Significance: ${art.academicSignificance}")
        }
        sb.appendLine()
    }

    if (dossier.ancientHistoricalTexts.isNotEmpty()) {
        sb.appendLine("📜 ANCIENT PRIMARY SOURCES:")
        dossier.ancientHistoricalTexts.forEach { cit ->
            sb.appendLine("• ${cit.author} — ${cit.sourceWork} (${cit.dateOrEra})")
            sb.appendLine("  Quote: \"${cit.quotation}\"")
        }
        sb.appendLine()
    }

    if (dossier.quickDebateBullets.isNotEmpty()) {
        sb.appendLine("🎯 QUICK DEBATE TAKEAWAYS:")
        dossier.quickDebateBullets.forEach { bullet ->
            sb.appendLine("• $bullet")
        }
        sb.appendLine()
    }

    sb.appendLine("— Generated by Word Context (Historical Exegesis & Apologetics)")
    return sb.toString()
}
