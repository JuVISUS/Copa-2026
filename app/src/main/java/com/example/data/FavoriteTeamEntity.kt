package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val code: String, // "BRA", "ARG" etc.
    val name: String,
    val priority: Int
)
