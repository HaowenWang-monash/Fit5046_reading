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
