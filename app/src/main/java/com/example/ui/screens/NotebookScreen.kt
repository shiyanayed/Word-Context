package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WordStudy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotebookScreen(
    studiesWithNotes: List<WordStudy>,
    favorites: List<WordStudy> = emptyList(),
    history: List<WordStudy> = emptyList(),
    onSelectStudy: (WordStudy) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Notes, 1: Bookmarks, 2: History
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    // Aggregate all unique tags from user studies
    val allTags = remember(studiesWithNotes) {
        studiesWithNotes.flatMap { it.userTags }.distinct().sorted()
    }

    val displayedStudies = remember(activeSubTab, studiesWithNotes, favorites, history, searchQuery, selectedTag) {
        val baseList = when (activeSubTab) {
            0 -> studiesWithNotes
            1 -> favorites
            2 -> history
            else -> studiesWithNotes
        }

        val q = searchQuery.trim().lowercase()
        baseList.filter { study ->
            val matchesQuery = if (q.isBlank()) true else {
                study.word.lowercase().contains(q) ||
                study.originalWord.lowercase().contains(q) ||
                study.userNotes.lowercase().contains(q) ||
                study.literalMeaning.lowercase().contains(q) ||
                study.userTags.any { it.lowercase().contains(q) }
            }
            val matchesTag = if (activeSubTab == 0 && selectedTag != null) {
                study.userTags.contains(selectedTag)
            } else true
            matchesQuery && matchesTag
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Exegetical Notebook",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Personal study notes, bookmarked entries, and research archives",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF2563EB).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tabs Switcher: Notes | Bookmarks | History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(0, "My Notes", studiesWithNotes.size),
                Triple(1, "Bookmarks", favorites.size),
                Triple(2, "History", history.size)
            ).forEach { (tabIdx, label, count) ->
                val isSelected = activeSubTab == tabIdx
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
                        .clickable {
                            activeSubTab = tabIdx
                            selectedTag = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$label ($count)",
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notebook_search_input"),
            placeholder = {
                Text(
                    when (activeSubTab) {
                        0 -> "Search your sermon notes & tags..."
                        1 -> "Search bookmarked word studies..."
                        else -> "Search study research history..."
                    },
                    color = Color(0xFF64748B),
                    fontSize = 12.5.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Tag Filters for "My Notes"
        if (activeSubTab == 0 && allTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All Tags" chip
                val isAllSelected = selectedTag == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                        .clickable { selectedTag = null }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "All Tags",
                        color = if (isAllSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                allTags.forEach { tag ->
                    val isSelected = selectedTag == tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTag = if (isSelected) null else tag }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = if (isSelected) Color.White else Color(0xFF93C5FD),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content List
        if (displayedStudies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (activeSubTab) {
                            0 -> Icons.AutoMirrored.Outlined.NoteAdd
                            1 -> Icons.Outlined.BookmarkBorder
                            else -> Icons.Outlined.History
                        },
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (activeSubTab) {
                            0 -> if (studiesWithNotes.isEmpty()) "No personal study notes yet" else "No matching notes found"
                            1 -> if (favorites.isEmpty()) "No bookmarked studies" else "No matching bookmarks"
                            else -> if (history.isEmpty()) "No research history yet" else "No matching history"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (activeSubTab) {
                            0 -> if (studiesWithNotes.isEmpty())
                                "Open any word study and scroll down to 'Personal Exegetical Notes' to attach sermon thoughts and reflections."
                            else "Try a different search query or reset tag filters."
                            1 -> "Tap the bookmark icon on any study detail screen to save it for quick reference."
                            else -> "Searched biblical words will automatically be recorded here for instant access."
                        },
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedStudies, key = { "${it.id}_${activeSubTab}" }) { study ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStudy(study) }
                            .testTag("notebook_item_${study.word.lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF25334D))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = study.word.replaceFirstChar { it.uppercase() },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${study.originalWord})",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 14.sp
                                    )
                                    if (study.strongsNumber.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = study.strongsNumber,
                                            color = Color(0xFFFBBF24),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = study.testament,
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (activeSubTab == 0 && study.userNotes.isNotBlank()) {
                                Text(
                                    text = study.userNotes,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = study.literalMeaning,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (study.userTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    study.userTags.take(4).forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                color = Color(0xFF93C5FD),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
