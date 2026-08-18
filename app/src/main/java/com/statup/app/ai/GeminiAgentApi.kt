package com.statup.app.ai

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

        // STOP = normal completion; MAX_TOKENS = hit our maxOutputTokens cap but the
        // partial reply is still valid (don't error). Everything else in Gemini's v1beta
        // finish-reason taxonomy (SAFETY, RECITATION, BLOCKLIST, PROHIBITED_CONTENT,
        // SPII, OTHER, MALFORMED_FUNCTION_CALL …) means the response was blocked or
        // scrubbed — surface as AgentSafetyException.
        private val BENIGN_FINISH_REASONS = setOf("STOP", "MAX_TOKENS")
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
                    // 2.5 Flash defaults to dynamic thinking (thinkingBudget=-1) when this is
                    // omitted, and thinking tokens are drawn from the same maxOutputTokens cap
                    // as the visible reply — with a low cap that silently ate the whole budget
                    // and truncated or emptied the answer. Disabled: a short coaching reply
                    // doesn't need chain-of-thought.
                    thinkingConfig = GeminiThinkingConfig(thinkingBudget = 0),
                    // Persona prompt asks for 2-4 short sentences; 512 leaves headroom for
                    // markdown (bullets/bold) without the reply ever needing thinking tokens.
                    maxOutputTokens = 512
                )
            )
            val body = json.encodeToString(GeminiRequest.serializer(), request)

            val response = httpClient.post("$BASE/$MODEL:generateContent") {
                // Send the key as a header, not a query parameter, so it can't leak into
                // proxy / CDN / server access logs the way a ?key=... URL can.
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            when {
                response.status == HttpStatusCode.Unauthorized ||
                    response.status == HttpStatusCode.Forbidden ->
                    throw AgentAuthException("Gemini rejected the API key (HTTP ${response.status.value}).")
                response.status == HttpStatusCode.TooManyRequests ->
                    throw AgentRateLimitException("Gemini rate limit reached. Wait a minute and try again.")
                // 5xx (overloaded / temporary outage) — surface as rate-limit so the UI
                // shows the same friendly "try again later" copy and the user isn't
                // confronted with a raw HTTP code.
                response.status.value in 500..599 ->
                    throw AgentRateLimitException("Gemini is temporarily unavailable (HTTP ${response.status.value}). Try again shortly.")
            }
            if (!response.status.isSuccess()) {
                val text = response.bodyAsText().take(300)
                throw Exception("Gemini error ${response.status.value}: $text")
            }

            val parsed = json.decodeFromString(GeminiResponse.serializer(), response.bodyAsText())
            val candidate = parsed.candidates.firstOrNull()
                ?: throw Exception("Gemini returned no candidates.")

            // Any non-STOP / non-MAX_TOKENS finish reason from Gemini's v1beta safety
            // taxonomy means the response was blocked or scrubbed. MAX_TOKENS is benign
            // (just a truncated reply) and should fall through to the normal text path.
            val finish = candidate.finishReason
            if (finish != null && finish !in BENIGN_FINISH_REASONS) {
                throw AgentSafetyException("Gemini blocked the response ($finish).")
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
    val maxOutputTokens: Int = 512,
    val thinkingConfig: GeminiThinkingConfig? = null
)

@Serializable
private data class GeminiThinkingConfig(
    val thinkingBudget: Int
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
