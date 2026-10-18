package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.apologetics.ApologeticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApologeticsDao {

    @Query("SELECT * FROM apologetics_entries ORDER BY timestamp DESC")
    fun getAllDossiers(): Flow<List<ApologeticsEntity>>

    @Query("SELECT * FROM apologetics_entries WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedDossiers(): Flow<List<ApologeticsEntity>>

    @Query("SELECT * FROM apologetics_entries WHERE id = :id LIMIT 1")
    suspend fun getDossierById(id: String): ApologeticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ApologeticsEntity): Long

    @Query("UPDATE apologetics_entries SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE apologetics_entries SET userPersonalNotes = :notes WHERE id = :id")
    suspend fun updatePersonalNotes(id: String, notes: String)

    @Query("DELETE FROM apologetics_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
