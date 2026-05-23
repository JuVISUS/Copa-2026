package com.juvisus.copa2026app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val code: String, // e.g. "BRA"
    val name: String, // e.g. "Brasil"
    val addedAtEpoch: Long = System.currentTimeMillis()
)
