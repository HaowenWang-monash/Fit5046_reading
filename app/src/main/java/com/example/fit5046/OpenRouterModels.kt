package com.example.fit5046

data class ChatMessage(val role: String, val content: String)
data class ChatRequest(val model: String, val messages: List<ChatMessage>)
data class ChatChoice(val message: ChatMessage)
data class ChatResponse(val choices: List<ChatChoice>)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correct: String
)

