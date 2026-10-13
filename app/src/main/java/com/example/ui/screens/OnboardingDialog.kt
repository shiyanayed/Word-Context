package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val badge: String
)

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Bridge the 2,000-Year Cultural Chasm",
            subtitle = "Modern readers often import 21st-century Western definitions into ancient Scripture. Word Context uncovers what biblical terms actually meant to their original first-century Roman and Jewish listeners.",
            icon = Icons.Outlined.HistoryEdu,
            iconBg = Color(0xFF2563EB),
            badge = "HISTORICAL INTEGRITY"
        ),
        OnboardingPage(
            title = "Roman Law & Ancient Covenant Treaties",
            subtitle = "From Roman adoption breaking legal debts to Greek patronage contracts explaining 'Grace' and Hebrew suzerainty oaths, discover the legal framework behind the biblical text.",
            icon = Icons.Outlined.Gavel,
            iconBg = Color(0xFF0284C7),
            badge = "LEGAL FRAMEWORK"
        ),
        OnboardingPage(
            title = "Preach, Teach & Study with Confidence",
            subtitle = "Generate homiletical sermon outlines, export academic research papers with epigraphic citations, and maintain your personal exegetical notebook—all backed by 100% offline Strong's lexicons.",
            icon = Icons.Outlined.AutoStories,
            iconBg = Color(0xFF059669),
            badge = "HOMILETICAL TOOLS"
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentPage < pages.size - 1) "Next" else "Begin Historical Study",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            if (currentPage < pages.size - 1) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip Tour", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(page.iconBg.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = page.iconBg,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(page.iconBg.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = page.badge,
                        color = page.iconBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = page.subtitle,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 20.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}
