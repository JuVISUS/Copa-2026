package com.copa.alerta2026.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val teamHome: String,
    val teamHomeCode: String,
    val teamAway: String,
    val teamAwayCode: String,
    val scoreHome: Int? = null,
    val scoreAway: Int? = null,
    val dateTimeEpoch: Long, // epoch time in seconds
    val isCompleted: Boolean,
    val stage: String, // e.g., "Fase de Grupos", "Oitavas de Final"
    val stadium: String,
    val city: String,
    val country: String,
    val broadcast: String // Channel name e.g. "TV Globo", "SporTV"
)
