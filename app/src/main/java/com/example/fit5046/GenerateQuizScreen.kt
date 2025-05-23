package com.example.fit5046

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046.data.AppDatabase
import com.example.fit5046.data.QuizDailyStat
import com.example.fit5046.model.QuizQuestion
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun GenerateQuizScreen() {
    var input by remember { mutableStateOf("") }
    var paragraph by remember { mutableStateOf("") }
    var quiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    var score by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: "guest"

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧠 AI Reading Challenge",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF6D4C41)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "🔍 Step 1: Enter a topic to learn about!",
                fontSize = 16.sp,
                color = Color(0xFF8D6E63),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Enter keywords") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        score = null
                        userAnswers.clear()

                        try {
                            val paragraphPrompt = "Write a short paragraph (under 100 words) for students about: $input"
                            val paraResponse = RetrofitClient.api.getResponse(
                                ChatRequest("mistralai/mistral-7b-instruct", listOf(
                                    ChatMessage("system", "You write educational reading paragraphs."),
                                    ChatMessage("user", paragraphPrompt)
                                ))
                            )
                            paragraph = paraResponse.choices.first().message.content.trim()
                        } catch (e: Exception) {
                            paragraph = "❌ Error generating paragraph: ${e.message}"
                            quiz = emptyList()
                            isLoading = false
                            return@launch
                        }

                        try {
                            val quizPrompt = """
                                You are a quiz generator.
                                Based ONLY on the paragraph below, generate exactly 3 multiple-choice reading comprehension questions.
                                Each question must include:
                                - \"question\": a clear and concise question text
                                - \"options\": an array of 4 choices labeled \"A.\", \"B.\", \"C.\", \"D.\"
                                - \"correct\": a string with the correct answer letter (e.g., \"A\")
                                ❗Return ONLY a valid JSON array. No explanations. No markdown formatting like ```json.
                                Paragraph:
                                $paragraph
                            """.trimIndent()

                            val quizResponse = RetrofitClient.api.getResponse(
                                ChatRequest("mistralai/mistral-7b-instruct", listOf(
                                    ChatMessage("system", "You create JSON quizzes."),
                                    ChatMessage("user", quizPrompt)
                                ))
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
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (paragraph.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📘 Paragraph:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Title: A Brief Overview of $input", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(paragraph, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                if (quiz.isNotEmpty()) {
                    item {
                        Text("🧠 Step 2: Answer the questions below!", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(quiz) { i, q ->
                        Column {
                            Text("${i + 1}. ${q.question}", fontWeight = FontWeight.Medium)
                            q.options.forEach { option ->
                                val letter = option.substringBefore(".").trim()
                                val selected = userAnswers[i] == letter
                                val isCorrect = score != null && q.correct == letter
                                val color = if (score != null) {
                                    if (isCorrect) MaterialTheme.colorScheme.primary
                                    else if (selected) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { userAnswers[i] = letter },
                                        enabled = score == null
                                    )
                                    Text(option, modifier = Modifier.padding(start = 4.dp), color = color)
                                }
                            }
                            if (score != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✔ Correct answer: ${q.correct}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val correctCount = quiz.countIndexed { index, question ->
                                userAnswers[index] == question.correct
                            }
                            score = correctCount

                            val dao = AppDatabase.getDatabase(context).quizStatDao()
                            val today = LocalDate.now().toString()
                            val subject = "English"
                            val totalCount = quiz.size

                            scope.launch {
                                val existing = dao.getStatByDateAndCategory(today, subject, userId)
                                if (existing != null) {
                                    dao.update(existing.copy(
                                        totalQuestions = existing.totalQuestions + totalCount,
                                        correctAnswers = existing.correctAnswers + correctCount
                                    ))
                                } else {
                                    dao.insert(
                                        QuizDailyStat(
                                            id = 0,
                                            userId = userId,
                                            date = today,
                                            category = subject,
                                            totalQuestions = totalCount,
                                            correctAnswers = correctCount
                                        )
                                    )
                                }
                            }
                        }) {
                            Text("📩 Submit")
                        }

                        score?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("🎉 You got $it out of ${quiz.size} correct!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

inline fun <T> List<T>.countIndexed(predicate: (Int, T) -> Boolean): Int {
    var count = 0
    forEachIndexed { i, item -> if (predicate(i, item)) count++ }
    return count
}