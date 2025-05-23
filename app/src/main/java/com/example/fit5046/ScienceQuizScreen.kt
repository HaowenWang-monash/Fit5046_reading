package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.example.fit5046.model.QuizQuestion
import com.example.fit5046.data.AppDatabase
import com.example.fit5046.data.QuizDailyStat
import java.time.LocalDate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ScienceQuizScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).quizStatDao()
    val userId = Firebase.auth.currentUser?.uid ?: "anonymous"

    val topics = listOf("Animals", "Plants", "Space", "Weather", "Human Body")
    var selectedTopic by remember { mutableStateOf(topics.first()) }
    var quiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    var score by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔬 Science Quiz", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Step 1: Choose a topic", fontSize = 16.sp)

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedTopic)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    topics.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedTopic = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    isLoading = true
                    score = null
                    userAnswers.clear()
                    quiz = emptyList()

                    try {
                        val prompt = """
                        You are a science quiz generator.
                        Based ONLY on the topic "$selectedTopic", generate exactly 3 multiple-choice science questions suitable for children aged 10 to 15.
                    
                        Return ONLY a JSON array like this:
                        [
                            {
                                "question": "...",
                                "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                                "correct": "A"
                            }
                        ]
                        ❗ No explanations. No markdown. Just a valid JSON array.
                    """.trimIndent()


                        val response = RetrofitClient.api.getResponse(
                            ChatRequest(
                                model = "mistralai/mistral-7b-instruct",
                                messages = listOf(
                                    ChatMessage("system", "You generate science quizzes for kids."),
                                    ChatMessage("user", prompt)
                                )
                            )
                        )

                        val json = response.choices.first().message.content
                            .replace("```json", "")
                            .replace("```", "")
                            .replace("“", "\"")
                            .replace("”", "\"")
                            .trim()

                        quiz = Gson().fromJson(json, Array<QuizQuestion>::class.java).toList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    isLoading = false
                }
            }) {
                Text("🚀 Generate Quiz")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (isLoading) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (quiz.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Step 2: Answer the questions below!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(quiz) { index, q ->
                    Text("${index + 1}. ${q.question}", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    q.options.forEach { option ->
                        val letter = option.substringBefore(". ").trim()
                        val selected = userAnswers[index] == letter
                        val isCorrect = score != null && q.correct == letter

                        val color = when {
                            score == null -> MaterialTheme.colorScheme.onSurface
                            isCorrect -> Color(0xFF66BB6A)
                            selected -> Color(0xFFEF5350)
                            else -> MaterialTheme.colorScheme.onSurface
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

                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    if (score == null) {
                        Button(onClick = {
                            val correctCount = quiz.indices.count { i ->
                                userAnswers[i]?.trim() == quiz[i].correct.trim()
                            }
                            score = correctCount

                            val today = LocalDate.now().toString()
                            val stat = QuizDailyStat(
                                id = 0,
                                userId = userId,
                                date = today,
                                category = "Science",
                                totalQuestions = quiz.size,
                                correctAnswers = correctCount
                            )

                            scope.launch { dao.insert(stat) }
                        }) {
                            Text("✅ Submit Answers")
                        }
                    }

                    score?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🎉 You got $it/${quiz.size} correct!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D4C41)
                        )
                    }
                }
            }
        }
    }
}





