package com.copa.alerta2026.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.copa.alerta2026.BuildConfig
import com.copa.alerta2026.data.ChatMessageEntity
import com.copa.alerta2026.data.FavoriteTeamEntity
import com.copa.alerta2026.data.FootballRepository
import com.copa.alerta2026.data.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

data class NotificationAlert(
    val matchTitle: String,
    val info: String,
    val triggerTimeDescription: String,
    val broadcast: String,
    val isUrgent: Boolean = false,
    val isSilenced: Boolean = false
)

class FootballViewModel(
    application: Application,
    private val repository: FootballRepository
) : AndroidViewModel(application) {

    val matches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTeams: StateFlow<List<FavoriteTeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _alertIntensity = MutableStateFlow("Máxima (De hora em hora)")
    val alertIntensity: StateFlow<String> = _alertIntensity.asStateFlow()

    private val _silenceQuietHours = MutableStateFlow(true)
    val silenceQuietHours: StateFlow<Boolean> = _silenceQuietHours.asStateFlow()

    private val _customFrequencyHours = MutableStateFlow(5)
    val customFrequencyHours: StateFlow<Int> = _customFrequencyHours.asStateFlow()

    private val _scheduledAlerts = MutableStateFlow<List<NotificationAlert>>(emptyList())
    val scheduledAlerts: StateFlow<List<NotificationAlert>> = _scheduledAlerts.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if matches are empty, if so, populate default Copa matches
            matches.collectLatest { list ->
                if (list.isEmpty()) {
                    populateDefaultMatches()
                } else {
                    generateNotificationFlow()
                }
            }
        }

        viewModelScope.launch {
            favoriteTeams.collectLatest {
                generateNotificationFlow()
            }
        }
    }

    private suspend fun populateDefaultMatches() {
        val currentDate = System.currentTimeMillis() / 1000L
        val oneDay = 86400L

        val defaultList = listOf(
            MatchEntity(
                id = 1,
                teamHome = "Brasil", teamHomeCode = "BRA",
                teamAway = "Argentina", teamAwayCode = "ARG",
                dateTimeEpoch = currentDate + (2 * oneDay) + 7200, // 2 days from now + 2 hours
                isCompleted = false,
                stage = "Fase de Grupos",
                stadium = "Estádio Azteca", city = "Cidade do México", country = "México",
                broadcast = "TV Globo"
            ),
            MatchEntity(
                id = 2,
                teamHome = "Portugal", teamHomeCode = "POR",
                teamAway = "França", teamAwayCode = "FRA",
                dateTimeEpoch = currentDate + (3 * oneDay) + 14400, // 3 days from now
                isCompleted = false,
                stage = "Fase de Grupos",
                stadium = "MetLife Stadium", city = "Nova York", country = "EUA",
                broadcast = "SporTV"
            ),
            MatchEntity(
                id = 3,
                teamHome = "Alemanha", teamHomeCode = "GER",
                teamAway = "Espanha", teamAwayCode = "ESP",
                dateTimeEpoch = currentDate + (5 * oneDay),
                isCompleted = false,
                stage = "Fase de Grupos",
                stadium = "SoFi Stadium", city = "Los Angeles", country = "EUA",
                broadcast = "CazéTV"
            ),
            MatchEntity(
                id = 4,
                teamHome = "Uruguai", teamHomeCode = "URU",
                teamAway = "Itália", teamAwayCode = "ITA",
                dateTimeEpoch = currentDate - (1 * oneDay), // Yesterday (completed)
                scoreHome = 2, scoreAway = 1,
                isCompleted = true,
                stage = "Fase de Grupos",
                stadium = "Hard Rock Stadium", city = "Miami", country = "EUA",
                broadcast = "SporTV"
            ),
            MatchEntity(
                id = 5,
                teamHome = "Inglaterra", teamHomeCode = "ENG",
                teamAway = "Holanda", teamAwayCode = "NED",
                dateTimeEpoch = currentDate + (8 * oneDay),
                isCompleted = false,
                stage = "Fase de Grupos",
                stadium = "BC Place", city = "Vancouver", country = "Canadá",
                broadcast = "TV Globo"
            ),
            MatchEntity(
                id = 6,
                teamHome = "Bélgica", teamHomeCode = "BEL",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = currentDate + (10 * oneDay),
                isCompleted = false,
                stage = "Fase de Grupos",
                stadium = "AT&T Stadium", city = "Dallas", country = "EUA",
                broadcast = "TV Globo"
            )
        )
        repository.insertMatches(defaultList)
        // Set Brazil as default favorite if favorite teams empty
        if (repository.favoriteTeams.first().isEmpty()) {
            repository.addFavoriteTeam("BRA", "Brasil")
        }
    }

    fun addFavorite(code: String, name: String) {
        viewModelScope.launch {
            repository.addFavoriteTeam(code, name)
        }
    }

    fun removeFavorite(code: String, name: String) {
        viewModelScope.launch {
            repository.removeFavoriteTeam(code, name)
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

        val favTeamsCodes = favoriteTeams.value.map { it.code }
        val allScheduledMatches = matches.value
        val alerts = mutableListOf<NotificationAlert>()

        for (match in allScheduledMatches) {
            val matchesFav = favTeamsCodes.contains(match.teamHomeCode) || favTeamsCodes.contains(match.teamAwayCode) || match.teamHomeCode == "BRA"
            if (!match.isCompleted && matchesFav) {
                val matchTitle = "${match.teamHome} x ${match.teamAway}"
                val broadcast = match.broadcast
                val stadiumStr = "${match.stadium} (${match.city})"

                val gameCal = Calendar.getInstance().apply {
                    timeInMillis = match.dateTimeEpoch * 1000L
                }
                val gameHour = gameCal.get(Calendar.HOUR_OF_DAY)
                val gameMinute = gameCal.get(Calendar.MINUTE)

                val formatTime = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(match.dateTimeEpoch * 1000L))
                val formatDate = SimpleDateFormat("d 'de' MMMM", Locale("pt", "BR")).format(Date(match.dateTimeEpoch * 1000L))

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

                // 2) NOTIFICAÇÕES 1 DIA ANTES
                val is1Day15hSilenced = isQuietHour(15, 30)
                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "Contagem regressiva: Amanhã tem $matchTitle às $formatTime! Transmissão: $broadcast no $stadiumStr.",
                    triggerTimeDescription = "1 dia antes (15:30)",
                    broadcast = broadcast,
                    isUrgent = false,
                    isSilenced = is1Day15hSilenced
                ))

                val is1Day22hSilenced = isQuietHour(22, 45)
                alerts.add(NotificationAlert(
                    matchTitle = matchTitle,
                    info = "[Urgente] É amanhã o grande dia! Sintonize na $broadcast às $formatTime para torcer muito. Jogo no $stadiumStr.",
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
                        if (intensity == "Moderada (2h e 1h antes)" && h > 2) {
                            continue
                        }

                        val triggerCal = Calendar.getInstance().apply {
                            timeInMillis = (match.dateTimeEpoch * 1000L) - (h * 3600_000L)
                        }
                        val triggerH = triggerCal.get(Calendar.HOUR_OF_DAY)
                        val triggerM = triggerCal.get(Calendar.MINUTE)
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
                val triggerCal10 = Calendar.getInstance().apply {
                    timeInMillis = (match.dateTimeEpoch * 1000L) - (10 * 60_000L)
                }
                val triggerH10 = triggerCal10.get(Calendar.HOUR_OF_DAY)
                val triggerM10 = triggerCal10.get(Calendar.MINUTE)
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

    // --- Gemini sport chat API Logic ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertChatMessage("user", text)
            _isChatLoading.value = true

            val response = callGeminiApi(text)
            repository.insertChatMessage("gemini", response)
            _isChatLoading.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    private suspend fun callGeminiApi(userText: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext "Para obter respostas de IA reais sobre a Copa 2026, adicione sua GEMINI_API_KEY no painel de Secrets ou configure as chaves ambientais."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val systemPrompt = "Você é o 'Assistente Copa 2026', um analista de futebol premium repleto de insights de futebol, estatísticas históricas, palpites inteligentes e notícias hipotéticas da Copa do Mundo de 2026. Responda em português (pt-BR) de forma moderna, empolgante, clara e sempre amigável."

        val requestJson = """
            {
               "contents": [
                   {
                       "parts": [
                           {"text": "$userText"}
                       ]
                   }
               ],
               "systemInstruction": {
                   "parts": [
                       {"text": "$systemPrompt"}
                   ]
               }
            }
        """.trimIndent()

        val requestBody = requestJson.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Desculpe pelo contratempo! Ocorreu um erro ao consultar o assistente de IA: Código ${response.code}."
                }
                val bodyString = response.body?.string() ?: ""
                val jsonObject = Json { ignoreUnknownKeys = true }.parseToJsonElement(bodyString)
                
                // Navigate candidate text manually avoiding heavy dynamic parsing errors
                val candidates = jsonObject.toString()
                if (candidates.contains("text")) {
                    val searchStr = "\"text\":"
                    val idx = candidates.indexOf(searchStr)
                    if (idx != -1) {
                        val sub = candidates.substring(idx + searchStr.length).trim()
                        if (sub.startsWith("\"")) {
                            val endIdx = sub.indexOf("\"", 1)
                            if (endIdx != -1) {
                                return@withContext sub.substring(1, endIdx)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\t", "\t")
                            }
                        }
                    }
                }
                return@withContext "Gol de placa! O assistente está concentrado, mas não pôde gerar uma resposta em formato de texto estruturado agora."
            }
        } catch (e: Exception) {
            return@withContext "Erro de conexão: ${e.localizedMessage ?: "Não foi possível conectar ao servidor de IA."}"
        }
    }
}
