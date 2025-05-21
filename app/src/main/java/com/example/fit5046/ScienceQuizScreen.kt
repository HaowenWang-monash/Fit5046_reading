package com.example.fit5046

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔬 Science Quiz", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Choose a topic:")

        var expanded by remember { mutableStateOf(false) }

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
                        Based on "$selectedTopic", generate 3 multiple-choice science questions for children.
                        Return raw JSON array like:
                        [{"question":"...","options":["A...","B...","C...","D..."],"correct":"A"}, ...]
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

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        quiz.forEachIndexed { index, q ->
            Text("${index + 1}. ${q.question}", fontWeight = FontWeight.Medium)
            q.options.forEach { option ->
                val letter = option.substringBefore(". ").trim()
                val selected = userAnswers[index] == letter
                val isCorrect = score != null && q.correct == letter

                val color = if (score != null) {
                    if (isCorrect) MaterialTheme.colorScheme.primary
                    else if (selected) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                } else MaterialTheme.colorScheme.onSurface

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

        if (quiz.isNotEmpty() && score == null) {
            Button(onClick = {
                val correctCount = quiz.indices.count { i ->
                    userAnswers[i]?.trim() == quiz[i].correct.trim()
                }
                score = correctCount

                val today = LocalDate.now().toString()
                val stat = QuizDailyStat(
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
            Text(
                text = "You got $it/${quiz.size} correct!",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}




