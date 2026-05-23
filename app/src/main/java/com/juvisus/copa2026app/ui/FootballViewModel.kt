package com.juvisus.copa2026app.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juvisus.copa2026app.BuildConfig
import com.juvisus.copa2026app.data.ChatMessageEntity
import com.juvisus.copa2026app.data.FavoriteTeamEntity
import com.juvisus.copa2026app.data.FootballRepository
import com.juvisus.copa2026app.data.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
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

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private fun parseDateToEpoch(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
            val date = sdf.parse(dateStr)
            date?.time?.div(1000L) ?: (System.currentTimeMillis() / 1000L)
        } catch (e: Exception) {
            System.currentTimeMillis() / 1000L
        }
    }

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

        // Automatic search & update from official APIs / internet on app launch
        viewModelScope.launch {
            syncRealMatchesFromInternet()
        }
    }

    private suspend fun populateDefaultMatches() {
        val defaultList = listOf(
            MatchEntity(
                id = 11,
                teamHome = "Inglaterra", teamHomeCode = "ENG",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = parseDateToEpoch("2024-03-23 16:00"),
                scoreHome = 0, scoreAway = 1,
                isCompleted = true,
                stage = "Amistoso Oficial",
                stadium = "Estádio de Wembley", city = "Londres", country = "Inglaterra",
                broadcast = "TV Globo"
            ),
            MatchEntity(
                id = 12,
                teamHome = "Espanha", teamHomeCode = "ESP",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = parseDateToEpoch("2024-03-26 17:30"),
                scoreHome = 3, scoreAway = 3,
                isCompleted = true,
                stage = "Amistoso Oficial",
                stadium = "Santiago Bernabéu", city = "Madrid", country = "Espanha",
                broadcast = "TV Globo"
            ),
            MatchEntity(
                id = 13,
                teamHome = "México", teamHomeCode = "MEX",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = parseDateToEpoch("2026-06-08 19:30"),
                isCompleted = false,
                stage = "Amistoso de Preparação",
                stadium = "Kyle Field", city = "Texas", country = "EUA",
                broadcast = "TV Globo • SporTV"
            ),
            MatchEntity(
                id = 14,
                teamHome = "Estados Unidos", teamHomeCode = "USA",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = parseDateToEpoch("2026-06-12 20:00"),
                isCompleted = false,
                stage = "Amistoso de Preparação",
                stadium = "Camping World", city = "Orlando", country = "EUA",
                broadcast = "TV Globo • SporTV"
            ),
            MatchEntity(
                id = 15,
                teamHome = "Brasil", teamHomeCode = "BRA",
                teamAway = "Colômbia", teamAwayCode = "COL",
                dateTimeEpoch = parseDateToEpoch("2026-06-17 20:00"),
                isCompleted = false,
                stage = "Copa do Mundo - Grupo",
                stadium = "MetLife Stadium", city = "East Rutherford", country = "EUA",
                broadcast = "TV Globo • SporTV"
            ),
            MatchEntity(
                id = 16,
                teamHome = "Bélgica", teamHomeCode = "BEL",
                teamAway = "Brasil", teamAwayCode = "BRA",
                dateTimeEpoch = parseDateToEpoch("2026-06-24 20:00"),
                isCompleted = false,
                stage = "Copa do Mundo - Grupo",
                stadium = "SoFi Stadium", city = "Los Angeles", country = "EUA",
                broadcast = "TV Globo • SporTV • CazéTV"
            ),
            MatchEntity(
                id = 17,
                teamHome = "México", teamHomeCode = "MEX",
                teamAway = "França", teamAwayCode = "FRA",
                dateTimeEpoch = parseDateToEpoch("2026-06-11 20:00"),
                isCompleted = false,
                stage = "Copa do Mundo - Abertura",
                stadium = "Estádio Azteca", city = "Cidade do México", country = "México",
                broadcast = "TV Globo"
            ),
            MatchEntity(
                id = 18,
                teamHome = "Canadá", teamHomeCode = "CAN",
                teamAway = "Holanda", teamAwayCode = "NED",
                dateTimeEpoch = parseDateToEpoch("2026-06-12 17:00"),
                isCompleted = false,
                stage = "Copa do Mundo - Grupo",
                stadium = "BMO Field", city = "Toronto", country = "Canadá",
                broadcast = "SporTV • CazéTV"
            ),
            MatchEntity(
                id = 19,
                teamHome = "Argentina", teamHomeCode = "ARG",
                teamAway = "Espanha", teamAwayCode = "ESP",
                dateTimeEpoch = parseDateToEpoch("2026-06-13 17:00"),
                isCompleted = false,
                stage = "Copa do Mundo - Grupo",
                stadium = "Gillette Stadium", city = "Boston", country = "EUA",
                broadcast = "TV Globo • SporTV"
            ),
            // Current week (relative to May 23, 2026)
            MatchEntity(
                id = 20,
                teamHome = "Itália", teamHomeCode = "ITA",
                teamAway = "Inglaterra", teamAwayCode = "ENG",
                dateTimeEpoch = parseDateToEpoch("2026-05-25 15:45"),
                isCompleted = false,
                stage = "Amistoso Oficial",
                stadium = "Estádio Olímpico", city = "Roma", country = "Itália",
                broadcast = "SporTV"
            ),
            MatchEntity(
                id = 21,
                teamHome = "Alemanha", teamHomeCode = "GER",
                teamAway = "França", teamAwayCode = "FRA",
                dateTimeEpoch = parseDateToEpoch("2026-05-28 16:00"),
                isCompleted = false,
                stage = "Amistoso Oficial",
                stadium = "Allianz Arena", city = "Munique", country = "Alemanha",
                broadcast = "CazéTV"
            ),
            // Next week
            MatchEntity(
                id = 22,
                teamHome = "Argentina", teamHomeCode = "ARG",
                teamAway = "Uruguai", teamAwayCode = "URU",
                dateTimeEpoch = parseDateToEpoch("2026-06-03 21:00"),
                isCompleted = false,
                stage = "Amistoso de Elite",
                stadium = "Monumental de Núñez", city = "Buenos Aires", country = "Argentina",
                broadcast = "TV Globo ・ SporTV"
            ),
            MatchEntity(
                id = 23,
                teamHome = "França", teamHomeCode = "FRA",
                teamAway = "Portugal", teamAwayCode = "POR",
                dateTimeEpoch = parseDateToEpoch("2026-06-04 15:45"),
                isCompleted = false,
                stage = "Amistoso Oficial",
                stadium = "Stade de France", city = "Paris", country = "França",
                broadcast = "SporTV"
            ),
            MatchEntity(
                id = 24,
                teamHome = "Espanha", teamHomeCode = "ESP",
                teamAway = "Holanda", teamAwayCode = "NED",
                dateTimeEpoch = parseDateToEpoch("2026-06-06 16:00"),
                isCompleted = false,
                stage = "Amistoso Oficial",
                stadium = "Estádio de La Cartuja", city = "Sevilha", country = "Espanha",
                broadcast = "CazéTV"
            )
        )
        repository.insertMatches(defaultList)
        // Set Brazil as default favorite if favorite teams empty
        if (repository.favoriteTeams.first().isEmpty()) {
            repository.addFavoriteTeam("BRA", "Brasil")
        }
    }

    fun syncRealMatchesFromInternet() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Atualizando dados oficiais...", Toast.LENGTH_SHORT).show()
                }

                val allFetched = mutableListOf<MatchEntity>()

                // 1) Fetch ESPN Scoreboard
                val espnUrl = "https://site.api.espn.com/apis/site/v2/sports/soccer/fifa.world/scoreboard"
                val espnList = fetchFromEspnScoreboard(espnUrl)
                if (espnList.isNotEmpty()) {
                    allFetched.addAll(espnList)
                }

                // 2) Fetch Gemini dynamic sports search
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isNotBlank()) {
                    val geminiList = fetchMatchesUsingGeminiSearch(apiKey)
                    if (geminiList.isNotEmpty()) {
                        allFetched.addAll(geminiList)
                    }
                }

                if (allFetched.isNotEmpty()) {
                    repository.insertMatches(allFetched)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
                generateNotificationFlow()
            }
        }
    }

    private suspend fun fetchFromEspnScoreboard(url: String): List<MatchEntity> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val parsedList = mutableListOf<MatchEntity>()
                val jsonElement = Json.parseToJsonElement(body)
                val jsonObject = jsonElement.jsonObject
                val events = jsonObject["events"]?.jsonArray ?: return@withContext emptyList()
                
                for (event in events) {
                    try {
                        val eventObj = event.jsonObject
                        val idStr = eventObj["id"]?.jsonPrimitive?.content ?: "0"
                        val id = idStr.toIntOrNull() ?: Random().nextInt(1000000)
                        
                        val competitions = eventObj["competitions"]?.jsonArray ?: continue
                        if (competitions.isEmpty()) continue
                        val compObj = competitions[0].jsonObject
                        
                        val dateStr = compObj["date"]?.jsonPrimitive?.content ?: ""
                        var epoch = System.currentTimeMillis() / 1000L
                        if (dateStr.isNotEmpty()) {
                            try {
                                val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)
                                df.timeZone = TimeZone.getTimeZone("UTC")
                                val d = df.parse(dateStr)
                                if (d != null) {
                                    epoch = d.time / 1000L
                                }
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                        
                        val venueObj = compObj["venue"]?.jsonObject
                        val stadiumName = venueObj?.get("fullName")?.jsonPrimitive?.content ?: "Estádio Especial"
                        val addressObj = venueObj?.get("address")?.jsonObject
                        val city = addressObj?.get("city")?.jsonPrimitive?.content ?: "Sede"
                        val country = addressObj?.get("country")?.jsonPrimitive?.content ?: "País"
                        
                        val broadcastsArray = compObj["broadcasts"]?.jsonArray
                        var broadcastChannel = "TV Globo • SporTV"
                        if (broadcastsArray != null && broadcastsArray.isNotEmpty()) {
                            val namesArray = broadcastsArray[0].jsonObject["names"]?.jsonArray
                            if (namesArray != null && namesArray.isNotEmpty()) {
                                broadcastChannel = namesArray.map { it.jsonPrimitive.content }.joinToString(" • ")
                            }
                        }
                        
                        val statusObj = compObj["status"]?.jsonObject
                        val typeObj = statusObj?.get("type")?.jsonObject
                        val isCompleted = typeObj?.get("completed")?.jsonPrimitive?.booleanOrNull ?: false
                        
                        val competitors = compObj["competitors"]?.jsonArray ?: continue
                        if (competitors.size < 2) continue
                        
                        val comp1 = competitors[0].jsonObject
                        val comp2 = competitors[1].jsonObject
                        
                        val isHome1 = comp1["homeAway"]?.jsonPrimitive?.content == "home"
                        val homeComp = if (isHome1) comp1 else comp2
                        val awayComp = if (isHome1) comp2 else comp1
                        
                        val homeTeamObj = homeComp["team"]?.jsonObject
                        val awayTeamObj = awayComp["team"]?.jsonObject
                        
                        val teamHome = homeTeamObj?.get("displayName")?.jsonPrimitive?.content ?: "Home"
                        val teamHomeCode = homeTeamObj?.get("abbreviation")?.jsonPrimitive?.content ?: "HOM"
                        val teamAway = awayTeamObj?.get("displayName")?.jsonPrimitive?.content ?: "Away"
                        val teamAwayCode = awayTeamObj?.get("abbreviation")?.jsonPrimitive?.content ?: "AWA"
                        
                        val scoreHome = homeComp["score"]?.jsonPrimitive?.content?.toIntOrNull()
                        val scoreAway = awayComp["score"]?.jsonPrimitive?.content?.toIntOrNull()
                        
                        parsedList.add(
                            MatchEntity(
                                id = id,
                                teamHome = teamHome,
                                teamHomeCode = teamHomeCode,
                                teamAway = teamAway,
                                teamAwayCode = teamAwayCode,
                                scoreHome = scoreHome,
                                scoreAway = scoreAway,
                                dateTimeEpoch = epoch,
                                isCompleted = isCompleted,
                                stage = "Fase de Grupos",
                                stadium = stadiumName,
                                city = city,
                                country = country,
                                broadcast = broadcastChannel
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                parsedList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchMatchesUsingGeminiSearch(apiKey: String): List<MatchEntity> = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val prompt = """
            Você é um integrador de dados de futebol da Copa do Mundo 2026. Pesquise e retorne uma lista em formato JSON estrito de partidas reais, oficiais e atualizadas da Seleção Brasileira (Eliminatórias & amistosos de preparação de 2024-2026) e jogos de abertura da Copa do Mundo 2026.
            Retorne APENAS um bloco em formato de array JSON contendo objetos estruturados exatamente como o modelo abaixo. Não adicione markdown explicativo nem tags extras estruturais além do JSON puro, sem ```json:
            [
              {
                "id": 100,
                "teamHome": "México",
                "teamHomeCode": "MEX",
                "teamAway": "Brasil",
                "teamAwayCode": "BRA",
                "scoreHome": null,
                "scoreAway": null,
                "dateTimeEpoch": 1780961400,
                "isCompleted": false,
                "stage": "Amistoso",
                "stadium": "Kyle Field",
                "city": "Texas",
                "country": "EUA",
                "broadcast": "TV Globo"
              }
            ]
            REGRAS OBRIGATÓRIAS:
            - Os dados devem ser REAIS.
            - Os horários/epochs devem corresponder à realidade em segundos UTC.
            - Não adicione textos adicionais, apenas o array JSON puro.
        """.trimIndent()

        val encodedPrompt = JsonPrimitive(prompt).toString()
        val requestJson = """
            {
               "contents": [
                   {
                       "parts": [
                           {"text": $encodedPrompt}
                       ]
                   }
               ]
            }
        """.trimIndent()

        val requestBody = requestJson.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyString = response.body?.string() ?: ""
                
                var rawJson = bodyString
                if (rawJson.contains("text\":")) {
                    val searchStr = "\"text\":"
                    val idx = rawJson.indexOf(searchStr)
                    if (idx != -1) {
                        val sub = rawJson.substring(idx + searchStr.length).trim()
                        if (sub.startsWith("\"")) {
                            val endIdx = sub.indexOf("\"", 1)
                            if (endIdx != -1) {
                                rawJson = sub.substring(1, endIdx)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\t", "\t")
                            }
                        }
                    }
                }
                
                val cleanJson = if (rawJson.contains("```json")) {
                    rawJson.substringAfter("```json").substringBefore("```").trim()
                } else if (rawJson.contains("```")) {
                    rawJson.substringAfter("```").substringBefore("```").trim()
                } else {
                    rawJson.trim()
                }

                val list = mutableListOf<MatchEntity>()
                val root = Json.parseToJsonElement(cleanJson)
                val array = root.jsonArray
                for (item in array) {
                    val obj = item.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.intOrNull ?: Random().nextInt(1000000)
                    val teamHome = obj["teamHome"]?.jsonPrimitive?.content ?: ""
                    val teamHomeCode = obj["teamHomeCode"]?.jsonPrimitive?.content ?: ""
                    val teamAway = obj["teamAway"]?.jsonPrimitive?.content ?: ""
                    val teamAwayCode = obj["teamAwayCode"]?.jsonPrimitive?.content ?: ""
                    val scoreHome = obj["scoreHome"]?.jsonPrimitive?.intOrNull
                    val scoreAway = obj["scoreAway"]?.jsonPrimitive?.intOrNull
                    val dateTimeEpoch = obj["dateTimeEpoch"]?.jsonPrimitive?.longOrNull ?: (System.currentTimeMillis() / 1000L)
                    val isCompleted = obj["isCompleted"]?.jsonPrimitive?.booleanOrNull ?: false
                    val stage = obj["stage"]?.jsonPrimitive?.content ?: "Copa do Mundo"
                    val stadium = obj["stadium"]?.jsonPrimitive?.content ?: ""
                    val city = obj["city"]?.jsonPrimitive?.content ?: ""
                    val country = obj["country"]?.jsonPrimitive?.content ?: ""
                    val broadcast = obj["broadcast"]?.jsonPrimitive?.content ?: "TV Globo"
                    list.add(MatchEntity(id, teamHome, teamHomeCode, teamAway, teamAwayCode, scoreHome, scoreAway, dateTimeEpoch, isCompleted, stage, stadium, city, country, broadcast))
                }
                list
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
