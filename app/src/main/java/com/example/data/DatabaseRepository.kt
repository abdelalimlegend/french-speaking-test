package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.Calendar

class DatabaseRepository(private val coachDao: CoachDao) {

    // --- Topics ---
    val allTopics: Flow<List<Topic>> = coachDao.getAllTopics()

    suspend fun getTopicById(id: Int): Topic? = coachDao.getTopicById(id)

    suspend fun insertTopic(topic: Topic): Long = coachDao.insertTopic(topic)

    suspend fun updateTopic(topic: Topic) = coachDao.updateTopic(topic)

    suspend fun deleteTopicById(id: Int) = coachDao.deleteTopicById(id)

    suspend fun toggleFavorite(topicId: Int, isFav: Boolean) = 
        coachDao.updateFavoriteStatus(topicId, isFav)

    // Seed database if empty with the 100 preset topics
    suspend fun seedPresetsIfNeeded() {
        // Run check to count/fetch how many we have
        val allOfThey = coachDao.getAllTopics().first()
        if (allOfThey.isEmpty()) {
            val list = TopicPresets.topics.map { preset ->
                Topic(
                    id = preset.id,
                    category = preset.category,
                    frenchTitle = preset.frenchTitle,
                    englishTranslation = preset.englishTranslation,
                    difficulty = preset.difficulty,
                    guidingQuestions = preset.guidingQuestions.joinToString("\n"),
                    isFavorite = false,
                    isCustom = false
                )
            }
            coachDao.insertTopics(list)
        }
    }

    // --- Session History ---
    val allHistory: Flow<List<SessionHistory>> = coachDao.getAllHistory()

    suspend fun getSessionById(id: Int): SessionHistory? = coachDao.getSessionById(id)

    suspend fun insertSession(session: SessionHistory): Long = coachDao.insertSession(session)

    suspend fun deleteSession(id: Int) = coachDao.deleteSession(id)

    // --- User Progress ---
    val userProgress: Flow<UserProgress?> = coachDao.getProgressFlow()

    suspend fun getProgressDirect(): UserProgress {
        var progress = coachDao.getProgress()
        if (progress == null) {
            progress = UserProgress()
            coachDao.insertOrUpdateProgress(progress)
        }
        return progress
    }

    suspend fun insertOrUpdateProgress(progress: UserProgress) {
        coachDao.insertOrUpdateProgress(progress)
    }

    // Comprehensive record update after completing a speaking session
    suspend fun completeSpeakingSession(
        topicId: Int,
        topicTitle: String,
        category: String,
        prepSec: Int,
        speakSec: Int,
        transcript: String,
        feedback: CoachFeedbackResponse,
        userNotes: String
    ): SessionHistory {
        // Save the session history trace
        val moshi = GeminiClient.jsonParser
        
        val grammarAdapter = moshi.adapter(List::class.java) // Simple raw mapping or specific
        val grammarString = moshi.adapter(Array<GrammarCorrectionItem>::class.java)
            .toJson(feedback.grammarCorrections.toTypedArray())
        val vocabString = moshi.adapter(Array<VocabularySuggestionItem>::class.java)
            .toJson(feedback.vocabularySuggestions.toTypedArray())
        val pronunciationString = moshi.adapter(Array<PronunciationFeedbackItem>::class.java)
            .toJson(feedback.pronunciationFeedback.toTypedArray())
        val tipsString = moshi.adapter(Array<String>::class.java)
            .toJson(feedback.improvementTips.toTypedArray())

        val historyEntity = SessionHistory(
            topicId = topicId,
            topicTitle = topicTitle,
            category = category,
            timestamp = System.currentTimeMillis(),
            prepDurationSec = prepSec,
            speakDurationSec = speakSec,
            transcript = transcript,
            fluencyScore = feedback.fluencyScore,
            cefrLevel = feedback.cefrLevel,
            grammarCorrectionJson = grammarString,
            vocabSuggestionsJson = vocabString,
            pronunciationFeedbackJson = pronunciationString,
            improvementTipsJson = tipsString,
            correctedTranscript = feedback.correctedTranscript,
            audioPath = null, // Path to local recorded file if recorded
            userNotes = userNotes
        )

        val id = coachDao.insertSession(historyEntity)
        val insertedSession = historyEntity.copy(id = id.toInt())

        // Calculate and award user XP and update stats
        val baseXP = 15
        val durationBonus = (speakSec / 10) // +1 XP per 10 seconds of speaking
        val qualityBonus = (feedback.fluencyScore / 5) // up to +20 XP for fluency
        val xpEarnt = baseXP + durationBonus + qualityBonus

        // Increment stats
        val currentProgress = getProgressDirect()
        val newXP = currentProgress.xp + xpEarnt
        val levelFactor = 150 // XP needed per level
        val newLevel = 1 + (newXP / levelFactor)

        // Streak computation
        val now = System.currentTimeMillis()
        val lastPracticed = currentProgress.lastPracticeTimestamp
        val currentStreak = currentProgress.streak
        val newStreak = when {
            lastPracticed == 0L -> 1
            else -> {
                val calNow = Calendar.getInstance().apply { timeInMillis = now }
                val calLast = Calendar.getInstance().apply { timeInMillis = lastPracticed }
                
                // Truncate hours/minutes for clear date difference
                calNow.set(Calendar.HOUR_OF_DAY, 0)
                calNow.set(Calendar.MINUTE, 0)
                calNow.set(Calendar.SECOND, 0)
                calNow.set(Calendar.MILLISECOND, 0)

                calLast.set(Calendar.HOUR_OF_DAY, 0)
                calLast.set(Calendar.MINUTE, 0)
                calLast.set(Calendar.SECOND, 0)
                calLast.set(Calendar.MILLISECOND, 0)

                val diffMs = calNow.timeInMillis - calLast.timeInMillis
                val diffDays = diffMs / (1000 * 60 * 60 * 24)

                when (diffDays) {
                    0L -> currentStreak // Practiced today already, streak stays the same
                    1L -> currentStreak + 1 // Practiced exactly yesterday, increment!
                    else -> 1 // Broken streak, reset to 1
                }
            }
        }

        val updatedProgress = currentProgress.copy(
            level = newLevel,
            xp = newXP,
            streak = newStreak,
            lastPracticeTimestamp = now,
            totalPracticeSeconds = currentProgress.totalPracticeSeconds + speakSec,
            completedTopicsCount = currentProgress.completedTopicsCount + 1
        )

        coachDao.insertOrUpdateProgress(updatedProgress)

        // Automatically convert vocabulary suggestions to learnable flashcards
        feedback.vocabularySuggestions.forEach { suggestion ->
            val vocabCard = VocabularyCard(
                word = suggestion.suggestedAlternative,
                translation = suggestion.originalWord, // basic helper
                frenchContext = suggestion.usageExample,
                creationTime = System.currentTimeMillis(),
                isLearned = false
            )
            coachDao.insertVocabulary(vocabCard)
        }

        return insertedSession
    }

    // --- Vocabulary Flashcards ---
    val allVocabulary: Flow<List<VocabularyCard>> = coachDao.getAllVocabulary()

    suspend fun insertVocabulary(card: VocabularyCard) = coachDao.insertVocabulary(card)

    suspend fun updateVocabulary(card: VocabularyCard) = coachDao.updateVocabulary(card)

    suspend fun deleteVocabulary(id: Int) = coachDao.deleteVocabulary(id)
}
