package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.google.gson.Gson

@Composable
fun GenerateQuizScreen() {
    var input by remember { mutableStateOf("") }
    var paragraph by remember { mutableStateOf("") }
    var quiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🤖 AI Paragraph Generator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter keywords") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true

                    // ✅ Step 1: 生成段落
                    try {
                        val paragraphPrompt =
                            "Write a short paragraph (under 100 words) for students about: $input"

                        val paraResponse = RetrofitClient.api.getResponse(
                            ChatRequest(
                                model = "mistralai/mistral-7b-instruct",
                                messages = listOf(
                                    ChatMessage("system", "You write educational reading paragraphs."),
                                    ChatMessage("user", paragraphPrompt)
                                )
                            )
                        )
                        paragraph = paraResponse.choices.first().message.content.trim()
                    } catch (e: Exception) {
                        paragraph = "❌ Error generating paragraph: ${e.message}"
                        quiz = emptyList()
                        isLoading = false
                        return@launch
                    }

                    // ✅ Step 2: 生成 quiz
                    try {
                        val quizPrompt = """
                            You are a quiz generator.
                            
                            Based ONLY on the paragraph below, generate exactly 3 multiple-choice reading comprehension questions.
                            
                            Each question must include:
                            - "question": a clear and concise question text
                            - "options": an array of 4 choices labeled "A.", "B.", "C.", "D."
                            - "correct": a string with the correct answer letter (e.g., "A")
                            
                            ❗Return ONLY a valid JSON array. No explanations. No markdown formatting like ```json.

                            Paragraph:
                            $paragraph
                        """.trimIndent()

                        val quizResponse = RetrofitClient.api.getResponse(
                            ChatRequest(
                                model = "mistralai/mistral-7b-instruct",
                                messages = listOf(
                                    ChatMessage("system", "You create JSON quizzes."),
                                    ChatMessage("user", quizPrompt)
                                )
                            )
                        )

                        val quizJson = quizResponse.choices.first().message.content
                        val cleanedJson = quizJson
                            .removePrefix("```json")
                            .removePrefix("```")
                            .removeSuffix("```")
                            .replace("“", "\"")
                            .replace("”", "\"")
                            .trim()

                        quiz = Gson().fromJson(cleanedJson, Array<QuizQuestion>::class.java).toList()

                    } catch (e: Exception) {
                        quiz = emptyList()
                        paragraph += "\n\n⚠️ Quiz generation failed: ${e.message}"
                    }

                    isLoading = false
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = input.isNotBlank() && !isLoading
        ) {
            Text("✨ Generate")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (paragraph.isNotEmpty()) {
            Text("📘 Paragraph:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(paragraph, modifier = Modifier.padding(vertical = 8.dp))

            if (quiz.isNotEmpty()) {
                Text("🧠 Questions:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                quiz.forEachIndexed { i, q ->
                    Text("${i + 1}. ${q.question}", fontWeight = FontWeight.Medium)
                    q.options.forEach { option ->
                        Text(option, modifier = Modifier.padding(start = 12.dp))
                    }
                    Text("✅ Correct: ${q.correct}", fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }
    }
}
