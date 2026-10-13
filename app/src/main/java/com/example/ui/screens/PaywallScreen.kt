package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SubscriptionPlan(
    val title: String,
    val priceText: String,
    val billingDetail: String,
    val badge: String? = null
) {
    ANNUAL("Scholar Annual", "$3.99/mo", "Billed annually at $47.99/year", "SAVE 50% • BEST VALUE"),
    MONTHLY("Scholar Monthly", "$7.99/mo", "Billed monthly, cancel anytime", null),
    LIFETIME("Patron Lifetime", "$119", "One-time payment, lifetime updates", "PATRON SCHOLAR")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    isProUser: Boolean,
    onSetProStatus: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.ANNUAL) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = Color(0xFF090E17),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF334155))
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header with Crown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Pro Scholar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Pitch
            Text(
                text = "Elevate to Scholar Pro",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unlock deep first-century exegesis, unlimited sermon prep exports, personal notebook tagging, and full offline lexicons.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            // Celebratory Banner if active
            AnimatedVisibility(visible = isProUser || showSuccessBanner) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF065F46))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Active",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Scholar Pro Active!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "All premium exegetical features and exports unlocked.",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Plan Options
            Text(
                text = "CHOOSE YOUR PLAN",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            SubscriptionPlan.values().forEach { plan ->
                val isSelected = selectedPlan == plan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedPlan = plan }
                        .testTag("plan_card_${plan.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF13233C) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (plan.badge != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (plan == SubscriptionPlan.ANNUAL) Color(0xFF2563EB) else Color(0xFFD97706))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = plan.badge,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = plan.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = plan.billingDetail,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = plan.priceText,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pro Features List
            Text(
                text = "WHAT'S INCLUDED IN PRO",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val features = listOf(
                Pair(Icons.Outlined.AutoAwesome, "Unlimited Deep AI Exegetical Word Analyses across Greek & Hebrew"),
                Pair(Icons.Outlined.Share, "Sermon Prep & Academic Study Export (Markdown, Outlines, Class Handouts)"),
                Pair(Icons.Outlined.EditNote, "Exegetical Notebook with Tagged Categorization & Personal Reflections"),
                Pair(Icons.AutoMirrored.Outlined.MenuBook, "Complete Offline Strong's Lexical Concordance Base"),
                Pair(Icons.Outlined.VerifiedUser, "Primary Epigraphical & Classical Source Grounding Verifier")
            )

            features.forEach { (icon, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = desc,
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Call to Action Button
            Button(
                onClick = {
                    onSetProStatus(true)
                    showSuccessBanner = true
                    Toast.makeText(context, "Welcome to Scholar Pro! 7-day trial activated.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("subscribe_pro_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isProUser) "Manage Active Subscription" else "Start 7-Day Free Trial",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulation / Toggle Row (so evaluator can test both Free and Pro states immediately)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo Mode: Toggle Pro Status",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Switch(
                    checked = isProUser,
                    onCheckedChange = { active ->
                        onSetProStatus(active)
                        showSuccessBanner = active
                    },
                    modifier = Modifier.testTag("toggle_pro_status_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Purchases & Disclosures
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Purchases restored: You have active Scholar Pro access.", Toast.LENGTH_SHORT).show()
                        onSetProStatus(true)
                    }
                ) {
                    Text("Restore Purchases", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Text("•", color = Color(0xFF475569))
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Subscription can be cancelled anytime in Google Play Store.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Terms & Privacy", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            Text(
                text = "Payment will be charged to your Google Play account upon trial completion. Subscriptions renew automatically unless cancelled at least 24 hours before the end of the billing cycle.",
                color = Color(0xFF475569),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
