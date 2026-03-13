package com.progreen.recycling.data.remote

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.1,
    @SerializedName("max_completion_tokens")
    val maxCompletionTokens: Int = 300
)

data class GroqMessage(
    val role: String,
    val content: List<GroqContent>
)

data class GroqContent(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: GroqImageUrl? = null
)

data class GroqImageUrl(
    val url: String
)

data class GroqChatResponse(
    val choices: List<GroqChoice> = emptyList()
)

data class GroqChoice(
    val message: GroqResponseMessage
)

data class GroqResponseMessage(
    val content: JsonElement?
)
