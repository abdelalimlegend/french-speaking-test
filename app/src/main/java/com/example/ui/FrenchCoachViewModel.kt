package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTHENTICATION,
    HOME_DASHBOARD,
    SPEAKING_SESSION,
    RECORDING_REVIEW,
    AI_FEEDBACK_REPORT,
    PROGRESS_ANALYTICS,
    ACHIEVEMENTS_FLASHCARDS,
    SETTINGS
}

enum class SessionPhase {
    PREPARATION,
    SPEAKING
}

class FrenchCoachViewModel(
    private val repository: DatabaseRepository,
    application: Application
) : AndroidViewModel(application) {

    // --- Screen Navigation ---
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // --- Authentication ---
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userDisplayName = MutableStateFlow<String>("Invite")
    val userDisplayName: StateFlow<String> = _userDisplayName.asStateFlow()

    // --- Preset & Custom Topics State ---
    val allTopics: StateFlow<List<Topic>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String>("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredTopics: StateFlow<List<Topic>> = combine(
        allTopics,
        _selectedCategory,
        _searchQuery
    ) { topics, category, query ->
        topics.filter { topic ->
            val matchesCategory = if (category == "All") true else topic.category == category
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                topic.frenchTitle.contains(query, ignoreCase = true) ||
                topic.englishTranslation.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Speaking Session Constants & Variables ---
    private val _activeTopic = MutableStateFlow<Topic?>(null)
    val activeTopic: StateFlow<Topic?> = _activeTopic.asStateFlow()

    private val _sessionPhase = MutableStateFlow(SessionPhase.PREPARATION)
    val sessionPhase: StateFlow<SessionPhase> = _sessionPhase.asStateFlow()

    // Configurable practice lengths
    private val _targetPrepSeconds = MutableStateFlow(60) // Default 1 minute (60s) or 2 minutes (120s)
    val targetPrepSeconds: StateFlow<Int> = _targetPrepSeconds.asStateFlow()

    private val _targetSpeakSeconds = MutableStateFlow(120) // Default 2 minutes (120s), 3 mins (180s), 5 mins (300s)
    val targetSpeakSeconds: StateFlow<Int> = _targetSpeakSeconds.asStateFlow()

    private val _timeRemaining = MutableStateFlow(60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _prepNotes = MutableStateFlow("")
    val prepNotes: StateFlow<String> = _prepNotes.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingTextMock = MutableStateFlow("") // Simulates speech transcript or let user modify
    val recordingTextMock: StateFlow<String> = _recordingTextMock.asStateFlow()

    private val _liveAudioAmplitude = MutableStateFlow(0f) // For pulsing microphone animations
    val liveAudioAmplitude: StateFlow<Float> = _liveAudioAmplitude.asStateFlow()

    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null
    private var elapsedSpeakSeconds = 0

    // --- AI Feedback Output ---
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _aiErrorResponse = MutableStateFlow<String?>(null)
    val aiErrorResponse: StateFlow<String?> = _aiErrorResponse.asStateFlow()

    private val _activeReviewSession = MutableStateFlow<SessionHistory?>(null)
    val activeReviewSession: StateFlow<SessionHistory?> = _activeReviewSession.asStateFlow()

    // --- Progress & Analytics ---
    val progress: StateFlow<UserProgress> = repository.userProgress
        .map { it ?: UserProgress() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgress())

    val history: StateFlow<List<SessionHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Vocabulary Flashcards ---
    val vocabulary: StateFlow<List<VocabularyCard>> = repository.allVocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Premium Setup & Custom Generator ---
    private val _customTopicCategory = MutableStateFlow("Daily Life")
    val customTopicCategory: StateFlow<String> = _customTopicCategory.asStateFlow()

    private val _customTopicDifficulty = MutableStateFlow("Medium")
    val customTopicDifficulty: StateFlow<String> = _customTopicDifficulty.asStateFlow()

    private val _isGeneratingTopic = MutableStateFlow(false)
    val isGeneratingTopic: StateFlow<Boolean> = _isGeneratingTopic.asStateFlow()

    init {
        // Run database seeding on startup asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedPresetsIfNeeded()
        }

        // Trigger dummy timer or setup
        startSplashCountdown()
    }

    private fun startSplashCountdown() {
        viewModelScope.launch {
            delay(2400)
            _currentScreen.value = AppScreen.AUTHENTICATION
        }
    }

    // --- Navigation Handlers ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun logout() {
        _userEmail.value = null
        _userDisplayName.value = "Invite"
        _currentScreen.value = AppScreen.AUTHENTICATION
    }

    fun login(email: String, name: String) {
        _userEmail.value = email
        _userDisplayName.value = name.ifBlank { "Apprenant" }
        _currentScreen.value = AppScreen.HOME_DASHBOARD
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Speaking Session Flow ---
    fun selectTopicForSession(topic: Topic) {
        _activeTopic.value = topic
        _sessionPhase.value = SessionPhase.PREPARATION
        _timeRemaining.value = _targetPrepSeconds.value
        _prepNotes.value = ""
        _recordingTextMock.value = ""
        _isTimerRunning.value = false
        _isRecording.value = false
        _liveAudioAmplitude.value = 0f
        elapsedSpeakSeconds = 0
        navigateTo(AppScreen.SPEAKING_SESSION)
        startTimer()
    }

    fun selectPrepTimeOption(seconds: Int) {
        _targetPrepSeconds.value = seconds
        if (_sessionPhase.value == SessionPhase.PREPARATION) {
            _timeRemaining.value = seconds
        }
    }

    fun selectSpeakTimeOption(seconds: Int) {
        _targetSpeakSeconds.value = seconds
    }

    fun startTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timeRemaining.value -= 1
                
                if (_sessionPhase.value == SessionPhase.SPEAKING) {
                    elapsedSpeakSeconds++
                }

                // Simulate audio visualizer amplitude bouncing if recording
                if (_isRecording.value) {
                    _liveAudioAmplitude.value = (2..10).random() / 10f
                }
            }
            if (_timeRemaining.value <= 0) {
                handleTimerSpit()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        _isRecording.value = false
        _liveAudioAmplitude.value = 0f
        timerJob?.cancel()
        amplitudeJob?.cancel()
    }

    private fun handleTimerSpit() {
        if (_sessionPhase.value == SessionPhase.PREPARATION) {
            // Auto transition to Speaking phase
            startSpeakingPhase()
        } else {
            // Speaking finish
            finishSpeakingAndReview()
        }
    }

    fun skipPrepToSpeaking() {
        timerJob?.cancel()
        startSpeakingPhase()
    }

    private fun startSpeakingPhase() {
        _sessionPhase.value = SessionPhase.SPEAKING
        _timeRemaining.value = _targetSpeakSeconds.value
        _isRecording.value = true
        _isTimerRunning.value = true
        elapsedSpeakSeconds = 0
        
        // Generate initial preset simulated text for French speech based on topic!
        val active = _activeTopic.value
        _recordingTextMock.value = getMouthFrenchSimulation(active?.frenchTitle ?: "")

        startTimer()
        startAmplitudeSimulation()
    }

    private fun startAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(150)
                _liveAudioAmplitude.value = (15..95).random() / 100f
            }
            _liveAudioAmplitude.value = 0f
        }
    }

    fun updatePrepNotes(text: String) {
        _prepNotes.value = text
    }

    fun updateRecordingText(text: String) {
        _recordingTextMock.value = text
    }

    fun finishSpeakingAndReview() {
        pauseTimer()
        navigateTo(AppScreen.RECORDING_REVIEW)
    }

    fun triggerAISpeakingCoachingAnalysis() {
        val topic = _activeTopic.value ?: return
        val speechTranscript = _recordingTextMock.value.ifBlank {
            "Je parler français un petit peu. J'ai allé à Paris pour les vacances."
        }

        _isAnalyzing.value = true
        _aiErrorResponse.value = null

        viewModelScope.launch {
            try {
                // Background call to Gemini or fallback
                val feedbackResult = FrenchCoachAIService.analyzeSpokenFrench(
                    topicTitle = topic.frenchTitle,
                    spokenText = speechTranscript
                )

                // Save results in Room, award XP, update level, streaking
                val savedSession = repository.completeSpeakingSession(
                    topicId = topic.id,
                    topicTitle = topic.frenchTitle,
                    category = topic.category,
                    prepSec = _targetPrepSeconds.value - _timeRemaining.value, // time spent
                    speakSec = elapsedSpeakSeconds.coerceAtLeast(5), // ensure non-zero
                    transcript = speechTranscript,
                    feedback = feedbackResult,
                    userNotes = _prepNotes.value
                )

                _activeReviewSession.value = savedSession
                _isAnalyzing.value = false
                navigateTo(AppScreen.AI_FEEDBACK_REPORT)

            } catch (e: Exception) {
                _isAnalyzing.value = false
                _aiErrorResponse.value = e.localizedMessage ?: "Failed analyzing your voice file."
            }
        }
    }

    // --- Favorites Toggle ---
    fun toggleFavoriteTopic(topic: Topic) {
        viewModelScope.launch {
            repository.toggleFavorite(topic.id, !topic.isFavorite)
        }
    }

    // --- Custom Topic Actions ---
    fun addCustomTopic(french: String, english: String, category: String, difficulty: String, questions: String) {
        viewModelScope.launch {
            val custom = Topic(
                category = category,
                frenchTitle = french,
                englishTranslation = english,
                difficulty = difficulty,
                guidingQuestions = questions.ifBlank { "Pourquoi ce sujet ?\nComment cela affecte-t-il la France ?" },
                isFavorite = false,
                isCustom = true
            )
            repository.insertTopic(custom)
        }
    }

    fun setGeneratorConfigs(category: String, difficulty: String) {
        _customTopicCategory.value = category
        _customTopicDifficulty.value = difficulty
    }

    fun generateAIPoweredTopic() {
        _isGeneratingTopic.value = true
        viewModelScope.launch {
            try {
                val cat = _customTopicCategory.value
                val diff = _customTopicDifficulty.value
                val topicResponse = FrenchCoachAIService.generateCustomTopic(cat, diff)

                val newlyMinted = Topic(
                    category = cat,
                    frenchTitle = topicResponse.frenchTitle,
                    englishTranslation = topicResponse.englishTranslation,
                    difficulty = diff,
                    guidingQuestions = topicResponse.guidingQuestions.joinToString("\n"),
                    isFavorite = false,
                    isCustom = true
                )

                val id = repository.insertTopic(newlyMinted)
                _isGeneratingTopic.value = false
                
                // Select topic immediately for session
                selectTopicForSession(newlyMinted.copy(id = id.toInt()))
            } catch (e: Exception) {
                _isGeneratingTopic.value = false
            }
        }
    }

    // --- Flashcards Logic ---
    fun markFlashcardLearned(card: VocabularyCard) {
        viewModelScope.launch {
            repository.updateVocabulary(card.copy(isLearned = !card.isLearned))
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteVocabulary(id)
        }
    }

    // --- Premium simulation toggle ---
    fun togglePremiumStatus() {
        viewModelScope.launch {
            val prog = progress.value
            repository.insertOrUpdateProgress(prog.copy(premiumUnlocked = !prog.premiumUnlocked))
        }
    }

    // Mock speech text presets corresponding to topics for a lively feel
    private fun getMouthFrenchSimulation(topic: String): String {
        return when {
            topic.contains("routine", ignoreCase = true) -> 
                "Tous les matins, je me réveille à sept heures. J'ai allé à la cuisine pour préparer un café serré et deux croissants. Je me prépare rapidement pour aller au travail de bonne humeur."
            topic.contains("plat", ignoreCase = true) ->
                "Mon plat préféré est sans conteste la ratatouille française avec du pain complet. J'adore les poivrons rouges, les courgettes et l'ail frémissant. C'est très bon pour ma santé physique."
            topic.contains("voyage", ignoreCase = true) ->
                "Le plus fantastique voyage de ma vie était à Paris. Je me suis promené le long de la Seine et j'ai admiré la Tour Eiffel de nuit avec mes amis. J'ai mangé de la baguette délicieuse."
            topic.contains("intelligence", ignoreCase = true) ->
                "L'intelligence artificielle est un sujet captivant. Je pense que l'IA va changer beaucoup d'emplois, mais elle ne peut pas remplacer la créativité humaine et la chaleur de notre cœur."
            topic.contains("environnement", ignoreCase = true) || topic.contains("climatique", ignoreCase = true) ->
                "Pour préserver l'environnement au quotidien, je prends le vélo au lieu d'utiliser ma voiture en ville. C'est plus sain et cela aide à réduire le gaz carbonique dans nos rues polluées."
            topic.contains("routine", ignoreCase = true) ->
                "Habituellement, je commence ma journée par un grand verre d'eau et dix minutes de lecture douce."
            else ->
                "Bonjour Clara ! À propos de ce sujet, je pense que c'est une excellente question de société. J'ai allé étudier ce problème et je trouve que la réponse nécessite beaucoup de réflexion pour donner des solutions viables."
        }
    }
}

// Factory
class FrenchCoachViewModelFactory(
    private val repository: DatabaseRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FrenchCoachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FrenchCoachViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
