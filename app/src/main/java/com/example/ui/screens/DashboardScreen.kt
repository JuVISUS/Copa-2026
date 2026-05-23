package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MatchEntity
import com.example.ui.FootballViewModel
import com.example.ui.components.FlagHelper
import com.example.ui.components.GlassCard
import com.example.ui.theme.BlueDeep
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenCard
import com.example.ui.theme.GreenDark
import com.example.ui.theme.YellowGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FootballViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()

    val context = LocalContext.current

    // Segregate BRAZIL matches and others
    val nextBrazilMatch = matches.firstOrNull { !it.isCompleted && (it.teamHomeCode == "BRA" || it.teamAwayCode == "BRA") }
    val favCodes = favoriteTeams.map { it.code }

    // Desta semana (Semana 1: June 11 to June 17, 2026)
    val matchesDestaSemana = matches.filter {
        !it.isCompleted && it.dateTimeEpoch >= 1781145600L && it.dateTimeEpoch < 1781750400L
    }.sortedBy { it.dateTimeEpoch }.take(6)

    // Semana seguinte (Semana 2: June 18 to June 24, 2026)
    val matchesSemanaSeguinte = matches.filter {
        !it.isCompleted && it.dateTimeEpoch >= 1781750400L && it.dateTimeEpoch < 1782355200L
    }.sortedBy { it.dateTimeEpoch }.take(6)

    // Completed matches for post-match summary
    val completedMatches = matches.filter { it.isCompleted }

    // Selected match for Summary details sheet
    var activeDetailMatch by remember { mutableStateOf<MatchEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GreenDark, BlueDeep)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASSISTENTE DA COPA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowGold,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FIFA World Cup 2026",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🇧🇷",
                                fontSize = 22.sp
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.triggerRefresh() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar Copa",
                            tint = Color.White
                        )
                    }
                }
            }

            // Copa Refresh Loading Overlay
            if (isRefreshing) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x3300FF66)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = GreenAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sincronizando FIFA, SofaScore & ESPN...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // O Próximo jogo do Brasil (CARD PRINCIPAL)
            item {
                Text(
                    text = "Destaque da Seleção 🇧🇷",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                nextBrazilMatch?.let { match ->
                    BrazilHighlightCard(match = match)
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sem próximos jogos programados para o Brasil.",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Agenda Semanal da Copa (SEGUNDO CARD - EXPANDIDO)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda Semanal da Copa 🏆",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    cornerRadius = 28.dp,
                    borderWidth = 1.dp,
                    borderColor = Color(0x19FFFFFF),
                    backgroundColor = Color(0x13FFFFFF),
                    elevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        // Title inside card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(YellowGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRONOGRAMA DE DUELOS COPA 2026",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YellowGold,
                                letterSpacing = 1.sp
                            )
                        }

                        // SUBCATEGORIA 1: “Próximos jogos desta semana”
                        Text(
                            text = "Próximos jogos desta semana",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        if (matchesDestaSemana.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum jogo programado para esta semana.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(matchesDestaSemana) { match ->
                                    HorizontalMatchItem(match = match)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        // SUBCATEGORIA 2: “Próximos jogos da semana seguinte”
                        Text(
                            text = "Próximos jogos da semana seguinte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        )

                        if (matchesSemanaSeguinte.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum jogo programado para a semana seguinte.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(matchesSemanaSeguinte) { match ->
                                    HorizontalMatchItem(match = match)
                                }
                            }
                        }
                    }
                }
            }

            // Resumos pós-jogo (TERCEIRO CARD)
            item {
                Text(
                    text = "Resumos e Estatísticas Pós-Jogo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (completedMatches.isEmpty()) {
                    Text(
                        text = "Nenhuma partida encerrada ainda nesta edição.",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (match in completedMatches) {
                            CompletedMatchCard(match = match, onClick = {
                                activeDetailMatch = match
                            })
                        }
                    }
                }
            }
        }

        // Floating Bottom Sheet/Dialog for detail viewing of completed matches (SofaScore / ESPN metrics)
        activeDetailMatch?.let { match ->
            AlertDialog(
                onDismissRequest = { activeDetailMatch = null },
                confirmButton = {
                    TextButton(onClick = { activeDetailMatch = null }) {
                        Text("Fechar", color = YellowGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(match.highlightsLink))
                        context.startActivity(intent)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Highlights", color = GreenAccent)
                        }
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = match.stage.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(match.teamHome, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${match.scoreHome} - ${match.scoreAway}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = YellowGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(match.teamAway, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Summary Text
                        Column {
                            Text("Resumo do Jogo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = YellowGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(match.summary, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 18.sp)
                        }

                        // Statistics
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Estatísticas d'A Partida", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = YellowGold)

                            // Possession
                            val posHome = match.statsPossession.split("-").first().replace("%", "").trim().toIntOrNull() ?: 50
                            StatBar(label = "Posse de Bola", homeVal = posHome, homeStr = "${posHome}%", awayStr = "${100 - posHome}%")

                            // Shots
                            val shotHome = match.statsShots.split("-").first().trim().toIntOrNull() ?: 10
                            val shotAway = match.statsShots.split("-").last().trim().toIntOrNull() ?: 10
                            StatBar(label = "Chutes ao Gol", homeVal = shotHome * 100 / (shotHome + shotAway).coerceAtLeast(1), homeStr = shotHome.toString(), awayStr = shotAway.toString())

                            // Fouls
                            val foulHome = match.statsFouls.split("-").first().trim().toIntOrNull() ?: 10
                            val foulAway = match.statsFouls.split("-").last().trim().toIntOrNull() ?: 10
                            StatBar(label = "Faltas Cometidas", homeVal = foulHome * 100 / (foulHome + foulAway).coerceAtLeast(1), homeStr = foulHome.toString(), awayStr = foulAway.toString())
                        }
                    }
                },
                containerColor = Color(0xFF131A13),
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

// ---------------- SUB COMPOSABLES ----------------

@Composable
fun BrazilHighlightCard(match: MatchEntity) {
    // Dynamic countdown timer
    var countdownText by remember { mutableStateOf("Calculando tempo...") }

    // Start countdown refresh
    LaunchedEffect(match.dateTimeEpoch) {
        while (true) {
            val nowSeconds = System.currentTimeMillis() / 1000L
            val difference = match.dateTimeEpoch - nowSeconds
            if (difference > 0) {
                val days = difference / (24 * 3600)
                val hours = (difference % (24 * 3600)) / 3600
                val minutes = (difference % 3600) / 60
                val seconds = difference % 60
                countdownText = String.format("%d dias e %02d:%02d:%02d", days, hours, minutes, seconds)
            } else {
                countdownText = "EM JOGO / AGORA"
            }
            delay(1000)
        }
    }

    val formatDayOfWeek = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("EEEE", Locale("pt", "BR"))
        sdf.format(Date(match.dateTimeEpoch * 1000L)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
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
        androidx.compose.ui.text.buildAnnotatedString {
            append("${match.stadium} • ${match.city} • ${match.country} • ")
            pushStyle(androidx.compose.ui.text.SpanStyle(color = GreenAccent, fontWeight = FontWeight.Bold))
            append(match.broadcast)
            pop()
        }
    }

    GlassCard(
        cornerRadius = 28.dp,
        borderWidth = 1.5.dp,
        borderColor = Color(0x33FFC72C), // Gold tinted glass border
        backgroundColor = GreenCard.copy(alpha = 0.5f),
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage/Stage Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE5A900).copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GreenAccent))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.stage.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowGold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Flags & Large Centered Time/Date Matchup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team Home (Brazil if Home)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    FlagHelper.Shield(code = match.teamHomeCode, size = 68.dp, fontSize = 36f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.teamHome,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = match.teamHomeCode,
                        color = YellowGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                // Centralized Time & Date (Most Highlighted Element)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.8f)
                ) {
                    Text(
                        text = formatedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = YellowGold,
                        letterSpacing = (-1).sp,
                        lineHeight = 44.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDayOfWeek,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDayAndMonth,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                // Team Away (Opponent)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    FlagHelper.Shield(code = match.teamAwayCode, size = 68.dp, fontSize = 36f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.teamAway,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = match.teamAwayCode,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            Divider(color = Color(0x19FFFFFF), modifier = Modifier.padding(vertical = 12.dp))

            // Discrete Bottom Line combining Stadium, City, Country and Broadcast
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = annotatedFooter,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // COUTDOWN DESIGN
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33101010)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CONTAGEM REGRESSIVA PARA O DUELO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = countdownText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = YellowGold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalMatchItem(match: MatchEntity, modifier: Modifier = Modifier) {
    val formatedTime = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val full = sdf.format(Date(match.dateTimeEpoch * 1000L))
        val shortWeek = when {
            full.startsWith("segunda") -> "Seg"
            full.startsWith("terça") -> "Ter"
            full.startsWith("quarta") -> "Qua"
            full.startsWith("quinta") -> "Qui"
            full.startsWith("sexta") -> "Sex"
            full.startsWith("sábado") -> "Sáb"
            full.startsWith("domingo") -> "Dom"
            else -> full.take(3)
        }
        val sdfHour = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
        "$shortWeek, ${sdfHour.format(Date(match.dateTimeEpoch * 1000L))}"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1BFFFFFF)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = modifier
            .width(160.dp)
            .height(148.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage/Group
            Text(
                text = match.stage.substringBefore(" -").uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = YellowGold,
                maxLines = 1,
                letterSpacing = 0.5.sp
            )

            // Dynamic Flags & VS row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlagHelper.Shield(code = match.teamHomeCode, size = 34.dp, fontSize = 18f)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "x",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlagHelper.Shield(code = match.teamAwayCode, size = 34.dp, fontSize = 18f)
            }

            // Names representing the teams
            Text(
                text = "${match.teamHome} x ${match.teamAway}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp
            )

            // Horizontal card time and broadcast line
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatedTime,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = match.broadcast.substringBefore(","),
                    fontSize = 9.sp,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MiniMatchCard(match: MatchEntity, modifier: Modifier = Modifier) {
    val formatedTime = remember(match.dateTimeEpoch) {
        val sdf = SimpleDateFormat("d 'de' MMM, HH:mm", Locale("pt", "BR"))
        sdf.format(Date(match.dateTimeEpoch * 1000L))
    }

    GlassCard(
        cornerRadius = 20.dp,
        modifier = modifier.height(150.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage / Competition
            Text(
                text = match.stage.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = YellowGold,
                maxLines = 1,
                letterSpacing = 0.5.sp
            )

            // Layout Team vs Team
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlagHelper.Shield(code = match.teamHomeCode, size = 32.dp, fontSize = 16f)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "x", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                FlagHelper.Shield(code = match.teamAwayCode, size = 32.dp, fontSize = 16f)
            }

            // Teams Names
            Text(
                text = "${match.teamHome} vs ${match.teamAway}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Date / Broadcast
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatedTime,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = match.broadcast,
                    fontSize = 9.sp,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CompletedMatchCard(match: MatchEntity, onClick: () -> Unit) {
    GlassCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Team Home
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlagHelper.Shield(code = match.teamHomeCode, size = 42.dp, fontSize = 20f)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = match.teamHome,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Board Score
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = match.stage.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = YellowGold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = match.scoreHome?.toString() ?: "0",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = " - ",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = match.scoreAway?.toString() ?: "0",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x13FFFFFF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Estatísticas",
                        tint = YellowGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VER RESUMO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = YellowGold)
                }
            }

            // Team Away
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlagHelper.Shield(code = match.teamAwayCode, size = 42.dp, fontSize = 20f)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = match.teamAway,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatBar(
    label: String,
    homeVal: Int,
    homeStr: String,
    awayStr: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
            Text(text = "$homeStr - $awayStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0x1AFFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeVal.coerceAtLeast(1).toFloat())
                    .background(YellowGold)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((100 - homeVal).coerceAtLeast(1).toFloat())
                    .background(GreenAccent)
            )
        }
    }
}
