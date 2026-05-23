package com.juvisus.copa2026app.ui.screens

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
import com.juvisus.copa2026app.data.MatchEntity
import com.juvisus.copa2026app.ui.FootballViewModel
import com.juvisus.copa2026app.ui.components.FlagHelper
import com.juvisus.copa2026app.ui.components.GlassCard
import com.juvisus.copa2026app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: FootballViewModel) {
    val matches by viewModel.matches.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var selectedFilter by remember { mutableStateOf("Todos") } // "Todos", "Favoritos", "Fase de Grupos", "Mata-Mata"

    val favoriteCodes = remember(favoriteTeams) { favoriteTeams.map { it.code } }

    val currentEpoch = remember { System.currentTimeMillis() / 1000L }
    val oneWeek = 7 * 86400L

    // BLOCK 1: Brazil Highlight Card (Próximo ou Último oficial do Brasil)
    val brazilHighlight = remember(matches) {
        val brMatches = matches.filter { it.teamHomeCode == "BRA" || it.teamAwayCode == "BRA" }
            .sortedBy { it.dateTimeEpoch }
        val nextMatch = brMatches.firstOrNull { it.dateTimeEpoch >= currentEpoch && !it.isCompleted }
        nextMatch ?: brMatches.lastOrNull()
    }

    val filteredMatches = remember(matches, selectedFilter, favoriteCodes, brazilHighlight) {
        matches.filter { match ->
            // Exclude Brazil Highlight from secondary blocks to avoid repeating
            if (brazilHighlight != null && match.id == brazilHighlight.id) return@filter false

            when (selectedFilter) {
                "Favoritos" -> {
                    favoriteCodes.contains(match.teamHomeCode) || favoriteCodes.contains(match.teamAwayCode) || match.teamHomeCode == "BRA"
                }
                "Fase de Grupos" -> match.stage.contains("Grupo") || match.stage.contains("Grupos")
                "Mata-Mata" -> !match.stage.contains("Grupo") && !match.stage.contains("Grupos") && !match.stage.contains("Amistoso")
                else -> true
            }
        }
    }

    // BLOCK 2: Semana Atual (excluindo highlight)
    val matchesThisWeek = remember(filteredMatches) {
        filteredMatches.filter { it.dateTimeEpoch >= currentEpoch && it.dateTimeEpoch < currentEpoch + oneWeek && !it.isCompleted }
    }

    // BLOCK 3: Próxima Semana (excluindo highlight)
    val matchesLater = remember(filteredMatches) {
        filteredMatches.filter { it.dateTimeEpoch >= currentEpoch + oneWeek && it.dateTimeEpoch < currentEpoch + (2 * oneWeek) && !it.isCompleted }
    }

    // BLOCK 4: Resultados Recentes do campeonato / Copa (completados ou passados)
    val matchesCompleted = remember(filteredMatches) {
        filteredMatches.filter { it.isCompleted || it.dateTimeEpoch < currentEpoch }.sortedByDescending { it.dateTimeEpoch }
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
                .padding(horizontal = 20.dp, vertical = 20.dp)
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
                            .background((if (isSyncing) YellowGold else GreenAccent).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { viewModel.syncRealMatchesFromInternet() }
                    ) {
                        Text(
                            text = if (isSyncing) "SYNCING" else "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSyncing) YellowGold else GreenAccent,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Small loading indicators for syncs
                if (isSyncing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = YellowGold,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
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

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BLOCK 1 item: Brazil Highlight Card (Always present at the very top of lists)
            if (brazilHighlight != null) {
                item {
                    BrazilHighlightCard(
                        match = brazilHighlight,
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

            // BLOCK 2: Semana Atual
            if (matchesThisWeek.isNotEmpty()) {
                stickyHeader {
                    SectionHeader(title = "Segundo Bloco: Jogos de Hoje/Semana")
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

            // BLOCK 3: Semana Seguinte
            if (matchesLater.isNotEmpty()) {
                stickyHeader {
                    SectionHeader(title = "Terceiro Bloco: Próxima Semana")
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

            // BLOCK 4: Resultados Recentes (Copa e amigáveis concluídos)
            if (matchesCompleted.isNotEmpty()) {
                stickyHeader {
                    SectionHeader(title = "Quarto Bloco: Resultados Recentes")
                }
                items(matchesCompleted, key = { it.id }) { match ->
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

@Composable
fun BrazilHighlightCard(
    match: MatchEntity,
    favoriteCodes: List<String>,
    onToggleFavorite: (String, String) -> Unit
) {
    val isUpcoming = match.dateTimeEpoch >= System.currentTimeMillis() / 1000L && !match.isCompleted

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

    val isHomeFav = favoriteCodes.contains(match.teamHomeCode)
    val isAwayFav = favoriteCodes.contains(match.teamAwayCode)

    GlassCard(
        cornerRadius = 24.dp,
        borderWidth = 2.dp,
        borderColor = YellowGold.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.12f),
                            YellowGold.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Card Title badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUpcoming) YellowGold else GreenAccent)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isUpcoming) "PRÓXIMO JOGO OFICIAL DO BRASIL 🇧🇷" else "ÚLTIMO JOGO OFICIAL DO BRASIL 🇧🇷",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = match.stage.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Central: Competitors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = FlagHelper.getFlagEmoji(match.teamHomeCode),
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamHome,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Verses / Scores
                Column(
                    modifier = Modifier.width(110.dp),
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
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "x",
                                fontSize = 16.sp,
                                color = YellowGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${match.scoreAway ?: 0}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "FIM DE JOGO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = formatedTime,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = YellowGold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formatDayOfWeek,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formatDayAndMonth,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Away
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = FlagHelper.getFlagEmoji(match.teamAwayCode),
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamAway,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Custom Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            // Venue Details & Transmission info in Highlight style
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏟️ ${match.stadium} • ${match.city}, ${match.country}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 Transmissão: ${match.broadcast}",
                        fontSize = 10.sp,
                        color = GreenAccent,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
