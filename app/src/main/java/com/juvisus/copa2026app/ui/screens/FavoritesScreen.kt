package com.juvisus.copa2026app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juvisus.copa2026app.data.FavoriteTeamEntity
import com.juvisus.copa2026app.ui.FootballViewModel
import com.juvisus.copa2026app.ui.NotificationAlert
import com.juvisus.copa2026app.ui.components.FlagHelper
import com.juvisus.copa2026app.ui.components.GlassCard
import com.juvisus.copa2026app.ui.theme.*

@Composable
fun FavoritesScreen(viewModel: FootballViewModel) {
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val scheduledAlerts by viewModel.scheduledAlerts.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val alertIntensity by viewModel.alertIntensity.collectAsState()
    val silenceQuietHours by viewModel.silenceQuietHours.collectAsState()
    val customFrequencyHours by viewModel.customFrequencyHours.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // List of selectables (excluding already added to avoid duplication)
    val availableTeams = listOf(
        "BRA" to "Brasil",
        "ARG" to "Argentina",
        "GER" to "Alemanha",
        "POR" to "Portugal",
        "ESP" to "Espanha",
        "FRA" to "França",
        "ENG" to "Inglaterra",
        "URU" to "Uruguai",
        "ITA" to "Itália",
        "NED" to "Holanda",
        "BEL" to "Bélgica"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
    ) {
        // Simple Top Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SELEÇÕES E ALERTAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = YellowGold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Configuração Alerta",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = YellowGold),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_favorite_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Favoritar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected teams visual pills list
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Suas Seleções Favoritas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    if (favoriteTeams.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x04FFFFFF))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma seleção adicionada. Clique em '+' para buscar.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.getDp()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Flow row simulation
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunked = favoriteTeams.chunked(3)
                                chunked.forEach { rowList ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        rowList.forEach { team ->
                                            FavoritePill(
                                                team = team,
                                                onDelete = { viewModel.removeFavorite(team.code, team.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Premium Customization Control Panel Card
            item {
                GlassCard(
                    cornerRadius = 24.dp,
                    borderWidth = 1.dp,
                    borderColor = Color(0x22FFFFFF),
                    backgroundColor = Color(0x13FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(YellowGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = YellowGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Configuração de Notificações",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (notificationsEnabled) "Lógica de envio inteligente ativa" else "Avisos totalmente desativados",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = YellowGold,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        if (notificationsEnabled) {
                            HorizontalDivider(color = Color(0x15FFFFFF))

                            // Intensity selector bar
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Intensidade dos Alertas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YellowGold,
                                    letterSpacing = 0.5.sp
                                )

                                val intensities = listOf(
                                    "Máxima (De hora em hora)",
                                    "Moderada (2h e 1h antes)",
                                    "Mínima (Apenas 10 min)"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    intensities.forEach { option ->
                                        val isSel = alertIntensity == option
                                        val optLabel = when {
                                            option.startsWith("Máxima") -> "Máxima 🚀"
                                            option.startsWith("Moderada") -> "Moderada ⚡"
                                            else -> "Mínima 🔔"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) YellowGold else Color(0x10FFFFFF))
                                                .clickable { viewModel.setAlertIntensity(option) }
                                                .wrapContentSize(Alignment.Center)
                                        ) {
                                            Text(
                                                text = optLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0x15FFFFFF))

                            // Quiet Hours Trigger Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Silêncio Noturno (Quiet Hours)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Silenciar avisos das 22:30 às 07:00",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                                Switch(
                                    checked = silenceQuietHours,
                                    onCheckedChange = { viewModel.setSilenceQuietHours(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = YellowGold,
                                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            }

                            if (alertIntensity == "Máxima (De hora em hora)") {
                                HorizontalDivider(color = Color(0x15FFFFFF))

                                // Custom alert frequency hours
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Frequência de Avisos",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Limitar antecedência máxima da contagem",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                        Text(
                                            text = "$customFrequencyHours horas antes",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = YellowGold
                                        )
                                    }

                                    Slider(
                                        value = customFrequencyHours.toFloat(),
                                        onValueChange = { viewModel.setCustomFrequencyHours(it.toInt()) },
                                        valueRange = 1f..5f,
                                        steps = 3,
                                        colors = SliderDefaults.colors(
                                            thumbColor = YellowGold,
                                            activeTrackColor = YellowGold,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "🔕 Notificações Gerais Desativadas. Ative o interruptor acima para agendar alertas baseados nas suas seleções.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Simulated notification list
            if (scheduledAlerts.isNotEmpty()) {
                item {
                    Text(
                        text = "Simulação Tempos de Alertas (${scheduledAlerts.size}) • Toque para disparar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(scheduledAlerts) { alert ->
                    AlertNotificationTile(
                        alert = alert,
                        onClick = { viewModel.triggerSimulatedNotification(alert) }
                    )
                }
            } else if (notificationsEnabled) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x06FFFFFF))
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum alerta pendente.\nFavorite seleções com jogos futuros para ver a agenda de pré-match.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal Selection Dialog for Adding Favorites
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Favoritar Seleção",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeFilters = favoriteTeams.map { it.code }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(availableTeams) { (code, name) ->
                            val alreadyFav = activeFilters.contains(code)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (alreadyFav) Color(0x1AFFFFFF) else Color.Transparent)
                                    .clickable(!alreadyFav) {
                                        viewModel.addFavorite(code, name)
                                        showAddDialog = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = FlagHelper.getFlagEmoji(code),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        color = if (alreadyFav) Color.White.copy(alpha = 0.5f) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (alreadyFav) {
                                    Text(
                                        text = "FAVORITO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = YellowGold
                                    )
                                } else {
                                    Text(
                                        text = "+ Favoritos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenAccent,
                                        modifier = Modifier.testTag("add_item_$code")
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = "CONCLUIR", color = YellowGold, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF13131A),
            textContentColor = Color.White
        )
    }
}

private fun Int.getDp(): androidx.compose.ui.unit.Dp = this.dp

@Composable
fun FavoritePill(team: FavoriteTeamEntity, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x19FFFFFF))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = FlagHelper.getFlagEmoji(team.code),
                fontSize = 16.sp
            )
            Text(
                text = team.name,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = RedAccent.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun AlertNotificationTile(alert: NotificationAlert, onClick: () -> Unit) {
    val borderColor = when {
        alert.isSilenced -> Color(0x11FFFFFF)
        alert.isUrgent -> Color(0xAAFF4D4D) // Red highlight border for final countdown
        else -> Color(0x19FFFFFF)
    }

    val iconContainerColor = when {
        alert.isSilenced -> Color(0x11FFFFFF)
        alert.isUrgent -> Color(0x40FF4D4D)
        else -> Color(0xFFE5A900).copy(alpha = 0.15f)
    }

    val iconColor = when {
        alert.isSilenced -> Color.White.copy(alpha = 0.4f)
        alert.isUrgent -> Color(0xFFFF4D4D)
        else -> YellowGold
    }

    val badgeColor = when {
        alert.isSilenced -> Color(0x1AFFFFFF)
        alert.isUrgent -> Color(0x40FF4D4D)
        else -> Color(0xFF00FF66).copy(alpha = 0.15f)
    }

    val badgeTextColor = when {
        alert.isSilenced -> Color.White.copy(alpha = 0.4f)
        alert.isUrgent -> Color(0xFFFF4D4D)
        else -> GreenAccent
    }

    val cardAlpha = if (alert.isSilenced) 0.55f else 1f

    GlassCard(
        borderWidth = if (alert.isUrgent) 1.5.dp else 1.dp,
        borderColor = borderColor,
        cornerRadius = 16.dp,
        backgroundColor = if (alert.isUrgent) Color(0x22FF4D4D) else Color(0x10FFFFFF),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.matchTitle,
                        color = Color.White.copy(alpha = cardAlpha),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (alert.isSilenced) "🔕 SILENCIADO" else alert.triggerTimeDescription,
                            color = badgeTextColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (alert.isSilenced) "Horário de Silêncio Ativo (22:30 - 07:00)" else "Transmissão: ${alert.broadcast}",
                    fontSize = 11.sp,
                    color = if (alert.isSilenced) Color.White.copy(alpha = 0.4f) else GreenAccent,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = alert.info,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = if (alert.isSilenced) 0.35f else 0.60f),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}
