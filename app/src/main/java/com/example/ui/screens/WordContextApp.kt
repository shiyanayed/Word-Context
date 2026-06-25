package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.model.WordStudy
import com.example.ui.SearchUiState
import com.example.ui.WordStudyViewModel
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

    val activeProvider by viewModel.activeProvider.collectAsState()
    val geminiKey by viewModel.geminiKey.collectAsState()
    val claudeKey by viewModel.claudeKey.collectAsState()
    val groqKey by viewModel.groqKey.collectAsState()
    val grokKey by viewModel.grokKey.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Hub, 1: History, 2: Favorites, 3: Settings

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "WORD CONTEXT",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Bible Study Logo",
                        modifier = Modifier.padding(start = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Revealing original 1st-century context", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = "Scholarly study Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xD90D1117),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color(0xFF21262D),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                tonalElevation = 0.dp
            ) {
                listOf(
                    Triple(0, Icons.Outlined.AutoAwesome, "Study Hub"),
                    Triple(1, Icons.Outlined.History, "History"),
                    Triple(2, Icons.Outlined.BookmarkBorder, "Saved"),
                    Triple(3, Icons.Default.Settings, "Settings")
                ).forEach { (tabIndex, iconVector, labelText) ->
                    val isSelected = activeTab == tabIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tabIndex },
                        icon = { 
                            Icon(
                                imageVector = iconVector, 
                                contentDescription = labelText,
                                tint = if (isSelected) Color(0xFFC9A84C) else Color(0xFF8B949E)
                            ) 
                        },
                        label = { 
                            Text(
                                text = labelText,
                                color = if (isSelected) Color(0xFFC9A84C) else Color(0xFF8B949E),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Serif
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF161B27)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (activeTab == 0) {
                // Header Image Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF161B27),
                                    Color(0xFF0D1117)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFC9A84C).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ancient_scroll_banner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.12f
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Historical Bible Study",
                            color = Color(0xFFC9A84C),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Revealing original Greek & Hebrew cultural meaning as understood by their first-century audience.",
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Word Search Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161B27)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF21262D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Study a Biblical Concept",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFC9A84C),
                            letterSpacing = 0.2.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // TextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            label = { Text("Enter Bible word (e.g. grace, covenant, adoption)", color = Color(0xFF8B949E)) },
                            placeholder = { Text("Search word...", color = Color(0xFF8B949E).copy(alpha = 0.6f)) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFFC9A84C)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8B949E))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0D1117),
                                unfocusedContainerColor = Color(0xFF0D1117),
                                disabledContainerColor = Color(0xFF0D1117),
                                focusedBorderColor = Color(0xFFC9A84C),
                                unfocusedBorderColor = Color(0xFF21262D),
                                focusedLabelColor = Color(0xFFC9A84C),
                                unfocusedLabelColor = Color(0xFF8B949E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Modern Segmented Control for Testament Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(Color(0xFF0D1117), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF21262D), shape = RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("New Testament", "Old Testament").forEach { testament ->
                                val isSelected = selectedTestament == testament
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            color = if (isSelected) Color(0xFFC9A84C) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.updateSelectedTestament(testament) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = testament.uppercase(),
                                        color = if (isSelected) Color(0xFF0D1117) else Color(0xFF8B949E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search Button with Premium Gold Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading) {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFE2C175),
                                                Color(0xFFC9A84C)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF21262D),
                                                Color(0xFF161B27)
                                            )
                                        )
                                    }
                                )
                                .clickable(
                                    enabled = searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading,
                                    onClick = { viewModel.performSearch() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Reveal",
                                    tint = if (searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading) Color(0xFF0D1117) else Color(0xFF8B949E),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reveal Historical Context",
                                    color = if (searchQuery.isNotBlank() && searchUiState !is SearchUiState.Loading) Color(0xFF0D1117) else Color(0xFF8B949E),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Curated Chips
                        Text(
                            text = "Curated Suggestions:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B949E),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val ntSuggestions = listOf("adoption", "grace", "redemption", "faith")
                        val otSuggestions = listOf("covenant", "lovingkindness", "redeemer", "righteousness")
                        val currentChips = if (selectedTestament == "New Testament") ntSuggestions else otSuggestions

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentChips.forEach { chipWord ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0D1117))
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF21262D),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.updateSearchQuery(chipWord)
                                            viewModel.performSearch()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = chipWord,
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
                        tts = tts
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
                        onSaveSettings = { provider, gemini, claude, groq, grok ->
                            viewModel.saveSettings(provider, gemini, claude, groq, grok)
                        }
                    )
                }
            }
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
    tts: android.speech.tts.TextToSpeech?
) {
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
                        tts = tts
                    )
                } else {
                    // Prominent scholarly empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Search Word",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Begin Your Study",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter a biblical word above to explore its original legal, cultural, and historical first-century meaning.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
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
                    tts = tts
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
                                    text = "To generate fresh AI studies for any Bible word, please enter your GEMINI_API_KEY in the Secrets panel on the left sidebar of Google AI Studio.\n\nAlternatively, you can study our curated pre-loaded words (like grace, covenant, adoption, redemption, or lovingkindness) completely offline without an API key!",
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
                    
                    Button(
                        onClick = onClearSearchState,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dismiss Error")
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
fun WordStudyDetailView(
    study: WordStudy,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onSearchWord: (String) -> Unit,
    tts: android.speech.tts.TextToSpeech?
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
    onSaveSettings: (provider: String, gemini: String, claude: String, groq: String, grok: String) -> Unit
) {
    var selectedProvider by remember { mutableStateOf(activeProvider) }
    var tempGemini by remember { mutableStateOf(geminiKey) }
    var tempClaude by remember { mutableStateOf(claudeKey) }
    var tempGroq by remember { mutableStateOf(groqKey) }
    var tempGrok by remember { mutableStateOf(grokKey) }

    var geminiVisible by remember { mutableStateOf(false) }
    var claudeVisible by remember { mutableStateOf(false) }
    var groqVisible by remember { mutableStateOf(false) }
    var grokVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
        Spacer(modifier = Modifier.height(16.dp))

        // Provider cards
        listOf(
            Triple("Gemini", "Google", "gemini-1.5-flash (Fast & Stable)"),
            Triple("Claude", "Anthropic", "claude-3-5-sonnet (Scholarly Context)"),
            Triple("Groq", "Groq Labs", "llama-3.3-70b (Ultrafast Llama)"),
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
                    .clickable { selectedProvider = id },
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
                        onClick = { selectedProvider = id }
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
            onValueChange = { tempGemini = it },
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
                focusedBorderColor = Color(0xFFC9A84C),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFFC9A84C),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Claude input
        OutlinedTextField(
            value = tempClaude,
            onValueChange = { tempClaude = it },
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
                focusedBorderColor = Color(0xFFC9A84C),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFFC9A84C),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Groq input
        OutlinedTextField(
            value = tempGroq,
            onValueChange = { tempGroq = it },
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
                focusedBorderColor = Color(0xFFC9A84C),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFFC9A84C),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        // Grok input
        OutlinedTextField(
            value = tempGrok,
            onValueChange = { tempGrok = it },
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
                focusedBorderColor = Color(0xFFC9A84C),
                unfocusedBorderColor = Color(0xFF21262D),
                focusedLabelColor = Color(0xFFC9A84C),
                unfocusedLabelColor = Color(0xFF8B949E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button with Premium Gold Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE2C175),
                            Color(0xFFC9A84C)
                        )
                    )
                )
                .clickable(
                    onClick = {
                        onSaveSettings(selectedProvider, tempGemini, tempClaude, tempGroq, tempGrok)
                        Toast.makeText(context, "API settings saved successfully!", Toast.LENGTH_SHORT).show()
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
