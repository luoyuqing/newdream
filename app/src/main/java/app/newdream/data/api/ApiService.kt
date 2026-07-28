package app.newdream.data.api

import app.newdream.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit

class ApiService {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun chatCompletion(
        provider: ApiProvider,
        model: String,
        messages: List<CompletionMessage>,
        temperature: Double = 0.8,
        maxTokens: Int = 2048,
        onStream: ((String) -> Unit)? = null
    ): Result<ChatCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                stream = onStream != null
            )
            val jsonBody = json.encodeToString(ChatCompletionRequest.serializer(), request)
            val baseUrl = provider.baseUrl.trimEnd('/')

            val url = when (provider.providerType) {
                ProviderType.OPENAI_COMPATIBLE -> "$baseUrl/chat/completions"
                ProviderType.GEMINI -> "$baseUrl/models/$model:streamGenerateContent?key=${provider.apiKey}"
                ProviderType.ANTHROPIC -> "$baseUrl/v1/messages"
            }

            val body = jsonBody.toRequestBody("application/json".toMediaType())
            val httpRequest = Request.Builder()
                .url(url)
                .post(body)
                .apply {
                    when (provider.providerType) {
                        ProviderType.OPENAI_COMPATIBLE -> {
                            addHeader("Authorization", "Bearer ${provider.apiKey}")
                            addHeader("Content-Type", "application/json")
                        }
                        ProviderType.ANTHROPIC -> {
                            addHeader("x-api-key", provider.apiKey)
                            addHeader("anthropic-version", "2023-06-01")
                            addHeader("Content-Type", "application/json")
                        }
                        else -> {}
                    }
                }
                .build()

            if (onStream != null) {
                val response = client.newCall(httpRequest).execute()
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: $responseBody")
                    )
                }

                // Try to parse streaming response, or handle as normal
                try {
                    // Parse as normal response (some providers return non-streaming even with stream=true)
                    val resp = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
                    onStream(resp.choices.firstOrNull()?.message?.content ?: "")
                    return@withContext Result.success(resp)
                } catch (e: Exception) {
                    // Parse SSE lines manually
                    val sb = StringBuilder()
                    responseBody.lines().forEach { line ->
                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ")
                            if (data == "[DONE]") return@forEach
                            try {
                                val chunk = json.decodeFromString(ChatCompletionResponse.serializer(), data)
                                val delta = chunk.choices.firstOrNull()?.delta?.content ?: ""
                                if (delta.isNotEmpty()) {
                                    sb.append(delta)
                                    onStream(delta)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    val fullContent = sb.toString()
                    return@withContext Result.success(
                        ChatCompletionResponse(
                            choices = listOf(
                                Choice(
                                    message = CompletionMessage("assistant", fullContent)
                                )
                            )
                        )
                    )
                }
            } else {
                val response = client.newCall(httpRequest).execute()
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: $responseBody")
                    )
                }
                val resp = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
                return@withContext Result.success(resp)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun testConnection(provider: ApiProvider): Result<String> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = provider.baseUrl.trimEnd('/')
            val url = when (provider.providerType) {
                ProviderType.OPENAI_COMPATIBLE -> "$baseUrl/models"
                ProviderType.GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models?key=${provider.apiKey}"
                ProviderType.ANTHROPIC -> "$baseUrl/v1/models"
            }
            val request = Request.Builder()
                .url(url)
                .apply {
                    when (provider.providerType) {
                        ProviderType.OPENAI_COMPATIBLE -> addHeader("Authorization", "Bearer ${provider.apiKey}")
                        ProviderType.ANTHROPIC -> {
                            addHeader("x-api-key", provider.apiKey)
                            addHeader("anthropic-version", "2023-06-01")
                        }
                        else -> {}
                    }
                }
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            return@withContext if (response.isSuccessful) {
                Result.success(body)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
