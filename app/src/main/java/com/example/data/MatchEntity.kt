package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamHome: String,
    val teamAway: String,
    val teamHomeCode: String,
    val teamAwayCode: String,
    val dateTimeEpoch: Long, // timestamp
    val stadium: String,
    val city: String,
    val country: String,
    val broadcast: String,
    val isCompleted: Boolean = false,
    val scoreHome: Int? = null,
    val scoreAway: Int? = null,
    val stage: String, // e.g., "Grupo A", "Grupo D"
    val summary: String = "",
    val statsPossession: String = "50% - 50%",
    val statsShots: String = "10 - 10",
    val statsFouls: String = "12 - 12",
    val highlightsLink: String = "https://youtube.com/fifa"
)
