package com.copa.alerta2026.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copa.alerta2026.data.MatchEntity
import com.copa.alerta2026.ui.FootballViewModel
import com.copa.alerta2026.ui.components.FlagHelper
import com.copa.alerta2026.ui.components.GlassCard
import com.copa.alerta2026.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: FootballViewModel) {
    val matches by viewModel.matches.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()

    var selectedFilter by remember { mutableStateOf("Todos") } // "Todos", "Favoritos", "Fase de Grupos", "Mata-Mata"

    val favoriteCodes = remember(favoriteTeams) { favoriteTeams.map { it.code } }

    val filteredMatches = remember(matches, selectedFilter, favoriteCodes) {
        matches.filter { match ->
            when (selectedFilter) {
                "Favoritos" -> {
                    favoriteCodes.contains(match.teamHomeCode) || favoriteCodes.contains(match.teamAwayCode) || match.teamHomeCode == "BRA"
                }
                "Fase de Grupos" -> match.stage == "Fase de Grupos"
                "Mata-Mata" -> match.stage != "Fase de Grupos"
                else -> true
            }
        }
    }

    val currentEpoch = System.currentTimeMillis() / 1000L
    val oneWeek = 7 * 86400L

    val matchesThisWeek = remember(filteredMatches, currentEpoch) {
        filteredMatches.filter { it.dateTimeEpoch < currentEpoch + oneWeek }
    }

    val matchesLater = remember(filteredMatches, currentEpoch) {
        filteredMatches.filter { it.dateTimeEpoch >= currentEpoch + oneWeek }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
    ) {
        // Upper Golden Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(YellowGold.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AGENDA COPA 2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowGold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Partidas & Alertas",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GreenAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filters Segmented Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Todos", "Favoritos", "Fase de Grupos", "Mata-Mata")
                    filters.forEach { filter ->
                        val isSel = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) YellowGold else Color(0x0EFFFFFF))
                                .clickable { selectedFilter = filter }
                                .wrapContentSize(Alignment.Center)
                                .testTag("filter_tab_$filter")
                        ) {
                            Text(
                                text = filter,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔕",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Nenhuma partida agendada com filtros atuais.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (matchesThisWeek.isNotEmpty()) {
                    stickyHeader {
                        SectionHeader(title = "Desta Semana")
                    }
                    items(matchesThisWeek, key = { it.id }) { match ->
                        MatchCardItem(
                            match = match,
                            favoriteCodes = favoriteCodes,
                            onToggleFavorite = { code, name ->
                                if (favoriteCodes.contains(code)) {
                                    viewModel.removeFavorite(code, name)
                                } else {
                                    viewModel.addFavorite(code, name)
                                }
                            }
                        )
                    }
                }

                if (matchesLater.isNotEmpty()) {
                    stickyHeader {
                        SectionHeader(title = "Semana Seguinte")
                    }
                    items(matchesLater, key = { it.id }) { match ->
                        MatchCardItem(
                            match = match,
                            favoriteCodes = favoriteCodes,
                            onToggleFavorite = { code, name ->
                                if (favoriteCodes.contains(code)) {
                                    viewModel.removeFavorite(code, name)
                                } else {
                                    viewModel.addFavorite(code, name)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightBg)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = YellowGold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    favoriteCodes: List<String>,
    onToggleFavorite: (String, String) -> Unit
) {
    val formatDayOfWeek = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("EEEE", Locale("pt", "BR"))
        sdf.format(Date(match.dateTimeEpoch * 1000L))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
    }

    val formatDayAndMonth = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("d 'de' MMMM", Locale("pt", "BR"))
        sdf.format(Date(match.dateTimeEpoch * 1000L))
    }

    val formatedTime = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
        sdf.format(Date(match.dateTimeEpoch * 1000L))
    }

    val annotatedFooter = remember(match.stadium, match.city, match.country, match.broadcast) {
        buildAnnotatedString {
            append("${match.stadium} • ${match.city} • ${match.country} • ")
            pushStyle(SpanStyle(color = GreenAccent, fontWeight = FontWeight.Bold))
            append(match.broadcast)
            pop()
        }
    }

    val isHomeFav = favoriteCodes.contains(match.teamHomeCode)
    val isAwayFav = favoriteCodes.contains(match.teamAwayCode)

    GlassCard(
        cornerRadius = 28.dp,
        borderWidth = 1.5.dp,
        borderColor = if (isHomeFav || isAwayFav) YellowGold.copy(alpha = 0.4f) else GlassBorder,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Stage Indicator and Header Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x0DFFFFFF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = match.stage.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Star toggles to flag teams quickly
                    IconButton(
                        onClick = { onToggleFavorite(match.teamHomeCode, match.teamHome) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isHomeFav) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (isHomeFav) YellowGold else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onToggleFavorite(match.teamAwayCode, match.teamAway) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isAwayFav) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (isAwayFav) YellowGold else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Central Game Row (Team vs Team + flags)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team Home
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = FlagHelper.getFlagEmoji(match.teamHomeCode),
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamHome,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score or Time Central layout
                Column(
                    modifier = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (match.isCompleted) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${match.scoreHome ?: 0}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "x",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${match.scoreAway ?: 0}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "FINALIZADO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            text = formatedTime,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = YellowGold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDayOfWeek,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formatDayAndMonth,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Team Away
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = FlagHelper.getFlagEmoji(match.teamAwayCode),
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamAway,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Stadium and Broadcast Info Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = annotatedFooter,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
