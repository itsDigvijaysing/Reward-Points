package com.rewardpoints.app.ai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Gemini Generative Language API client.
 *
 * Endpoint: POST `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key=API_KEY`
 *
 * Model: `gemini-2.5-flash` — current stable Flash model. Free tier (as of May 2026):
 * 10 RPM, 250k TPM, 500 RPD. The previously-used `gemini-2.0-flash` was retired on
 * 2026-03-03 — calls against that name now return errors (typically 404 or 429), which
 * is what caused the unexplained "TooManyRequests" spikes a user might see even without
 * actively sending messages. See: https://ai.google.dev/gemini-api/docs/models
 *
 * Request shape (simplified):
 * ```
 * { "system_instruction": { "parts": [{ "text": "..." }] },
 *   "contents": [{ "role": "user"|"model", "parts": [{ "text": "..." }] }, ...],
 *   "generationConfig": { "temperature": 0.7, "maxOutputTokens": 512 } }
 * ```
 *
 * Response shape: `candidates[0].content.parts[0].text` (or `finishReason=SAFETY` if blocked).
 */
class GeminiAgentApi(
    private val httpClient: HttpClient,
    private val apiKeyProvider: suspend () -> String?
) : AgentApi {

    companion object {
        private const val MODEL = "gemini-2.5-flash"
        private const val BASE = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    override suspend fun chat(
        systemInstruction: String,
        transcript: List<AgentMessage>
    ): Result<String> {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(AgentAuthException("No Gemini API key. Add one in Settings."))
        }
        return runCatching {
            val request = GeminiRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(systemInstruction))
                ),
                contents = transcript
                    .filter { !it.isPending }
                    .map {
                        GeminiContent(
                            role = if (it.role == AgentMessage.Role.USER) "user" else "model",
                            parts = listOf(GeminiPart(it.content))
                        )
                    },
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7,
                    // Tight cap: keeps replies snappy + bounds latency. Persona prompt asks
                    // for 2-4 short sentences, so 256 is plenty even with light markdown.
                    maxOutputTokens = 256
                )
            )
            val body = json.encodeToString(GeminiRequest.serializer(), request)

            val response = httpClient.post("$BASE/$MODEL:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            when (response.status) {
                HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden ->
                    throw AgentAuthException("Gemini rejected the API key (HTTP ${response.status.value}).")
                HttpStatusCode.TooManyRequests ->
                    throw AgentRateLimitException("Gemini rate limit reached. Wait a minute and try again.")
            }
            if (!response.status.isSuccess()) {
                val text = response.bodyAsText().take(300)
                throw Exception("Gemini error ${response.status.value}: $text")
            }

            val parsed = json.decodeFromString(GeminiResponse.serializer(), response.bodyAsText())
            val candidate = parsed.candidates.firstOrNull()
                ?: throw Exception("Gemini returned no candidates.")

            // Safety-filter or other non-STOP finish reasons → surface as typed error.
            if (candidate.finishReason == "SAFETY" || candidate.finishReason == "RECITATION") {
                throw AgentSafetyException("Gemini blocked the response (${candidate.finishReason}).")
            }

            val text = candidate.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Gemini returned an empty response.")
            text.trim()
        }
    }
}

// --- Wire types ---

@Serializable
private data class GeminiRequest(
    @SerialName("system_instruction")
    val systemInstruction: GeminiContent? = null,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
private data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(val text: String)

@Serializable
private data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 512
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)
