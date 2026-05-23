package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FlagHelper {
    fun getEmoji(code: String): String {
        return when (code.uppercase()) {
            "BRA" -> "🇧🇷"
            "ARG" -> "🇦🇷"
            "FRA" -> "🇫🇷"
            "ENG" -> "🇬🇧" // England representation for simplicity
            "ESP" -> "🇪🇸"
            "GER" -> "🇩🇪"
            "POR" -> "🇵🇹"
            "URU" -> "🇺🇾"
            "ITA" -> "🇮🇹"
            "NED" -> "🇳🇱"
            "BEL" -> "🇧🇪"
            "MEX" -> "🇲🇽"
            "USA" -> "🇺🇸"
            "JPN" -> "🇯🇵"
            "ALG" -> "🇩🇿"
            "MAR" -> "🇲🇦"
            "CAN" -> "🇨🇦"
            "CRO" -> "🇭🇷"
            "KSA" -> "🇸🇦"
            else -> "🏳️"
        }
    }

    @Composable
    fun Shield(
        code: String,
        modifier: Modifier = Modifier,
        size: Dp = 48.dp,
        fontSize: Float = 24f
    ) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0x1AFFFFFF))
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = getEmoji(code),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
