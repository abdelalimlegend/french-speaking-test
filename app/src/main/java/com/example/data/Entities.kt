package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val frenchTitle: String,
    val englishTranslation: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val guidingQuestions: String, // Comma or newline separated
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(tableName = "session_history")
data class SessionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicId: Int,
    val topicTitle: String,
    val category: String,
    val timestamp: Long,
    val prepDurationSec: Int,
    val speakDurationSec: Int,
    val transcript: String,
    val fluencyScore: Int,
    val cefrLevel: String, // "A1", "A2", "B1", "B2", "C1", "C2"
    val grammarCorrectionJson: String, // JSON list of corrected blocks
    val vocabSuggestionsJson: String, // JSON list of word switches
    val pronunciationFeedbackJson: String, // JSON list of phoneme/word targets
    val improvementTipsJson: String, // JSON list of advice
    val correctedTranscript: String = "",
    val audioPath: String? = null,
    val userNotes: String = ""
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Single entry row
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastPracticeTimestamp: Long = 0,
    val totalPracticeSeconds: Int = 0,
    val completedTopicsCount: Int = 0,
    val weeklyGoalMinutes: Int = 15,
    val premiumUnlocked: Boolean = false
)

@Entity(tableName = "vocabulary_cards")
data class VocabularyCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val translation: String,
    val frenchContext: String,
    val creationTime: Long = System.currentTimeMillis(),
    val isLearned: Boolean = false
)
