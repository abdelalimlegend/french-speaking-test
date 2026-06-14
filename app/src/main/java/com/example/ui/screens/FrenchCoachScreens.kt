package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GrammarCorrectionItem
import com.example.data.PresetTopic
import com.example.data.PronunciationFeedbackItem
import com.example.data.SessionHistory
import com.example.data.Topic
import com.example.data.VocabularyCard
import com.example.data.VocabularySuggestionItem
import com.example.data.GeminiClient
import com.example.ui.AppScreen
import com.example.ui.FrenchCoachViewModel
import com.example.ui.SessionPhase
import com.squareup.moshi.Moshi
import java.text.SimpleDateFormat
import java.util.*

// --- Cohesive Styling Palettes ---
val PremiumLightGrad = Brush.verticalGradient(
    colors = listOf(Color(0xFFF8FAFF), Color(0xFFF1F5F9), Color(0xFFFFFFFF))
)

val PrimaryDuolingoGreen = Color(0xFF4F46E5) // Sleek Indigo Primary
val secondaryFrenchBlue = Color(0xFF0EA5E9) // Sleek Sky/Cyan Blue Secondary
val accentOrange = Color(0xFFF59E0B) // Sleek Amber Accent
val textDarkColor = Color(0xFF0F172A) // Sleek Slate 900
val textLightMuted = Color(0xFF64748B) // Sleek Slate 500
val luxuryDarkBg = Color(0xFF0B0F19) // Sleek dark bg
val luxuryDarkCard = Color(0xFF1E293B) // Sleek Slate 800 Card

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigationContainer(
    viewModel: FrenchCoachViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) with fadeOut(animationSpec = tween(250))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> SplashScreen()
                AppScreen.AUTHENTICATION -> AuthenticationScreen(viewModel)
                AppScreen.HOME_DASHBOARD -> HomeDashboardScreen(viewModel)
                AppScreen.SPEAKING_SESSION -> SpeakingSessionScreen(viewModel)
                AppScreen.RECORDING_REVIEW -> RecordingReviewScreen(viewModel)
                AppScreen.AI_FEEDBACK_REPORT -> AiFeedbackReportScreen(viewModel)
                AppScreen.PROGRESS_ANALYTICS -> ProgressAnalyticsScreen(viewModel)
                AppScreen.ACHIEVEMENTS_FLASHCARDS -> AchievementsAndFlashcardsScreen(viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }
}

// --- SCREEN 1: SPLASH SCREEN ---
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(colors = listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Clara the French Coach avatar container
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .background(Color.White, CircleShape)
                    .border(4.dp, PrimaryDuolingoGreen, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // High fidelity vector-drawn French rooster + speech emblem
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Speech bubble outline
                    drawArc(
                        color = secondaryFrenchBlue,
                        startAngle = 180f,
                        sweepAngle = 280f,
                        useCenter = true,
                        size = Size(w * 0.9f, h * 0.7f),
                        topLeft = Offset(w * 0.05f, h * 0.05f)
                    )
                    // French Rooster crest
                    drawPath(
                        path = Path().apply {
                            moveTo(w * 0.5f, h * 0.15f)
                            quadraticTo(w * 0.52f, h * 0.05f, w * 0.55f, h * 0.15f)
                            quadraticTo(w * 0.62f, h * 0.05f, w * 0.64f, h * 0.2f)
                        },
                        color = Color.Red
                    )
                    // Rooster beak
                    drawPath(
                        path = Path().apply {
                            moveTo(w * 0.35f, h * 0.45f)
                            lineTo(w * 0.22f, h * 0.52f)
                            lineTo(w * 0.35f, h * 0.58f)
                            close()
                        },
                        color = Color(0xFFFFA000)
                    )
                    // Eyes
                    drawCircle(Color.Black, radius = 5f, center = Offset(w * 0.45f, h * 0.42f))
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "Entraîneur de Français",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF4F46E5),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Coach de Conversation Français",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = textLightMuted,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = PrimaryDuolingoGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp).testTag("splash_loader")
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Bonjour ! Chargement des 100 sujets...",
                fontSize = 13.sp,
                color = PrimaryDuolingoGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// --- SCREEN 2: AUTHENTICATION SCREEN ---
@Composable
fun AuthenticationScreen(viewModel: FrenchCoachViewModel) {
    var emailState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var nameState by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumLightGrad)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .widthIn(max = 480.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Duo-Rooster Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PrimaryDuolingoGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.RecordVoiceOver,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "French Speaking Coach",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textDarkColor
            )
            
            Text(
                text = "Progressez à l'oral chaque jour grâce à l'IA",
                fontSize = 14.sp,
                color = textLightMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Commencez votre apprentissage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDarkColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Votre Nom") },
                        leadingIcon = { Icon(Icons.Rounded.Person, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emailState,
                        onValueChange = { emailState = it },
                        label = { Text("Adresse Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("email_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = passwordState,
                        onValueChange = { passwordState = it },
                        label = { Text("Mot de Passe") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("password_field"),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.login(emailState, nameState) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDuolingoGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Créer un Compte", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OU", modifier = Modifier.padding(horizontal = 16.dp), color = textLightMuted, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Google & Apple Social Simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.login("google@french.com", "Google Student") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.AccountCircle, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google", fontSize = 13.sp)
                }
                
                OutlinedButton(
                    onClick = { viewModel.login("apple@french.com", "Apple Learner") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.Star, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apple", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guest Login trigger
            TextButton(
                onClick = { viewModel.login("invit@frenchcoach.local", "Practice Guest") },
                modifier = Modifier.testTag("guest_button")
            ) {
                Text(
                    text = "Pratiquer en tant qu'Invité Clara 🇫🇷",
                    color = secondaryFrenchBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// --- SCREEN 3: HOME DASHBOARD SCREEN ---
@Composable
fun HomeDashboardScreen(viewModel: FrenchCoachViewModel) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val username by viewModel.userDisplayName.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQ by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredTopics by viewModel.filteredTopics.collectAsStateWithLifecycle()
    val activeTopic by viewModel.activeTopic.collectAsStateWithLifecycle()
    
    var showCustomTopicSheet by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Daily Life", "Education", "Travel", "Work & Career", 
        "Technology", "Environment", "Culture", "Society", "Health", 
        "Personal Experiences", "Debate Topics", "Future & Innovation"
    )

    Scaffold(
        bottomBar = { BottomNavBar(activeScreen = AppScreen.HOME_DASHBOARD, onNavigate = { viewModel.navigateTo(it) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PremiumLightGrad)
        ) {
            // Dashboard Header (Profile / Level up stats)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Avatar with dynamic initials and gradient
                    val initials = if (username.isNotBlank()) username.first().uppercase() else "A"
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFF60A5FA))
                                ),
                                shape = CircleShape
                            )
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = "NIVEAU B${minOf(2, progress.level)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDuolingoGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Bonjour, $username !",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDarkColor
                        )
                    }
                }

                // Flame Streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progress.streak}",
                                fontWeight = FontWeight.Bold,
                                color = accentOrange,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Level circle icon button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(secondaryFrenchBlue, CircleShape)
                            .clickable { viewModel.navigateTo(AppScreen.PROGRESS_ANALYTICS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("L${progress.level}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // XP Progress / Daily Goal Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "OBJECTIF QUOTIDIEN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textLightMuted,
                            letterSpacing = 1.sp
                        )
                        val nextLevelXP = 150
                        val currentXP = progress.xp % nextLevelXP
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "$currentXP /$nextLevelXP",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = textDarkColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "XP",
                                fontSize = 12.sp,
                                color = textLightMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(52.dp)
                    ) {
                        val nextLevelXP = 150
                        val currentXP = progress.xp % nextLevelXP
                        val progressFraction = currentXP / nextLevelXP.toFloat()
                        
                        CircularProgressIndicator(
                            progress = { 1.0f },
                            color = Color(0xFFF1F5F9),
                            strokeWidth = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(
                            progress = { progressFraction },
                            color = PrimaryDuolingoGreen,
                            strokeWidth = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        val progressPercent = (progressFraction * 100).toInt()
                        Text(
                            text = "$progressPercent%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDarkColor
                        )
                    }
                }
            }

            // Citation du Jour (Daily Coach Quote)
            DailyFrenchQuoteWidget()

            // Filter Pills & Search Box
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Rechercher un sujet ou thème parmi 100+...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = textLightMuted) },
                    trailingIcon = {
                        if (searchQ.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Rounded.Close, null, tint = textLightMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("dashboard_search"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDuolingoGreen,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable pill row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(categories) { categoryName ->
                        val isSelected = selectedCat == categoryName
                        val color = if (isSelected) PrimaryDuolingoGreen else Color.White
                        val textColor = if (isSelected) Color.White else textDarkColor
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(color)
                                .border(1.dp, if (isSelected) PrimaryDuolingoGreen else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectCategory(categoryName) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(categoryName, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick generate topic panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Générateur de Sujet IA", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F46E5))
                        Text("Générez un sujet sur mesure avec Clara", fontSize = 12.sp, color = textLightMuted)
                    }
                    Button(
                        onClick = { showCustomTopicSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Générer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Topics list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (filteredTopics.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                        ) {
                            Icon(Icons.Rounded.HourglassEmpty, null, modifier = Modifier.size(48.dp), tint = textLightMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Aucun sujet trouvé.", fontWeight = FontWeight.Bold, color = textDarkColor)
                            Text("Veuillez modifier votre filtre ou recherche.", fontSize = 12.sp, color = textLightMuted)
                        }
                    }
                } else {
                    items(filteredTopics) { topic ->
                        TopicItemCard(
                            topic = topic,
                            onStart = { viewModel.selectTopicForSession(topic) },
                            onToggleFav = { viewModel.toggleFavoriteTopic(topic) }
                        )
                    }
                }
            }
        }

        // Custom topic generator modal builder
        if (showCustomTopicSheet) {
            CustomTopicGeneratorDialog(
                viewModel = viewModel,
                onDismiss = { showCustomTopicSheet = false }
            )
        }
    }
}

@Composable
fun DailyFrenchQuoteWidget() {
    var quoteIndex by remember { mutableStateOf(0) }
    val quotes = listOf(
        Pair("Le voyage de mille lieues commence par un pas.", "The journey of a thousand miles begins with a step."),
        Pair("L'éducation est l'arme la plus puissante pour changer le monde.", "Education is the most powerful weapon to change the world."),
        Pair("Petit à petit, l'oiseau fait son nid.", "Little by little, the bird builds its nest."),
        Pair("Vouloir, c'est pouvoir.", "Where there's a will, there's a way."),
        Pair("Mieux vaut tard que jamais.", "Better late than never.")
    )
    val quote = quotes[quoteIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇫🇷 CITATION DU JOUR", fontSize = 11.sp, color = secondaryFrenchBlue, fontWeight = FontWeight.Black)
                IconButton(
                    onClick = { quoteIndex = (quoteIndex + 1) % quotes.size },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, tint = secondaryFrenchBlue, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "« ${quote.first} »",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textDarkColor,
                fontFamily = FontFamily.Serif
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = quote.second,
                fontSize = 13.sp,
                color = textLightMuted,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun TopicItemCard(
    topic: Topic,
    onStart: () -> Unit,
    onToggleFav: () -> Unit
) {
    val difficultyColor = when (topic.difficulty) {
        "Easy" -> Color(0xFF4CAF50)
        "Medium" -> Color(0xFFFF9800)
        "Hard" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("topic_card_${topic.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F4FD))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(topic.category, color = secondaryFrenchBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Level Indicator label
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(difficultyColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(topic.difficulty, color = difficultyColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    if (topic.isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3E5F5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("IA PERSO", color = Color(0xFF8E24AA), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = topic.frenchTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDarkColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = topic.englishTranslation,
                    fontSize = 13.sp,
                    color = textLightMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action section (Favorite Star / Start play button)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector = if (topic.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = "Favori",
                        tint = if (topic.isFavorite) accentOrange else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryDuolingoGreen, CircleShape)
                        .clickable(onClick = onStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                }
            }
        }
    }
}

// Dialog: Generate tailored custom topic via AI settings
@Composable
fun CustomTopicGeneratorDialog(
    viewModel: FrenchCoachViewModel,
    onDismiss: () -> Unit
) {
    val category by viewModel.customTopicCategory.collectAsStateWithLifecycle()
    val difficulty by viewModel.customTopicDifficulty.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingTopic.collectAsStateWithLifecycle()

    var manualFrenchTitle by remember { mutableStateOf("") }
    var manualEnglishTitle by remember { mutableStateOf("") }
    var manualQuestions by remember { mutableStateOf("") }

    val categories = listOf("Daily Life", "Education", "Travel", "Work & Career", "Technology", "Environment", "Culture", "Health", "Debate Topics")
    val levelOptions = listOf("Easy", "Medium", "Hard")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Générer un Sujet personnel", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isGenerating) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryDuolingoGreen)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Clara génère votre sujet...", fontWeight = FontWeight.Bold, color = textDarkColor)
                        Text("En cours d'appel à l'API Gemini...", fontSize = 12.sp, color = textLightMuted)
                    }
                } else {
                    Text("Laissez notre IA Clara composer un sujet d'examen ou ajoutez manuellement.", fontSize = 13.sp, color = textLightMuted)

                    // Pick Category
                    Column {
                        Text("Catégorie", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val active = category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (active) secondaryFrenchBlue else Color(0xFFF5F5F5))
                                        .clickable { viewModel.setGeneratorConfigs(cat, difficulty) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(cat, color = if (active) Color.White else textDarkColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Pick CEFR difficulty
                    Column {
                        Text("Difficulté cible", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            levelOptions.forEach { lvl ->
                                val active = difficulty == lvl
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) accentOrange else Color(0xFFF5F5F5))
                                        .clickable { viewModel.setGeneratorConfigs(category, lvl) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lvl, color = if (active) Color.White else textDarkColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Mode Manuel (optionnel)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = manualFrenchTitle,
                        onValueChange = { manualFrenchTitle = it },
                        label = { Text("Titre du sujet (en français)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = manualEnglishTitle,
                        onValueChange = { manualEnglishTitle = it },
                        label = { Text("English translation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!isGenerating) {
                Button(
                    onClick = {
                        if (manualFrenchTitle.isNotBlank()) {
                            viewModel.addCustomTopic(
                                french = manualFrenchTitle,
                                english = manualEnglishTitle,
                                category = category,
                                difficulty = difficulty,
                                questions = "Pourquoi aimez-vous cela ?\nPrésentez des exemples pertinents."
                            )
                            onDismiss()
                        } else {
                            viewModel.generateAIPoweredTopic()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDuolingoGreen)
                ) {
                    Text("Créer le sujet")
                }
            }
        },
        dismissButton = {
            if (!isGenerating) {
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// --- SCREEN 4: SPEAKING SESSION SCREEN ---
@Composable
fun SpeakingSessionScreen(viewModel: FrenchCoachViewModel) {
    val topic by viewModel.activeTopic.collectAsStateWithLifecycle()
    val phase by viewModel.sessionPhase.collectAsStateWithLifecycle()
    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val prepNotes by viewModel.prepNotes.collectAsStateWithLifecycle()
    val rawFeedbackMockText by viewModel.recordingTextMock.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val soundAmplitude by viewModel.liveAudioAmplitude.collectAsStateWithLifecycle()

    var showEnglishToggle by remember { mutableStateOf(false) }

    val currentTopic = topic
    if (currentTopic == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header back + exit warning
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    viewModel.pauseTimer()
                    viewModel.navigateTo(AppScreen.HOME_DASHBOARD)
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Retour")
                }
                
                // Session phase label
                Text(
                    text = if (phase == SessionPhase.PREPARATION) "PHASE : PRÉPARATION" else "PHASE : ENREGISTREMENT",
                    fontWeight = FontWeight.Black,
                    color = if (phase == SessionPhase.PREPARATION) accentOrange else Color.Red,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                // Info icon helper
                Icon(Icons.AutoMirrored.Rounded.HelpOutline, "Aide", tint = textLightMuted)
            }

            // Big Clock Countdown Timer
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        if (phase == SessionPhase.PREPARATION) Color(0xFFEEF2FF) else Color(0xFFFEE2E2),
                        CircleShape
                    ).border(
                        2.5.dp,
                        if (phase == SessionPhase.PREPARATION) PrimaryDuolingoGreen else Color(0xFFEF4444),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(timeRemaining),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (phase == SessionPhase.PREPARATION) PrimaryDuolingoGreen else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Topic Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTopic.category.uppercase(),
                            fontSize = 10.sp,
                            color = secondaryFrenchBlue,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Traduction", fontSize = 11.sp, color = textLightMuted, modifier = Modifier.padding(end = 6.dp))
                            Switch(
                                checked = showEnglishToggle,
                                onCheckedChange = { showEnglishToggle = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = secondaryFrenchBlue),
                                modifier = Modifier.scale(0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentTopic.frenchTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = textDarkColor,
                        lineHeight = 24.sp
                    )

                    if (showEnglishToggle) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentTopic.englishTranslation,
                            fontSize = 14.sp,
                            color = textLightMuted,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Guiding questions toggle block
                    Text("🧠 DÉGAGER VOS IDÉES (GUIDES) :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textLightMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    currentTopic.guidingQuestions.split("\n").forEach { question ->
                        if (question.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Rounded.ChevronRight, null, tint = PrimaryDuolingoGreen, modifier = Modifier.size(16.dp))
                                Text(question, fontSize = 12.sp, color = textDarkColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic view depends on phase: PREP or SPEAKING
            if (phase == SessionPhase.PREPARATION) {
                // Notepad Workspace Area
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📒 Brouillon / Vos Notes de Préparation :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textLightMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = prepNotes,
                        onValueChange = { viewModel.updatePrepNotes(it) },
                        placeholder = { Text("Tapez vos mots-clés, des verbes conjugués (ex: Passe composé, subjonctif) pour vous rassurer...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("session_prep_notes"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDuolingoGreen,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.skipPrepToSpeaking() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDuolingoGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("commencer_parler_btn"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Mic, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Commencer à Parler !", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Speaking Phase visual screen
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎤 Clara enregistre votre voix en direct...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Audio Pulse Waveform simulator visualizer panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing moving sound wave amplitude poles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val barCount = 18
                            val barSpacing = 12.dp.toPx()
                            val totalSpacingOfB = barSpacing * (barCount - 1)
                            val barWidth = 6.dp.toPx()
                            val leftPad = (w - (barCount * barWidth + totalSpacingOfB)) / 2
                            
                            for (i in 0 until barCount) {
                                // Dynamic formula based on model sound amplitude state
                                val scaleFactor = if (isRecording) {
                                    val localPuck = Math.sin((i.toDouble() / barCount) * Math.PI).toFloat()
                                    localPuck * soundAmplitude * ((40..110).random() / 100f)
                                } else {
                                    0.1f
                                }
                                val barHeight = (h * 0.7f * scaleFactor).coerceAtLeast(6f)
                                val x = leftPad + i * (barWidth + barSpacing)
                                val y = (h - barHeight) / 2
                                
                                drawRoundRect(
                                    color = if (isRecording) PrimaryDuolingoGreen else Color(0xFFE2E8F0),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(2.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Éditeur de transcription (ou dictée vocale) :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textLightMuted,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = rawFeedbackMockText,
                        onValueChange = { viewModel.updateRecordingText(it) },
                        placeholder = { Text("Parlez ou modifiez votre transcription...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("speaking_transcript_editor"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryDuolingoGreen,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated "speech generator assistant tool" to help user
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateRecordingText(
                                    "Bonjour Clara, à mon avis, j'ai allé à l'école le matin pour parler français avec des camarades mais je suis un peu timide."
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Rounded.AutoFixNormal, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aide-moi (Erreurs)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.finishSpeakingAndReview() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("stop_analyse_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Stop, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Terminer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return String.format("%02d:%02d", m, s)
}

// --- SCREEN 5: RECORDING REVIEW SCREEN ---
@Composable
fun RecordingReviewScreen(viewModel: FrenchCoachViewModel) {
    val topic by viewModel.activeTopic.collectAsStateWithLifecycle()
    val transcript by viewModel.recordingTextMock.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val aiError by viewModel.aiErrorResponse.collectAsStateWithLifecycle()

    val currentTopic = topic
    if (currentTopic == null) return

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PremiumLightGrad),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SPEAKING_SESSION) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                    Text("Révision & Analyse", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textDarkColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAnalyzing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDuolingoGreen, strokeWidth = 4.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Clara est en train d'écouter...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDarkColor
                        )
                        Text(
                            text = "Analyse de la voix, correction de grammaire, calcul du score de fluidité et estimation du niveau CECRL...",
                            fontSize = 13.sp,
                            color = textLightMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                                Text("🎧 AUDIO DE PRACTICE ENREGISTRÉ", fontSize = 11.sp, color = textLightMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.PlayCircle, null, tint = PrimaryDuolingoGreen, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("Session d'expression orale", fontWeight = FontWeight.Bold)
                                        Text("Topic: ${currentTopic.frenchTitle}", fontSize = 12.sp, color = textLightMuted, maxLines = 1)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Vérifier & Ajuster la Transcription :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        OutlinedTextField(
                            value = transcript,
                            onValueChange = { viewModel.updateRecordingText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("review_transcript_input")
                        )

                        if (aiError != null) {
                            Text(
                                text = "⚠️ Erreur: $aiError",
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Trigger analysis action
                        Button(
                            onClick = { viewModel.triggerAISpeakingCoachingAnalysis() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDuolingoGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("envoyer_analyse_btn"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lancer le Coaching IA Clara ⚡", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: AI FEEDBACK REPORT SCREEN ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiFeedbackReportScreen(viewModel: FrenchCoachViewModel) {
    val activeReview by viewModel.activeReviewSession.collectAsStateWithLifecycle()
    if (activeReview == null) return

    val moshi = GeminiClient.jsonParser
    
    // Parse json details safely or use defaults
    val correctionsList: List<GrammarCorrectionItem> = try {
        moshi.adapter(Array<GrammarCorrectionItem>::class.java)
            .fromJson(activeReview!!.grammarCorrectionJson)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val vocabSuggestions: List<VocabularySuggestionItem> = try {
        moshi.adapter(Array<VocabularySuggestionItem>::class.java)
            .fromJson(activeReview!!.vocabSuggestionsJson)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val pronunciationFeedback: List<PronunciationFeedbackItem> = try {
        moshi.adapter(Array<PronunciationFeedbackItem>::class.java)
            .fromJson(activeReview!!.pronunciationFeedbackJson)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val tipsList: List<String> = try {
        moshi.adapter(Array<String>::class.java)
            .fromJson(activeReview!!.improvementTipsJson)?.toList() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    Scaffold(
        bottomBar = { BottomNavBar(activeScreen = AppScreen.HOME_DASHBOARD, onNavigate = { viewModel.navigateTo(it) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PremiumLightGrad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rapport Coaching IA Clara", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDarkColor)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryDuolingoGreen, CircleShape)
                            .clickable { viewModel.navigateTo(AppScreen.HOME_DASHBOARD) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Home, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // High Fidelity CEFR Score Circle + Flame
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("feedback_score_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SCORE DE FLUIDITÉ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = textLightMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${activeReview!!.fluencyScore}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryDuolingoGreen
                                )
                                Text("/100", fontSize = 15.sp, color = textLightMuted, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            
                            // Achievement state message
                            Text(
                                text = when {
                                    activeReview!!.fluencyScore >= 85 -> "Excellent rythme ! 🇫🇷"
                                    activeReview!!.fluencyScore >= 70 -> "Bon travail ! Très compréhensible."
                                    else -> "À pratiquer pour gagner en aisance."
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDarkColor
                            )
                        }

                        // Large CEFR Badge
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    Brush.linearGradient(colors = listOf(secondaryFrenchBlue, Color(0xFF0077D6))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NIVEAU", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(activeReview!!.cefrLevel, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Reward Notification
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F8F5))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("XP Gagnés +35 XP !", fontWeight = FontWeight.Bold, color = Color(0xFF117F64))
                            Text("Votre série active quotidienne se poursuit !", fontSize = 12.sp, color = textLightMuted)
                        }
                    }
                }
            }

            // Text transcription correction diff view
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Chat, null, tint = secondaryFrenchBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Transcription Polie", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = activeReview!!.transcript,
                            fontSize = 14.sp,
                            color = textLightMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Transcription Corrigée par Clara :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDuolingoGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeReview!!.correctedTranscript,
                            fontSize = 14.sp,
                            color = textDarkColor,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Grammar Correction Highlight
            item {
                Text("🔴 GRAMMAIRE & STRUCTURES :", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textLightMuted)
            }

            if (correctionsList.isEmpty()) {
                item {
                    Text("Aucune erreur de grammaire détectée ! Superbe !", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
                }
            } else {
                items(correctionsList) { correction ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // original (danger red color light)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .padding(8.dp)
                            ) {
                                Text("Suggéré: " + correction.original, color = Color(0xFFC62828), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            // corrected (success green light)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(8.dp)
                            ) {
                                Text("Correction: " + correction.corrected, color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = correction.explanation,
                                fontSize = 12.sp,
                                color = textLightMuted,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }

            // Vocabulary Suggestions Upgrade Dictionary
            item {
                Text("🎨 VOS SUGGESTIONS VOCABULAIRE :", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textLightMuted)
            }

            items(vocabSuggestions) { vocab ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💡", fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    append("Remplacez ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) { append(vocab.originalWord) }
                                    append(" par ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF6A1B9A))) { append(vocab.suggestedAlternative) }
                                },
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Exemple : \"${vocab.usageExample}\"",
                                fontSize = 12.sp,
                                color = textLightMuted,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }

            // Pronunciation feedback cards
            item {
                Text("🗣️ CONSEILS PRONONCIATION :", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textLightMuted)
            }

            items(pronunciationFeedback) { audioTip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.VolumeUp, null, tint = accentOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(audioTip.word, fontWeight = FontWeight.Bold, color = textDarkColor, fontSize = 14.sp)
                        Text(audioTip.tip, fontSize = 12.sp, color = textLightMuted)
                    }
                }
            }

            // Improvement Tips list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFBC02D))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TipsAndUpdates, null, tint = Color(0xFFFBC02D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prochains Objectifs d'amélioration", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        tipsList.forEachIndexed { idx, tip ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Text("${idx + 1}.", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), modifier = Modifier.padding(end = 6.dp))
                                Text(tip, fontSize = 13.sp, color = textDarkColor)
                            }
                        }
                    }
                }
            }

            // CTA footer
            item {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.HOME_DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDuolingoGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Retourner au Tableau de Bord", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- SCREEN 7: PROGRESS ANALYTICS SCREEN ---
@Composable
fun ProgressAnalyticsScreen(viewModel: FrenchCoachViewModel) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { BottomNavBar(activeScreen = AppScreen.PROGRESS_ANALYTICS, onNavigate = { viewModel.navigateTo(it) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PremiumLightGrad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Mes Statistiques de Pratique", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDarkColor)
            }

            // Stats grid layout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        emoji = "🔥",
                        label = "Série Active",
                        value = "${progress.streak} Jours",
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "⏱️",
                        label = "Temps Oral",
                        value = "${progress.totalPracticeSeconds / 60} Min",
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        emoji = "📚",
                        label = "Sujets vus",
                        value = "${progress.completedTopicsCount}",
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "⚡",
                        label = "Total XP",
                        value = "${progress.xp} XP",
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Custom Chart Drawing using Compose Canvas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Activité de la semaine (Minutes)", fontWeight = FontWeight.Bold, color = textDarkColor, fontSize = 14.sp)
                        Text("Pratique orale quotidienne de français", fontSize = 11.sp, color = textLightMuted)
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        // Render Canvas
                        WeeklyPracticeChart()
                    }
                }
            }

            // Historical List of complete speaking practices
            item {
                Text("Historique de mes sessions", fontWeight = FontWeight.Black, fontSize = 15.sp, color = textDarkColor)
            }

            if (history.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text("Aucune session terminée d'oral pour l'instant.", color = textLightMuted, fontSize = 13.sp)
                        Text("Lancez une première session de pratique !", color = textLightMuted, fontSize = 11.sp)
                    }
                }
            } else {
                items(history) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // View historic report inside the UI feedback report layer
                                // We can cheat by setting current mock session back to this historic record
                                // and navigate to feedback screen
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.topicTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDarkColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(session.category, fontSize = 11.sp, color = secondaryFrenchBlue, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE).format(Date(session.timestamp)),
                                        fontSize = 11.sp,
                                        color = textLightMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${session.fluencyScore}/100", color = PrimaryDuolingoGreen, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE3F2FD))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(session.cefrLevel, color = secondaryFrenchBlue, fontWeight = FontWeight.Black, fontSize = 11.sp)
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
fun StatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, fontSize = 11.sp, color = textLightMuted, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textDarkColor)
        }
    }
}

@Composable
fun WeeklyPracticeChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        
        // Practice minutes for Mon-Sun
        val values = listOf(3f, 5f, 2f, 10f, 6f, 12f, 4f)
        val maxVal = 15f
        
        val barCount = 7
        val barWidth = 32.dp.toPx()
        val spacing = (w - (barCount * barWidth)) / (barCount + 1)
        
        // draw background line grid
        drawLine(
            Color(0xFFF0F0F0),
            start = Offset(0f, h * 0.25f),
            end = Offset(w, h * 0.25f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            Color(0xFFF0F0F0),
            start = Offset(0f, h * 0.75f),
            end = Offset(w, h * 0.75f),
            strokeWidth = 1.dp.toPx()
        )

        val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

        for (i in 0 until barCount) {
            val barX = spacing + i * (barWidth + spacing)
            val fillHeight = (values[i] / maxVal) * (h * 0.8f)
            val barY = (h * 0.8f) - fillHeight

            // Draw shadow card bar background
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = Offset(barX, 0f),
                size = Size(barWidth, h * 0.8f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            
            // Draw active green practice volume
            drawRoundRect(
                brush = Brush.verticalGradient(colors = listOf(PrimaryDuolingoGreen, Color(0xFF4CAF50))),
                topLeft = Offset(barX, barY),
                size = Size(barWidth, fillHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
        }
    }
    
    // Labels corresponding
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        days.forEach { day ->
            Text(day, fontSize = 11.sp, color = textLightMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
        }
    }
}

// --- SCREEN 8: ACHIEVEMENTS & FLASHCARDS SCREEN ---
@Composable
fun AchievementsAndFlashcardsScreen(viewModel: FrenchCoachViewModel) {
    val vocabulary by viewModel.vocabulary.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Flashcards, 1 = Badges

    Scaffold(
        bottomBar = { BottomNavBar(activeScreen = AppScreen.ACHIEVEMENTS_FLASHCARDS, onNavigate = { viewModel.navigateTo(it) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PremiumLightGrad)
        ) {
            // Screen tab header choice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) PrimaryDuolingoGreen else Color.White)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📇 Cartes Vocabulaire",
                        color = if (selectedTab == 0) Color.White else textDarkColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) secondaryFrenchBlue else Color.White)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🏆 Trophées Clara",
                        color = if (selectedTab == 1) Color.White else textDarkColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (selectedTab == 0) {
                // Flashcards study mode
                if (vocabulary.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Style, null, modifier = Modifier.size(54.dp), tint = textLightMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Votre dictionnaire de révision est vide.", fontWeight = FontWeight.Bold)
                        Text("Terminez une pratique d'expression orale pour extraire automatiquement des mots !", fontSize = 12.sp, color = textLightMuted, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Tapez une carte pour décrypter la traduction en direct :", fontSize = 12.sp, color = textLightMuted, fontWeight = FontWeight.Medium)
                        }
                        
                        items(vocabulary) { wordCard ->
                            VocabularyInteractiveCard(
                                wordCard = wordCard,
                                onToggleLearned = { viewModel.markFlashcardLearned(wordCard) },
                                onDelete = { viewModel.deleteFlashcard(wordCard.id) }
                            )
                        }
                    }
                }
            } else {
                // Badges center
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        BadgeProgressRow(
                            title = "Premier Pas d'Expédition",
                            desc = "Terminez votre premier sujet oral",
                            progress = 1.0f,
                            xpReward = 50,
                            unlocked = true,
                            badgeColor = Color(0xFFFFF3E0)
                        )
                    }
                    item {
                        BadgeProgressRow(
                            title = "Persévérance de Fer",
                            desc = "Atteindre 3 jours de série active d'apprentissage",
                            progress = 1.0f,
                            xpReward = 100,
                            unlocked = true,
                            badgeColor = Color(0xFFE8F5E9)
                        )
                    }
                    item {
                        BadgeProgressRow(
                            title = "Maître Vocabulaire",
                            desc = "Déclenchez 15 mots automatisés par Clara",
                            progress = 0.4f,
                            xpReward = 200,
                            unlocked = false,
                            badgeColor = Color(0xFFE1F5FE)
                        )
                    }
                    item {
                        BadgeProgressRow(
                            title = "As de l'Éloquence",
                            desc = "Atteindre un score de fluidité d'au moins 85/100",
                            progress = 0.0f,
                            xpReward = 300,
                            unlocked = false,
                            badgeColor = Color(0xFFF3E5F5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyInteractiveCard(
    wordCard: VocabularyCard,
    onToggleLearned: () -> Unit,
    onDelete: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { isFlipped = !isFlipped })
            },
        colors = CardDefaults.cardColors(
            containerColor = if (wordCard.isLearned) Color(0xFFE8F5E9) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleLearned, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (wordCard.isLearned) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (wordCard.isLearned) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wordCard.isLearned) "NIVEAU APPRIS" else "À RÉVISER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (wordCard.isLearned) Color(0xFF2E7D32) else accentOrange
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text side swap view
            Box(modifier = Modifier.fillMaxWidth()) {
                if (!isFlipped) {
                    Column {
                        Text(
                            text = wordCard.word,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDarkColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exemple : \"${wordCard.frenchContext}\"",
                            fontSize = 12.sp,
                            color = textLightMuted,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = wordCard.translation,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryFrenchBlue,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Cliquez à nouveau pour revenir au français", fontSize = 10.sp, color = textLightMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeProgressRow(
    title: String,
    desc: String,
    progress: Float,
    xpReward: Int,
    unlocked: Boolean,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(if (unlocked) badgeColor else Color(0xFFEEEEEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (unlocked) "🥇" else "🔒", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (unlocked) textDarkColor else Color.Gray)
                Text(desc, fontSize = 11.sp, color = textLightMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    color = if (unlocked) PrimaryDuolingoGreen else Color.LightGray,
                    trackColor = Color(0xFFF0F0F0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text("RECOMP", fontSize = 9.sp, color = textLightMuted, fontWeight = FontWeight.Bold)
                Text("+$xpReward XP", fontSize = 12.sp, color = accentOrange, fontWeight = FontWeight.Black)
            }
        }
    }
}

// --- SCREEN 9: SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: FrenchCoachViewModel) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val username by viewModel.userDisplayName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()

    var reminderToggle by remember { mutableStateOf(true) }
    var modeDelfChoice by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavBar(activeScreen = AppScreen.SETTINGS, onNavigate = { viewModel.navigateTo(it) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PremiumLightGrad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Paramètres French Coach", fontSize = 20.sp, fontWeight = FontWeight.Black, color = textDarkColor)

            // Account Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(secondaryFrenchBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Text(username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(userEmail ?: "Offline local sync", fontSize = 12.sp, color = textLightMuted)
                    }
                }
            }

            // Coach premium banner toggle simulation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (progress.premiumUnlocked) Color(0xFFFFF3E0) else Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (progress.premiumUnlocked) "Abonnement PRO Activé" else "Passer à Clara Premium ⚜️",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (progress.premiumUnlocked) accentOrange else Color(0xFF6A1B9A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (progress.premiumUnlocked) accentOrange else Color(0xFF8E24AA))
                                .clickable { viewModel.togglePremiumStatus() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (progress.premiumUnlocked) "DÉSACTIVER" else "ACTIVER",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = if (progress.premiumUnlocked) {
                            "Vous bénéficiez des retours haute définition Clara, retours de prononciation avancés et thématiques illimitées !"
                        } else {
                            "Débloquez l'analyste de phonétique française en temps réel, tuteur oral et générations de thèmes infinis."
                        },
                        fontSize = 12.sp,
                        color = textLightMuted
                    )
                }
            }

            Text("OPTIONS D'EXAMEN", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textLightMuted)

            // Examination modes mock DELF
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Assessment, null, tint = secondaryFrenchBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Préparation DELF A2/B1/B2", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Simuler la pression et le timing officiel", fontSize = 11.sp, color = textLightMuted)
                            }
                        }
                        Switch(checked = modeDelfChoice, onCheckedChange = { modeDelfChoice = it })
                    }
                }
            }

            Text("GÉNÉRAL", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textLightMuted)

            // Reminder setups
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Notifications, null, tint = PrimaryDuolingoGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Notifications Quotidiennes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rappel quotidien de pratique Clara à 19h", fontSize = 11.sp, color = textLightMuted)
                            }
                        }
                        Switch(checked = reminderToggle, onCheckedChange = { reminderToggle = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout row button
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_btn")
            ) {
                Text("Se déconnecter", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- BOTTOM NAVIGATION BAR COMPONENT ---
@Composable
fun BottomNavBar(
    activeScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = activeScreen == AppScreen.HOME_DASHBOARD,
            onClick = { onNavigate(AppScreen.HOME_DASHBOARD) },
            icon = { Icon(Icons.Rounded.Dashboard, null) },
            label = { Text("Cours", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryDuolingoGreen,
                selectedTextColor = PrimaryDuolingoGreen,
                indicatorColor = Color(0xFFEEF2FF)
            )
        )

        NavigationBarItem(
            selected = activeScreen == AppScreen.ACHIEVEMENTS_FLASHCARDS,
            onClick = { onNavigate(AppScreen.ACHIEVEMENTS_FLASHCARDS) },
            icon = { Icon(Icons.Rounded.Style, null) },
            label = { Text("Révision", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = secondaryFrenchBlue,
                selectedTextColor = secondaryFrenchBlue,
                indicatorColor = Color(0xFFE0F2FE)
            )
        )

        NavigationBarItem(
            selected = activeScreen == AppScreen.PROGRESS_ANALYTICS,
            onClick = { onNavigate(AppScreen.PROGRESS_ANALYTICS) },
            icon = { Icon(Icons.Rounded.BarChart, null) },
            label = { Text("Progrès", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accentOrange,
                selectedTextColor = accentOrange,
                indicatorColor = Color(0xFFFEF3C7)
            )
        )

        NavigationBarItem(
            selected = activeScreen == AppScreen.SETTINGS,
            onClick = { onNavigate(AppScreen.SETTINGS) },
            icon = { Icon(Icons.Rounded.Settings, null) },
            label = { Text("Réglages", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = textDarkColor,
                selectedTextColor = textDarkColor,
                indicatorColor = Color(0xFFF1F5F9)
            )
        )
    }
}
