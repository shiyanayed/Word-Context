package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.apologetics.ApologeticsCategory
import com.example.data.apologetics.ApologeticsDossier
import com.example.data.apologetics.ApologeticsEntity
import com.example.data.apologetics.CuratedApologeticsLibrary

@Composable
fun ApologeticsScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ApologeticsCategory?,
    onSelectCategory: (ApologeticsCategory?) -> Unit,
    selectedDossier: ApologeticsDossier?,
    onSelectDossier: (ApologeticsDossier?) -> Unit,
    onToggleBookmark: (ApologeticsDossier) -> Unit,
    onSaveNotes: (id: String, notes: String) -> Unit,
    onGenerateDefense: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    savedDossiers: List<ApologeticsEntity>,
    isScholarDesk: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isAskDialogOpen by remember { mutableStateOf(false) }
    var activeSubFilter by remember { mutableStateOf(0) } // 0: All Library, 1: Bookmarked

    val textPrimary = if (isScholarDesk) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textSecondary = if (isScholarDesk) Color(0xFF64748B) else Color(0xFF94A3B8)
    val cardBg = if (isScholarDesk) Color(0xFFFFFFFF) else Color(0xFF131D2E)
    val cardBorder = if (isScholarDesk) Color(0xFFE2D9C8) else Color(0xFF1E2E45)

    // Build merged list of curated + saved entities
    val displayedDossiers = remember(searchQuery, selectedCategory, activeSubFilter, savedDossiers) {
        val curatedList = CuratedApologeticsLibrary.allDossiers
        val savedMapped = savedDossiers.map { entity ->
            val cat = try { ApologeticsCategory.valueOf(entity.categoryName) } catch (_: Exception) { ApologeticsCategory.HISTORICAL_JESUS }
            ApologeticsDossier(
                id = entity.id,
                title = entity.title,
                shortSummary = entity.shortSummary,
                category = cat,
                skepticalClaim = entity.skepticalClaim,
                historicalThesis = entity.historicalThesis,
                detailedDefense = entity.detailedDefense,
                keyScriptureReferences = emptyList(),
                isBookmarked = entity.isBookmarked,
                userPersonalNotes = entity.userPersonalNotes,
                isUserGenerated = entity.isUserGenerated,
                timestamp = entity.timestamp
            )
        }

        // Merge without duplicating IDs
        val savedMap = savedDossiers.associateBy { it.id }
        val merged = curatedList.map { curated ->
            val dbEntry = savedMap[curated.id]
            if (dbEntry != null) {
                curated.copy(
                    isBookmarked = dbEntry.isBookmarked,
                    userPersonalNotes = dbEntry.userPersonalNotes
                )
            } else {
                curated
            }
        }.toMutableList()

        // Add custom user-generated ones from DB that aren't in curated list
        savedMapped.filter { it.isUserGenerated }.forEach { custom ->
            if (merged.none { it.id == custom.id }) {
                merged.add(0, custom)
            }
        }

        // Filter by subfilter (0: all, 1: bookmarked)
        val afterSubFilter = if (activeSubFilter == 1) {
            merged.filter { it.isBookmarked }
        } else {
            merged
        }

        // Filter by category
        val afterCatFilter = if (selectedCategory != null) {
            afterSubFilter.filter { it.category == selectedCategory }
        } else {
            afterSubFilter
        }

        // Filter by search query
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            afterCatFilter
        } else {
            afterCatFilter.filter { dossier ->
                dossier.title.lowercase().contains(q) ||
                    dossier.shortSummary.lowercase().contains(q) ||
                    dossier.skepticalClaim.lowercase().contains(q) ||
                    dossier.historicalThesis.lowercase().contains(q) ||
                    dossier.detailedDefense.lowercase().contains(q) ||
                    dossier.keyScriptureReferences.any { it.lowercase().contains(q) } ||
                    dossier.archaeologicalEvidence.any { it.artifactName.lowercase().contains(q) }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("apologetics_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HISTORICAL APOLOGETICS",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vetted Biblical Defenses",
                        color = textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // "Ask AI Defense" Button with Glow
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF0284C7))
                            )
                        )
                        .clickable { isAskDialogOpen = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("open_ask_apologetics_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Ask Question",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Dismantling modern skepticism, alleged contradictions, and copycat myths using hard archaeology, 1st-century context, and patristic records.",
                color = textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search topics, objections, inscriptions (e.g. Pilate, Nicaea, Mithras)...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF38BDF8)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Clear", tint = textSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apologetics_search_bar"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = cardBorder,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-filter tabs: All vs Bookmarked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Defense Briefs (${CuratedApologeticsLibrary.allDossiers.size})", "Saved & Bookmarked").forEachIndexed { idx, label ->
                    val isSelected = activeSubFilter == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Color(0xFF2563EB).copy(alpha = 0.2f) else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else cardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSubFilter = idx }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("subfilter_tab_$idx")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF38BDF8) else textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All Categories" chip
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isAllSelected) Color(0xFF38BDF8) else cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(null) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cat_chip_all")
                ) {
                    Text(
                        text = "All Categories",
                        color = if (isAllSelected) Color(0xFF38BDF8) else textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }

                ApologeticsCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(if (isSelected) null else category) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cat_chip_${category.name.lowercase()}")
                    ) {
                        Text(
                            text = category.displayName,
                            color = if (isSelected) Color(0xFF38BDF8) else textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                }

                // Count and reset header
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VETTED DEFENSE BRIEFS (${displayedDossiers.size})",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (searchQuery.isNotBlank() || selectedCategory != null || activeSubFilter != 0) {
                        Text(
                            text = "Reset Filters",
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                onSearchQueryChange("")
                                onSelectCategory(null)
                                activeSubFilter = 0
                            }
                        )
                    }
                }
            }
        }

        // Dossiers List
        if (displayedDossiers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No apologetics dossiers found.",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query, selecting another category, or ask AI to synthesize a new defense.",
                            color = textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isAskDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AI to Defend This Topic")
                        }
                    }
                }
            }
        } else {
            items(displayedDossiers, key = { it.id }) { dossier ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 7.dp)
                ) {
                    ApologeticsCardItem(
                        dossier = dossier,
                        onClick = { onSelectDossier(dossier) },
                        onToggleBookmark = { onToggleBookmark(dossier) },
                        isScholarDesk = isScholarDesk
                    )
                }
            }
        }
    }

    // Modal Sheet for Full Dossier Detail
    if (selectedDossier != null) {
        ApologeticsDetailSheet(
            dossier = selectedDossier,
            onDismiss = { onSelectDossier(null) },
            onToggleBookmark = onToggleBookmark,
            onSaveNotes = onSaveNotes,
            isScholarDesk = isScholarDesk
        )
    }

    // Dialog for Interactive AI Defense Request
    if (isAskDialogOpen) {
        AskApologeticsDialog(
            isOpen = isAskDialogOpen,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onDismiss = { isAskDialogOpen = false },
            onSubmitQuery = { query ->
                onGenerateDefense(query)
                isAskDialogOpen = false
            },
            isScholarDesk = isScholarDesk
        )
    }
}

@Composable
fun ApologeticsCardItem(
    dossier: ApologeticsDossier,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    isScholarDesk: Boolean = false
) {
    val cardBg = if (isScholarDesk) Color(0xFFFFFFFF) else Color(0xFF131D2E)
    val cardBorder = if (isScholarDesk) Color(0xFFE2D9C8) else Color(0xFF1E2E45)
    val textPrimary = if (isScholarDesk) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textSecondary = if (isScholarDesk) Color(0xFF64748B) else Color(0xFF94A3B8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("apologetics_card_${dossier.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Bookmark Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dossier.category.displayName.uppercase(),
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (dossier.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (dossier.isBookmarked) Color(0xFFF59E0B) else textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = dossier.title,
                color = textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Short summary
            Text(
                text = dossier.shortSummary,
                color = textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Skeptical Claim Banner (Red-tinted excerpt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dossier.skepticalClaim,
                        color = if (isScholarDesk) Color(0xFF7F1D1D) else Color(0xFFFECACA),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Evidence Feature Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dossier.archaeologicalEvidence.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.AccountBalance, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Archaeology", color = textSecondary, fontSize = 11.sp)
                    }
                }
                if (dossier.ancientHistoricalTexts.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.HistoryEdu, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ancient Texts", color = textSecondary, fontSize = 11.sp)
                    }
                }
                if (dossier.quickDebateBullets.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Psychology, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${dossier.quickDebateBullets.size} Bullets", color = textSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Tap to View Brief →",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
