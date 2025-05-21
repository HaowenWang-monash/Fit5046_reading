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

@Composable
fun ScienceQuizScreen() {
    val topics = listOf("Animals", "Plants", "Space", "Weather", "Human Body")
    var selectedTopic by remember { mutableStateOf(topics.first()) }
    var paragraph by remember { mutableStateOf("") }
    var quiz by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    var score by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔬 Science Quiz",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Choose a topic:")

        DropdownMenuWithSelectedItem(
            items = topics,
            selectedItem = selectedTopic,
            onItemSelected = { selectedTopic = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    score = null
                    userAnswers.clear()
                    quiz = emptyList()

                    try {
                        val prompt = """
                                You are a science quiz generator.
                                Based ONLY on the topic \"$selectedTopic\", generate exactly 3 multiple-choice science questions for children.
                                
                                Return ONLY a JSON array like this:
                                [
                                    {
                                        "question": "...",
                                        "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                                        "correct": "A"
                                    },
                                    ...
                                ]
                                ❗No explanations. No paragraph. No markdown. Only raw JSON array.
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

                if (score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✔ Correct answer: ${q.correct}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                }) {
                    Text("✅ Submit Answers")
                }
            } else {
                Text(
                    text = "You got $score/${quiz.size} correct!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val dao = AppDatabase.getDatabase(context).quizStatDao()
                val today = LocalDate.now().toString()
                val subject = "Science"
                val totalCount = quiz.size
                val correctCount = score ?: 0

                LaunchedEffect(Unit) {
                    val existing = dao.getStatByDateAndSubject(today, subject)
                    if (existing != null) {
                        dao.update(existing.copy(
                            total = existing.total + totalCount,
                            correct = existing.correct + correctCount
                        ))
                    } else {
                        dao.insert(QuizDailyStat(
                            date = today,
                            subject = subject,
                            total = totalCount,
                            correct = correctCount
                        ))
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuWithSelectedItem(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedItem)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}


