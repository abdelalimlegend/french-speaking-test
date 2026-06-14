package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {

    // --- Topics ---
    @Query("SELECT * FROM topics ORDER BY category ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Int): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTopics(topics: List<Topic>)

    @Update
    suspend fun updateTopic(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: Int)

    @Query("UPDATE topics SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean)

    // --- Session History ---
    @Query("SELECT * FROM session_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SessionHistory>>

    @Query("SELECT * FROM session_history WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Int): SessionHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionHistory): Long

    @Query("DELETE FROM session_history WHERE id = :id")
    suspend fun deleteSession(id: Int)

    // --- User Progress ---
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgress(): UserProgress?

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getProgressFlow(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    // --- Vocabulary Flashcards ---
    @Query("SELECT * FROM vocabulary_cards ORDER BY creationTime DESC")
    fun getAllVocabulary(): Flow<List<VocabularyCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(card: VocabularyCard): Long

    @Update
    suspend fun updateVocabulary(card: VocabularyCard)

    @Query("DELETE FROM vocabulary_cards WHERE id = :id")
    suspend fun deleteVocabulary(id: Int)
}
