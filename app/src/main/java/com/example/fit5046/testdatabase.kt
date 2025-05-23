package com.example.fit5046

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fit5046.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text

import androidx.compose.ui.platform.LocalContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fit5046.work.ClearQuizStatsWorker
@Composable
fun TriggerWorkerButton() {
    val context = LocalContext.current

    Button(onClick = {
        val request = OneTimeWorkRequestBuilder<ClearQuizStatsWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }) {
        Text("🧪 Test Clear Worker")
    }
}
@Composable
fun TestDatabaseScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).quizStatDao()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("🧪 Test Room Database", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                val stats = dao.getAllStats()

                if (stats.isEmpty()) {
                    Log.d("QuizStats", "⚠️ No records found.")
                } else {
                    stats.forEach {
                        Log.d(
                            "QuizStats",
                            "✅ ${it.date} | ${it.category} | ${it.userId} | total: ${it.totalQuestions}, correct: ${it.correctAnswers}"
                        )
                    }
                }
            }
        }) {
            Text("🔍 Print All Stats to Logcat")
        }

        Spacer(modifier = Modifier.height(36.dp))


        TriggerWorkerButton()
    }
}


