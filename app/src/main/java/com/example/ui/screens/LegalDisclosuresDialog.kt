package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LegalTab(val title: String) {
    PRIVACY("Privacy Policy"),
    TERMS("Terms of Service"),
    THEOLOGY("Theological Integrity")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDisclosuresDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(LegalTab.PRIVACY) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2563EB).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Gavel,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Legal & Integrity Disclosures",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Google Play Store & Academic Compliance",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegalTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 340.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B13))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (activeTab) {
                    LegalTab.PRIVACY -> PrivacyPolicyContent()
                    LegalTab.TERMS -> TermsOfServiceContent()
                    LegalTab.THEOLOGY -> TheologicalStatementContent()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_legal_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Understood & Accept", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column {
        Text(
            text = "Privacy Policy (Local-First Guarantee)",
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = """
Word Context is built upon a strict privacy-first and local-first architecture:

1. ZERO PERSONAL DATA HARVESTING
We do not collect, track, sell, or transmit any personally identifiable information, contact lists, device identifiers, or search histories to third-party ad networks or data brokers.

2. LOCAL-FIRST ENCRYPTED STORAGE
All your search history, personal exegetical notes, bookmarks, and custom tags are stored exclusively on your device inside an offline SQLite database via Android Room. If you uninstall the app or clear app data, your local data is deleted.

3. DIRECT API CREDENTIAL SECURITY
Any third-party LLM API keys you configure (Google Gemini, Anthropic Claude, Groq, xAI Grok) are stored directly inside Android's private SharedPreferences and encrypted database partitions on your phone. Network requests are sent strictly to the respective official provider endpoints directly from your device.

4. THIRD-PARTY LLM PROCESSING
When you perform live AI studies or AI chat sessions, the text of your query is transmitted directly to the configured AI API provider (Google, Anthropic, Groq, or xAI) subject to their respective enterprise privacy guidelines.
""".trimIndent(),
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TermsOfServiceContent() {
    Column {
        Text(
            text = "Terms of Service & Usage Agreement",
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = """
1. SCHOLARLY & RESEARCH USE
Word Context is provided as an educational, homiletical, and academic study companion designed to assist pastors, students, scholars, and believers in exploring ancient linguistic, cultural, and legal frameworks of biblical texts.

2. USER OWNERSHIP OF STUDY NOTES
Any sermon outlines, notes, annotations, or reflections authored by you within the app remain your exclusive intellectual property. You may freely publish, preach, distribute, and quote them without attribution or royalty obligations.

3. GOOGLE PLAY SUBSCRIPTIONS & CANCELLATION
Scholar Pro subscription plans are managed through your Google Play account in accordance with Google Play Billing terms. You may cancel your subscription at any time via Google Play > Subscriptions. If cancelled, Pro access remains active until the end of the current billing cycle.

4. ACCURACY OF HISTORICAL CITATIONS
While our synthesis engine references historical lexicons (BDAG, HALOT, Thayer's) and epigraphical corpus, theological research should always be cross-referenced with primary sources and pastoral counsel.
""".trimIndent(),
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TheologicalStatementContent() {
    Column {
        Text(
            text = "Theological & Hermeneutical Statement of Integrity",
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = """
1. THE HISTORICAL-GRAMMATICAL METHOD
Word Context is anchored in the historical-grammatical approach to biblical interpretation: discovering what the inspired author intended to communicate to their specific, original audience in their historical, cultural, legal, and linguistic environment.

2. AGAINST EISEGESIS & ANACHRONISM
Scripture was written FOR modern believers, but it was not originally written TO modern Westerners. Anachronistic readings (interpreting ancient Greek or Hebrew words using 21st-century English cultural assumptions) frequently obscure the richness of God's Word.

3. RESPECT FOR BIBLICAL AUTHORITY
By recovering the first-century Roman legal significance of adoption, the Greco-Roman patronage structure of grace, and the ancient Near Eastern covenantal nature of righteousness and peace, Word Context seeks to exalt the authority and beauty of Scripture.
""".trimIndent(),
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
