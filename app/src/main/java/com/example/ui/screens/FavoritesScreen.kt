package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoriteTeamEntity
import com.example.ui.FootballViewModel
import com.example.ui.NotificationAlert
import com.example.ui.components.FlagHelper
import com.example.ui.components.GlassCard
import com.example.ui.theme.BlueDeep
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenDark
import com.example.ui.theme.YellowGold

@Composable
fun FavoritesScreen(
    viewModel: FootballViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val scheduledAlerts by viewModel.scheduledAlerts.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val alertIntensity by viewModel.alertIntensity.collectAsState()
    val silenceQuietHours by viewModel.silenceQuietHours.collectAsState()
    val customFrequencyHours by viewModel.customFrequencyHours.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(top = 16.dp, bottom = 82.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "ALERTAS INTELIGENTES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowGold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Favoritos & Notificações",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Add Team Button
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(YellowGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Seleção",
                            tint = Color.Black
                        )
                    }
                }
            }

            // Premium Customization Panel Control Card
            item {
                GlassCard(
                    cornerRadius = 24.dp,
                    borderWidth = 1.dp,
                    borderColor = Color(0x22FFFFFF),
                    backgroundColor = Color(0x13FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
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
                            Divider(color = Color(0x15FFFFFF))

                            // Intensity Select
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

                            Divider(color = Color(0x15FFFFFF))

                            // Quiet Hours Toggle
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
                                Divider(color = Color(0x15FFFFFF))

                                // Frequencia Customizada (De hora em hora)
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

            // Favorite Teams List Title
            item {
                Text(
                    text = "Suas Seleções Favoritas (${favoriteTeams.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (favoriteTeams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Você desmarcou todos os favoritos. Toque no botão [+] acima para adicionar seleções e receber alertas!",
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(favoriteTeams) { team ->
                    FavoriteTeamItem(
                        team = team,
                        onMoveUp = { viewModel.moveTeamPriorityUp(team) },
                        onMoveDown = { viewModel.moveTeamPriorityDown(team) },
                        onRemove = { viewModel.toggleFavoriteTeam(team) }
                    )
                }
            }

            // CRONOGRAMA DE ALERTAS ATIVOS
            item {
                Text(
                    text = "Simulador de Alertas de Jogos Active Stream",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "Toque em qualquer alerta abaixo para disparar uma simulação de push imediata!",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            if (scheduledAlerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum jogo favoritado programado. Adicione seleções aos favoritos!",
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(scheduledAlerts) { alert ->
                    AlertNotificationTile(alert = alert, onClick = {
                        viewModel.triggerSimulatedNotification(alert)
                    })
                }
            }
        }

        // Add Favorite selection list popup/dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Pronto", color = YellowGold)
                    }
                },
                title = {
                    Text(
                        text = "Favoritar Seleções Copa 2026",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        Text(
                            text = "Selecione as seleções importantes para ver no Dashboard e disparar alertas:",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewModel.availableTeams) { option ->
                                val isSelected = favoriteTeams.any { it.code == option.code }
                                TeamSelectionRow(
                                    team = option,
                                    isSelected = isSelected,
                                    onSelectToggled = {
                                        viewModel.toggleFavoriteTeam(option)
                                    }
                                )
                            }
                        }
                    }
                },
                containerColor = Color(0xFF131317),
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun FavoriteTeamItem(
    team: FavoriteTeamEntity,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    GlassCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Flag and Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagHelper.Shield(code = team.code, size = 44.dp, fontSize = 22f)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = team.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Prioridade: #${team.priority}",
                        color = YellowGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Controls & Up Down arrows
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Up
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x13FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Mover para Cima",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Down
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x13FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Mover para Baixo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete
                Text(
                    text = "REMOVER",
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onRemove() }
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                )
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.width(12.dp))

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

@Composable
fun TeamSelectionRow(
    team: FavoriteTeamEntity,
    isSelected: Boolean,
    onSelectToggled: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x19FFFFFF) else Color.Transparent)
            .clickable { onSelectToggled() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlagHelper.Shield(code = team.code, size = 32.dp, fontSize = 16f)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = team.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectToggled() },
            colors = CheckboxDefaults.colors(
                checkedColor = YellowGold,
                uncheckedColor = Color.White.copy(alpha = 0.4f),
                checkmarkColor = Color.Black
            )
        )
    }
}
