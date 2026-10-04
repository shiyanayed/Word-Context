@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.MorphologicalRow
import com.example.data.local.PhilologyFamilyHelper
import com.example.data.model.WordStudy
import com.example.data.verification.SourceTrustVerifier
import com.example.data.verification.TrustTier
import com.example.data.verification.GroundingSource
import com.example.data.verification.VerificationMetadata
import com.example.ui.SearchUiState
import com.example.ui.WordStudyViewModel
import com.example.ui.ChatMessage
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordContextApp(
    viewModel: WordStudyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Initialize TextToSpeech engine
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    DisposableEffect(context) {
        val ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale.US
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val history by viewModel.history.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTestament by viewModel.selectedTestament.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()
    val viewingStudy by viewModel.viewingStudy.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()
    val chatError by viewModel.chatError.collectAsState()

    val activeProvider by viewModel.activeProvider.collectAsState()
    val geminiKey by viewModel.geminiKey.collectAsState()
    val claudeKey by viewModel.claudeKey.collectAsState()
    val groqKey by viewModel.groqKey.collectAsState()
    val grokKey by viewModel.grokKey.collectAsState()
    val useSearchGrounding by viewModel.useSearchGrounding.collectAsState()
    val strictVerification by viewModel.strictVerification.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Hub, 1: History, 2: Favorites, 3: Settings
    var isChatOpen by remember { mutableStateOf(false) }

    // Beautiful loading trivia loop
    val triviaList = listOf(
        "In Roman Law, adoption breaks 'Patria Potestas'—the absolute authority of the birth father—legally erasing all prior debts forever.",
        "The Greek word for Grace (Charis) meant reciprocal patronage in the first-century Roman Empire—the patron provides benevolence, the client offers loyalty.",
        "A Covenant (Hebrew 'Berit') wasn't a business contract; it was a life-and-death legal union sealed with blood and solemn oaths.",
        "Redemption (Greek 'Apolytrosis') was a legal commercial term used for purchasing a slave's freedom in the Roman marketplace.",
        "The Greek word for church (Ekklesia) originally referred to a legal, democratic assembly of citizens summoned to govern a city-state."
    )
    var currentTrivia by remember { mutableStateOf(triviaList[0]) }
    LaunchedEffect(searchUiState) {
        if (searchUiState is SearchUiState.Loading) {
            var index = 0
            while (searchUiState is SearchUiState.Loading) {
                currentTrivia = triviaList[index % triviaList.size]
                delay(4000)
                index++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Background color matching image (rich deep dark space blue)
                drawRect(color = Color(0xFF080C14))
                
                // Top right soft blue light glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.38f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.75f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.width * 0.75f
                )
                
                // Center-bottom soft blue light glow (behind action button)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2563EB).copy(alpha = 0.32f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.55f),
                        radius = size.width * 0.85f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.55f),
                    radius = size.width * 0.85f
                )
            }
    ) {
        Scaffold(
            topBar = {
                // Fixed beautiful integrated top bar matching the image exactly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = "Bible Study Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "WORD CONTEXT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Ask AI Button with Google Grounding styling
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { isChatOpen = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ask AI",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ask AI",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Circular translucent close button with 48dp touch target
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("top_bar_close_button")
                                .clickable {
                                    viewModel.clearSearchState()
                                    viewModel.setViewingStudy(null)
                                    viewModel.updateSearchQuery("")
                                    activeTab = 0
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(percent = 50))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(percent = 50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Elegant custom bottom navigation bar matching the design
                NavigationBar(
                    containerColor = Color(0xFF080C14),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1E2E45),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .navigationBarsPadding(),
                    tonalElevation = 0.dp
                ) {
                    listOf(
                        Triple(0, Icons.Outlined.Search, "Study Hub"),
                        Triple(1, Icons.Outlined.MenuBook, "Lexicon"),
                        Triple(2, Icons.Outlined.Bookmarks, "Insights"),
                        Triple(3, Icons.Outlined.Settings, "Settings")
                    ).forEach { (tabIndex, iconVector, labelText) ->
                        val isSelected = activeTab == tabIndex
                        val tabColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF5A6E85)
                        val tabTag = when (tabIndex) {
                            0 -> "nav_study_hub"
                            1 -> "nav_lexicon"
                            2 -> "nav_insights"
                            3 -> "nav_settings"
                            else -> "nav_tab_$tabIndex"
                        }
                        NavigationBarItem(
                            modifier = Modifier.testTag(tabTag),
                            selected = isSelected,
                            onClick = { activeTab = tabIndex },
                            icon = { 
                                Icon(
                                    imageVector = iconVector, 
                                    contentDescription = labelText,
                                    tint = tabColor,
                                    modifier = Modifier.size(22.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = labelText,
                                    color = tabColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                if (activeTab == 0) {
                    if (viewingStudy == null && searchUiState is SearchUiState.Idle) {
                        // DISPLAY THE ENTIRE HERO FORM MATCHING THE IMAGE EXACTLY
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // "Historical Bible Study" Big Title
                            Text(
                                text = "Historical Bible Study",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Elegant scholarly description paragraph
                            Text(
                                text = "Greek and Hebrew word context, unfolding cultural nuance with scholarly lexicons. Discover biblical background and historical significance.",
                                color = Color(0xFF8B949E),
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Beautiful Custom Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search word (e.g. grace, covenant, faith)...", color = Color(0xFF8B949E)) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF8B949E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F1524),
                                    unfocusedContainerColor = Color(0xFF0F1524),
                                    disabledContainerColor = Color(0xFF0F1524),
                                    focusedBorderColor = Color(0xFF2A3E5C),
                                    unfocusedBorderColor = Color(0xFF1F293D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_input")
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Custom Testament Selection Tabs side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(28.dp)
                            ) {
                                listOf("New Testament", "Old Testament").forEach { testament ->
                                    val isSelected = selectedTestament == testament
                                    val testamentTag = if (testament.startsWith("New")) "testament_tab_new" else "testament_tab_old"
                                    Column(
                                        modifier = Modifier
                                            .testTag(testamentTag)
                                            .clickable { viewModel.updateSelectedTestament(testament) }
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = testament.uppercase(),
                                            color = if (isSelected) Color.White else Color(0xFF5A6E85),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (isSelected) {
                                            // Glowing blue underline bar spanning width of text
                                            Box(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(3.dp)
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color(0xFF38BDF8).copy(alpha = 0.1f),
                                                                Color(0xFF38BDF8),
                                                                Color(0xFF38BDF8).copy(alpha = 0.1f)
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(1.5.dp)
                                                    )
                                            )
                                        } else {
                                            Box(modifier = Modifier.height(3.dp))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // Action Pill Button ("Reveal Historical Context") with Glow
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(27.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF60A5FA), // light cyan-blue
                                                Color(0xFF2563EB)  // royal blue
                                            )
                                        )
                                    )
                                    .testTag("reveal_context_button")
                                    .clickable(
                                        enabled = searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading,
                                        onClick = { viewModel.performSearch() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reveal Historical Context",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        viewModel.sendChatMessage(searchQuery, viewingStudy)
                                        isChatOpen = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ask AI Question (Google Search Grounded)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // Suggestion Tags Title
                            Text(
                                text = "Curated Suggestion Tags",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.2.sp
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Suggestions Capsule Tags in FlowRow
                            val chipSuggestions = listOf(
                                "euaggelion", "grace", "covenant", "adoption", 
                                "redemption", "faith", "shalom", "agape", 
                                "lovingkindness", "redeemer", "righteousness", "logos"
                            )
                            
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                chipSuggestions.forEach { chipWord ->
                                    val isNT = chipWord in listOf("euaggelion", "grace", "adoption", "redemption", "faith", "agape", "logos")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF0F1622))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF26354A),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                viewModel.updateSelectedTestament(if (isNT) "New Testament" else "Old Testament")
                                                viewModel.updateSearchQuery(chipWord)
                                                viewModel.performSearch()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = chipWord,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    } else {
                        // VIEWING STUDY DETAILS / LOADING / ERROR: Minimized Search Bar at the top for quick searches
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search another word...", color = Color(0xFF8B949E)) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F1524),
                                    unfocusedContainerColor = Color(0xFF0F1524),
                                    disabledContainerColor = Color(0xFF0F1524),
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF1F293D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.performSearch() },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text("Reveal Context", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } // End of if (activeTab == 0)
    
                // Main Display Tab Routing
                when (activeTab) {
                    0 -> {
                        // STUDY HUB (Current or Selected Study result)
                        StudyHubTab(
                            searchUiState = searchUiState,
                            viewingStudy = viewingStudy,
                            onCloseDetail = { viewModel.setViewingStudy(null) },
                            onClearSearchState = { viewModel.clearSearchState() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onShare = { study ->
                                val textToCopy = formatStudyForSharing(study)
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Copied word study to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            currentTrivia = currentTrivia,
                            onSearchWord = { word ->
                                viewModel.updateSearchQuery(word)
                                viewModel.performSearch()
                            },
                            tts = tts,
                            chatMessages = chatMessages,
                            chatLoading = chatLoading,
                            chatError = chatError,
                            useSearchGrounding = useSearchGrounding,
                            onToggleSearchGrounding = { viewModel.toggleSearchGrounding(it) },
                            onSendChatMessage = { text, study -> viewModel.sendChatMessage(text, study) },
                            onResetChat = { viewModel.resetChat() },
                            onOpenChat = { isChatOpen = true },
                            onRetrySearch = { viewModel.performSearch() }
                        )
                    }
                1 -> {
                    // HISTORY TAB
                    HistoryTab(
                        history = history,
                        onSelectStudy = { study ->
                            viewModel.setViewingStudy(study)
                            activeTab = 0 // Switch to hub to view it
                        },
                        onDeleteStudy = { viewModel.deleteStudy(it.id) }
                    )
                }
                2 -> {
                    // SAVED/FAVORITES TAB
                    FavoritesTab(
                        favorites = favorites,
                        onSelectStudy = { study ->
                            viewModel.setViewingStudy(study)
                            activeTab = 0 // Switch to hub to view it
                        },
                        onRemoveFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
                3 -> {
                    // SETTINGS TAB
                    SettingsTab(
                        activeProvider = activeProvider,
                        geminiKey = geminiKey,
                        claudeKey = claudeKey,
                        groqKey = groqKey,
                        grokKey = grokKey,
                        useSearchGrounding = useSearchGrounding,
                        onToggleSearchGrounding = { viewModel.toggleSearchGrounding(it) },
                        strictVerification = strictVerification,
                        onToggleStrictVerification = { viewModel.toggleStrictVerification(it) },
                        onSaveSettings = { provider, gemini, claude, groq, grok ->
                            viewModel.saveSettings(provider, gemini, claude, groq, grok)
                        },
                        onClearKeys = {
                            viewModel.clearAllKeys()
                        }
                    )
                }
            }
        }
    }

    BackHandler(enabled = isChatOpen) {
        isChatOpen = false
    }

    AnimatedVisibility(
        visible = isChatOpen,
        modifier = Modifier.fillMaxSize(),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 280)
        ) + fadeIn(animationSpec = tween(280)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 240)
        ) + fadeOut(animationSpec = tween(240))
    ) {
        val activeStudy = viewingStudy ?: (searchUiState as? SearchUiState.Success)?.wordStudy
        WordStudyChatView(
            study = activeStudy,
            chatMessages = chatMessages,
            chatLoading = chatLoading,
            chatError = chatError,
            useSearchGrounding = useSearchGrounding,
            onToggleSearchGrounding = { viewModel.toggleSearchGrounding(it) },
            strictVerification = strictVerification,
            onToggleStrictVerification = { viewModel.toggleStrictVerification(it) },
            onSendMessage = { viewModel.sendChatMessage(it, activeStudy) },
            onResetChat = { viewModel.resetChat() },
            onClose = { isChatOpen = false }
        )
    }
}
}

@Composable
fun StudyHubTab(
    searchUiState: SearchUiState,
    viewingStudy: WordStudy?,
    onCloseDetail: () -> Unit,
    onClearSearchState: () -> Unit,
    onToggleFavorite: (WordStudy) -> Unit,
    onShare: (WordStudy) -> Unit,
    currentTrivia: String,
    onSearchWord: (String) -> Unit,
    tts: android.speech.tts.TextToSpeech?,
    chatMessages: List<ChatMessage>,
    chatLoading: Boolean,
    chatError: String?,
    useSearchGrounding: Boolean = true,
    onToggleSearchGrounding: (Boolean) -> Unit = {},
    onSendChatMessage: (String, WordStudy?) -> Unit,
    onResetChat: () -> Unit,
    onOpenChat: () -> Unit,
    onRetrySearch: () -> Unit = {}
) {
    var lastStudyId by remember { mutableStateOf(viewingStudy?.id) }
    LaunchedEffect(viewingStudy?.id) {
        if (viewingStudy != null && viewingStudy.id != lastStudyId) {
            lastStudyId = viewingStudy.id
            onResetChat()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = searchUiState,
            label = "SearchUiStateTransition"
        ) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    if (viewingStudy != null) {
                        // Viewing cached/selected study
                        WordStudyDetailView(
                            study = viewingStudy,
                            onToggleFavorite = { onToggleFavorite(viewingStudy) },
                            onShare = { onShare(viewingStudy) },
                            onSearchWord = onSearchWord,
                            tts = tts,
                            onOpenChat = onOpenChat
                        )
                    } else {
                        // Prominent scholarly empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Search Word",
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Begin Your Study",
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter a biblical word above to explore its original legal, cultural, and historical first-century meaning.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // AI Research Partner Grounded Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenChat() },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "AI RESEARCH PARTNER",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981).copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(5.dp).background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "GOOGLE SEARCH GROUNDED",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ask any complex historical, cultural, legal, or biblical question with live real-time Google search data.",
                                        color = Color(0xFFF0F6FC),
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val sampleQueries = listOf(
                                        "What was Roman adoption?",
                                        "Priene Inscription & Gospel",
                                        "Honor/Shame culture"
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        sampleQueries.forEach { q ->
                                            Surface(
                                                onClick = {
                                                    onOpenChat()
                                                    onSendChatMessage(q, null)
                                                },
                                                color = Color(0xFF1E293B),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFF334155))
                                            ) {
                                                Text(
                                                    text = q,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.5.sp,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Consulting Ancient Sources...",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "HISTORICAL TRIVIA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentTrivia,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                is SearchUiState.Success -> {
                    // Display retrieved study
                    WordStudyDetailView(
                        study = state.wordStudy,
                        onToggleFavorite = { onToggleFavorite(state.wordStudy) },
                        onShare = { onShare(state.wordStudy) },
                        onSearchWord = onSearchWord,
                        tts = tts,
                        onOpenChat = onOpenChat
                    )
                }
                is SearchUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Retrieval Failed",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val isApiKeyError = state.message.contains("API key", ignoreCase = true) || state.message.contains("GEMINI_API_KEY", ignoreCase = true)
                        
                        if (isApiKeyError) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "SECURE API KEY REQUIRED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "To generate fresh AI studies for any Bible word, please enter your GEMINI_API_KEY in the Secrets panel on the left sidebar of Google AI Studio, or configure Claude/Groq/Grok in the Settings tab.\n\nAlternatively, you can study our rich curated words (like euaggelion, grace, covenant, adoption, redemption, agape, or shalom) completely offline without an API key!",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = state.message,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClearSearchState,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Dismiss")
                            }
                            Button(
                                onClick = onRetrySearch,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry Search")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getWordFamily(word: String, testament: String): List<String> {
    return when (word.lowercase().trim()) {
        "adoption" -> listOf("huiothesia (Sonship)", "pater (Father)", "klēronomos (Heir)", "huios (Son)")
        "grace" -> listOf("charis (Grace)", "pistis (Faith)", "charisma (Gift)", "synergia (Co-working)")
        "redemption" -> listOf("apolytrosis (Redemption)", "lytron (Ransom)", "eleutheria (Freedom)", "exagorazo (Buy out)")
        "faith" -> listOf("pistis (Faith)", "pisteuo (Trust)", "charis (Grace)", "peitho (Persuade)")
        "covenant" -> listOf("berit (Covenant)", "chesed (Loyalty)", "shaba (Oath)", "karat (Cut)")
        "lovingkindness" -> listOf("chesed (Loyalty)", "rachamim (Mercy)", "berit (Covenant)", "chanan (Grace)")
        "redeemer" -> listOf("goel (Redeemer)", "padah (Ransom)", "geullah (Redemption)", "shalam (Restore)")
        "righteousness" -> listOf("tsedakah (Righteousness)", "mishpat (Justice)", "shalom (Peace)", "yashar (Upright)")
        else -> {
            if (testament.contains("New", ignoreCase = true)) {
                listOf("charis (Grace)", "pistis (Faith)", "ekklesia (Assembly)", "agape (Love)")
            } else {
                listOf("berit (Covenant)", "chesed (Loyalty)", "shalom (Peace)", "shema (Hear)")
            }
        }
    }
}

@Composable
fun MorphologicalProfileCard(
    study: WordStudy,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isMarkdownView by remember { mutableStateOf(false) }

    val philProfile = remember(study.word, study.rootLemma, study.morphologicalFamilyTable, study.translationDisconnect) {
        PhilologyFamilyHelper.getPhilologicalProfile(
            word = study.word,
            originalWord = study.originalWord,
            transliteration = study.transliteration,
            testament = study.testament,
            customRootLemma = study.rootLemma,
            customTable = study.morphologicalFamilyTable,
            customDisconnect = study.translationDisconnect
        )
    }

    val markdownTable = remember(philProfile, study.morphologicalFamilyTable) {
        if (study.morphologicalFamilyTable.isNotBlank()) {
            study.morphologicalFamilyTable
        } else {
            PhilologyFamilyHelper.buildMarkdownTable(philProfile.morphologicalRows)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
        border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFC9A84C).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Philological Analysis",
                            tint = Color(0xFFC9A84C),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ORIGINAL LANGUAGE LEMMA PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9A84C),
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Co-presented Root Family • ${philProfile.originalLanguage}",
                            fontSize = 10.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF238636).copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF238636).copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "UNIFIED ROOT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3FB950),
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Root Lemma Highlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF30363D), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ROOT LEMMA & CO-PRESENTED ANCHOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = philProfile.rootLemma,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0F6FC),
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Comparative Table Header & View Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPARATIVE WORD FAMILY ANALYSIS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC9A84C),
                    letterSpacing = 0.8.sp
                )

                // Mode toggle
                Row(
                    modifier = Modifier
                        .background(Color(0xFF0D1117), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF21262D), shape = RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (!isMarkdownView) Color(0xFF21262D) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { isMarkdownView = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Table",
                            fontSize = 10.sp,
                            fontWeight = if (!isMarkdownView) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isMarkdownView) Color(0xFFF0F6FC) else Color(0xFF8B949E)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isMarkdownView) Color(0xFF21262D) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { isMarkdownView = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Markdown",
                            fontSize = 10.sp,
                            fontWeight = if (isMarkdownView) FontWeight.Bold else FontWeight.Normal,
                            color = if (isMarkdownView) Color(0xFFF0F6FC) else Color(0xFF8B949E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isMarkdownView) {
                // Formatted Visual Comparative Table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                ) {
                    Column {
                        // Table header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF21262D))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PART OF SPEECH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B949E),
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = "ORIGINAL / TRANSLIT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B949E),
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "ENGLISH TRANSLATION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B949E),
                                modifier = Modifier.weight(1.6f)
                            )
                        }

                        // Data rows
                        philProfile.morphologicalRows.forEachIndexed { index, row ->
                            val bg = if (index % 2 == 0) Color(0xFF0D1117) else Color(0xFF161B27)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Part of speech pill
                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .padding(end = 4.dp)
                                ) {
                                    val chipColor = when {
                                        row.partOfSpeech.contains("Noun", ignoreCase = true) -> Color(0xFF38BDF8)
                                        row.partOfSpeech.contains("Verb", ignoreCase = true) -> Color(0xFFF59E0B)
                                        row.partOfSpeech.contains("Adj", ignoreCase = true) -> Color(0xFFA78BFA)
                                        else -> Color(0xFF34D399)
                                    }
                                    Text(
                                        text = row.partOfSpeech,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipColor
                                    )
                                }

                                // Original Term & Transliteration
                                Column(modifier = Modifier.weight(1.3f).padding(end = 4.dp)) {
                                    Text(
                                        text = row.originalTerm,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC9A84C),
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        text = row.transliteration,
                                        fontSize = 10.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF8B949E)
                                    )
                                }

                                // English Translation
                                Text(
                                    text = row.englishTranslation,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF0F6FC),
                                    modifier = Modifier.weight(1.6f),
                                    lineHeight = 15.sp
                                )
                            }
                            if (index < philProfile.morphologicalRows.lastIndex) {
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
            } else {
                // Raw Markdown Table View with Copy Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF30363D), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RAW MARKDOWN COMPARATIVE TABLE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B949E),
                                letterSpacing = 0.8.sp
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(markdownTable))
                                    Toast.makeText(context, "Markdown table copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Copy Markdown",
                                    tint = Color(0xFF8B949E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = markdownTable,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF7EE787),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. The Translation Disconnect Highlight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1B12), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Translation Disconnect",
                            tint = Color(0xFFC9A84C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "THE \"TRANSLATION DISCONNECT\" HIGHLIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9A84C),
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hidden Linguistic Paradox & Ancient Mindset Restored",
                        fontSize = 10.sp,
                        color = Color(0xFF8B949E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = philProfile.translationDisconnect,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFF0F6FC),
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
fun WordStudyDetailView(
    study: WordStudy,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onSearchWord: (String) -> Unit,
    tts: android.speech.tts.TextToSpeech?,
    onOpenChat: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Actions & Study Meta Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labelText = if (study.testament == "New Testament") "Greek Study" else "Hebrew Study"
            Box(
                modifier = Modifier
                    .background(Color(0xFFC9A84C).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.25f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = labelText.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFC9A84C),
                    fontFamily = FontFamily.Serif
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOpenChat, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Chat with AI",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Study",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // 1. HORIZONTAL ELEGANT WORD DISPLAY
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Large Greek/Hebrew Word - completely horizontal and never vertical
                        Text(
                            text = study.originalWord,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFC9A84C)
                        )
                        
                        Column {
                            Text(
                                text = study.transliteration,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFF0F6FC)
                            )
                            Text(
                                text = "Strong's ${study.strongsNumber}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF8B949E)
                            )
                        }

                        // Speaker icon (pronunciation)
                        var isPlaying by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (isPlaying) 1.25f else 1.0f, label = "SpeakerScale")
                        IconButton(
                            onClick = {
                                isPlaying = true
                                tts?.speak(study.originalWord, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                (context as? android.app.Activity)?.runOnUiThread {
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isPlaying = false
                                    }, 600)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .scale(scale)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = "Pronunciation",
                                tint = if (isPlaying) Color(0xFFC9A84C) else Color(0xFF8B949E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Heart icon for saving study with animated visual feedback
                    var isFavoriteActive by remember { mutableStateOf(study.isFavorite) }
                    LaunchedEffect(study.isFavorite) {
                        isFavoriteActive = study.isFavorite
                    }
                    val heartScale by animateFloatAsState(if (isFavoriteActive) 1.25f else 1.0f, label = "HeartScale")
                    IconButton(
                        onClick = {
                            isFavoriteActive = !isFavoriteActive
                            onToggleFavorite()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .scale(heartScale)
                    ) {
                        Icon(
                            imageVector = if (isFavoriteActive) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save Study",
                            tint = if (isFavoriteActive) Color(0xFFD32F2F) else Color(0xFF8B949E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF21262D), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "LITERAL MEANING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B949E),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = study.literalMeaning,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F6FC),
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }

        // ==========================================
        // 1.5 CHAT WITH AI RESEARCH PARTNER (Premium Glow Card)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onOpenChat() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF38BDF8).copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Research Assistant",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI RESEARCH PARTNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ask deep historical or cultural questions about '${study.word}'...",
                            fontSize = 12.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open Chat",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ==========================================
        // 1.8 ORIGINAL LANGUAGE LEMMA PROFILE & COMPARATIVE MORPHOLOGICAL WORD FAMILY
        // ==========================================
        MorphologicalProfileCard(study = study)

        // ==========================================
        // 2. NEW TESTAMENT / OLD TESTAMENT CORE SIGNIFICANCE
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = "Scholarly meaning",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FIRST-CENTURY SCHOLARLY MEANING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9A84C),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = study.thenMeaning,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // ==========================================
        // 3. COMMON MODERN MISREADING (Subtle Warning Styling)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0x33EF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMON MODERN MISREADING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        letterSpacing = 0.8.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = study.nowMeaning,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // ==========================================
        // 4. HISTORICAL CONTEXT CARD
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Historical Context",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HISTORICAL CONTEXT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9A84C),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "HISTORICAL ERA & SETTING",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = study.historicalBackground,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "CULTURAL NORMS & SOCIAL FRAMEWORK",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = study.culturalContext,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val lawSystem = if (study.testament == "New Testament") "ROMAN LAW PERSPECTIVE" else "COVENANT JURISPRUDENCE"
                Text(
                    text = lawSystem,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = study.legalDimension,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // ==========================================
        // 5. THEOLOGICAL IMPORTANCE
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmark,
                        contentDescription = "Theological Importance",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "THEOLOGICAL IMPORTANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9A84C),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = study.theologicalWeight,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // ==========================================
        // 6. KEY VERSES (Clean List Format)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = "Key Scriptures",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KEY SCRIPTURES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9A84C),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                study.keyScriptures.forEachIndexed { index, verse ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0D1117), shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF21262D), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFFC9A84C),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = verse,
                            fontSize = 13.5.sp,
                            color = Color(0xFFF0F6FC),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // 7. RELATED WORD FAMILY (Elegant, Tappable Chips)
        // ==========================================
        val familyWords = getWordFamily(study.word, study.testament)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFF21262D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Related Word Family",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RELATED WORD FAMILY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9A84C),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    familyWords.forEach { term ->
                        val cleanTerm = term.split("(")[0].trim()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D1117))
                                .border(1.dp, Color(0xFF21262D), shape = RoundedCornerShape(8.dp))
                                .clickable {
                                    onSearchWord(cleanTerm)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = term,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC9A84C),
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. SCHOLARLY INSIGHT
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B27)),
            border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SCHOLARLY INSIGHT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC9A84C),
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = study.closingInsight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp,
                    color = Color(0xFFF0F6FC),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // ==========================================
        // 9. VERIFIED SOURCES & SCHOLARLY PROVENANCE (Authenticity & Trust Audit)
        // ==========================================
        val uriHandler = LocalUriHandler.current
        var showMethodologyDialog by remember { mutableStateOf(false) }
        val (auditedSources, verificationMeta) = remember(study) {
            SourceTrustVerifier.auditStringSources(
                rawStrings = study.searchGroundingSources,
                word = study.word,
                strictFilter = true
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with Trust badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Authenticity Verified",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTHENTICITY & TRUST AUDIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${verificationMeta.overallTrustScore}% TRUST SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = verificationMeta.auditSummary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quality guarantee & Methodology pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ Quality Gate: Peer-reviewed & epigraphy only",
                        fontSize = 9.5.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Audit Methodology ›",
                        fontSize = 10.sp,
                        color = Color(0xFFC9A84C),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showMethodologyDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sources list
                auditedSources.forEach { source ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)),
                        border = BorderStroke(0.5.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    if (source.url.isNotBlank() && source.url.startsWith("http")) {
                                        try { uriHandler.openUri(source.url) } catch (_: Exception) {}
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = source.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(source.trustTier.badgeColorHex).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(source.trustTier.badgeColorHex).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${source.trustScore}% Authentic",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(source.trustTier.badgeColorHex)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = source.trustTier.label,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(source.trustTier.badgeColorHex)
                                )
                                if (source.primaryCorpus.isNotBlank()) {
                                    Text(
                                        text = source.primaryCorpus,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = source.verificationNote,
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        if (showMethodologyDialog) {
            AlertDialog(
                onDismissRequest = { showMethodologyDialog = false },
                containerColor = Color(0xFF0F172A),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Verification Methodology",
                        tint = Color(0xFF10B981)
                    )
                },
                title = {
                    Text(
                        text = "Source Authenticity Engine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9),
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "How the AI verifies authenticity before presenting outputs:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9A84C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Epigraphical & Archaeological Vetting: Cross-checked against primary ancient corpora such as the Corpus Inscriptionum Latinarum (CIL), Orientis Graeci Inscriptiones Selectae (OGIS), and excavations.\n\n" +
                                    "2. Academic Peer-Review Requirement: Validated against accredited university presses (Oxford, Cambridge, Harvard, Yale) and peer-reviewed classical journals.\n\n" +
                                    "3. Lexical Manuscript Integrity: Benchmarked against authoritative lexicons (BDAG, LSJ, Tyndale House).\n\n" +
                                    "4. Strict Exclusion Gate: Unverified public forums (Reddit, Quora), commercial clickbait, and personal blogs are automatically discarded to preserve factual historical integrity.",
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMethodologyDialog = false }) {
                        Text("Understood", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun StudyBreakdownSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun HistoryTab(
    history: List<WordStudy>,
    onSelectStudy: (WordStudy) -> Unit,
    onDeleteStudy: (WordStudy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "SEARCH HISTORY",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No studies in history yet.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                history.forEach { study ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStudy(study) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = study.word.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${study.originalWord} (${study.transliteration}) • ${study.testament}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (study.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Saved",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteStudy(study) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
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

@Composable
fun FavoritesTab(
    favorites: List<WordStudy>,
    onSelectStudy: (WordStudy) -> Unit,
    onRemoveFavorite: (WordStudy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "SAVED WORD STUDIES",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No saved studies yet.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                favorites.forEach { study ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStudy(study) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = study.word.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${study.originalWord} (${study.transliteration}) • ${study.literalMeaning}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(
                                onClick = { onRemoveFavorite(study) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Remove Bookmark",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatStudyForSharing(study: WordStudy): String {
    return """
        BIBLE WORD STUDY: ${study.word.uppercase()} (${study.testament})
        Original word: ${study.originalWord} (${study.transliteration})
        Strong's: ${study.strongsNumber}
        Literal translation: ${study.literalMeaning}
        
        [WHAT IT MEANT THEN]
        ${study.thenMeaning}
        
        [WHAT WE THINK NOW]
        ${study.nowMeaning}
        
        [HISTORICAL BACKGROUND]
        ${study.historicalBackground}
        
        [CULTURAL CONTEXT]
        ${study.culturalContext}
        
        [LEGAL DIMENSION]
        ${study.legalDimension}
        
        [THEOLOGICAL WEIGHT]
        ${study.theologicalWeight}
        
        [KEY SCRIPTURES]
        ${study.keyScriptures.joinToString(", ")}
        
        [CLOSING INSIGHT - WHAT BELIEVERS MISS]
        ${study.closingInsight}
        
        Shared via Word Context Bible study tool.
    """.trimIndent()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

@Composable
fun SettingsTab(
    activeProvider: String,
    geminiKey: String,
    claudeKey: String,
    groqKey: String,
    grokKey: String,
    useSearchGrounding: Boolean = true,
    onToggleSearchGrounding: (Boolean) -> Unit = {},
    strictVerification: Boolean = true,
    onToggleStrictVerification: (Boolean) -> Unit = {},
    onSaveSettings: (provider: String, gemini: String, claude: String, groq: String, grok: String) -> Unit,
    onClearKeys: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(activeProvider) }
    var tempGemini by remember { mutableStateOf(geminiKey) }
    var tempClaude by remember { mutableStateOf(claudeKey) }
    var tempGroq by remember { mutableStateOf(groqKey) }
    var tempGrok by remember { mutableStateOf(grokKey) }

    // Synchronize local UI state whenever persistent StateFlow updates
    LaunchedEffect(activeProvider) { selectedProvider = activeProvider }
    LaunchedEffect(geminiKey) { tempGemini = geminiKey }
    LaunchedEffect(claudeKey) { tempClaude = claudeKey }
    LaunchedEffect(groqKey) { tempGroq = groqKey }
    LaunchedEffect(grokKey) { tempGrok = grokKey }

    var geminiVisible by remember { mutableStateOf(false) }
    var claudeVisible by remember { mutableStateOf(false) }
    var groqVisible by remember { mutableStateOf(false) }
    var grokVisible by remember { mutableStateOf(false) }
    var showSessionLogoutDialog by remember { mutableStateOf(false) }
    var showClearKeysDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showSessionLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showSessionLogoutDialog = false },
            title = {
                Text(
                    text = "End Research Session?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = "Logging out ends the active research session. All your configured API keys and offline word studies remain securely preserved in persistent storage on your phone.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSessionLogoutDialog = false
                        Toast.makeText(context, "Session ended. Your API keys remain safely stored.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Log Out (Keep Keys)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSessionLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF161B27)
        )
    }

    if (showClearKeysDialog) {
        AlertDialog(
            onDismissRequest = { showClearKeysDialog = false },
            title = {
                Text(
                    text = "Permanently Delete Saved Keys?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = "This will erase all saved API keys from your device's persistent storage and SQLite database. Next time you use AI features, you will need to re-enter them.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearKeysDialog = false
                        onClearKeys()
                        Toast.makeText(context, "Saved API keys erased.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Keys", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearKeysDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF161B27)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "API CREDENTIALS & PROVIDERS",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select your preferred AI engine and configure your personal API keys below.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Persistent Storage & Auto-save Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F1E36)
            ),
            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PERSISTENT DEVICE STORAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Auto-save active: Keys are saved to local SQLite & device storage and persist through app close, restart, and phone reboots.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Real-Time Google Search Grounding Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0C1929)
            ),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Grounding",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Google Search Grounding",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE WEB",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "Access live Google Search data, ancient inscriptions, and verified archaeological discoveries in real-time.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = useSearchGrounding,
                    onCheckedChange = { onToggleSearchGrounding(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2563EB),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Source Authenticity & Trustworthiness Verifier Card
        var showVerificationInfoDialog by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0C241D)
            ),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Source Authenticity Verifier",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Source Authenticity & Trust Gate",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE FILTER",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "Audits AI web citations against peer-reviewed journals & classical epigraphy. Low-trust forums & opinion blogs are automatically excluded.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = strictVerification,
                        onCheckedChange = { onToggleStrictVerification(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (strictVerification) "✓ Strict peer-reviewed consensus mode active" else "⚠️ Relaxed filter: All web citations shown",
                        fontSize = 10.sp,
                        color = if (strictVerification) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "How Verification Works ›",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showVerificationInfoDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (showVerificationInfoDialog) {
            AlertDialog(
                onDismissRequest = { showVerificationInfoDialog = false },
                containerColor = Color(0xFF0F172A),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Verification Policy",
                        tint = Color(0xFF10B981)
                    )
                },
                title = {
                    Text(
                        text = "Underlying Verification Architecture",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "To guarantee high-quality, truthful historical insights, our engine evaluates all sources prior to presentation:",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Primary Inscriptions (100-98%): Epigraphical databases (CIL, Packard Humanities, Perseus) and classical historians (Josephus, Tacitus).\n\n" +
                                    "• Academic University Consensus (98-95%): Peer-reviewed publications (Cambridge, Oxford, Harvard, JSTOR, Brill).\n\n" +
                                    "• Authoritative Lexicons (96-93%): Critical Greek and Hebrew apparatuses (BDAG, LSJ, Strong's, StepBible).\n\n" +
                                    "• Auto-Exclusion Filter (0-45%): Unreviewed personal blogs, Reddit, Quora, and commercial clickbait are stripped out.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVerificationInfoDialog = false }) {
                        Text("Close", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Provider cards
        listOf(
            Triple("Gemini", "Google", "gemini-3.5-flash (Fast & Stable)"),
            Triple("Claude", "Anthropic", "claude-3-5-sonnet (Scholarly Context)"),
            Triple("Groq", "Groq Labs", "openai/gpt-oss-120b (Ultrafast OSS)"),
            Triple("Grok", "xAI (X)", "grok-2-1212 (Cutting-edge Reasoner)")
        ).forEach { (id, brand, desc) ->
            val isSelected = selectedProvider == id
            val hasKey = when (id) {
                "Gemini" -> tempGemini.isNotBlank()
                "Claude" -> tempClaude.isNotBlank()
                "Groq" -> tempGroq.isNotBlank()
                "Grok" -> tempGrok.isNotBlank()
                else -> false
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedProvider = id
                        onSaveSettings(id, tempGemini, tempClaude, tempGroq, tempGrok)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            selectedProvider = id
                            onSaveSettings(id, tempGemini, tempClaude, tempGroq, tempGrok)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = brand,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = id.uppercase(),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    if (hasKey) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Key Present",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Key Missing",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CONFIGURE KEYS",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Gemini input
        OutlinedTextField(
            value = tempGemini,
            onValueChange = {
                tempGemini = it
                onSaveSettings(selectedProvider, it, tempClaude, tempGroq, tempGrok)
            },
            label = { Text("Gemini API Key", color = Color(0xFF8B949E)) },
            placeholder = { Text("AIzaSy...", color = Color(0xFF8B949E).copy(alpha = 0.6f)) },
            singleLine = true,
            visualTransformation = if (geminiVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { geminiVisible = !geminiVisible }) {
                    Icon(
                        imageVector = if (geminiVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle key visibility",
                        tint = Color(0xFF8B949E)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161B27),
                unfocusedContainerColor = Color(0xFF161B27),
                disabledContainerColor = Color(0xFF161B27),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF1F293D),
                focusedLabelColor = Color(0xFF38BDF8),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Claude input
        OutlinedTextField(
            value = tempClaude,
            onValueChange = {
                tempClaude = it
                onSaveSettings(selectedProvider, tempGemini, it, tempGroq, tempGrok)
            },
            label = { Text("Claude API Key", color = Color(0xFF8B949E)) },
            placeholder = { Text("sk-ant-...", color = Color(0xFF8B949E).copy(alpha = 0.6f)) },
            singleLine = true,
            visualTransformation = if (claudeVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { claudeVisible = !claudeVisible }) {
                    Icon(
                        imageVector = if (claudeVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle key visibility",
                        tint = Color(0xFF8B949E)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161B27),
                unfocusedContainerColor = Color(0xFF161B27),
                disabledContainerColor = Color(0xFF161B27),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFF38BDF8),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Groq input
        OutlinedTextField(
            value = tempGroq,
            onValueChange = {
                tempGroq = it
                onSaveSettings(selectedProvider, tempGemini, tempClaude, it, tempGrok)
            },
            label = { Text("Groq API Key", color = Color(0xFF8B949E)) },
            placeholder = { Text("gsk_...", color = Color(0xFF8B949E).copy(alpha = 0.6f)) },
            singleLine = true,
            visualTransformation = if (groqVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { groqVisible = !groqVisible }) {
                    Icon(
                        imageVector = if (groqVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle key visibility",
                        tint = Color(0xFF8B949E)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161B27),
                unfocusedContainerColor = Color(0xFF161B27),
                disabledContainerColor = Color(0xFF161B27),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFF38BDF8),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Grok input
        OutlinedTextField(
            value = tempGrok,
            onValueChange = {
                tempGrok = it
                onSaveSettings(selectedProvider, tempGemini, tempClaude, tempGroq, it)
            },
            label = { Text("Grok (xAI) API Key", color = Color(0xFF8B949E)) },
            placeholder = { Text("xai-...", color = Color(0xFF8B949E).copy(alpha = 0.6f)) },
            singleLine = true,
            visualTransformation = if (grokVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { grokVisible = !grokVisible }) {
                    Icon(
                        imageVector = if (grokVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle key visibility",
                        tint = Color(0xFF8B949E)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161B27),
                unfocusedContainerColor = Color(0xFF161B27),
                disabledContainerColor = Color(0xFF161B27),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFF38BDF8),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button with Premium Glowing Blue Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF60A5FA),
                            Color(0xFF2563EB)
                        )
                    )
                )
                .clickable(
                    onClick = {
                        onSaveSettings(selectedProvider, tempGemini, tempClaude, tempGroq, tempGrok)
                        Toast.makeText(context, "API settings securely saved to persistent storage!", Toast.LENGTH_SHORT).show()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save settings",
                    tint = Color(0xFF0D1117)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save API Settings",
                    color = Color(0xFF0D1117),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Session & Logout Action (Safe: keeps API keys safely saved on device)
        OutlinedButton(
            onClick = { showSessionLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF60A5FA)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Log out of session",
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF60A5FA)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out of Session (Keep Keys Saved)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF60A5FA)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Explicit Clear Keys Action (with prominent warning)
        TextButton(
            onClick = { showClearKeysDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete stored API keys",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFEF4444).copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Delete Stored API Keys From Phone",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFEF4444).copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "WHERE TO GET API KEYS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val bulletPoints = listOf(
                    "Gemini key: Get from Google AI Studio Secrets",
                    "Claude key: Get from Anthropic Developer Console",
                    "Groq key: Get from Groq Console (usually offers a free tier!)",
                    "Grok key: Get from xAI Console"
                )
                bulletPoints.forEach { pt ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(pt, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PhilologicalChatBubbleText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(text) { text.lines() }
    val contentBlocks = remember(text) {
        val blocks = mutableListOf<Pair<Boolean, String>>() // true = isTable, false = isText
        val tableLines = mutableListOf<String>()
        var inTable = false
        val currentText = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                if (!inTable) {
                    if (currentText.isNotBlank()) {
                        blocks.add(false to currentText.toString().trim())
                        currentText.clear()
                    }
                    inTable = true
                }
                tableLines.add(line)
            } else {
                if (inTable) {
                    if (tableLines.size >= 3) {
                        blocks.add(true to tableLines.joinToString("\n"))
                    } else {
                        currentText.append(tableLines.joinToString("\n")).append("\n")
                    }
                    tableLines.clear()
                    inTable = false
                }
                currentText.append(line).append("\n")
            }
        }
        if (inTable && tableLines.size >= 3) {
            blocks.add(true to tableLines.joinToString("\n"))
        } else if (currentText.isNotBlank()) {
            blocks.add(false to currentText.toString().trim())
        }
        blocks
    }

    if (contentBlocks.isEmpty()) {
        Text(
            text = text,
            color = Color(0xFFF0F6FC),
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            fontFamily = FontFamily.Serif,
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        contentBlocks.forEach { (isTable, block) ->
            if (isTable) {
                val parsedRows = remember(block) { PhilologyFamilyHelper.parseMarkdownTable(block) }
                if (parsedRows.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column {
                            // Table header row
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFF21262D))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PART OF SPEECH",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.width(115.dp)
                                )
                                Text(
                                    text = "ORIGINAL TERM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.width(115.dp)
                                )
                                Text(
                                    text = "TRANSLITERATION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.width(115.dp)
                                )
                                Text(
                                    text = "ENGLISH TRANSLATION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B949E),
                                    modifier = Modifier.width(140.dp)
                                )
                            }

                            // Data rows
                            parsedRows.forEachIndexed { idx, row ->
                                val rowBg = if (idx % 2 == 0) Color(0xFF0D1117) else Color(0xFF161B27)
                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val chipColor = when {
                                        row.partOfSpeech.contains("Noun", ignoreCase = true) -> Color(0xFF38BDF8)
                                        row.partOfSpeech.contains("Verb", ignoreCase = true) -> Color(0xFFF59E0B)
                                        row.partOfSpeech.contains("Adj", ignoreCase = true) -> Color(0xFFA78BFA)
                                        else -> Color(0xFF34D399)
                                    }
                                    Text(
                                        text = row.partOfSpeech,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipColor,
                                        modifier = Modifier.width(115.dp)
                                    )
                                    Text(
                                        text = row.originalTerm,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC9A84C),
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.width(115.dp)
                                    )
                                    Text(
                                        text = row.transliteration,
                                        fontSize = 10.5.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF8B949E),
                                        modifier = Modifier.width(115.dp)
                                    )
                                    Text(
                                        text = row.englishTranslation,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFF0F6FC),
                                        lineHeight = 15.sp,
                                        modifier = Modifier.width(140.dp)
                                    )
                                }
                                if (idx < parsedRows.lastIndex) {
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
                } else {
                    Text(
                        text = block,
                        color = Color(0xFFF0F6FC),
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            } else {
                Text(
                    text = block,
                    color = Color(0xFFF0F6FC),
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordStudyChatView(
    study: WordStudy? = null,
    chatMessages: List<ChatMessage>,
    chatLoading: Boolean,
    chatError: String?,
    useSearchGrounding: Boolean = true,
    onToggleSearchGrounding: (Boolean) -> Unit = {},
    strictVerification: Boolean = true,
    onToggleStrictVerification: (Boolean) -> Unit = {},
    onSendMessage: (String) -> Unit,
    onResetChat: () -> Unit = {},
    onClose: () -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var showChatMethodologyDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    BackHandler(enabled = true) {
        onClose()
    }

    LaunchedEffect(chatMessages.size, chatLoading) {
        if (chatMessages.isNotEmpty()) {
            delay(120)
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val sendMessageAction: () -> Unit = {
        if (textState.trim().isNotEmpty() && !chatLoading) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onSendMessage(textState.trim())
            textState = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Clean, unobtrusive header bar focused on reading
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .border(width = 1.dp, color = Color(0xFF21262D))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFC9A84C),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXPERT BIBLICAL PHILOLOGIST",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC9A84C),
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (study != null) "${study.word.uppercase()} (${study.transliteration})" else "Koine Greek & Biblical Hebrew",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Serif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Sleek discrete action buttons: Info & Reset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showChatMethodologyDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Grounding Settings & Info",
                            tint = if (useSearchGrounding) Color(0xFF10B981) else Color(0xFF94A3B8),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetChat,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Restart Chat",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            if (showChatMethodologyDialog) {
                AlertDialog(
                    onDismissRequest = { showChatMethodologyDialog = false },
                    containerColor = Color(0xFF0F172A),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Authenticity Verification",
                            tint = Color(0xFF10B981)
                        )
                    },
                    title = {
                        Text(
                            text = "Search Grounding & Verification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Serif
                        )
                    },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            // In-dialog toggle so it doesn't clutter chat screen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Live Search Grounding",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (useSearchGrounding) "Active: Cross-referencing live academic data" else "Offline: Scholarly lexicons & internal corpus only",
                                        fontSize = 10.sp,
                                        color = if (useSearchGrounding) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }
                                Switch(
                                    checked = useSearchGrounding,
                                    onCheckedChange = { onToggleSearchGrounding(it) },
                                    modifier = Modifier.scale(0.75f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF10B981),
                                        uncheckedThumbColor = Color(0xFF64748B),
                                        uncheckedTrackColor = Color(0xFF1E293B)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Every fact and citation retrieved by the AI is audited prior to presentation:",
                                fontSize = 11.5.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Primary Archaeological Inscriptions (100-98%)\n" +
                                        "• Academic University Presses (98-95%)\n" +
                                        "• Standard Scholarly Lexicons (96-93%)\n" +
                                        "• Auto-Exclusion Filter: User forums, blogs, and opinions are actively blocked to safeguard truthful outputs.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showChatMethodologyDialog = false }) {
                            Text("Done", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Message List - Unobstructed reading area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.1f), RoundedCornerShape(percent = 50))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(percent = 50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (study != null) "Ask About '${study.word}'" else "Ask Any Historical or Biblical Question",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Serif
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (study != null) {
                                "Query the AI philologist to explore original lemmas, morphological families, and theological nuances of '${study.word}'."
                            } else {
                                "Ask any question about Greek/Hebrew root lemmas, ancient Roman law, Near Eastern archaeology, or comparative biblical words."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF8B949E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "SUGGESTED QUESTIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9A84C),
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val suggestions = if (study != null) {
                            listOf(
                                "What is the complete morphological word family of '${study.word}'?",
                                "What translation disconnect exists between English and the original lemma?",
                                "Can you map the noun, verb, and adjective forms in a comparative table?",
                                "What is the exhaustive historical and legal setting of '${study.word}'?"
                            )
                        } else {
                            listOf(
                                "Explain the morphological word family of 'dikaiosyne' (Righteousness / Justification).",
                                "What is the root lemma and translation disconnect of 'hagios' (Holy / Sanctify)?",
                                "Map out the comparative forms of 'pistis' (Faith / Believe / Faithful).",
                                "How did ancient Near Eastern covenant ceremonies differ from Roman contracts?"
                            )
                        }
                        
                        suggestions.forEach { suggestion ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onSendMessage(suggestion)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                                border = BorderStroke(1.dp, Color(0xFF21262D)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFC9A84C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFFF0F6FC),
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(chatMessages) { message ->
                            val isUser = message.role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                if (isUser) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .widthIn(max = 300.dp)
                                            .background(
                                                color = Color(0xFF1D4ED8),
                                                shape = RoundedCornerShape(
                                                    topStart = 14.dp,
                                                    topEnd = 14.dp,
                                                    bottomStart = 14.dp,
                                                    bottomEnd = 3.dp
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = message.text,
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            lineHeight = 20.sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(Color(0xFF111827), RoundedCornerShape(percent = 50))
                                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(percent = 50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF8B949E),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    // AI Response Card - 100% focused on prominent, readable text & tables
                                    var isSourcesExpanded by remember { mutableStateOf(false) }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(max = 640.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                                        border = BorderStroke(1.dp, Color(0xFF21262D)),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
                                    ) {
                                        Column {
                                            // Top card banner with Philologist title & Copy action
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF0D1117))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = Color(0xFFC9A84C),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "✦ BIBLICAL PHILOLOGIST",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFC9A84C),
                                                        letterSpacing = 0.8.sp
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(message.text))
                                                        Toast.makeText(context, "Response copied to clipboard", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.ContentCopy,
                                                        contentDescription = "Copy response",
                                                        tint = Color(0xFF8B949E),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(0.5.dp)
                                                    .background(Color(0xFF21262D))
                                            )

                                            // Main Response Text with Rich Table Rendering
                                            PhilologicalChatBubbleText(
                                                text = message.text,
                                                modifier = Modifier.padding(14.dp)
                                            )

                                            // Unobtrusive Footer: Verification & Citations
                                            if (message.isGrounded || message.sources.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(0.5.dp)
                                                        .background(Color(0xFF21262D))
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (message.isGrounded) {
                                                        val trustScore = message.verification?.overallTrustScore ?: 96
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.VerifiedUser,
                                                                contentDescription = "Verified Grounded",
                                                                tint = Color(0xFF10B981),
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Verified Grounded ($trustScore% Trust)",
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF10B981)
                                                            )
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.width(1.dp))
                                                    }

                                                    if (message.sources.isNotEmpty()) {
                                                        Text(
                                                            text = if (isSourcesExpanded) "Hide Sources ▲" else "${message.sources.size} Audited Sources ▼",
                                                            fontSize = 9.5.sp,
                                                            color = Color(0xFF38BDF8),
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .clickable { isSourcesExpanded = !isSourcesExpanded }
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                // Expandable sources list (only visible if user taps to expand)
                                                if (isSourcesExpanded && message.sources.isNotEmpty()) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFF0D131F))
                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        message.sources.forEach { source ->
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .clickable {
                                                                        if (source.url.isNotBlank() && source.url.startsWith("http")) {
                                                                            try { uriHandler.openUri(source.url) } catch (_: Exception) {}
                                                                        }
                                                                    }
                                                                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                                                                    .border(0.5.dp, Color(source.trustTier.badgeColorHex).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = source.title,
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = Color(0xFF60A5FA),
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .background(Color(source.trustTier.badgeColorHex).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = "${source.trustScore}%",
                                                                            fontSize = 8.5.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(source.trustTier.badgeColorHex)
                                                                        )
                                                                    }
                                                                }
                                                                if (source.domain.isNotBlank()) {
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text(
                                                                        text = source.domain,
                                                                        fontSize = 8.5.sp,
                                                                        color = Color(0xFF64748B),
                                                                        fontFamily = FontFamily.Monospace
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
                        
                        if (chatLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f), RoundedCornerShape(percent = 50))
                                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(percent = 50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                                        border = BorderStroke(1.dp, Color(0xFF21262D)),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(13.dp),
                                                color = Color(0xFF38BDF8),
                                                strokeWidth = 1.5.dp
                                            )
                                            Text(
                                                text = if (useSearchGrounding) "Analyzing root lemmas & Google Search citations..." else "Analyzing original Greek & Hebrew root lemmas...",
                                                color = Color(0xFF8B949E),
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (chatError != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x26EF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "ERROR:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = chatError,
                                            fontSize = 12.sp,
                                            color = Color(0xFFF0F6FC)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Row - Dismisses keyboard on submit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .border(width = 1.dp, color = Color(0xFF21262D))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = {
                        Text(
                            text = if (study != null) "Ask philological question on '${study.word}'..." else "Ask about root lemmas or biblical context...",
                            color = Color(0xFF8B949E),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSend = { sendMessageAction() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF080C14),
                        unfocusedContainerColor = Color(0xFF080C14),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF21262D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { sendMessageAction() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (textState.trim().isNotEmpty() && !chatLoading) Color(0xFF1D4ED8) else Color(0xFF161B22),
                            shape = RoundedCornerShape(percent = 50)
                        ),
                    enabled = textState.trim().isNotEmpty() && !chatLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (textState.trim().isNotEmpty() && !chatLoading) Color.White else Color(0xFF8B949E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
