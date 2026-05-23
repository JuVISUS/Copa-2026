package com.example.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessageEntity
import com.example.data.FavoriteTeamEntity
import com.example.data.FootballRepository
import com.example.data.MatchEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FootballTab {
    DASHBOARD,
    CHAT,
    FAVORITES
}

data class NotificationAlert(
    val matchTitle: String,
    val info: String,
    val triggerTimeDescription: String,
    val broadcast: String,
    val isUrgent: Boolean = false,
    val isSilenced: Boolean = false
)

class FootballViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FootballRepository(application)

    private val _currentTab = MutableStateFlow(FootballTab.DASHBOARD)
    val currentTab: StateFlow<FootballTab> = _currentTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    val matches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTeams: StateFlow<List<FavoriteTeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customizable User Notifications Preferences
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _alertIntensity = MutableStateFlow("Máxima (De hora em hora)") // "Máxima (De hora em hora)", "Moderada (2h e 1h antes)", "Mínima (Apenas 10 min)"
    val alertIntensity: StateFlow<String> = _alertIntensity.asStateFlow()

    private val _silenceQuietHours = MutableStateFlow(true) // Silencia entre 22:30 e 07:00
    val silenceQuietHours: StateFlow<Boolean> = _silenceQuietHours.asStateFlow()

    private val _customFrequencyHours = MutableStateFlow(5) // Frequência de contagem: a partir de 5 horas antes
    val customFrequencyHours: StateFlow<Int> = _customFrequencyHours.asStateFlow()

    // Simulated alerts scheduled dynamically based on user favorites
    private val _scheduledAlerts = MutableStateFlow<List<NotificationAlert>>(emptyList())
    val scheduledAlerts: StateFlow<List<NotificationAlert>> = _scheduledAlerts.asStateFlow()

    // Supported global teams for selection
    val availableTeams = listOf(
        FavoriteTeamEntity("BRA", "Brasil", 0),
        FavoriteTeamEntity("POR", "Portugal", 0),
        FavoriteTeamEntity("ARG", "Argentina", 0),
        FavoriteTeamEntity("FRA", "França", 0),
        FavoriteTeamEntity("GER", "Alemanha", 0),
        FavoriteTeamEntity("ESP", "Espanha", 0),
        FavoriteTeamEntity("ENG", "Inglaterra", 0),
        FavoriteTeamEntity("ITA", "Itália", 0),
        FavoriteTeamEntity("NED", "Holanda", 0),
        FavoriteTeamEntity("BEL", "Bélgica", 0),
        FavoriteTeamEntity("URU", "Uruguai", 0),
        FavoriteTeamEntity("MEX", "México", 0),
        FavoriteTeamEntity("USA", "EUA", 0),
        FavoriteTeamEntity("JPN", "Japão", 0)
    )

    init {
        viewModelScope.launch {
            repository.initDatabaseIfNeeded()
            generateNotificationFlow()
        }
    }

    fun selectTab(tab: FootballTab) {
        _currentTab.value = tab
    }

    fun onChatInputChange(text: String) {
        _chatInput.value = text
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulate Copa live dynamic sync with SofaScore / FIFA servers!
            delay(1500)
            _isRefreshing.value = false
            Toast.makeText(getApplication(), "Dados da Copa 2026 sincronizados ao vivo!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendChatMessage() {
        val query = _chatInput.value.trim()
        if (query.isEmpty()) return

        _chatInput.value = ""
        _isChatLoading.value = true

        viewModelScope.launch {
            repository.sendChatMessage(query)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun toggleFavoriteTeam(team: FavoriteTeamEntity) {
        viewModelScope.launch {
            val currentFavs = favoriteTeams.first()
            val exists = currentFavs.any { it.code == team.code }
            if (exists) {
                repository.removeFavoriteTeam(team.code)
                Toast.makeText(getApplication(), "${team.name} removido dos favoritos", Toast.LENGTH_SHORT).show()
            } else {
                repository.addFavoriteTeam(team.code, team.name)
                Toast.makeText(getApplication(), "${team.name} adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
            }
            generateNotificationFlow()
        }
    }

    fun moveTeamPriorityUp(team: FavoriteTeamEntity) {
        viewModelScope.launch {
            val list = favoriteTeams.first().toMutableList()
            val index = list.indexOfFirst { it.code == team.code }
            if (index > 0) {
                // Swap priorities
                val upper = list[index - 1]
                list[index - 1] = team.copy(priority = upper.priority)
                list[index] = upper.copy(priority = team.priority)
                repository.saveFavoriteTeams(list)
                Toast.makeText(getApplication(), "Prioridade atualizada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveTeamPriorityDown(team: FavoriteTeamEntity) {
        viewModelScope.launch {
            val list = favoriteTeams.first().toMutableList()
            val index = list.indexOfFirst { it.code == team.code }
            if (index != -1 && index < list.size - 1) {
                val lower = list[index + 1]
                list[index + 1] = team.copy(priority = lower.priority)
                list[index] = lower.copy(priority = team.priority)
                repository.saveFavoriteTeams(list)
                Toast.makeText(getApplication(), "Prioridade atualizada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch { generateNotificationFlow() }
    }

    fun setAlertIntensity(intensity: String) {
        _alertIntensity.value = intensity
        viewModelScope.launch { generateNotificationFlow() }
    }

    fun setSilenceQuietHours(silence: Boolean) {
        _silenceQuietHours.value = silence
        viewModelScope.launch { generateNotificationFlow() }
    }

    fun setCustomFrequencyHours(hours: Int) {
        _customFrequencyHours.value = hours
        viewModelScope.launch { generateNotificationFlow() }
    }

    private fun isQuietHour(triggerHour: Int, triggerMinute: Int): Boolean {
        if (!_silenceQuietHours.value) return false
        val totalMinutes = triggerHour * 60 + triggerMinute
        // 22:30 is 1350 mins. 07:00 is 420 mins.
        return totalMinutes >= 1350 || totalMinutes < 420
    }

    private suspend fun generateNotificationFlow() {
        if (!_notificationsEnabled.value) {
            _scheduledAlerts.value = emptyList()
            return
        }

        val favTeamsCodes = favoriteTeams.first().map { it.code }
        val allScheduledMatches = matches.first()
        val alerts = mutableListOf<NotificationAlert>()

        // For each upcoming match where at least one of the teams is favorited, trigger notifications!
        for (match in allScheduledMatches) {
            val matchesFav = favTeamsCodes.contains(match.teamHomeCode) || favTeamsCodes.contains(match.teamAwayCode) || match.teamHomeCode == "BRA"
            if (!match.isCompleted && matchesFav) {
                val matchTitle = "${match.teamHome} x ${match.teamAway}"
                val broadcast = match.broadcast
                val stadiumStr = "${match.stadium} (${match.city})"

                val gameCal = java.util.Calendar.getInstance().apply {
                    timeInMillis = match.dateTimeEpoch * 1000L
                }
                val gameHour = gameCal.get(java.util.Calendar.HOUR_OF_DAY)
                val gameMinute = gameCal.get(java.util.Calendar.MINUTE)

                val formatTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR")).format(java.util.Date(match.dateTimeEpoch * 1000L))
                val formatDate = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale("pt", "BR")).format(java.util.Date(match.dateTimeEpoch * 1000L))

                // 1) NOTIFICAÇÃO 2 DIAS ANTES
                val is2DaysSilenced = isQuietHour(gameHour, gameMinute)
                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "Jogaço no dia $formatDate às $formatTime no $stadiumStr! Transmissão ao vivo na $broadcast.",
                    triggerTimeDescription = "2 dias antes",
                    broadcast = broadcast,
                    isUrgent = false,
                    isSilenced = is2DaysSilenced
                ))

                // 2) NOTIFICAÇÕES 1 DIA ANTES (DUAS NOTIFICAÇÕES)
                // 1ª notificação: entre 15:00 e 16:00
                val is1Day15hSilenced = isQuietHour(15, 30)
                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "Contagem regressiva: Amanhã tem $matchTitle às $formatTime! Transmissão: $broadcast no $stadiumStr.",
                    triggerTimeDescription = "1 dia antes (15:30)",
                    broadcast = broadcast,
                    isUrgent = false,
                    isSilenced = is1Day15hSilenced
                ))

                // 2ª notificação: entre 22:30 e 23:00
                val is1Day22hSilenced = isQuietHour(22, 45)
                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "[Urgente] É amanhã o grande dia! Sintonize na $broadcast às $formatTime pra torcer muito. Jogo no $stadiumStr.",
                    triggerTimeDescription = "1 dia antes (22:45)",
                    broadcast = broadcast,
                    isUrgent = false,
                    isSilenced = is1Day22hSilenced
                ))

                // 3) NOTIFICAÇÕES NO DIA DO JOGO (DE HORA EM HORA, A PARTIR DE 5 HORAS ANTES)
                val intensity = _alertIntensity.value
                if (intensity != "Mínima (Apenas 10 min)") {
                    val maxHours = _customFrequencyHours.value
                    for (h in maxHours downTo 1) {
                        // If intensity is Moderada, only include 2h and 1h options
                        if (intensity == "Moderada (2h e 1h antes)" && h > 2) {
                            continue
                        }

                        // Calculate trigger time
                        val triggerCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = (match.dateTimeEpoch * 1000L) - (h * 3600_000L)
                        }
                        val triggerH = triggerCal.get(java.util.Calendar.HOUR_OF_DAY)
                        val triggerM = triggerCal.get(java.util.Calendar.MINUTE)
                        val isHourSilenced = isQuietHour(triggerH, triggerM)

                        val infoMsg = when (h) {
                            5 -> "Tensão pré-jogo: Faltam 5 horas para $matchTitle! Preparativos de elite no $stadiumStr."
                            4 -> "Concentração total: Faltam 4 horas para a bola rolar! Sintonize na $broadcast."
                            3 -> "Vestiários abertos: Faltam 3 horas! As seleções estão chegando ao $stadiumStr."
                            2 -> "Aquecimento iniciado: Apenas 2 horas para $matchTitle! Transmissão garantida por $broadcast."
                            else -> "Escalações confirmadas: 1 hora para o apito inicial de $matchTitle! Quem leva a melhor hoje?"
                        }

                        alerts.add(NotificationAlert(
                            matchTitle = matchTitle,
                            info = infoMsg,
                            triggerTimeDescription = "Faltam $h horas",
                            broadcast = broadcast,
                            isUrgent = false,
                            isSilenced = isHourSilenced
                        ))
                    }
                }

                // 4) NOTIFICAÇÃO FINAL (10 MINUTOS ANTES)
                val triggerCal10 = java.util.Calendar.getInstance().apply {
                    timeInMillis = (match.dateTimeEpoch * 1000L) - (10 * 60_000L)
                }
                val triggerH10 = triggerCal10.get(java.util.Calendar.HOUR_OF_DAY)
                val triggerM10 = triggerCal10.get(java.util.Calendar.MINUTE)
                val is10Silenced = isQuietHour(triggerH10, triggerM10)

                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "$matchTitle começa em 10 minutos • Assista na $broadcast • Estádio ${match.stadium}",
                    triggerTimeDescription = "10 minutos antes (Urgente)",
                    broadcast = broadcast,
                    isUrgent = true,
                    isSilenced = is10Silenced
                ))
            }
        }

        _scheduledAlerts.value = alerts
    }

    fun triggerSimulatedNotification(alert: NotificationAlert) {
        val msg = if (alert.isSilenced) {
            "🔕 [Modo Silencioso Ativo] O alerta seria silenciado entre 22:30 e 07:00.\n${alert.matchTitle}: ${alert.info}"
        } else if (alert.isUrgent) {
            "🔥 ${alert.info}"
        } else {
            "🛎️ [Copa - ${alert.triggerTimeDescription}]\n${alert.info}"
        }
        Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show()
    }
}

data class LineupTeam(
    val code: String,
    val name: String
)
