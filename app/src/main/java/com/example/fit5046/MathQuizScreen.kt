package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.example.fit5046.model.QuizQuestion
import com.example.fit5046.components.DropdownMenuWithSelectedItem
import com.example.fit5046.firebase.FirebaseManager
import com.example.fit5046.model.QuizResult
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun MathQuizScreen() {
    val levels = listOf("Easy", "Medium", "Hard")
    var selectedLevel by remember { mutableStateOf(levels.first()) }
    var paragraph by remember { mutableStateOf("") }
    var quiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    var score by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager.getInstance() }
    var quizStartTime by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "➗ Math Practice",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Choose a difficulty level:")

        DropdownMenuWithSelectedItem(
            items = levels,
            selectedItem = selectedLevel,
            onItemSelected = { selectedLevel = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    score = null
                    userAnswers.clear()
                    quiz = emptyList()
                    quizStartTime = System.currentTimeMillis()

                    try {
                        val prompt = """
                            You are a math quiz generator.
                            Based on the difficulty level "$selectedLevel", generate 3 multiple-choice math questions for young students.

                            Return ONLY a JSON array like:
                            [
                                {
                                    "question": "...",
                                    "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                                    "correct": "A"
                                }, ...
                            ]
                            No explanations. No markdown.
                        """.trimIndent()

                        val response = RetrofitClient.api.getResponse(
                            ChatRequest(
                                model = "mistralai/mistral-7b-instruct", 
                                messages = listOf(
                                    ChatMessage("system", "You generate math quizzes for kids."),
                                    ChatMessage("user", prompt)
                                )
                            )
                        )

                        val json = response.choices.first().message.content
                            .replace("```json", "")
                            .replace("```", "")
                            .replace(""", "\"")
                            .replace(""", "\"")
                            .trim()

                        quiz = Gson().fromJson(json, Array<QuizQuestion>::class.java).toList()
                    } catch (e: Exception) {
                        paragraph = "⚠️ Failed to generate quiz: ${e.message}"
                    }

                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("🚀 Generate Quiz")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (paragraph.isNotEmpty() && quiz.isEmpty()) {
            Text(paragraph)
        }

        if (quiz.isNotEmpty()) {
            quiz.forEachIndexed { index, q ->
                Text(
                    text = "${index + 1}. ${q.question}",
                    fontWeight = FontWeight.Medium
                )

                q.options.forEach { option ->
                    val letter = option.substringBefore(". ").trim()
                    val selected = userAnswers[index] == letter
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
                            onClick = { userAnswers[index] = letter },
                            enabled = score == null
                        )
                        Text(option, color = color)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (score == null) {
                Button(onClick = {
                    var correctCount = 0
                    quiz.forEachIndexed { i, q ->
                        val correct = q.correct.trim()
                        val selected = userAnswers[i]?.trim()
                        if (correct == selected) correctCount++
                    }
                    score = correctCount
                    
                    // 计算得分和用时
                    val quizEndTime = System.currentTimeMillis()
                    val timeTaken = quizEndTime - quizStartTime
                    val scorePercentage = (correctCount * 100) / quiz.size
                    
                    // 保存到Firebase
                    if (firebaseManager.getCurrentUserId() != null) {
                        scope.launch {
                            val quizResult = QuizResult(
                                timestamp = quizEndTime,
                                subject = "Math",
                                difficulty = selectedLevel,
                                totalQuestions = quiz.size,
                                correctAnswers = correctCount,
                                score = scorePercentage,
                                timeTaken = timeTaken
                            )
                            
                            val success = firebaseManager.saveQuizResult(quizResult)
                            if (success) {
                                Toast.makeText(context, "Quiz result saved to cloud", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text("✅ Submit Answers")
                }
            } else {
                Text(
                    text = "You got $score/${quiz.size} correct!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

