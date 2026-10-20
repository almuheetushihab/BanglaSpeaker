package com.banglaenglish.speaker.data

import com.banglaenglish.speaker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiTranslationEngine(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) : TranslationEngine {

    private val MODEL_NAME = "gemini-2.5-flash"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun translate(request: TranslationRequest): TranslationResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalStateException("GEMINI_API_KEY is not configured in local.properties")
        }

        var lastException: Exception? = null
        for (attempt in 1..2) {
            try {
                return@withContext executeApiCall(request)
            } catch (e: Exception) {
                lastException = e
                if (attempt < 2) delay(500)
            }
        }
        throw lastException ?: IOException("Gemini API call failed after retries")
    }

    private fun executeApiCall(requestPayload: TranslationRequest): TranslationResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        val langName = if (requestPayload.appLanguageCode.equals("en", ignoreCase = true)) "English" else "Bangla"

        val systemInstructionText = """
            You are an expert Bangla-to-English interpreter helping a Bangladeshi learner speak fluent, natural English.
            The input comes from speech recognition and may contain minor errors or phonetic mistakes: use context to infer what the speaker really meant and fix it silently in 'corrected_bangla'.
            Detect the intent: question / answer / statement / request / command / greeting / other. If it answers a previous question in context, translate it as a natural answer to that question.
            Never translate word-by-word. Produce fluent, grammatically correct, native-sounding English with correct punctuation, preserving meaning, tone, politeness level and emotion. Do not add or drop information.
            Handle Bangla-English code-mixing (keep English words/tech terms as they are). Keep names and places correctly transliterated.
            Current conversation scenario: ${requestPayload.scenario.rawValue}. Adapt vocabulary and formality to this scenario.
            Output JSON strictly matching schema:
            {
              "corrected_bangla": "fixed Bangla string",
              "intent": "question/answer/statement/request/command/greeting/other",
              "english": "natural fluent English version",
              "english_formal": "formal polite English version",
              "english_casual": "casual conversational English version",
              "tip_bn": "optional 1-2 line tip in $langName explaining vocabulary or grammar, or null",
              "key_phrases": [
                { "english": "phrase in english", "bangla": "phrase in bangla" }
              ]
            }
        """.trimIndent()

        val contentsList = mutableListOf<GeminiContent>()

        // Send last 6 conversation turns as context
        val contextHistory = requestPayload.history.takeLast(6)
        for (item in contextHistory) {
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "User (Bangla): ${item.bangla}"))
                )
            )
            contentsList.add(
                GeminiContent(
                    role = "model",
                    parts = listOf(GeminiPart(text = "Assistant (English): ${item.english}"))
                )
            )
        }

        // Current turn
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = requestPayload.banglaText))
            )
        )

        val apiRequestBody = GeminiRequest(
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstructionText))
            ),
            contents = contentsList,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            )
        )

        val jsonString = json.encodeToString(GeminiRequest.serializer(), apiRequestBody)
        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                throw IOException("Gemini API error ${response.code}: $errBody")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response from Gemini")
            val apiResponse = json.decodeFromString(GeminiResponse.serializer(), responseBody)

            val textContent = apiResponse.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw IOException("No content returned from Gemini")

            val cleanJson = textContent.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedResult = json.decodeFromString(GeminiTranslationSchema.serializer(), cleanJson)

            return TranslationResult(
                correctedBangla = parsedResult.correctedBangla.ifBlank { requestPayload.banglaText },
                intent = TranslationIntent.fromRawValue(parsedResult.intent),
                english = parsedResult.english,
                englishFormal = parsedResult.englishFormal.ifBlank { parsedResult.english },
                englishCasual = parsedResult.englishCasual.ifBlank { parsedResult.english },
                tipBn = parsedResult.tipBn,
                keyPhrases = parsedResult.keyPhrases.take(3).map { KeyPhrase(it.english, it.bangla) },
                engineUsed = TranslationEngineType.GEMINI
            )
        }
    }
}

@Serializable
private data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent? = null,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
private data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String
)

@Serializable
private data class GeminiGenerationConfig(
    val temperature: Float = 0.3f,
    val responseMimeType: String? = null
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
private data class GeminiTranslationSchema(
    @SerialName("corrected_bangla") val correctedBangla: String = "",
    val intent: String = "statement",
    val english: String = "",
    @SerialName("english_formal") val englishFormal: String = "",
    @SerialName("english_casual") val englishCasual: String = "",
    @SerialName("tip_bn") val tipBn: String? = null,
    @SerialName("key_phrases") val keyPhrases: List<KeyPhraseSchema> = emptyList()
)

@Serializable
private data class KeyPhraseSchema(
    val english: String = "",
    val bangla: String = ""
)
