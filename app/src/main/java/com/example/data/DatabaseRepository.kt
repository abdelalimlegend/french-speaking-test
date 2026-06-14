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

    // Comprehensive record update after completing a speaking session (fully local and offline)
    suspend fun completeSpeakingSession(
        topicId: Int,
        topicTitle: String,
        category: String,
        prepSec: Int,
        speakSec: Int,
        transcript: String,
        fluencyScore: Int,
        cefrLevel: String,
        userNotes: String,
        audioPath: String?
    ): SessionHistory {
        val historyEntity = SessionHistory(
            topicId = topicId,
            topicTitle = topicTitle,
            category = category,
            timestamp = System.currentTimeMillis(),
            prepDurationSec = prepSec,
            speakDurationSec = speakSec,
            transcript = transcript,
            fluencyScore = fluencyScore,
            cefrLevel = cefrLevel,
            grammarCorrectionJson = "[]",
            vocabSuggestionsJson = "[]",
            pronunciationFeedbackJson = "[]",
            improvementTipsJson = "[]",
            correctedTranscript = transcript,
            audioPath = audioPath,
            userNotes = userNotes
        )

        val id = coachDao.insertSession(historyEntity)
        val insertedSession = historyEntity.copy(id = id.toInt())

        // Calculate and award user XP and update stats
        val baseXP = 20
        val durationBonus = (speakSec / 10).coerceAtMost(30) // +1 XP per 10 seconds of speaking, up to 30 bonus XP
        val qualityBonus = (fluencyScore / 5) // up to 20 XP
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
                val calNow = java.util.Calendar.getInstance().apply { timeInMillis = now }
                val calLast = java.util.Calendar.getInstance().apply { timeInMillis = lastPracticed }
                
                // Truncate hours/minutes for clear date difference
                calNow.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calNow.set(java.util.Calendar.MINUTE, 0)
                calNow.set(java.util.Calendar.SECOND, 0)
                calNow.set(java.util.Calendar.MILLISECOND, 0)

                calLast.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calLast.set(java.util.Calendar.MINUTE, 0)
                calLast.set(java.util.Calendar.SECOND, 0)
                calLast.set(java.util.Calendar.MILLISECOND, 0)

                val diffMs = calNow.timeInMillis - calLast.timeInMillis
                val diffDays = diffMs / (1000 * 60 * 60 * 24)

                when (diffDays) {
                    0L -> currentStreak // Practiced today already, streak stays the same
                    1L -> currentStreak + 1 // Practiced exactly yesterday, increment!
                    else -> 1 // Broken streak, reset to 1
                }
            }
        }

        // Longest streak tracking
        val maxStreak = maxOf(currentProgress.streak, newStreak)
        val newLongestStreak = if (currentProgress.longestStreak < maxStreak) maxStreak else currentProgress.longestStreak

        val updatedProgress = currentProgress.copy(
            level = newLevel,
            xp = newXP,
            streak = newStreak,
            longestStreak = newLongestStreak,
            lastPracticeTimestamp = now,
            totalPracticeSeconds = currentProgress.totalPracticeSeconds + speakSec,
            completedTopicsCount = currentProgress.completedTopicsCount + 1
        )

        coachDao.insertOrUpdateProgress(updatedProgress)

        return insertedSession
    }

    // --- Vocabulary Flashcards ---
    val allVocabulary: Flow<List<VocabularyCard>> = coachDao.getAllVocabulary()

    suspend fun insertVocabulary(card: VocabularyCard) = coachDao.insertVocabulary(card)

    suspend fun updateVocabulary(card: VocabularyCard) = coachDao.updateVocabulary(card)

    suspend fun deleteVocabulary(id: Int) = coachDao.deleteVocabulary(id)
}
