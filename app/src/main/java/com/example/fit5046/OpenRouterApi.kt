package com.example.fit5046

import com.example.fit5046.*
import retrofit2.http.*

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun getResponse(
        @Body request: ChatRequest,
        @Header("Authorization") token: String = "Bearer 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"
    ): ChatResponse
}
