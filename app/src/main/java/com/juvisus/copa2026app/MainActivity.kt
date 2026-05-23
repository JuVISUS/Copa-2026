package com.juvisus.copa2026app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.juvisus.copa2026app.data.AppDatabase
import com.juvisus.copa2026app.data.FootballRepository
import com.juvisus.copa2026app.ui.FootballViewModel
import com.juvisus.copa2026app.ui.screens.ChatScreen
import com.juvisus.copa2026app.ui.screens.DashboardScreen
import com.juvisus.copa2026app.ui.screens.FavoritesScreen
import com.juvisus.copa2026app.ui.theme.CopaAlerta2026Theme
import com.juvisus.copa2026app.ui.theme.MidnightBg
import com.juvisus.copa2026app.ui.theme.YellowGold

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: FootballRepository
    private lateinit var viewModel: FootballViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room DB
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "alerta_copa_database_v2"
        ).fallbackToDestructiveMigration().build()

        repository = FootballRepository(database)
        viewModel = FootballViewModel(application, repository)

        setContent {
            CopaAlerta2026Theme {
                var currentTab by remember { mutableStateOf(0) } // 0 = Agenda, 1 = Alertas, 2 = Chat IA

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF131317),
                            contentColor = Color.White,
                            tonalElevation = 8.dp,
                            modifier = Modifier.height(72.dp).testTag("bottom_selector_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Partidas"
                                    )
                                },
                                label = { Text("Agenda", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = YellowGold,
                                    indicatorColor = YellowGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("tab_agenda")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alertas"
                                    )
                                },
                                label = { Text("Alertas", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = YellowGold,
                                    indicatorColor = YellowGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("tab_alertas")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Chat IA"
                                    )
                                },
                                label = { Text("Chat IA", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = YellowGold,
                                    indicatorColor = YellowGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("tab_chat")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MidnightBg)
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(viewModel = viewModel)
                            1 -> FavoritesScreen(viewModel = viewModel)
                            2 -> ChatScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
