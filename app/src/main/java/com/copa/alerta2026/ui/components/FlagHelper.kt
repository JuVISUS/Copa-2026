package com.copa.alerta2026.ui.components

object FlagHelper {
    fun getFlagEmoji(teamCode: String): String {
        return when (teamCode.trim().uppercase()) {
            "BRA" -> "🇧🇷"
            "ARG" -> "🇦🇷"
            "GER" -> "🇩🇪"
            "POR" -> "🇵🇹"
            "ESP" -> "🇪🇸"
            "FRA" -> "🇫🇷"
            "ENG" -> "🇬🇧"
            "URU" -> "🇺🇾"
            "ITA" -> "🇮🇹"
            "NED" -> "🇳🇱"
            "BEL" -> "🇧🇪"
            else -> "🏳️"
        }
    }
}
