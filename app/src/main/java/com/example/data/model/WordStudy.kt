package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "word_studies")
@JsonClass(generateAdapter = true)
data class WordStudy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val testament: String,
    val originalWord: String,
    val transliteration: String,
    val strongsNumber: String,
    val literalMeaning: String,
    val thenMeaning: String,
    val nowMeaning: String,
    val historicalBackground: String,
    val culturalContext: String,
    val legalDimension: String,
    val theologicalWeight: String,
    val keyScriptures: List<String>,
    val closingInsight: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val searchGroundingSources: List<String> = emptyList()
)
