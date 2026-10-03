package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WordStudy
import kotlinx.coroutines.flow.Flow

@Dao
interface WordStudyDao {
    @Query("SELECT * FROM word_studies ORDER BY timestamp DESC")
    fun getAllWordStudies(): Flow<List<WordStudy>>

    @Query("SELECT * FROM word_studies WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWordStudies(): Flow<List<WordStudy>>

    @Query("SELECT * FROM word_studies WHERE (LOWER(word) = LOWER(:word) OR LOWER(transliteration) = LOWER(:word) OR originalWord = :word) AND LOWER(testament) = LOWER(:testament) LIMIT 1")
    suspend fun getWordStudyByWordAndTestament(word: String, testament: String): WordStudy?

    @Query("SELECT * FROM word_studies WHERE LOWER(word) = LOWER(:word) OR LOWER(transliteration) = LOWER(:word) OR originalWord = :word LIMIT 1")
    suspend fun getWordStudyAnyTestament(word: String): WordStudy?

    @Query("SELECT COUNT(*) FROM word_studies")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordStudy(wordStudy: WordStudy): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(wordStudies: List<WordStudy>): List<Long>

    @Update
    suspend fun updateWordStudy(wordStudy: WordStudy)

    @Delete
    suspend fun deleteWordStudy(wordStudy: WordStudy)

    @Query("DELETE FROM word_studies WHERE id = :id")
    suspend fun deleteWordStudyById(id: Long)
}
