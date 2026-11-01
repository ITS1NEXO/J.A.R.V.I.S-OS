package com.zyvix.zorinm4

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS).build()

    fun reply(baseUrl: String, model: String, apiKey: String, messages: List<ChatMessage>, memories: List<String>): String {
        require(apiKey.isNotBlank()) { "Add your API key in Settings first." }
        val endpoint = baseUrl.trimEnd('/') + "/chat/completions"
        val arr = JSONArray()
        val system = buildString {
            append("You are ZORIN-M4, a calm, capable, friendly Android AI companion. ")
            append("Reply naturally and concisely. Do not claim to control the phone or perform actions you cannot perform.")
            if (memories.isNotEmpty()) append("\nRelevant saved memories:\n" + memories.joinToString("\n") { "- $it" })
        }
        arr.put(JSONObject().put("role", "system").put("content", system))
        messages.takeLast(24).forEach { arr.put(JSONObject().put("role", it.role).put("content", it.content)) }
        val body = JSONObject().put("model", model).put("messages", arr).put("temperature", 0.7)
        val req = Request.Builder().url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        http.newCall(req).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException("AI request failed (${response.code}): ${raw.take(500)}")
            val json = JSONObject(raw)
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        }
    }
}
