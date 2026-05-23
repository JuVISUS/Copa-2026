package com.copa.alerta2026.data

import kotlinx.coroutines.flow.Flow

class FootballRepository(private val db: AppDatabase) {

    val allMatches: Flow<List<MatchEntity>> = db.matchDao().getAllMatches()
    val favoriteTeams: Flow<List<FavoriteTeamEntity>> = db.favoriteTeamDao().getFavoriteTeams()
    val chatMessages: Flow<List<ChatMessageEntity>> = db.chatMessageDao().getAllMessages()

    suspend fun insertMatches(matches: List<MatchEntity>) {
        db.matchDao().insertMatches(matches)
    }

    suspend fun addFavoriteTeam(code: String, name: String) {
        db.favoriteTeamDao().insertFavoriteTeam(FavoriteTeamEntity(code = code, name = name))
    }

    suspend fun removeFavoriteTeam(code: String, name: String) {
        db.favoriteTeamDao().deleteFavoriteTeam(FavoriteTeamEntity(code = code, name = name))
    }

    suspend fun insertChatMessage(sender: String, text: String) {
        db.chatMessageDao().insertMessage(ChatMessageEntity(sender = sender, text = text))
    }

    suspend fun clearChatHistory() {
        db.chatMessageDao().clearAll()
    }
}
