package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.local.AnomalyType
import com.example.data.local.LinguisticAnomalyHelper
import com.example.data.model.WordStudy

@Composable
fun LinguisticDisconnectAlertCard(
    study: WordStudy,
    modifier: Modifier = Modifier
) {
    val anomaly = remember(study) {
        LinguisticAnomalyHelper.resolveAnomaly(study)
    } ?: return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val accentColor = when (anomaly.anomalyType) {
        AnomalyType.CONTRONYM -> Color(0xFFF43F5E) // Vivid Crimson/Rose for self-antonyms
        AnomalyType.HOMONYM -> Color(0xFFF59E0B) // Amber for identical spellings / unrelated roots
        AnomalyType.SPLIT_TRANSLATION -> Color(0xFF38BDF8) // Electric Sky for split translations
    }

    val containerBg = when (anomaly.anomalyType) {
        AnomalyType.CONTRONYM -> Color(0xFF1E0E14)
        AnomalyType.HOMONYM -> Color(0xFF1B140B)
        AnomalyType.SPLIT_TRANSLATION -> Color(0xFF0C1626)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("linguistic_disconnect_alert_card"),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Alert Badge & Anomaly Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        val icon = when (anomaly.anomalyType) {
                            AnomalyType.CONTRONYM -> Icons.AutoMirrored.Filled.CompareArrows
                            AnomalyType.HOMONYM -> Icons.Default.Warning
                            AnomalyType.SPLIT_TRANSLATION -> Icons.Default.Bolt
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = anomaly.anomalyType.displayName,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LINGUISTIC DISCONNECT ALERT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = anomaly.anomalyType.description,
                            fontSize = 9.5.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = anomaly.anomalyType.badgeLabel,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Root Word & Strong's Identification Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF080C14).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF21262D), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TRANSLITERATED ROOT LEMMA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B949E),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = anomaly.rootWord,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F6FC),
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (anomaly.strongsNumber.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFC9A84C).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = anomaly.strongsNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC9A84C),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF38BDF8).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (study.testament.contains("Old", ignoreCase = true)) "Hebrew" else "Greek",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Side-by-Side Quick Comparison Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Column 1: Contextual Meaning
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "CONTEXT MEANING",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                letterSpacing = 0.6.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = anomaly.primaryMeaning,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Column 2: Hidden Alternate Meaning
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(accentColor, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            val label = when (anomaly.anomalyType) {
                                AnomalyType.CONTRONYM -> "POLAR OPPOSITE"
                                AnomalyType.HOMONYM -> "HIDDEN HOMONYM"
                                AnomalyType.SPLIT_TRANSLATION -> "SPLIT TRANSLATION"
                            }
                            Text(
                                text = label,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 0.6.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = anomaly.alternateMeaning,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Side-by-Side Detailed Comparison Table
            if (anomaly.comparisonRows.isNotEmpty()) {
                Text(
                    text = "SIDE-BY-SIDE LINGUISTIC COMPARISON",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF21262D), RoundedCornerShape(8.dp))
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF080C14))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIMENSION / FOCUS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B949E),
                                modifier = Modifier.width(130.dp)
                            )
                            Text(
                                text = "CONTEXTUAL MEANING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.width(170.dp)
                            )
                            Text(
                                text = "HIDDEN ALTERNATE / OPPOSITE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.width(180.dp)
                            )
                            if (anomaly.comparisonRows.any { it.scripturalNote.isNotBlank() }) {
                                Text(
                                    text = "SCRIPTURAL OCCURRENCE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC9A84C),
                                    modifier = Modifier.width(140.dp)
                                )
                            }
                        }

                        // Table Rows
                        anomaly.comparisonRows.forEachIndexed { idx, row ->
                            val rowBg = if (idx % 2 == 0) Color(0xFF0F172A) else Color(0xFF131D31)
                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.dimension,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.width(130.dp)
                                )
                                Text(
                                    text = row.contextMeaning,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.width(170.dp),
                                    lineHeight = 15.sp
                                )
                                Text(
                                    text = row.alternateMeaning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = accentColor,
                                    modifier = Modifier.width(180.dp),
                                    lineHeight = 15.sp
                                )
                                if (anomaly.comparisonRows.any { it.scripturalNote.isNotBlank() }) {
                                    Text(
                                        text = row.scripturalNote,
                                        fontSize = 10.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFFC9A84C),
                                        modifier = Modifier.width(140.dp)
                                    )
                                }
                            }
                            if (idx < anomaly.comparisonRows.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(Color(0xFF21262D))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 'Wow Factor' Takeaway Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC9A84C).copy(alpha = 0.12f),
                                Color(0xFF1E293B).copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Wow factor",
                                tint = Color(0xFFC9A84C),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✦ THE 'WOW FACTOR' DISCONNECT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC9A84C),
                                letterSpacing = 0.8.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(anomaly.wowFactor))
                                Toast.makeText(context, "Copied 'Wow Factor' Takeaway", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Wow Factor",
                                tint = Color(0xFF8B949E),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = anomaly.wowFactor,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFF0F6FC),
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}
