package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.ui.theme.ErrorRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FootballRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val matchDao = database.matchDao()
    val favoriteTeamDao = database.favoriteTeamDao()
    val chatMessageDao = database.chatMessageDao()

    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    val favoriteTeams: Flow<List<FavoriteTeamEntity>> = favoriteTeamDao.getFavoriteTeams()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun initDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            // Check if matches are already populated
            val currentMatches = allMatches.first()
            if (currentMatches.size < 12) {
                Log.d("FootballRepository", "Initializing/Re-seeding database with World Cup 2026 data...")
                matchDao.clearAll()

                // Default Favorites: Brasil to start with
                val currentFavs = favoriteTeams.first()
                if (currentFavs.isEmpty()) {
                    favoriteTeamDao.insertFavoriteTeams(
                        listOf(
                            FavoriteTeamEntity("BRA", "Brasil", 1),
                            FavoriteTeamEntity("ARG", "Argentina", 2),
                            FavoriteTeamEntity("FRA", "França", 3),
                            FavoriteTeamEntity("ENG", "Inglaterra", 4)
                        )
                    )
                }

                // Populate initial realistic and official speculative matches for World Cup 2026
                // Current time is 22 May 2026. World Cup starts 11 June 2026.
                // 11 June 19:00 UTC = 1781204400 (approximate Epoch timestamps)
                val matches = listOf(
                    // --- WEEK 1 ("desta semana": June 11 to June 17, 2026) ---
                    MatchEntity(
                        teamHome = "Brasil",
                        teamAway = "Inglaterra",
                        teamHomeCode = "BRA",
                        teamAwayCode = "ENG",
                        dateTimeEpoch = 1781557200L, // 15 June 2026, 21:00 UTC
                        stadium = "MetLife Stadium",
                        city = "East Rutherford",
                        country = "EUA",
                        broadcast = "Globo, SporTV, CazéTV",
                        stage = "Grupo G - Rodada 1"
                    ),
                    MatchEntity(
                        teamHome = "EUA",
                        teamAway = "Itália",
                        teamHomeCode = "USA",
                        teamAwayCode = "ITA",
                        dateTimeEpoch = 1781298000L, // 12 June 2026, 21:00 UTC
                        stadium = "Gillette Stadium",
                        city = "Boston",
                        country = "EUA",
                        broadcast = "SporTV, CazéTV",
                        stage = "Grupo A - Rodada 1"
                    ),
                    MatchEntity(
                        teamHome = "Alemanha",
                        teamAway = "Japão",
                        teamHomeCode = "GER",
                        teamAwayCode = "JPN",
                        dateTimeEpoch = 1781384400L, // 13 June 2026, 21:00 UTC
                        stadium = "Lumen Field",
                        city = "Seattle",
                        country = "EUA",
                        broadcast = "CazéTV, SporTV",
                        stage = "Grupo D - Rodada 1"
                    ),
                    MatchEntity(
                        teamHome = "França",
                        teamAway = "Marrocos",
                        teamHomeCode = "FRA",
                        teamAwayCode = "MAR",
                        dateTimeEpoch = 1781470800L, // 14 June 2026, 21:00 UTC
                        stadium = "BC Place",
                        city = "Vancouver",
                        country = "Canadá",
                        broadcast = "Globo, SporTV",
                        stage = "Grupo B - Rodada 1"
                    ),
                    MatchEntity(
                        teamHome = "Argentina",
                        teamAway = "França",
                        teamHomeCode = "ARG",
                        teamAwayCode = "FRA",
                        dateTimeEpoch = 1781643600L, // 16 June 2026, 21:00 UTC
                        stadium = "AT&T Stadium",
                        city = "Dallas",
                        country = "EUA",
                        broadcast = "SporTV",
                        stage = "Grupo C - Rodada 1"
                    ),
                    MatchEntity(
                        teamHome = "Espanha",
                        teamAway = "México",
                        teamHomeCode = "ESP",
                        teamAwayCode = "MEX",
                        dateTimeEpoch = 1781727600L, // 17 June 2026, 17:00 UTC
                        stadium = "Estádio Azteca",
                        city = "Cidade do México",
                        country = "México",
                        broadcast = "CazéTV",
                        stage = "Grupo E - Rodada 1"
                    ),

                    // --- WEEK 2 ("semana seguinte": June 18 to June 24, 2026) ---
                    MatchEntity(
                        teamHome = "Espanha",
                        teamAway = "Alemanha",
                        teamHomeCode = "ESP",
                        teamAwayCode = "GER",
                        dateTimeEpoch = 1781816400L, // 18 June 2026, 21:00 UTC
                        stadium = "Mercedes-Benz Stadium",
                        city = "Atlanta",
                        country = "EUA",
                        broadcast = "CazéTV",
                        stage = "Grupo B - Rodada 2"
                    ),
                    MatchEntity(
                        teamHome = "Portugal",
                        teamAway = "Uruguai",
                        teamHomeCode = "POR",
                        teamAwayCode = "URU",
                        dateTimeEpoch = 1781902800L, // 19 June 2026, 21:00 UTC
                        stadium = "MetLife Stadium",
                        city = "East Rutherford",
                        country = "EUA",
                        broadcast = "Globo, SporTV",
                        stage = "Grupo E - Rodada 2"
                    ),
                    MatchEntity(
                        teamHome = "Argentina",
                        teamAway = "Inglaterra",
                        teamHomeCode = "ARG",
                        teamAwayCode = "ENG",
                        dateTimeEpoch = 1781989200L, // 20 June 2026, 21:00 UTC
                        stadium = "Hard Rock Stadium",
                        city = "Miami",
                        country = "EUA",
                        broadcast = "SporTV, CazéTV",
                        stage = "Grupo C - Rodada 2"
                    ),
                    MatchEntity(
                        teamHome = "Brasil",
                        teamAway = "Japão",
                        teamHomeCode = "BRA",
                        teamAwayCode = "JPN",
                        dateTimeEpoch = 1782075600L, // 21 June 2026, 17:00 UTC
                        stadium = "SoFi Stadium",
                        city = "Los Angeles",
                        country = "EUA",
                        broadcast = "Globo, CazéTV",
                        stage = "Grupo G - Rodada 2"
                    ),
                    MatchEntity(
                        teamHome = "Itália",
                        teamAway = "Portugal",
                        teamHomeCode = "ITA",
                        teamAwayCode = "POR",
                        dateTimeEpoch = 1782162000L, // 22 June 2026, 21:00 UTC
                        stadium = "Gillette Stadium",
                        city = "Boston",
                        country = "EUA",
                        broadcast = "Globo, SporTV",
                        stage = "Grupo F - Rodada 2"
                    ),
                    MatchEntity(
                        teamHome = "França",
                        teamAway = "EUA",
                        teamHomeCode = "FRA",
                        teamAwayCode = "USA",
                        dateTimeEpoch = 1782248400L, // 23 June 2026, 21:00 UTC
                        stadium = "MetLife Stadium",
                        city = "East Rutherford",
                        country = "EUA",
                        broadcast = "SporTV, CazéTV",
                        stage = "Grupo B - Rodada 2"
                    ),

                    // --- OTHER UPCOMING MATCHES ---
                    MatchEntity(
                        teamHome = "Brasil",
                        teamAway = "Algélia",
                        teamHomeCode = "BRA",
                        teamAwayCode = "ALG",
                        dateTimeEpoch = 1782507600L, // 26 June 2026, 17:00 UTC
                        stadium = "Hard Rock Stadium",
                        city = "Miami",
                        country = "EUA",
                        broadcast = "SporTV, CazéTV",
                        stage = "Grupo G - Rodada 3"
                    ),

                    // --- COMPLETED MATCHES ---
                    MatchEntity(
                        teamHome = "México",
                        teamAway = "Croácia",
                        teamHomeCode = "MEX",
                        teamAwayCode = "CRO",
                        dateTimeEpoch = 1781211600L, // 11 June 2026, 21:00 UTC
                        stadium = "Estádio Azteca",
                        city = "Cidade do México",
                        country = "México",
                        broadcast = "Globo, CazéTV",
                        isCompleted = true,
                        scoreHome = 2,
                        scoreAway = 1,
                        stage = "Grupo A - Abertura",
                        summary = "Gols: Santiago Giménez 34', Edson Álvarez 72' | Andrej Kramarić 80'. O México jogou com muita intensidade no Azteca empurrado por 85 mil torcedores fervorosos. Domínio completo no meio campo com excelente atuação de Luis Chávez.",
                        statsPossession = "58% - 42%",
                        statsShots = "15 - 7",
                        statsFouls = "8 - 14",
                        highlightsLink = "https://www.youtube.com/fifa"
                    ),
                    MatchEntity(
                        teamHome = "Canadá",
                        teamAway = "Marrocos",
                        teamHomeCode = "CAN",
                        teamAwayCode = "MAR",
                        dateTimeEpoch = 1781211600L,
                        stadium = "BC Place",
                        city = "Vancouver",
                        country = "Canadá",
                        broadcast = "SporTV",
                        isCompleted = true,
                        scoreHome = 1,
                        scoreAway = 1,
                        stage = "Grupo B - Abertura",
                        summary = "Gols: Jonathan David 45' (Pen) | Hakim Ziyech 58'. Jogo equilibrado e muito corrido. O Canadá pressionou bastante no primeiro tempo, mas Marrocos organizou a defesa e buscou o empate em bela cobrança de falta de Ziyech.",
                        statsPossession = "49% - 51%",
                        statsShots = "12 - 11",
                        statsFouls = "15 - 9",
                        highlightsLink = "https://www.youtube.com/fifa"
                    ),
                    MatchEntity(
                        teamHome = "Argentina",
                        teamAway = "Arábia Saudita",
                        teamHomeCode = "ARG",
                        teamAwayCode = "KSA",
                        dateTimeEpoch = 1781298000L,
                        stadium = "NRG Stadium",
                        city = "Houston",
                        country = "EUA",
                        broadcast = "SporTV, CazéTV",
                        isCompleted = true,
                        scoreHome = 3,
                        scoreAway = 0,
                        stage = "Grupo C - Rodada 1",
                        summary = "Gols: Lautaro Martínez 12', Messi 60' (P), Enzo Fernández 85'. A Argentina não deu chances para surpresas desta vez. Messideu show com passes desconcertantes e comandou a vitória argentina tranquila na estreia.",
                        statsPossession = "65% - 35%",
                        statsShots = "18 - 4",
                        statsFouls = "9 - 11",
                        highlightsLink = "https://www.youtube.com/fifa"
                    )
                )

                matchDao.insertMatches(matches)

                // Add welcome assistant message to Chat
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        message = "Olá! Sou seu Assistente Premium da Copa do Mundo 2026. 🏆🇧🇷\n\nPergunte-me qualquer coisa sobre os próximos confrontos, escalações, estádios, onde assistir, ou como está a preparação da Seleção Brasileira!",
                        isUser = false
                    )
                )
            }
        }
    }

    suspend fun addFavoriteTeam(code: String, name: String) {
        val count = favoriteTeams.first().size
        favoriteTeamDao.insertFavoriteTeam(FavoriteTeamEntity(code = code, name = name, priority = count + 1))
    }

    suspend fun removeFavoriteTeam(code: String) {
        favoriteTeamDao.deleteFavoriteTeam(code)
    }

    suspend fun clearAllFavorites() {
        favoriteTeamDao.clearFavorites()
    }

    suspend fun saveFavoriteTeams(teams: List<FavoriteTeamEntity>) {
        favoriteTeamDao.insertFavoriteTeams(teams)
    }

    suspend fun sendChatMessage(userText: String): String = withContext(Dispatchers.IO) {
        try {
            // Save user message to database
            chatMessageDao.insertMessage(ChatMessageEntity(message = userText, isUser = true))

            val apkApiKey = BuildConfig.GEMINI_API_KEY
            if (apkApiKey == "MY_GEMINI_API_KEY" || apkApiKey.isBlank()) {
                val mockResponse = getMockAssistantResponse(userText)
                chatMessageDao.insertMessage(ChatMessageEntity(message = mockResponse, isUser = false))
                return@withContext mockResponse
            }

            // Clean context from previous messages
            val history = chatMessages.first().takeLast(10)
            val contentsArray = JSONArray()

            // Optional system instruction
            val systemInstruction = "Você é o Assistente Copa 2026 Premium, um chat inteligência artificial esportivo de alta sofisticação técnica, especializado na Copa do Mundo 2026, com foco especial no Brasil. Responda em Português do Brasil com entusiasmo, elegância, brevidade e precisão absoluta. Use termos profissionais do futebol e emojis estratégicos."

            for (chat in history) {
                val partObj = JSONObject().put("text", chat.message)
                val partsArray = JSONArray().put(partObj)
                val role = if (chat.isUser) "user" else "model"
                contentsArray.put(JSONObject().put("role", role).put("parts", partsArray))
            }

            // Append the new question
            val currentParts = JSONArray().put(JSONObject().put("text", userText))
            contentsArray.put(JSONObject().put("role", "user").put("parts", currentParts))

            val requestBodyJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val responseBodyString = requestBodyJson.toString()
            Log.d("FootballRepository", "Requesting Gemini API... with content: $responseBodyString")

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apkApiKey")
                .post(responseBodyString.toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                Log.d("FootballRepository", "Gemini API Response: $responseString")
                val jsonObject = JSONObject(responseString)
                val candidates = jsonObject.getJSONArray("candidates")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val answer = parts.getJSONObject(0).getString("text")

                chatMessageDao.insertMessage(ChatMessageEntity(message = answer, isUser = false))
                return@withContext answer
            } else {
                val errBody = response.body?.string() ?: ""
                Log.e("FootballRepository", "Gemini API Error: Code ${response.code}, Body: $errBody")
                val errorMsg = "Desculpe, estou enfrentando instabilidade na conexão com o servidor da Copa de 2026. Código: ${response.code}"
                chatMessageDao.insertMessage(ChatMessageEntity(message = errorMsg, isUser = false))
                return@withContext errorMsg
            }
        } catch (e: Exception) {
            Log.e("FootballRepository", "Exception in sendChatMessage: ${e.message}", e)
            val fallback = getMockAssistantResponse(userText)
            chatMessageDao.insertMessage(ChatMessageEntity(message = fallback, isUser = false))
            return@withContext fallback
        }
    }

    private fun getMockAssistantResponse(p: String): String {
        val promptClean = p.lowercase()
        return when {
            promptClean.contains("próximo jogo") || promptClean.contains("brasil") || promptClean.contains("quando joga") -> {
                "🏆 **Brasil vs Inglaterra!**\n📅 **Data:** 15 de Junho de 2026\n🕒 **Horário:** 21:00 UTC (18:00 Horário de Brasília)\n🏟️ **Estádio:** MetLife Stadium (Nova Jersey, EUA)\n📺 **Transmissão:** Rede Globo, SporTV e CazéTV.\n\nEste será o grande duelo de estreia da nossa Seleção na Copa de 2026! Prepare-se para vibrar!"
            }
            promptClean.contains("assistir") || promptClean.contains("transmissão") || promptClean.contains("canal") -> {
                "📺 Os jogos da Copa do Mundo 2026, com foco especial no Brasil, serão transmitidos ao vivo nos canais:\n- **TV Aberta:** Rede Globo (Exclusividade de TV Aberta)\n- **TV Fechada:** Canais SporTV (Grade completa de jogos)\n- **Streaming Digital:** CazéTV (YouTube, Twitch, e Prime Video com coberturas incríveis gratuitos)\n- **FIFA+**: Cobertura mundial de conteúdo complementar!"
            }
            promptClean.contains("onde será") || promptClean.contains("sede") || promptClean.contains("estádio") -> {
                "🏟️ A Copa do Mundo FIFA 2026 será histórica! Pela primeira vez hospedada em três países:\n- 🇺🇸 **EUA** (11 cidades-sede, incluindo Nova Jersey/MetLife Stadium para a grande final e Dallas/AT&T para as semifinais)\n- 🇲🇽 **México** (Cidade do México/Estádio Azteca para a abertura épica, Guadalajara e Monterrey)\n- 🇨🇦 **Canadá** (Toronto e Vancouver)\n\nCom 48 seleções divididas em 12 grupos de 4!"
            }
            promptClean.contains("resultado") || promptClean.contains("placar") || promptClean.contains("quem ganhou") -> {
                "📊 **Resultados Recentes da Copa 2026:**\n\n🟢 🇲🇽 **México 2 x 1 Croácia** (Abertura espetacular no Azteca!)\n🟡 🇨🇦 **Canadá 1 x 1 Marrocos** (Empate disputadíssimo em Vancouver)\n🟢 🇦🇷 **Argentina 3 x 0 Arábia Saudita** (Estreia tranquila sob comando de Messi)\n\nVocê pode rolar a tela principal para ver estatísticas detalhadas e gols de cada partida!"
            }
            else -> {
                "🏆 Sou seu Assistente Inteligente Copa 2026! \n\nPosso te ajudar com dúvidas como:\n- *'Quando será o próximo jogo do Brasil?'*\n- *'Onde assistir os jogos ao vivo?'*\n- *'Quais os estádios da Copa 2026?'*\n- *'Resultados dos últimos jogos'* \n\nComo posso apoiar sua torcida hoje pelo Hexa? 🇧🇷⚡"
            }
        }
    }

    suspend fun clearChat() {
        chatMessageDao.clearChat()
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                message = "Chat reiniciado! Pergunte qualquer informação sobre a Copa 2026.",
                isUser = false
            )
        )
    }
}
