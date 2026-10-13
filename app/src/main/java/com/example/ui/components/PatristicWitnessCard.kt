package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.PatristicEpoch
import com.example.data.local.PatristicWitness
import com.example.data.local.PatristicWitnessData
import com.example.data.model.WordStudy

@Composable
fun PatristicWitnessCard(
    study: WordStudy,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(true) }

    val profile = remember(study.word, study.testament) {
        PatristicWitnessData.getVettedHistoricalContext(study.word, study.testament)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("patristic_witness_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101928)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Color(0xFFD4AF37).copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Early Church Icon & Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFD4AF37).copy(alpha = 0.25f), Color(0xFFB45309).copy(alpha = 0.15f))
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HistoryEdu,
                            contentDescription = "Early Church Fathers",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PATRISTIC WITNESS (100–450 AD)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .border(0.5.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "VETTED PRIMARY TEXTS",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        Text(
                            text = "How the Early Church Fathers understood \"${study.word}\"",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = "Toggle Section",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Summary Insight Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090E17))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = profile.summaryInsight,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PRIMARY PATRISTIC CITATIONS & MANUSCRIPT PROVENANCE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    profile.primaryWitnesses.forEach { witness ->
                        PatristicWitnessItem(
                            witness = witness,
                            onCopyQuote = {
                                val citation = "\"${witness.directQuotation}\"\n— ${witness.fatherName}, ${witness.primaryWorkCited} (${witness.eraAndCentury})"
                                clipboardManager.setText(AnnotatedString(citation))
                                Toast.makeText(context, "Copied citation from ${witness.fatherName}", Toast.LENGTH_SHORT).show()
                            },
                            onShareQuote = {
                                val citation = buildString {
                                    appendLine("📜 Patristic Citation")
                                    appendLine("Father: ${witness.fatherName} (${witness.historicalRole})")
                                    appendLine("Era: ${witness.eraAndCentury} • ${witness.historicalEpoch.label}")
                                    appendLine("Primary Work: ${witness.primaryWorkCited}")
                                    appendLine("──────────────────────────────")
                                    appendLine("“${witness.directQuotation}”")
                                    appendLine("──────────────────────────────")
                                    appendLine("Historical Significance: ${witness.theologicalSignificance}")
                                    appendLine("\nShared from Word Context • Historical Exegesis Engine")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "Patristic Citation: ${witness.fatherName}")
                                    putExtra(Intent.EXTRA_TEXT, citation)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Patristic Citation via"))
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PatristicWitnessItem(
    witness: PatristicWitness,
    onCopyQuote: () -> Unit,
    onShareQuote: () -> Unit
) {
    val epochBadgeColor = when (witness.historicalEpoch) {
        PatristicEpoch.APOSTOLIC_FATHERS -> Color(0xFFF59E0B) // Amber
        PatristicEpoch.ANTE_NICENE -> Color(0xFF38BDF8) // Sky Blue
        PatristicEpoch.NICENE_ERA -> Color(0xFFA855F7) // Purple
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111C)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Father Name, Epoch & Era Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = witness.fatherName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9),
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "${witness.historicalRole} • ${witness.eraAndCentury}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(epochBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, epochBadgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = witness.historicalEpoch.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = epochBadgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Work Citation Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131D2D), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = witness.primaryWorkCited,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Quotation Box (Italicized ancient excerpt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF060910))
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRIMARY TEXT VERBATIM:",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onCopyQuote,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("copy_patristic_quote_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Quote",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            IconButton(
                                onClick = onShareQuote,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("share_patristic_quote_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share Quote",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "“${witness.directQuotation}”",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFF1F5F9),
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Theological Significance
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Significance: ",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )
                Text(
                    text = witness.theologicalSignificance,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
