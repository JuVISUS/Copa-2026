package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.ui.FootballViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.BlueDeep
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenCard
import com.example.ui.theme.GreenDark
import com.example.ui.theme.YellowGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: FootballViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val suggestionChips = listOf(
        "Próximo jogo do Brasil 🇧🇷",
        "Onde assistir os jogos? 📺",
        "Resultados e Resumos 📊",
        "Estádios da Copa 🏟️",
        "Favoritos ao Hexa ⭐"
    )

    // Scroll to latest message on add
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GreenDark, BlueDeep)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ASSISTENTE VIRTUAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowGold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Chat Inteligente Copa 2026",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Clear Chat History
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x19FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar Conversa",
                        tint = Color.White
                    )
                }
            }

            // Message Board
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(chatMessages) { chat ->
                    ChatMessageItem(chat = chat)
                }

                if (isChatLoading) {
                    item {
                        AssistantTypingItem()
                    }
                }
            }

            // Suggestion Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionChips) { chipText ->
                    SuggestionChipItem(text = chipText, onClick = {
                        viewModel.onChatInputChange(chipText)
                        viewModel.sendChatMessage()
                    })
                }
            }

            // Input Row
            GlassCard(
                cornerRadius = 32.dp,
                borderWidth = 1.dp,
                borderColor = Color(0x33FFFFFF),
                backgroundColor = Color(0x1A0D1F1D),
                elevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 82.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatInput,
                        onValueChange = { viewModel.onChatInputChange(it) },
                        placeholder = {
                            Text(
                                "Pergunte sobre horários, estádios, transmissões...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.sendChatMessage()
                                keyboardController?.hide()
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendChatMessage()
                            keyboardController?.hide()
                        },
                        enabled = chatInput.isNotBlank() && !isChatLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (chatInput.isNotBlank()) YellowGold else Color(0x19FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (chatInput.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(chat: ChatMessageEntity) {
    val bubbleShape = if (chat.isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val containerColor = if (chat.isUser) {
        Color(0xFFE5A900).copy(alpha = 0.15f) // Gold transparent tint
    } else {
        Color(0x1CFFFFFF) // Silver glass bubble
    }

    val alignment = if (chat.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val textColor = if (chat.isUser) YellowGold else Color.White

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (chat.isUser) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (chat.isUser) Arrangement.End else Arrangement.Start,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = if (chat.isUser) "VOCÊ 🇧🇷" else "AISTUDIO COPA ASSISTENT 🏆",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(containerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = chat.message,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (chat.isUser) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AssistantTypingItem() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            Text(
                text = "PESQUISANDO DIRETAMENTE NAS FONTES...",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = GreenAccent,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = GreenAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Elaborando resposta inteligente...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SuggestionChipItem(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0x1E00FF66),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300FF66)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
