package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
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
import java.io.File

enum class AppScreen {
    SPLASH,
    AUTHENTICATION,
    HOME_DASHBOARD,
    SPEAKING_SESSION,
    RECORDING_REVIEW,
    AI_FEEDBACK_REPORT, // Kept to avoid route compile errors in other layouts, but bypassed
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

    // --- Authentication (Guest mode by default) ---
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userDisplayName = MutableStateFlow<String>("Invité Clara 🇫🇷")
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

    // --- Speaking Session State ---
    private val _activeTopic = MutableStateFlow<Topic?>(null)
    val activeTopic: StateFlow<Topic?> = _activeTopic.asStateFlow()

    private val _sessionPhase = MutableStateFlow(SessionPhase.PREPARATION)
    val sessionPhase: StateFlow<SessionPhase> = _sessionPhase.asStateFlow()

    // Configurable practice lengths
    private val _targetPrepSeconds = MutableStateFlow(60) // 1 minute default
    val targetPrepSeconds: StateFlow<Int> = _targetPrepSeconds.asStateFlow()

    private val _targetSpeakSeconds = MutableStateFlow(120) // 2 minutes default
    val targetSpeakSeconds: StateFlow<Int> = _targetSpeakSeconds.asStateFlow()

    private val _timeRemaining = MutableStateFlow(60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _prepNotes = MutableStateFlow("")
    val prepNotes: StateFlow<String> = _prepNotes.asStateFlow()

    private val _liveAudioAmplitude = MutableStateFlow(0f)
    val liveAudioAmplitude: StateFlow<Float> = _liveAudioAmplitude.asStateFlow()

    // --- Audio Record Engine ---
    private var mediaRecorder: MediaRecorder? = null
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordedAudioPath = MutableStateFlow<String?>(null)
    val recordedAudioPath: StateFlow<String?> = _recordedAudioPath.asStateFlow()

    // --- Audio Play Engine ---
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playDuration = MutableStateFlow(0)
    val playDuration: StateFlow<Int> = _playDuration.asStateFlow()

    private val _playPosition = MutableStateFlow(0)
    val playPosition: StateFlow<Int> = _playPosition.asStateFlow()

    // --- Self-Evaluation Ratings ---
    private val _selfFluency = MutableStateFlow(75)
    val selfFluency: StateFlow<Int> = _selfFluency.asStateFlow()

    private val _selfPronunciation = MutableStateFlow(70)
    val selfPronunciation: StateFlow<Int> = _selfPronunciation.asStateFlow()

    private val _selfGrammar = MutableStateFlow(65)
    val selfGrammar: StateFlow<Int> = _selfGrammar.asStateFlow()

    private val _selfVocabulary = MutableStateFlow(70)
    val selfVocabulary: StateFlow<Int> = _selfVocabulary.asStateFlow()

    private val _selfCefr = MutableStateFlow("B1")
    val selfCefr: StateFlow<String> = _selfCefr.asStateFlow()

    private val _transcriptMock = MutableStateFlow("")
    val transcriptMock: StateFlow<String> = _transcriptMock.asStateFlow()

    private val _recordingTextMock = MutableStateFlow("")
    val recordingTextMock: StateFlow<String> = _recordingTextMock.asStateFlow()

    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null
    private var elapsedSpeakSeconds = 0

    // Bypassed legacy AI state flows to prevent compilations crashes
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

    // --- Custom Topics dialog configs ---
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

        startSplashCountdown()
    }

    private fun startSplashCountdown() {
        viewModelScope.launch {
            delay(2000)
            // Navigate directly to the dashboard, guest mode is enabled by default!
            _currentScreen.value = AppScreen.HOME_DASHBOARD
        }
    }

    // --- Navigation Handlers ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun showSessionReport(session: SessionHistory) {
        _activeReviewSession.value = session
        _recordedAudioPath.value = session.audioPath
        _playPosition.value = 0
        _playDuration.value = 0
        _isPlaying.value = false
        navigateTo(AppScreen.AI_FEEDBACK_REPORT)
    }

    fun logout() {
        _userEmail.value = null
        _userDisplayName.value = "Invité Clara 🇫🇷"
        _currentScreen.value = AppScreen.AUTHENTICATION
    }

    fun login(email: String, name: String) {
        _userEmail.value = email
        _userDisplayName.value = name.ifBlank { "Pratiquant de français" }
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
        
        // Reset Self evaluation values
        _selfFluency.value = 75
        _selfPronunciation.value = 70
        _selfGrammar.value = 65
        _selfVocabulary.value = 70
        _selfCefr.value = when (topic.difficulty) {
            "Easy" -> "A2"
            "Hard" -> "C1"
            else -> "B1"
        }

        // Release old recording & player objects
        releaseAudioResources()

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
                    _liveAudioAmplitude.value = (3..9).random() / 10f
                }
            }
            if (_timeRemaining.value <= 0) {
                handleTimerEnd()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    private fun handleTimerEnd() {
        if (_sessionPhase.value == SessionPhase.PREPARATION) {
            startSpeakingPhase()
        } else {
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
        _isTimerRunning.value = true
        elapsedSpeakSeconds = 0
        
        // Expose a realistic initial mock transcript based on preset
        val active = _activeTopic.value
        _recordingTextMock.value = getMouthFrenchSimulation(active?.frenchTitle ?: "")

        startTimer()
        startAmplitudeSimulation()
        startAudioRecording()
    }

    private fun startAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(120)
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
        stopAudioRecording()
        _isRecording.value = false
        navigateTo(AppScreen.RECORDING_REVIEW)
    }

    // --- Real Audio Recording Logic ---
    private fun startAudioRecording() {
        try {
            val cacheDirectory = getApplication<Application>().cacheDir
            val file = File(cacheDirectory, "recording_${System.currentTimeMillis()}.mp4")
            _recordedAudioPath.value = file.absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(getApplication<Application>().applicationContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    fun pauseAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && _isRecording.value) {
            try {
                mediaRecorder?.pause()
                _isRecording.value = false
                pauseTimer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resumeAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                _isRecording.value = true
                startTimer()
                startAmplitudeSimulation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopAudioRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            _isRecording.value = false
        }
    }

    // --- Real Audio Playback Logic ---
    fun playRecording() {
        val path = _recordedAudioPath.value ?: return
        if (path.isEmpty() || !File(path).exists()) return

        if (_playPosition.value > 0 && mediaPlayer != null) {
            // Resume play
            try {
                mediaPlayer?.start()
                _isPlaying.value = true
                trackPlayProgress()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playPosition.value = 0
                }
                start()
            }
            _isPlaying.value = true
            _playDuration.value = mediaPlayer?.duration ?: 0
            trackPlayProgress()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseRecordingPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _playPosition.value = it.currentPosition
                }
            }
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekPlaybackTo(positionMs: Float) {
        try {
            mediaPlayer?.let {
                it.seekTo(positionMs.toInt())
                _playPosition.value = positionMs.toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAudioRecording() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            _playPosition.value = 0
            _playDuration.value = 0
            _recordedAudioPath.value?.let { path ->
                val f = File(path)
                if (f.exists()) f.delete()
            }
            _recordedAudioPath.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackPlayProgress() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _playPosition.value = it.currentPosition
                    }
                }
                delay(100)
            }
        }
    }

    private fun releaseAudioResources() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaRecorder = null
        mediaPlayer = null
        _isRecording.value = false
        _isPlaying.value = false
        _playPosition.value = 0
        _playDuration.value = 0
    }

    // --- Self-Evaluation Mutators ---
    fun setSelfFluency(value: Int) {
        _selfFluency.value = value
    }

    fun setSelfPronunciation(value: Int) {
        _selfPronunciation.value = value
    }

    fun setSelfGrammar(value: Int) {
        _selfGrammar.value = value
    }

    fun setSelfVocabulary(value: Int) {
        _selfVocabulary.value = value
    }

    fun setSelfCefr(value: String) {
        _selfCefr.value = value
    }

    // --- Saving Completed Practice Session Offline ---
    fun saveSelfEvaluationSession() {
        val topic = _activeTopic.value ?: return
        val text = _recordingTextMock.value.ifBlank { "Session orale de français." }
        val prepTime = _targetPrepSeconds.value - _timeRemaining.value
        val speakingTime = elapsedSpeakSeconds.coerceAtLeast(3)

        // Set isAnalyzing briefly for animated realistic transitions!
        _isAnalyzing.value = true
        viewModelScope.launch {
            delay(800) // Small visual breathing space
            try {
                // Compute combined score
                val finalScore = ((_selfFluency.value + _selfPronunciation.value + _selfGrammar.value + _selfVocabulary.value) / 4)
                
                // Add a smart flashcard locally automatically based on high quality self evaluation text!
                // Let's parse words of length > 6 and insert a realistic helper vocabulary card
                val words = text.split(" ", ",", ".").filter { it.length > 6 }.distinct()
                if (words.isNotEmpty()) {
                    val randomWord = words.random().lowercase()
                    repository.insertVocabulary(
                        VocabularyCard(
                            word = randomWord,
                            translation = "Mot de contextualisation",
                            frenchContext = "Exemple d'expression : \"$text\"",
                            isLearned = false
                        )
                    )
                }

                repository.completeSpeakingSession(
                    topicId = topic.id,
                    topicTitle = topic.frenchTitle,
                    category = topic.category,
                    prepSec = prepTime,
                    speakSec = speakingTime,
                    transcript = text,
                    fluencyScore = finalScore,
                    cefrLevel = _selfCefr.value,
                    userNotes = _prepNotes.value,
                    audioPath = _recordedAudioPath.value
                )

                _isAnalyzing.value = false
                releaseAudioResources()
                navigateTo(AppScreen.HOME_DASHBOARD)
            } catch (e: Exception) {
                _isAnalyzing.value = false
                e.printStackTrace()
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
                guidingQuestions = questions.ifBlank { "Pourquoi ce sujet ?\nQu'est-ce que vous en pensez ?" },
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

    // Beautiful simulated offline clever generator so the user always has fresh custom options!
    fun generateAIPoweredTopic() {
        _isGeneratingTopic.value = true
        viewModelScope.launch {
            try {
                delay(800)
                val cat = _customTopicCategory.value
                val diff = _customTopicDifficulty.value
                
                val customTopicsSample = when (cat) {
                    "Daily Life" -> listOf(
                        Pair("Décrivez vos habitudes culinaires.", "Describe your cooking habits."),
                        Pair("Comment organisez-vous votre espace ?", "How do you organize your space?"),
                        Pair("Parlez de la lessive de week-end.", "Talk about weekend laundry.")
                    )
                    "Travel" -> listOf(
                        Pair("Pourquoi préférez-vous le train au vol ?", "Why do you prefer train over flights?"),
                        Pair("Le charme insolite des campagnes.", "The unusual charm of countrysides.")
                    )
                    "Education" -> listOf(
                        Pair("Le rôle central de l'écriture manuscrite.", "The central role of handwriting."),
                        Pair("L'intérêt d'étudier la poésie classique.", "The benefit of studying classical poetry.")
                    )
                    "Work & Career" -> listOf(
                        Pair("Aimer son lieu de travail physique.", "Loving one's physical workplace."),
                        Pair("Comment dire non poliment en réunion.", "How to politely say no during meetings.")
                    )
                    else -> listOf(
                        Pair("Un sujet optionnel de discussion.", "An optional topic of discussion."),
                        Pair("Comment concevez-vous la liberté ?", "How do you define freedom?")
                    )
                }

                val pair = customTopicsSample.random()
                val newlyMinted = Topic(
                    category = cat,
                    frenchTitle = pair.first,
                    englishTranslation = pair.second,
                    difficulty = diff,
                    guidingQuestions = "Pourquoi ce sujet ?\nComment cela affecte-t-il la France ?",
                    isFavorite = false,
                    isCustom = true
                )

                val id = repository.insertTopic(newlyMinted)
                _isGeneratingTopic.value = false
                
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

    override fun onCleared() {
        super.onCleared()
        releaseAudioResources()
    }

    private fun getMouthFrenchSimulation(topic: String): String {
        return when {
            topic.contains("routine", ignoreCase = true) -> 
                "Tous les matins, je me réveille à sept heures. J'ai de la chance de me préparer un café bien chaud et deux croissants croustillants. Je me prépare calmement pour ma journée."
            topic.contains("plat", ignoreCase = true) || topic.contains("chocolat", ignoreCase = true) ->
                "Mon régal préféré est sans conteste la ratatouille française traditionnelle avec du pain complet frais. J'adore les poivrons rouges et l'ail frémissant. C'est très exquis !"
            topic.contains("voyage", ignoreCase = true) ->
                "Le plus fantastique voyage de ma vie était à Paris. Je me suis promené longuement le long de la Seine et j'ai admiré la Tour Eiffel scintillante à la nuit tombée."
            topic.contains("technologie", ignoreCase = true) || topic.contains("intelligence", ignoreCase = true) ->
                "L'évolution technologique est un sujet captivant. Je pense que les innovations changent nos métiers, mais qu'elles ne peuvent pas remplacer l'empathie humaine."
            topic.contains("environnement", ignoreCase = true) || topic.contains("climatique", ignoreCase = true) || topic.contains("écologique", ignoreCase = true) ->
                "Pour préserver notre belle planète au quotidien, je prends le vélo au lieu d'utiliser ma voiture en ville. C'est plus sain et cela évite de polluer l'air de nos rues."
            else ->
                "À propos de ce sujet d'expression orale, je pense que c'est une excellente question de société. J'ai étudié ce problème et je trouve que la réponse nécessite beaucoup de nuances."
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
